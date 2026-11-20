package co.edu.umb.medirecord.data;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.util.Log;

import androidx.core.app.NotificationManagerCompat;
import androidx.lifecycle.LiveData;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import co.edu.umb.medirecord.alarm.AlarmScheduler;
import co.edu.umb.medirecord.data.entity.Horario;
import co.edu.umb.medirecord.data.entity.Medicamento;
import co.edu.umb.medirecord.data.entity.Toma;
import co.edu.umb.medirecord.data.model.MedicamentoConHorarios;
import co.edu.umb.medirecord.data.model.RegistroHistorial;
import co.edu.umb.medirecord.data.model.ResumenAdherencia;
import co.edu.umb.medirecord.data.model.TomaDelDia;

/**
 * Única puerta de acceso a los datos. Los ViewModel nunca tocan Room directamente.
 * Lecturas: LiveData de Room (se actualizan solas). Escrituras: hilo de fondo único,
 * lo que además serializa las operaciones y evita condiciones de carrera.
 */
public class MedicamentoRepository {

    public static final String TAG_PERF = "MediRecordPerf";
    private static final String TAG = "MediRecordRepo";

    public enum Resultado { OK, NOMBRE_DUPLICADO, NO_ENCONTRADO, ERROR }

    public interface Callback<T> {
        void onResultado(T resultado);
    }

    private static volatile MedicamentoRepository instancia;

    private final Context app;
    private final AppDatabase db;
    private final ExecutorService io = Executors.newSingleThreadExecutor();
    private final Handler main = new Handler(Looper.getMainLooper());

    MedicamentoRepository(Context context, AppDatabase db) {
        this.app = context.getApplicationContext();
        this.db = db;
    }

    public static MedicamentoRepository get(Context context) {
        if (instancia == null) {
            synchronized (MedicamentoRepository.class) {
                if (instancia == null) {
                    instancia = new MedicamentoRepository(context, AppDatabase.get(context));
                }
            }
        }
        return instancia;
    }

    public AppDatabase db() {
        return db;
    }

    public void enSegundoPlano(Runnable r) {
        io.execute(r);
    }

    // ---------- Lecturas observables ----------

    public LiveData<List<MedicamentoConHorarios>> observarMedicamentos() {
        return db.medicamentoDao().observarTodos();
    }

    public LiveData<List<TomaDelDia>> observarDia(String fecha) {
        return db.tomaDao().observarDia(fecha);
    }

    public LiveData<List<RegistroHistorial>> observarHistorial() {
        return db.tomaDao().observarHistorial();
    }

    public LiveData<ResumenAdherencia> observarResumen(String desde) {
        return db.tomaDao().observarResumen(desde);
    }

    // ---------- Operaciones ----------

    public void cargar(long id, Callback<MedicamentoConHorarios> cb) {
        io.execute(() -> {
            MedicamentoConHorarios m = db.medicamentoDao().obtener(id);
            main.post(() -> cb.onResultado(m));
        });
    }

    /**
     * Inserta o actualiza el medicamento y sincroniza sus horarios sin borrar
     * los que se conservan (así no se pierde su historial de tomas).
     */
    public void guardar(Medicamento m, List<Integer> minutos, Callback<Resultado> cb) {
        io.execute(() -> {
            long t0 = SystemClock.elapsedRealtime();
            Resultado r;
            try {
                r = guardarSync(m, minutos);
            } catch (Exception e) {
                Log.e(TAG, "Error guardando medicamento", e);
                r = Resultado.ERROR;
            }
            Log.d(TAG_PERF, "guardarMedicamento ms=" + (SystemClock.elapsedRealtime() - t0)
                    + " resultado=" + r);
            Resultado fin = r;
            main.post(() -> cb.onResultado(fin));
        });
    }

    /** Versión síncrona (se llama desde el hilo de fondo; pública para las pruebas). */
    public Resultado guardarSync(Medicamento m, List<Integer> minutos) {
        // COLLATE NOCASE de SQLite solo ignora mayúsculas ASCII ("Á" ≠ "á"); por eso se compara en Java.
        String clave = normalizar(m.nombre);
        for (String otro : db.medicamentoDao().nombresExcepto(m.id)) {
            if (normalizar(otro).equals(clave)) return Resultado.NOMBRE_DUPLICADO;
        }
        List<Long> eliminados = new ArrayList<>();
        db.runInTransaction(() -> {
            if (m.id == 0) {
                m.creadoEn = System.currentTimeMillis();
                m.id = db.medicamentoDao().insertar(m);
            } else {
                db.medicamentoDao().actualizar(m);
            }
            Set<Integer> deseados = new LinkedHashSet<>(minutos);
            for (Horario h : db.medicamentoDao().horariosDe(m.id)) {
                if (!deseados.remove(h.minutos)) {
                    eliminados.add(h.id);
                }
            }
            if (!eliminados.isEmpty()) {
                db.medicamentoDao().eliminarHorarios(eliminados);
            }
            List<Horario> nuevos = new ArrayList<>();
            for (int mm : deseados) nuevos.add(new Horario(m.id, mm));
            if (!nuevos.isEmpty()) db.medicamentoDao().insertarHorarios(nuevos);
        });
        for (long id : eliminados) AlarmScheduler.cancelar(app, id);
        for (Horario h : db.medicamentoDao().horariosDe(m.id)) {
            AlarmScheduler.programar(app, h.id, h.minutos);
        }
        return Resultado.OK;
    }

    static String normalizar(String s) {
        return s == null ? "" : s.trim().toLowerCase(new java.util.Locale("es", "CO"));
    }

    public void eliminar(long medicamentoId, Callback<Resultado> cb) {
        io.execute(() -> {
            Resultado r;
            try {
                for (Horario h : db.medicamentoDao().horariosDe(medicamentoId)) {
                    AlarmScheduler.cancelar(app, h.id);
                    NotificationManagerCompat.from(app).cancel((int) h.id);
                }
                int filas = db.medicamentoDao().eliminar(medicamentoId);
                r = filas > 0 ? Resultado.OK : Resultado.NO_ENCONTRADO;
            } catch (Exception e) {
                Log.e(TAG, "Error eliminando", e);
                r = Resultado.ERROR;
            }
            Resultado fin = r;
            main.post(() -> cb.onResultado(fin));
        });
    }

    public void registrarToma(long horarioId, String fecha, String estado) {
        io.execute(() -> {
            long t0 = SystemClock.elapsedRealtime();
            db.tomaDao().registrar(new Toma(horarioId, fecha, estado, System.currentTimeMillis()));
            NotificationManagerCompat.from(app).cancel((int) horarioId);
            Log.d(TAG_PERF, "registrarToma ms=" + (SystemClock.elapsedRealtime() - t0));
        });
    }

    public void deshacerToma(long horarioId, String fecha) {
        io.execute(() -> db.tomaDao().deshacer(horarioId, fecha));
    }

    /** Vuelve a programar todas las alarmas (arranque del equipo, cambio de hora, inicio de la app). */
    public void reprogramarTodo() {
        io.execute(() -> {
            long t0 = SystemClock.elapsedRealtime();
            List<Horario> horarios = db.medicamentoDao().todosLosHorarios();
            for (Horario h : horarios) AlarmScheduler.programar(app, h.id, h.minutos);
            Log.d(TAG_PERF, "reprogramarTodo horarios=" + horarios.size()
                    + " ms=" + (SystemClock.elapsedRealtime() - t0));
        });
    }
}

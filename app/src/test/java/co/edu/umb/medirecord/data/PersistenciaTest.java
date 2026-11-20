package co.edu.umb.medirecord.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.robolectric.Shadows.shadowOf;

import android.app.AlarmManager;
import android.content.Context;

import androidx.room.Room;
import androidx.test.core.app.ApplicationProvider;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import co.edu.umb.medirecord.data.entity.Horario;
import co.edu.umb.medirecord.data.entity.Medicamento;
import co.edu.umb.medirecord.data.entity.Toma;
import co.edu.umb.medirecord.data.model.ResumenAdherencia;
import co.edu.umb.medirecord.data.model.TomaDelDia;

/**
 * Pruebas de la capa de datos sobre una base Room en memoria (SQLite real vía Robolectric):
 * CRUD, integridad referencial, prevención de duplicados y programación de alarmas.
 */
@RunWith(RobolectricTestRunner.class)
@Config(sdk = 34)
public class PersistenciaTest {

    private static final String HOY = "2026-11-20";

    private AppDatabase db;
    private MedicamentoRepository repo;
    private Context ctx;

    @Before
    public void setUp() {
        ctx = ApplicationProvider.getApplicationContext();
        db = Room.inMemoryDatabaseBuilder(ctx, AppDatabase.class).allowMainThreadQueries().build();
        repo = new MedicamentoRepository(ctx, db);
    }

    @After
    public void tearDown() {
        db.close();
    }

    private Medicamento med(String nombre) {
        Medicamento m = new Medicamento();
        m.nombre = nombre;
        m.dosis = "50 mg";
        m.tipo = "Pastilla";
        return m;
    }

    @Test
    public void guardar_creaMedicamentoConSusHorarios_yProgramaAlarmas() {
        Medicamento m = med("Losartán");
        assertEquals(MedicamentoRepository.Resultado.OK, repo.guardarSync(m, Arrays.asList(480, 1200)));

        assertTrue(m.id > 0);
        List<Horario> hs = db.medicamentoDao().horariosDe(m.id);
        assertEquals(2, hs.size());
        assertEquals(480, hs.get(0).minutos);

        AlarmManager am = (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);
        assertEquals(2, shadowOf(am).getScheduledAlarms().size());
    }

    @Test
    public void guardar_nombreDuplicadoSinImportarMayusculas_esRechazado() {
        repo.guardarSync(med("Losartán"), Arrays.asList(480));
        assertEquals(MedicamentoRepository.Resultado.NOMBRE_DUPLICADO,
                repo.guardarSync(med("LOSARTÁN"), Arrays.asList(600)));
    }

    @Test
    public void editar_conservaHorariosExistentes_yNoPierdeHistorial() {
        Medicamento m = med("Metformina");
        repo.guardarSync(m, Arrays.asList(480, 1200));
        long idHorario8am = db.medicamentoDao().horariosDe(m.id).get(0).id;
        db.tomaDao().registrar(new Toma(idHorario8am, HOY, Toma.ESTADO_TOMADA, 1L));

        // Se quita 20:00 y se agrega 14:00; 08:00 se conserva.
        m.dosis = "850 mg";
        assertEquals(MedicamentoRepository.Resultado.OK, repo.guardarSync(m, new ArrayList<>(Arrays.asList(480, 840))));

        List<Horario> hs = db.medicamentoDao().horariosDe(m.id);
        assertEquals(2, hs.size());
        assertEquals(idHorario8am, hs.get(0).id);
        assertEquals(840, hs.get(1).minutos);
        assertEquals(1, db.tomaDao().contarTodas());
        assertEquals("850 mg", db.medicamentoDao().obtener(m.id).medicamento.dosis);
    }

    @Test
    public void registrarDosVecesElMismoDia_noDuplica_yQuedaElUltimoEstado() {
        Medicamento m = med("Losartán");
        repo.guardarSync(m, Arrays.asList(480));
        long h = db.medicamentoDao().horariosDe(m.id).get(0).id;

        db.tomaDao().registrar(new Toma(h, HOY, Toma.ESTADO_TOMADA, 1L));
        db.tomaDao().registrar(new Toma(h, HOY, Toma.ESTADO_TOMADA, 2L));
        db.tomaDao().registrar(new Toma(h, HOY, Toma.ESTADO_OMITIDA, 3L));

        assertEquals(1, db.tomaDao().contarTodas());
        assertEquals(Toma.ESTADO_OMITIDA, db.tomaDao().obtenerDia(HOY).get(0).estado);
    }

    @Test
    public void tomasDelDia_muestraPendientesYRegistradas_ordenadasPorHora() {
        Medicamento a = med("Losartán");
        repo.guardarSync(a, Arrays.asList(1200, 480));
        long h8 = db.medicamentoDao().horariosDe(a.id).get(0).id;
        db.tomaDao().registrar(new Toma(h8, HOY, Toma.ESTADO_TOMADA, 1L));

        List<TomaDelDia> dia = db.tomaDao().obtenerDia(HOY);
        assertEquals(2, dia.size());
        assertEquals(480, dia.get(0).minutos);
        assertEquals(Toma.ESTADO_TOMADA, dia.get(0).estado);
        assertNull(dia.get(1).estado); // 20:00 sigue pendiente

        // Otro día: todo pendiente.
        assertNull(db.tomaDao().obtenerDia("2026-11-21").get(0).estado);
    }

    @Test
    public void deshacer_eliminaLaToma() {
        Medicamento m = med("Losartán");
        repo.guardarSync(m, Arrays.asList(480));
        long h = db.medicamentoDao().horariosDe(m.id).get(0).id;
        db.tomaDao().registrar(new Toma(h, HOY, Toma.ESTADO_TOMADA, 1L));
        assertEquals(1, db.tomaDao().deshacer(h, HOY));
        assertEquals(0, db.tomaDao().contarTodas());
    }

    @Test
    public void eliminarMedicamento_borraEnCascadaHorariosYTomas() {
        Medicamento m = med("Losartán");
        repo.guardarSync(m, Arrays.asList(480, 1200));
        long h = db.medicamentoDao().horariosDe(m.id).get(0).id;
        db.tomaDao().registrar(new Toma(h, HOY, Toma.ESTADO_TOMADA, 1L));

        assertEquals(1, db.medicamentoDao().eliminar(m.id));
        assertEquals(0, db.medicamentoDao().todosLosHorarios().size());
        assertEquals(0, db.tomaDao().contarTodas());
    }

    @Test
    public void resumen_cuentaTomadasYOmitidasDesdeUnaFecha() {
        Medicamento m = med("Losartán");
        repo.guardarSync(m, Arrays.asList(480, 1200));
        List<Horario> hs = db.medicamentoDao().horariosDe(m.id);
        db.tomaDao().registrar(new Toma(hs.get(0).id, "2026-11-19", Toma.ESTADO_TOMADA, 1L));
        db.tomaDao().registrar(new Toma(hs.get(1).id, "2026-11-19", Toma.ESTADO_OMITIDA, 1L));
        db.tomaDao().registrar(new Toma(hs.get(0).id, HOY, Toma.ESTADO_TOMADA, 1L));
        db.tomaDao().registrar(new Toma(hs.get(0).id, "2026-10-01", Toma.ESTADO_OMITIDA, 1L)); // fuera del rango

        ResumenAdherencia r = db.tomaDao().obtenerResumen("2026-11-14");
        assertEquals(2, r.tomadas);
        assertEquals(1, r.omitidas);
    }
}

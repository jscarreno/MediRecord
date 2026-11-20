package co.edu.umb.medirecord.ui;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.robolectric.Shadows.shadowOf;

import android.Manifest;
import android.app.Application;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.Looper;
import android.view.View;

import androidx.recyclerview.widget.RecyclerView;
import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;

import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputLayout;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.GraphicsMode;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Arrays;
import java.util.List;

import co.edu.umb.medirecord.R;
import co.edu.umb.medirecord.data.AppDatabase;
import co.edu.umb.medirecord.data.MedicamentoRepository;
import co.edu.umb.medirecord.data.entity.Horario;
import co.edu.umb.medirecord.data.entity.Medicamento;
import co.edu.umb.medirecord.data.entity.Toma;
import co.edu.umb.medirecord.domain.HoraUtils;
import co.edu.umb.medirecord.ui.medicamentos.MedicamentoFormActivity;
import co.edu.umb.medirecord.ui.medicamentos.MedicamentoFormViewModel;

/**
 * Pruebas de interfaz en JVM (Robolectric, gráficos nativos): flujos principales,
 * validaciones, recreación por rotación y capturas de pantalla en build/capturas/.
 * Pantalla simulada: 411x891 dp, xxhdpi (similar a un Galaxy A34).
 */
@RunWith(RobolectricTestRunner.class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = 34, qualifiers = "es-rCO-w411dp-h891dp-port-xxhdpi")
public class PantallasTest {

    private static final File CAPTURAS = new File("build/capturas");

    private MedicamentoRepository repo;

    @Before
    public void setUp() {
        Application app = ApplicationProvider.getApplicationContext();
        shadowOf(app).grantPermissions(Manifest.permission.POST_NOTIFICATIONS);
        repo = MedicamentoRepository.get(app);
        enHilo(() -> {
            AppDatabase.get(app).clearAllTables();
            return null;
        });
        CAPTURAS.mkdirs();
    }

    private long sembrar(String nombre, String dosis, String tipo, Integer... horas) {
        Medicamento m = new Medicamento();
        m.nombre = nombre;
        m.dosis = dosis;
        m.tipo = tipo;
        enHilo(() -> repo.guardarSync(m, Arrays.asList(horas)));
        return m.id;
    }

    private void datosDeEjemplo() {
        enHilo(() -> {
            datosDeEjemploSync();
            return null;
        });
    }

    private void datosDeEjemploSync() {
        long a = sembrar("Losartán", "50 mg", "Pastilla", 7 * 60, 19 * 60);
        sembrar("Metformina", "850 mg", "Pastilla", 13 * 60);
        sembrar("Jarabe para la tos", "10 ml", "Jarabe", 21 * 60 + 30);
        List<Horario> hs = AppDatabase.get(ApplicationProvider.getApplicationContext()).medicamentoDao().horariosDe(a);
        AppDatabase db = AppDatabase.get(ApplicationProvider.getApplicationContext());
        long ahora = System.currentTimeMillis();
        db.tomaDao().registrar(new Toma(hs.get(0).id, HoraUtils.hoy(), Toma.ESTADO_TOMADA, ahora));
        for (int d = 1; d <= 3; d++) {
            String f = HoraUtils.haceDias(d);
            db.tomaDao().registrar(new Toma(hs.get(0).id, f, Toma.ESTADO_TOMADA, ahora));
            db.tomaDao().registrar(new Toma(hs.get(1).id, f, d == 2 ? Toma.ESTADO_OMITIDA : Toma.ESTADO_TOMADA, ahora));
        }
    }

    /** Room prohíbe consultas en el hilo principal: los datos de prueba se preparan en otro hilo. */
    private static <T> T enHilo(java.util.concurrent.Callable<T> c) {
        try {
            java.util.concurrent.ExecutorService ex = java.util.concurrent.Executors.newSingleThreadExecutor();
            T r = ex.submit(c).get();
            ex.shutdown();
            return r;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /** Room entrega LiveData desde un hilo de fondo: se espera y se procesa la cola del hilo principal. */
    private static void esperarUI() {
        for (int i = 0; i < 30; i++) {
            try {
                Thread.sleep(40);
            } catch (InterruptedException ignored) {
            }
            shadowOf(Looper.getMainLooper()).idle();
        }
    }

    private static void capturar(View raiz, String nombre) throws Exception {
        Bitmap bmp = Bitmap.createBitmap(raiz.getWidth(), raiz.getHeight(), Bitmap.Config.ARGB_8888);
        raiz.draw(new Canvas(bmp));
        try (FileOutputStream out = new FileOutputStream(new File(CAPTURAS, nombre + ".png"))) {
            bmp.compress(Bitmap.CompressFormat.PNG, 100, out);
        }
    }

    @Test
    public void pantallaHoy_muestraTomasDelDia_yPermiteMarcarTomada() throws Exception {
        datosDeEjemplo();
        try (ActivityScenario<MainActivity> s = ActivityScenario.launch(MainActivity.class)) {
            esperarUI();
            s.onActivity(a -> {
                RecyclerView lista = a.findViewById(R.id.lista);
                assertEquals(4, lista.getAdapter().getItemCount());
                try {
                    capturar(a.getWindow().getDecorView(), "01_hoy");
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                // Pulsar "Ya la tomé" en la tarjeta de Metformina (13:00, segunda fila).
                RecyclerView.ViewHolder vh = lista.findViewHolderForAdapterPosition(1);
                assertNotNull(vh);
                vh.itemView.findViewById(R.id.btnTomar).performClick();
                // Doble pulsación: el botón queda deshabilitado de inmediato.
                assertTrue(!vh.itemView.findViewById(R.id.btnTomar).isEnabled());
            });
            esperarUI();
            AppDatabase db = AppDatabase.get(ApplicationProvider.getApplicationContext());
            assertEquals(Toma.ESTADO_TOMADA, enHilo(() -> db.tomaDao().obtenerDia(HoraUtils.hoy()).get(1).estado));
            s.onActivity(a -> {
                try {
                    capturar(a.getWindow().getDecorView(), "02_hoy_tras_marcar");
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
        }
    }

    @Test
    public void pantallaMedicamentos_eHistorial_muestranDatos() throws Exception {
        datosDeEjemplo();
        try (ActivityScenario<MainActivity> s = ActivityScenario.launch(MainActivity.class)) {
            esperarUI();
            s.onActivity(a -> ((com.google.android.material.bottomnavigation.BottomNavigationView)
                    a.findViewById(R.id.navegacion)).setSelectedItemId(R.id.nav_medicamentos));
            esperarUI();
            s.onActivity(a -> {
                RecyclerView lista = a.findViewById(R.id.lista);
                assertEquals(3, lista.getAdapter().getItemCount());
                try {
                    capturar(a.getWindow().getDecorView(), "03_medicamentos");
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                ((com.google.android.material.bottomnavigation.BottomNavigationView)
                        a.findViewById(R.id.navegacion)).setSelectedItemId(R.id.nav_historial);
            });
            esperarUI();
            s.onActivity(a -> {
                RecyclerView lista = a.findViewById(R.id.lista);
                assertEquals(7, lista.getAdapter().getItemCount());
                try {
                    capturar(a.getWindow().getDecorView(), "04_historial");
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
        }
    }

    @Test
    public void estadosVacios_seMuestranSinDatos() throws Exception {
        try (ActivityScenario<MainActivity> s = ActivityScenario.launch(MainActivity.class)) {
            esperarUI();
            s.onActivity(a -> {
                assertEquals(View.VISIBLE, a.findViewById(R.id.tvVacio).getVisibility());
                try {
                    capturar(a.getWindow().getDecorView(), "05_hoy_vacio");
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
        }
    }

    @Test
    public void formulario_vacio_muestraErroresDeValidacion() throws Exception {
        try (ActivityScenario<MedicamentoFormActivity> s = ActivityScenario.launch(MedicamentoFormActivity.class)) {
            esperarUI();
            s.onActivity(a -> {
                a.findViewById(R.id.btnGuardar).performClick();
                TextInputLayout nombre = a.findViewById(R.id.tilNombre);
                TextInputLayout dosis = a.findViewById(R.id.tilDosis);
                assertEquals(a.getString(R.string.err_nombre_vacio), String.valueOf(nombre.getError()));
                assertEquals(a.getString(R.string.err_dosis_vacia), String.valueOf(dosis.getError()));
                assertEquals(View.VISIBLE, a.findViewById(R.id.tvErrorHorarios).getVisibility());
            });
            esperarUI();
            s.onActivity(a -> {
                try {
                    capturar(a.getWindow().getDecorView(), "06_formulario_errores");
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
            AppDatabase db = AppDatabase.get(ApplicationProvider.getApplicationContext());
            assertEquals(0, (int) enHilo(() -> db.medicamentoDao().todosLosHorarios().size()));
        }
    }

    @Test
    public void formulario_rotacion_conservaHorariosYTexto() throws Exception {
        try (ActivityScenario<MedicamentoFormActivity> s = ActivityScenario.launch(MedicamentoFormActivity.class)) {
            esperarUI();
            s.onActivity(a -> {
                MedicamentoFormViewModel vm = new androidx.lifecycle.ViewModelProvider(a).get(MedicamentoFormViewModel.class);
                vm.agregarHorario(8 * 60);
                vm.agregarHorario(20 * 60);
                assertTrue(!vm.agregarHorario(8 * 60)); // duplicado rechazado
                ((android.widget.EditText) a.findViewById(R.id.etNombre)).setText("Atorvastatina");
            });
            s.recreate(); // equivalente a rotar la pantalla
            esperarUI();
            s.onActivity(a -> {
                ChipGroup chips = a.findViewById(R.id.chipsHorarios);
                assertEquals(2, chips.getChildCount());
                assertEquals("Atorvastatina",
                        ((android.widget.EditText) a.findViewById(R.id.etNombre)).getText().toString());
            });
        }
    }

    @Test
    public void formulario_edicion_cargaDatos_yGuarda() throws Exception {
        long id = sembrar("Losartán", "50 mg", "Pastilla", 7 * 60, 19 * 60);
        Intent i = new Intent(ApplicationProvider.getApplicationContext(), MedicamentoFormActivity.class)
                .putExtra(MedicamentoFormActivity.EXTRA_ID, id);
        try (ActivityScenario<MedicamentoFormActivity> s = ActivityScenario.launch(i)) {
            esperarUI();
            s.onActivity(a -> {
                assertEquals("Losartán", ((android.widget.EditText) a.findViewById(R.id.etNombre)).getText().toString());
                assertEquals(2, ((ChipGroup) a.findViewById(R.id.chipsHorarios)).getChildCount());
                assertEquals(View.VISIBLE, a.findViewById(R.id.btnEliminar).getVisibility());
                try {
                    capturar(a.getWindow().getDecorView(), "07_formulario_edicion");
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                ((android.widget.EditText) a.findViewById(R.id.etDosis)).setText("100 mg");
                a.findViewById(R.id.btnGuardar).performClick();
            });
            esperarUI();
            AppDatabase db = AppDatabase.get(ApplicationProvider.getApplicationContext());
            assertEquals("100 mg", enHilo(() -> db.medicamentoDao().obtener(id).medicamento.dosis));
        }
    }
}

package co.edu.umb.medirecord.ui;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuItem;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import co.edu.umb.medirecord.R;
import co.edu.umb.medirecord.alarm.AlarmScheduler;
import co.edu.umb.medirecord.data.MedicamentoRepository;
import co.edu.umb.medirecord.databinding.ActivityMainBinding;
import co.edu.umb.medirecord.ui.historial.HistorialFragment;
import co.edu.umb.medirecord.ui.hoy.HoyFragment;
import co.edu.umb.medirecord.ui.medicamentos.MedicamentosFragment;

/** Contenedor de las tres secciones (Hoy, Medicamentos, Historial) con navegación inferior. */
public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding b;

    private final ActivityResultLauncher<String> pedirPermisoNotif =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), concedido -> {
                // El banner de la pantalla Hoy refleja el estado en onResume.
                Fragment f = getSupportFragmentManager().findFragmentById(R.id.contenedor);
                if (f instanceof HoyFragment) ((HoyFragment) f).actualizarBanner();
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());
        setSupportActionBar(b.toolbar);

        b.navegacion.setOnItemSelectedListener(item -> {
            mostrar(item.getItemId());
            return true;
        });

        if (savedInstanceState == null) {
            b.navegacion.setSelectedItemId(R.id.nav_hoy);
            pedirPermisoNotificacionesSiFalta();
            MedicamentoRepository.get(this).reprogramarTodo();
        }
    }

    private void mostrar(int itemId) {
        Fragment f;
        int titulo;
        if (itemId == R.id.nav_medicamentos) {
            f = new MedicamentosFragment();
            titulo = R.string.titulo_medicamentos;
        } else if (itemId == R.id.nav_historial) {
            f = new HistorialFragment();
            titulo = R.string.titulo_historial;
        } else {
            f = new HoyFragment();
            titulo = R.string.titulo_hoy;
        }
        setTitle(titulo);
        getSupportFragmentManager().beginTransaction().replace(R.id.contenedor, f).commit();
    }

    public void pedirPermisoNotificacionesSiFalta() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                && ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
            if (shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {
                new MaterialAlertDialogBuilder(this)
                        .setTitle(R.string.permiso_titulo)
                        .setMessage(R.string.permiso_explicacion)
                        .setPositiveButton(R.string.permitir,
                                (d, w) -> pedirPermisoNotif.launch(Manifest.permission.POST_NOTIFICATIONS))
                        .setNegativeButton(R.string.ahora_no, null)
                        .show();
            } else {
                pedirPermisoNotif.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
        }
    }

    public void abrirAjustesNotificaciones() {
        Intent i = new Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                .putExtra(Settings.EXTRA_APP_PACKAGE, getPackageName());
        startActivity(i);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_principal, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem exactas = menu.findItem(R.id.menu_alarmas_exactas);
        exactas.setVisible(!AlarmScheduler.puedeUsarAlarmasExactas(this));
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_alarmas_exactas && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            startActivity(new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM,
                    Uri.parse("package:" + getPackageName())));
            return true;
        } else if (item.getItemId() == R.id.menu_acerca) {
            new MaterialAlertDialogBuilder(this)
                    .setTitle(R.string.app_name)
                    .setMessage(R.string.acerca_texto)
                    .setPositiveButton(android.R.string.ok, null)
                    .show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        invalidateOptionsMenu();
    }
}

package co.edu.umb.medirecord.ui.medicamentos;

import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.chip.Chip;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.Calendar;
import java.util.List;
import java.util.Set;

import co.edu.umb.medirecord.R;
import co.edu.umb.medirecord.data.MedicamentoRepository;
import co.edu.umb.medirecord.data.model.MedicamentoConHorarios;
import co.edu.umb.medirecord.databinding.ActivityMedicamentoFormBinding;
import co.edu.umb.medirecord.domain.HoraUtils;
import co.edu.umb.medirecord.domain.MedicamentoValidator;
import co.edu.umb.medirecord.domain.MedicamentoValidator.Error;

/** Vista "detalle": crear, ver/editar y eliminar un medicamento con sus horarios. */
public class MedicamentoFormActivity extends AppCompatActivity {

    public static final String EXTRA_ID = "medicamentoId";

    private ActivityMedicamentoFormBinding b;
    private MedicamentoFormViewModel vm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = ActivityMedicamentoFormBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());
        setSupportActionBar(b.toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        vm = new ViewModelProvider(this).get(MedicamentoFormViewModel.class);
        vm.iniciar(getIntent().getLongExtra(EXTRA_ID, 0));

        setTitle(vm.esEdicion() ? R.string.titulo_editar : R.string.titulo_nuevo);
        b.btnEliminar.setVisibility(vm.esEdicion() ? View.VISIBLE : View.GONE);

        String[] tipos = getResources().getStringArray(R.array.tipos_medicamento);
        b.etTipo.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, tipos));
        if (savedInstanceState == null && !vm.esEdicion()) b.etTipo.setText(tipos[0], false);

        vm.getCargado().observe(this, this::aplicarDatos);
        vm.getHorarios().observe(this, this::pintarHorarios);
        vm.getOcupado().observe(this, ocupado -> {
            b.progreso.setVisibility(ocupado ? View.VISIBLE : View.GONE);
            b.btnGuardar.setEnabled(!ocupado);
            b.btnEliminar.setEnabled(!ocupado);
        });
        vm.getResultadoGuardar().observe(this, this::alGuardar);
        vm.getResultadoEliminar().observe(this, r -> {
            if (r == null) return;
            vm.consumirResultados();
            if (r == MedicamentoRepository.Resultado.OK) {
                Toast.makeText(this, R.string.msg_eliminado, Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, R.string.msg_error_bd, Toast.LENGTH_LONG).show();
            }
        });

        b.btnAgregarHorario.setOnClickListener(v -> abrirSelectorHora());
        b.btnGuardar.setOnClickListener(v -> guardar());
        b.btnEliminar.setOnClickListener(v -> confirmarEliminar());
        b.toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void aplicarDatos(MedicamentoConHorarios m) {
        if (m == null || vm.camposAplicados) return;
        vm.camposAplicados = true;
        b.etNombre.setText(m.medicamento.nombre);
        b.etDosis.setText(m.medicamento.dosis);
        b.etTipo.setText(m.medicamento.tipo, false);
        b.etNotas.setText(m.medicamento.notas);
    }

    private void pintarHorarios(List<Integer> horarios) {
        b.chipsHorarios.removeAllViews();
        for (int mins : horarios) {
            Chip chip = new Chip(this);
            String hora = HoraUtils.formatear(mins);
            chip.setText(hora);
            chip.setTextSize(18);
            chip.setCloseIconVisible(true);
            chip.setCloseIconContentDescription(getString(R.string.cd_quitar_horario, hora));
            chip.setOnCloseIconClickListener(v -> vm.quitarHorario(mins));
            b.chipsHorarios.addView(chip);
        }
        b.tvSinHorarios.setVisibility(horarios.isEmpty() ? View.VISIBLE : View.GONE);
        if (!horarios.isEmpty()) b.tvErrorHorarios.setVisibility(View.GONE);
    }

    private void abrirSelectorHora() {
        if (vm.getHorarios().getValue().size() >= MedicamentoValidator.MAX_HORARIOS) {
            mostrarErrorHorarios(getString(R.string.err_demasiados_horarios, MedicamentoValidator.MAX_HORARIOS));
            return;
        }
        Calendar c = Calendar.getInstance();
        new TimePickerDialog(this, (picker, hora, minuto) -> {
            if (!vm.agregarHorario(HoraUtils.aMinutos(hora, minuto))) {
                mostrarErrorHorarios(getString(R.string.err_horario_duplicado));
            }
        }, c.get(Calendar.HOUR_OF_DAY), 0, DateFormat.is24HourFormat(this)).show();
    }

    private void guardar() {
        limpiarErrores();
        Set<Error> errores = vm.guardar(texto(b.etNombre.getText()), texto(b.etDosis.getText()),
                texto(b.etTipo.getText()), texto(b.etNotas.getText()));
        if (errores.isEmpty()) return;

        if (errores.contains(Error.NOMBRE_VACIO)) b.tilNombre.setError(getString(R.string.err_nombre_vacio));
        else if (errores.contains(Error.NOMBRE_CORTO)) b.tilNombre.setError(getString(R.string.err_nombre_corto));
        else if (errores.contains(Error.NOMBRE_LARGO))
            b.tilNombre.setError(getString(R.string.err_nombre_largo, MedicamentoValidator.MAX_NOMBRE));

        if (errores.contains(Error.DOSIS_VACIA)) b.tilDosis.setError(getString(R.string.err_dosis_vacia));
        else if (errores.contains(Error.DOSIS_LARGA))
            b.tilDosis.setError(getString(R.string.err_dosis_larga, MedicamentoValidator.MAX_DOSIS));

        if (errores.contains(Error.TIPO_VACIO)) b.tilTipo.setError(getString(R.string.err_tipo_vacio));

        if (errores.contains(Error.SIN_HORARIOS)) mostrarErrorHorarios(getString(R.string.err_sin_horarios));
        else if (errores.contains(Error.HORARIO_DUPLICADO)) mostrarErrorHorarios(getString(R.string.err_horario_duplicado));
        else if (errores.contains(Error.DEMASIADOS_HORARIOS))
            mostrarErrorHorarios(getString(R.string.err_demasiados_horarios, MedicamentoValidator.MAX_HORARIOS));

        // Lleva el foco al primer campo con error (útil también con TalkBack).
        if (b.tilNombre.getError() != null) b.etNombre.requestFocus();
        else if (b.tilDosis.getError() != null) b.etDosis.requestFocus();
    }

    private void alGuardar(MedicamentoRepository.Resultado r) {
        if (r == null) return;
        vm.consumirResultados();
        switch (r) {
            case OK:
                Toast.makeText(this, R.string.msg_guardado, Toast.LENGTH_SHORT).show();
                finish();
                break;
            case NOMBRE_DUPLICADO:
                b.tilNombre.setError(getString(R.string.err_nombre_duplicado));
                b.etNombre.requestFocus();
                break;
            case NO_ENCONTRADO:
                Toast.makeText(this, R.string.msg_no_encontrado, Toast.LENGTH_LONG).show();
                finish();
                break;
            default:
                Toast.makeText(this, R.string.msg_error_bd, Toast.LENGTH_LONG).show();
        }
    }

    private void confirmarEliminar() {
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.eliminar_titulo)
                .setMessage(getString(R.string.eliminar_mensaje, texto(b.etNombre.getText())))
                .setPositiveButton(R.string.eliminar, (d, w) -> vm.eliminar())
                .setNegativeButton(R.string.cancelar, null)
                .show();
    }

    private void mostrarErrorHorarios(String msg) {
        b.tvErrorHorarios.setText(msg);
        b.tvErrorHorarios.setVisibility(View.VISIBLE);
        b.tvErrorHorarios.announceForAccessibility(msg);
    }

    private void limpiarErrores() {
        b.tilNombre.setError(null);
        b.tilDosis.setError(null);
        b.tilTipo.setError(null);
        b.tvErrorHorarios.setVisibility(View.GONE);
    }

    private static String texto(CharSequence cs) {
        return cs == null ? "" : cs.toString();
    }
}

package co.edu.umb.medirecord.ui.medicamentos;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import co.edu.umb.medirecord.data.MedicamentoRepository;
import co.edu.umb.medirecord.data.entity.Horario;
import co.edu.umb.medirecord.data.entity.Medicamento;
import co.edu.umb.medirecord.data.model.MedicamentoConHorarios;
import co.edu.umb.medirecord.domain.MedicamentoValidator;

/**
 * Estado del formulario. Sobrevive a la rotación: los horarios agregados y el
 * estado "guardando" no se pierden al recrear la Activity.
 */
public class MedicamentoFormViewModel extends AndroidViewModel {

    private final MedicamentoRepository repo;

    private final MutableLiveData<List<Integer>> horarios = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Boolean> ocupado = new MutableLiveData<>(false);
    private final MutableLiveData<MedicamentoConHorarios> cargado = new MutableLiveData<>();
    private final MutableLiveData<MedicamentoRepository.Resultado> resultadoGuardar = new MutableLiveData<>();
    private final MutableLiveData<MedicamentoRepository.Resultado> resultadoEliminar = new MutableLiveData<>();

    private long id;
    private boolean iniciado;
    /** true cuando los campos de texto ya se llenaron con los datos cargados (no repetir tras rotar). */
    public boolean camposAplicados;

    public MedicamentoFormViewModel(@NonNull Application app) {
        super(app);
        repo = MedicamentoRepository.get(app);
    }

    public void iniciar(long id) {
        if (iniciado) return;
        iniciado = true;
        this.id = id;
        if (id > 0) {
            ocupado.setValue(true);
            repo.cargar(id, m -> {
                ocupado.setValue(false);
                if (m == null) {
                    resultadoGuardar.setValue(MedicamentoRepository.Resultado.NO_ENCONTRADO);
                    return;
                }
                List<Integer> mins = new ArrayList<>();
                for (Horario h : m.horarios) mins.add(h.minutos);
                Collections.sort(mins);
                horarios.setValue(mins);
                cargado.setValue(m);
            });
        }
    }

    public boolean esEdicion() {
        return id > 0;
    }

    public LiveData<List<Integer>> getHorarios() {
        return horarios;
    }

    public LiveData<Boolean> getOcupado() {
        return ocupado;
    }

    public LiveData<MedicamentoConHorarios> getCargado() {
        return cargado;
    }

    public LiveData<MedicamentoRepository.Resultado> getResultadoGuardar() {
        return resultadoGuardar;
    }

    public LiveData<MedicamentoRepository.Resultado> getResultadoEliminar() {
        return resultadoEliminar;
    }

    public void consumirResultados() {
        resultadoGuardar.setValue(null);
        resultadoEliminar.setValue(null);
    }

    /** @return false si la hora ya estaba en la lista. */
    public boolean agregarHorario(int minutos) {
        List<Integer> actual = new ArrayList<>(horarios.getValue());
        if (actual.contains(minutos)) return false;
        actual.add(minutos);
        Collections.sort(actual);
        horarios.setValue(actual);
        return true;
    }

    public void quitarHorario(int minutos) {
        List<Integer> actual = new ArrayList<>(horarios.getValue());
        actual.remove(Integer.valueOf(minutos));
        horarios.setValue(actual);
    }

    /** Valida y, si todo está bien, guarda. Devuelve los errores para pintarlos en la vista. */
    public Set<MedicamentoValidator.Error> guardar(String nombre, String dosis, String tipo, String notas) {
        List<Integer> hs = horarios.getValue();
        Set<MedicamentoValidator.Error> errores = MedicamentoValidator.validar(nombre, dosis, tipo, hs);
        if (!errores.isEmpty() || Boolean.TRUE.equals(ocupado.getValue())) return errores;

        Medicamento m = new Medicamento();
        m.id = id;
        m.nombre = nombre.trim();
        m.dosis = dosis.trim();
        m.tipo = tipo.trim();
        m.notas = notas == null || notas.trim().isEmpty() ? null : notas.trim();
        MedicamentoConHorarios previo = cargado.getValue();
        if (previo != null) m.creadoEn = previo.medicamento.creadoEn;

        ocupado.setValue(true);
        repo.guardar(m, hs, r -> {
            ocupado.setValue(false);
            if (r == MedicamentoRepository.Resultado.OK) id = m.id;
            resultadoGuardar.setValue(r);
        });
        return errores;
    }

    public void eliminar() {
        if (id <= 0 || Boolean.TRUE.equals(ocupado.getValue())) return;
        ocupado.setValue(true);
        repo.eliminar(id, r -> {
            ocupado.setValue(false);
            resultadoEliminar.setValue(r);
        });
    }
}

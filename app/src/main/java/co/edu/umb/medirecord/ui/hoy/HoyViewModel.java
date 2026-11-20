package co.edu.umb.medirecord.ui.hoy;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import java.util.List;

import co.edu.umb.medirecord.data.MedicamentoRepository;
import co.edu.umb.medirecord.data.entity.Toma;
import co.edu.umb.medirecord.data.model.ResumenAdherencia;
import co.edu.umb.medirecord.data.model.TomaDelDia;
import co.edu.umb.medirecord.domain.HoraUtils;

public class HoyViewModel extends AndroidViewModel {

    private final MedicamentoRepository repo;
    private final MutableLiveData<String> fecha = new MutableLiveData<>(HoraUtils.hoy());

    public final LiveData<List<TomaDelDia>> tomas;
    public final LiveData<ResumenAdherencia> resumen;

    public HoyViewModel(@NonNull Application app) {
        super(app);
        repo = MedicamentoRepository.get(app);
        tomas = Transformations.switchMap(fecha, f -> repo.observarDia(f));
        resumen = Transformations.switchMap(fecha, f -> repo.observarResumen(HoraUtils.haceDias(6)));
    }

    public LiveData<String> getFecha() {
        return fecha;
    }

    /** Si la app quedó abierta pasada la medianoche, cambia al nuevo día. */
    public void refrescarFecha() {
        String hoy = HoraUtils.hoy();
        if (!hoy.equals(fecha.getValue())) fecha.setValue(hoy);
    }

    public void tomar(TomaDelDia t) {
        repo.registrarToma(t.horarioId, fecha.getValue(), Toma.ESTADO_TOMADA);
    }

    public void omitir(TomaDelDia t) {
        repo.registrarToma(t.horarioId, fecha.getValue(), Toma.ESTADO_OMITIDA);
    }

    public void deshacer(TomaDelDia t) {
        repo.deshacerToma(t.horarioId, fecha.getValue());
    }
}

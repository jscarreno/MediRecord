package co.edu.umb.medirecord.ui.historial;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.List;

import co.edu.umb.medirecord.data.MedicamentoRepository;
import co.edu.umb.medirecord.data.model.RegistroHistorial;
import co.edu.umb.medirecord.data.model.ResumenAdherencia;
import co.edu.umb.medirecord.domain.HoraUtils;

public class HistorialViewModel extends AndroidViewModel {

    public final LiveData<List<RegistroHistorial>> registros;
    public final LiveData<ResumenAdherencia> resumen30;

    public HistorialViewModel(@NonNull Application app) {
        super(app);
        MedicamentoRepository repo = MedicamentoRepository.get(app);
        registros = repo.observarHistorial();
        resumen30 = repo.observarResumen(HoraUtils.haceDias(29));
    }
}

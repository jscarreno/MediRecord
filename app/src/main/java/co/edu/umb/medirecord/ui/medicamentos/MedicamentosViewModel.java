package co.edu.umb.medirecord.ui.medicamentos;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.List;

import co.edu.umb.medirecord.data.MedicamentoRepository;
import co.edu.umb.medirecord.data.model.MedicamentoConHorarios;

public class MedicamentosViewModel extends AndroidViewModel {

    public final LiveData<List<MedicamentoConHorarios>> medicamentos;

    public MedicamentosViewModel(@NonNull Application app) {
        super(app);
        medicamentos = MedicamentoRepository.get(app).observarMedicamentos();
    }
}

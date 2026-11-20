package co.edu.umb.medirecord.ui.medicamentos;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import co.edu.umb.medirecord.databinding.FragmentMedicamentosBinding;

/** Vista "maestro": lista de medicamentos. Tocar uno abre su detalle/edición. */
public class MedicamentosFragment extends Fragment {

    private FragmentMedicamentosBinding b;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        b = FragmentMedicamentosBinding.inflate(inflater, container, false);
        return b.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        MedicamentosViewModel vm = new ViewModelProvider(this).get(MedicamentosViewModel.class);
        MedicamentoAdapter adapter = new MedicamentoAdapter(m -> abrirFormulario(m.medicamento.id));
        b.lista.setLayoutManager(new LinearLayoutManager(requireContext()));
        b.lista.setAdapter(adapter);

        vm.medicamentos.observe(getViewLifecycleOwner(), lista -> {
            adapter.submitList(lista);
            boolean vacia = lista.isEmpty();
            b.tvVacio.setVisibility(vacia ? View.VISIBLE : View.GONE);
            b.lista.setVisibility(vacia ? View.GONE : View.VISIBLE);
        });

        b.fabAgregar.setOnClickListener(v -> abrirFormulario(0));
    }

    private void abrirFormulario(long id) {
        Intent i = new Intent(requireContext(), MedicamentoFormActivity.class);
        if (id > 0) i.putExtra(MedicamentoFormActivity.EXTRA_ID, id);
        startActivity(i);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        b = null;
    }
}

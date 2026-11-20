package co.edu.umb.medirecord.ui.historial;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import co.edu.umb.medirecord.R;
import co.edu.umb.medirecord.databinding.FragmentHistorialBinding;
import co.edu.umb.medirecord.domain.AdherenciaCalculator;

public class HistorialFragment extends Fragment {

    private FragmentHistorialBinding b;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        b = FragmentHistorialBinding.inflate(inflater, container, false);
        return b.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        HistorialViewModel vm = new ViewModelProvider(this).get(HistorialViewModel.class);
        HistorialAdapter adapter = new HistorialAdapter();
        b.lista.setLayoutManager(new LinearLayoutManager(requireContext()));
        b.lista.setAdapter(adapter);

        vm.registros.observe(getViewLifecycleOwner(), lista -> {
            adapter.submitList(lista);
            boolean vacia = lista.isEmpty();
            b.tvVacio.setVisibility(vacia ? View.VISIBLE : View.GONE);
            b.lista.setVisibility(vacia ? View.GONE : View.VISIBLE);
        });

        vm.resumen30.observe(getViewLifecycleOwner(), r -> {
            int p = AdherenciaCalculator.porcentaje(r.tomadas, r.omitidas);
            b.tvResumen.setText(p == AdherenciaCalculator.SIN_DATOS
                    ? getString(R.string.resumen_sin_datos)
                    : getString(R.string.resumen_mes, p, r.tomadas, r.omitidas));
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        b = null;
    }
}

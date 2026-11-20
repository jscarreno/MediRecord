package co.edu.umb.medirecord.ui.hoy;

import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import co.edu.umb.medirecord.R;
import co.edu.umb.medirecord.alarm.NotificationHelper;
import co.edu.umb.medirecord.data.MedicamentoRepository;
import co.edu.umb.medirecord.data.model.TomaDelDia;
import co.edu.umb.medirecord.databinding.FragmentHoyBinding;
import co.edu.umb.medirecord.domain.AdherenciaCalculator;
import co.edu.umb.medirecord.domain.HoraUtils;
import co.edu.umb.medirecord.ui.MainActivity;

public class HoyFragment extends Fragment implements TomaDelDiaAdapter.Listener {

    private FragmentHoyBinding b;
    private HoyViewModel vm;
    private TomaDelDiaAdapter adapter;
    private long inicioCarga;
    private boolean primeraCargaMedida;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        inicioCarga = SystemClock.elapsedRealtime();
        b = FragmentHoyBinding.inflate(inflater, container, false);
        return b.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        vm = new ViewModelProvider(this).get(HoyViewModel.class);
        adapter = new TomaDelDiaAdapter(this);
        b.lista.setLayoutManager(new LinearLayoutManager(requireContext()));
        b.lista.setAdapter(adapter);

        vm.getFecha().observe(getViewLifecycleOwner(), f ->
                b.tvFecha.setText(getString(R.string.hoy_fecha, HoraUtils.fechaLegible(f))));

        vm.tomas.observe(getViewLifecycleOwner(), lista -> {
            if (!primeraCargaMedida) {
                primeraCargaMedida = true;
                Log.d(MedicamentoRepository.TAG_PERF, "pantallaHoy primerosDatos ms="
                        + (SystemClock.elapsedRealtime() - inicioCarga) + " filas=" + lista.size());
            }
            adapter.submitList(lista);
            boolean vacia = lista.isEmpty();
            b.tvVacio.setVisibility(vacia ? View.VISIBLE : View.GONE);
            b.lista.setVisibility(vacia ? View.GONE : View.VISIBLE);
            actualizarProgresoDia(lista);
        });

        vm.resumen.observe(getViewLifecycleOwner(), r -> {
            int p = AdherenciaCalculator.porcentaje(r.tomadas, r.omitidas);
            b.tvResumen.setText(p == AdherenciaCalculator.SIN_DATOS
                    ? getString(R.string.resumen_sin_datos)
                    : getString(R.string.resumen_semana, p, r.tomadas, r.tomadas + r.omitidas));
        });

        // Si el permiso fue denegado, el sistema ya no muestra el diálogo: se lleva al usuario a Ajustes.
        b.btnActivarNotif.setOnClickListener(v ->
                ((MainActivity) requireActivity()).abrirAjustesNotificaciones());
    }

    private void actualizarProgresoDia(java.util.List<TomaDelDia> lista) {
        int hechas = 0;
        for (TomaDelDia t : lista) if (!t.estaPendiente()) hechas++;
        b.tvProgreso.setText(getString(R.string.hoy_progreso, hechas, lista.size()));
    }

    @Override
    public void onResume() {
        super.onResume();
        vm.refrescarFecha();
        adapter.actualizarHoraActual();
        actualizarBanner();
    }

    public void actualizarBanner() {
        if (b == null) return;
        boolean ok = NotificationHelper.tienePermiso(requireContext());
        b.bannerNotif.setVisibility(ok ? View.GONE : View.VISIBLE);
    }

    @Override
    public void onTomar(TomaDelDia t) {
        vm.tomar(t);
    }

    @Override
    public void onOmitir(TomaDelDia t) {
        vm.omitir(t);
    }

    @Override
    public void onDeshacer(TomaDelDia t) {
        vm.deshacer(t);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        b = null;
    }
}

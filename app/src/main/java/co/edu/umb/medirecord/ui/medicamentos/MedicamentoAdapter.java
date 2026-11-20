package co.edu.umb.medirecord.ui.medicamentos;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import co.edu.umb.medirecord.R;
import co.edu.umb.medirecord.data.entity.Horario;
import co.edu.umb.medirecord.data.model.MedicamentoConHorarios;
import co.edu.umb.medirecord.databinding.ItemMedicamentoBinding;
import co.edu.umb.medirecord.domain.HoraUtils;

public class MedicamentoAdapter extends ListAdapter<MedicamentoConHorarios, MedicamentoAdapter.VH> {

    public interface OnClick {
        void onClick(MedicamentoConHorarios m);
    }

    private static final DiffUtil.ItemCallback<MedicamentoConHorarios> DIFF = new DiffUtil.ItemCallback<>() {
        @Override
        public boolean areItemsTheSame(@NonNull MedicamentoConHorarios a, @NonNull MedicamentoConHorarios b) {
            return a.medicamento.id == b.medicamento.id;
        }

        @Override
        public boolean areContentsTheSame(@NonNull MedicamentoConHorarios a, @NonNull MedicamentoConHorarios b) {
            return Objects.equals(a.medicamento.nombre, b.medicamento.nombre)
                    && Objects.equals(a.medicamento.dosis, b.medicamento.dosis)
                    && Objects.equals(a.medicamento.tipo, b.medicamento.tipo)
                    && horariosTexto(a).equals(horariosTexto(b));
        }
    };

    private final OnClick onClick;

    public MedicamentoAdapter(OnClick onClick) {
        super(DIFF);
        this.onClick = onClick;
    }

    static String horariosTexto(MedicamentoConHorarios m) {
        List<Integer> mins = new ArrayList<>();
        for (Horario h : m.horarios) mins.add(h.minutos);
        Collections.sort(mins);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < mins.size(); i++) {
            if (i > 0) sb.append(" · ");
            sb.append(HoraUtils.formatear(mins.get(i)));
        }
        return sb.toString();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new VH(ItemMedicamentoBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        MedicamentoConHorarios m = getItem(position);
        Context c = h.b.getRoot().getContext();
        String horarios = horariosTexto(m);
        h.b.tvNombre.setText(m.medicamento.nombre);
        h.b.tvDosis.setText(c.getString(R.string.dosis_tipo, m.medicamento.dosis, m.medicamento.tipo));
        h.b.tvHorarios.setText(c.getString(R.string.horarios_lista, horarios));
        h.b.getRoot().setContentDescription(c.getString(R.string.cd_medicamento,
                m.medicamento.nombre, m.medicamento.dosis, horarios));
        h.b.getRoot().setOnClickListener(v -> onClick.onClick(m));
    }

    static class VH extends RecyclerView.ViewHolder {
        final ItemMedicamentoBinding b;

        VH(ItemMedicamentoBinding b) {
            super(b.getRoot());
            this.b = b;
        }
    }
}

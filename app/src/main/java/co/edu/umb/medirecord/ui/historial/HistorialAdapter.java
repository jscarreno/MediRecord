package co.edu.umb.medirecord.ui.historial;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import co.edu.umb.medirecord.R;
import co.edu.umb.medirecord.data.entity.Toma;
import co.edu.umb.medirecord.data.model.RegistroHistorial;
import co.edu.umb.medirecord.databinding.ItemHistorialBinding;
import co.edu.umb.medirecord.domain.HoraUtils;

public class HistorialAdapter extends ListAdapter<RegistroHistorial, HistorialAdapter.VH> {

    private static final DiffUtil.ItemCallback<RegistroHistorial> DIFF = new DiffUtil.ItemCallback<>() {
        @Override
        public boolean areItemsTheSame(@NonNull RegistroHistorial a, @NonNull RegistroHistorial b) {
            return a.tomaId == b.tomaId;
        }

        @Override
        public boolean areContentsTheSame(@NonNull RegistroHistorial a, @NonNull RegistroHistorial b) {
            return a.equals(b);
        }
    };

    public HistorialAdapter() {
        super(DIFF);
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new VH(ItemHistorialBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        RegistroHistorial r = getItem(position);
        Context c = h.b.getRoot().getContext();
        boolean tomada = Toma.ESTADO_TOMADA.equals(r.estado);
        String fecha = HoraUtils.fechaLegible(r.fecha);
        String hora = HoraUtils.formatear(r.minutos);
        String registrada = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date(r.registradaEn));

        h.b.tvFechaHora.setText(c.getString(R.string.historial_fecha_hora, fecha, hora));
        h.b.tvNombre.setText(c.getString(R.string.nombre_dosis, r.nombre, r.dosis));
        String estado = tomada ? c.getString(R.string.estado_tomada_a, registrada) : c.getString(R.string.estado_omitida);
        h.b.tvEstado.setText(estado);
        h.b.tvEstado.setTextColor(ContextCompat.getColor(c, tomada ? R.color.estado_tomada : R.color.estado_omitida));
        h.b.getRoot().setContentDescription(c.getString(R.string.cd_historial, fecha, hora, r.nombre, estado));
    }

    static class VH extends RecyclerView.ViewHolder {
        final ItemHistorialBinding b;

        VH(ItemHistorialBinding b) {
            super(b.getRoot());
            this.b = b;
        }
    }
}

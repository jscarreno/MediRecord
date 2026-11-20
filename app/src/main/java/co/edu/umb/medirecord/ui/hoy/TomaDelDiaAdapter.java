package co.edu.umb.medirecord.ui.hoy;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
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
import co.edu.umb.medirecord.data.model.TomaDelDia;
import co.edu.umb.medirecord.databinding.ItemTomaBinding;
import co.edu.umb.medirecord.domain.HoraUtils;

public class TomaDelDiaAdapter extends ListAdapter<TomaDelDia, TomaDelDiaAdapter.VH> {

    public interface Listener {
        void onTomar(TomaDelDia t);

        void onOmitir(TomaDelDia t);

        void onDeshacer(TomaDelDia t);
    }

    private static final DiffUtil.ItemCallback<TomaDelDia> DIFF = new DiffUtil.ItemCallback<>() {
        @Override
        public boolean areItemsTheSame(@NonNull TomaDelDia a, @NonNull TomaDelDia b) {
            return a.horarioId == b.horarioId;
        }

        @Override
        public boolean areContentsTheSame(@NonNull TomaDelDia a, @NonNull TomaDelDia b) {
            return a.equals(b);
        }
    };

    private final Listener listener;
    private int minutosAhora = HoraUtils.minutosAhora();

    public TomaDelDiaAdapter(Listener listener) {
        super(DIFF);
        this.listener = listener;
    }

    @SuppressWarnings("NotifyDataSetChanged")
    public void actualizarHoraActual() {
        minutosAhora = HoraUtils.minutosAhora();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new VH(ItemTomaBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        TomaDelDia t = getItem(position);
        Context c = h.b.getRoot().getContext();
        String hora = HoraUtils.formatear(t.minutos);

        h.b.tvHora.setText(hora);
        h.b.tvNombre.setText(t.nombre);
        h.b.tvDosis.setText(c.getString(R.string.dosis_tipo, t.dosis, t.tipo));

        String estadoTexto;
        int color;
        if (Toma.ESTADO_TOMADA.equals(t.estado)) {
            String a = t.registradaEn == null ? "" :
                    new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date(t.registradaEn));
            estadoTexto = c.getString(R.string.estado_tomada_a, a);
            color = R.color.estado_tomada;
        } else if (Toma.ESTADO_OMITIDA.equals(t.estado)) {
            estadoTexto = c.getString(R.string.estado_omitida);
            color = R.color.estado_omitida;
        } else if (t.minutos < minutosAhora) {
            estadoTexto = c.getString(R.string.estado_atrasada);
            color = R.color.estado_atrasada;
        } else {
            estadoTexto = c.getString(R.string.estado_pendiente);
            color = R.color.estado_pendiente;
        }
        h.b.tvEstado.setText(estadoTexto);
        h.b.tvEstado.setTextColor(ContextCompat.getColor(c, color));

        boolean pendiente = t.estaPendiente();
        h.b.grupoAcciones.setVisibility(pendiente ? View.VISIBLE : View.GONE);
        h.b.btnDeshacer.setVisibility(pendiente ? View.GONE : View.VISIBLE);
        h.b.btnTomar.setEnabled(true);
        h.b.btnOmitir.setEnabled(true);
        h.b.btnDeshacer.setEnabled(true);

        // Accesibilidad: TalkBack lee la tarjeta completa en una sola frase.
        h.b.contenido.setContentDescription(c.getString(R.string.cd_toma, hora, t.nombre, t.dosis, estadoTexto));
        h.b.btnTomar.setContentDescription(c.getString(R.string.cd_tomar, t.nombre, hora));
        h.b.btnOmitir.setContentDescription(c.getString(R.string.cd_omitir, t.nombre, hora));

        // Evita pulsaciones repetidas: se deshabilita hasta que llegue la lista actualizada.
        h.b.btnTomar.setOnClickListener(v -> {
            bloquear(h);
            listener.onTomar(t);
        });
        h.b.btnOmitir.setOnClickListener(v -> {
            bloquear(h);
            listener.onOmitir(t);
        });
        h.b.btnDeshacer.setOnClickListener(v -> {
            h.b.btnDeshacer.setEnabled(false);
            listener.onDeshacer(t);
        });
    }

    private void bloquear(VH h) {
        h.b.btnTomar.setEnabled(false);
        h.b.btnOmitir.setEnabled(false);
    }

    static class VH extends RecyclerView.ViewHolder {
        final ItemTomaBinding b;

        VH(ItemTomaBinding b) {
            super(b.getRoot());
            this.b = b;
        }
    }
}

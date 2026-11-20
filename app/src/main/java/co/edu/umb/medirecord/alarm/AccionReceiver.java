package co.edu.umb.medirecord.alarm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.core.app.NotificationManagerCompat;

import co.edu.umb.medirecord.data.MedicamentoRepository;
import co.edu.umb.medirecord.data.entity.Toma;
import co.edu.umb.medirecord.domain.HoraUtils;

/** Botones de la notificación: "Ya la tomé" y "Posponer 10 min". */
public class AccionReceiver extends BroadcastReceiver {

    public static final String ACCION_TOMADA = "co.edu.umb.medirecord.TOMADA";
    public static final String ACCION_POSPONER = "co.edu.umb.medirecord.POSPONER";
    public static final long POSPONER_MS = 10 * 60_000L;

    @Override
    public void onReceive(Context context, Intent intent) {
        long horarioId = intent.getLongExtra(AlarmScheduler.EXTRA_HORARIO_ID, -1);
        if (horarioId < 0) return;
        Context app = context.getApplicationContext();
        NotificationManagerCompat.from(app).cancel((int) horarioId);

        if (ACCION_TOMADA.equals(intent.getAction())) {
            MedicamentoRepository.get(app).registrarToma(horarioId, HoraUtils.hoy(), Toma.ESTADO_TOMADA);
        } else if (ACCION_POSPONER.equals(intent.getAction())) {
            AlarmScheduler.posponer(app, horarioId, POSPONER_MS);
        }
    }
}

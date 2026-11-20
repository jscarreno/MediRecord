package co.edu.umb.medirecord.alarm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import co.edu.umb.medirecord.data.MedicamentoRepository;
import co.edu.umb.medirecord.data.model.TomaDelDia;
import co.edu.umb.medirecord.domain.HoraUtils;

/** Se dispara a la hora programada: muestra la notificación y agenda el día siguiente. */
public class AlarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (!AlarmScheduler.ACCION_RECORDAR.equals(intent.getAction())) return;
        final long horarioId = intent.getLongExtra(AlarmScheduler.EXTRA_HORARIO_ID, -1);
        final boolean pospuesto = intent.getBooleanExtra(AlarmScheduler.EXTRA_POSPUESTO, false);
        if (horarioId < 0) return;

        final Context app = context.getApplicationContext();
        final PendingResult resultado = goAsync();
        MedicamentoRepository repo = MedicamentoRepository.get(app);
        repo.enSegundoPlano(() -> {
            try {
                TomaDelDia info = repo.db().medicamentoDao().infoHorario(horarioId, HoraUtils.hoy());
                if (info == null) return; // el horario fue eliminado
                if (!pospuesto) {
                    AlarmScheduler.programar(app, info.horarioId, info.minutos);
                }
                if (info.estaPendiente()) {
                    NotificationHelper.mostrar(app, info);
                }
            } finally {
                resultado.finish();
            }
        });
    }
}

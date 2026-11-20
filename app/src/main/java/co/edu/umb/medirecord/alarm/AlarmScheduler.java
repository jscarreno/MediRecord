package co.edu.umb.medirecord.alarm;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import java.util.TimeZone;

import co.edu.umb.medirecord.domain.HoraUtils;

/**
 * Programa los recordatorios con AlarmManager.
 * Si el sistema permite alarmas exactas se usan; si no, una ventana de 1 minuto
 * (no requiere permiso especial). Cada horario tiene su propio PendingIntent.
 */
public final class AlarmScheduler {

    public static final String ACCION_RECORDAR = "co.edu.umb.medirecord.RECORDAR";
    public static final String EXTRA_HORARIO_ID = "horarioId";
    public static final String EXTRA_POSPUESTO = "pospuesto";

    private static final int OFFSET_POSPUESTO = 1_000_000;
    private static final long VENTANA_MS = 60_000L;

    private AlarmScheduler() {
    }

    public static void programar(Context ctx, long horarioId, int minutos) {
        long disparo = HoraUtils.proximoDisparo(minutos, System.currentTimeMillis(), TimeZone.getDefault());
        PendingIntent pi = pendingIntent(ctx, horarioId, false,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        programarEn(ctx, disparo, pi);
    }

    public static void posponer(Context ctx, long horarioId, long retrasoMs) {
        PendingIntent pi = pendingIntent(ctx, horarioId, true,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        programarEn(ctx, System.currentTimeMillis() + retrasoMs, pi);
    }

    public static void cancelar(Context ctx, long horarioId) {
        AlarmManager am = (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);
        for (boolean pospuesto : new boolean[]{false, true}) {
            PendingIntent pi = pendingIntent(ctx, horarioId, pospuesto,
                    PendingIntent.FLAG_NO_CREATE | PendingIntent.FLAG_IMMUTABLE);
            if (pi != null) {
                am.cancel(pi);
                pi.cancel();
            }
        }
    }

    public static boolean puedeUsarAlarmasExactas(Context ctx) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return true;
        AlarmManager am = (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);
        return am.canScheduleExactAlarms();
    }

    private static void programarEn(Context ctx, long disparo, PendingIntent pi) {
        AlarmManager am = (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);
        if (puedeUsarAlarmasExactas(ctx)) {
            am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, disparo, pi);
        } else {
            am.setWindow(AlarmManager.RTC_WAKEUP, disparo, VENTANA_MS, pi);
        }
    }

    private static PendingIntent pendingIntent(Context ctx, long horarioId, boolean pospuesto, int flags) {
        Intent i = new Intent(ctx, AlarmReceiver.class)
                .setAction(ACCION_RECORDAR)
                .putExtra(EXTRA_HORARIO_ID, horarioId)
                .putExtra(EXTRA_POSPUESTO, pospuesto);
        int requestCode = (int) horarioId + (pospuesto ? OFFSET_POSPUESTO : 0);
        return PendingIntent.getBroadcast(ctx, requestCode, i, flags);
    }
}

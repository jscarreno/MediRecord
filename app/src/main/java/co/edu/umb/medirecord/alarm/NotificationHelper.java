package co.edu.umb.medirecord.alarm;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import co.edu.umb.medirecord.R;
import co.edu.umb.medirecord.data.model.TomaDelDia;
import co.edu.umb.medirecord.domain.HoraUtils;
import co.edu.umb.medirecord.ui.MainActivity;

public final class NotificationHelper {

    public static final String CANAL_RECORDATORIOS = "recordatorios";

    private NotificationHelper() {
    }

    public static void crearCanal(Context ctx) {
        NotificationChannel canal = new NotificationChannel(CANAL_RECORDATORIOS,
                ctx.getString(R.string.canal_nombre), NotificationManager.IMPORTANCE_HIGH);
        canal.setDescription(ctx.getString(R.string.canal_descripcion));
        canal.enableVibration(true);
        ctx.getSystemService(NotificationManager.class).createNotificationChannel(canal);
    }

    public static boolean tienePermiso(Context ctx) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                && ContextCompat.checkSelfPermission(ctx, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
            return false;
        }
        return NotificationManagerCompat.from(ctx).areNotificationsEnabled();
    }

    @SuppressLint("MissingPermission") // se verifica con tienePermiso()
    public static void mostrar(Context ctx, TomaDelDia info) {
        if (!tienePermiso(ctx)) return;
        int id = (int) info.horarioId;

        Intent abrir = new Intent(ctx, MainActivity.class)
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent piAbrir = PendingIntent.getActivity(ctx, id, abrir,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        String texto = ctx.getString(R.string.notif_texto, info.nombre, info.dosis,
                HoraUtils.formatear(info.minutos));

        NotificationCompat.Builder b = new NotificationCompat.Builder(ctx, CANAL_RECORDATORIOS)
                .setSmallIcon(R.drawable.ic_stat_pill)
                .setContentTitle(ctx.getString(R.string.notif_titulo))
                .setContentText(texto)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(texto))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_REMINDER)
                .setAutoCancel(true)
                .setContentIntent(piAbrir)
                .addAction(0, ctx.getString(R.string.accion_tomada),
                        accion(ctx, AccionReceiver.ACCION_TOMADA, info.horarioId, id * 10 + 1))
                .addAction(0, ctx.getString(R.string.accion_posponer),
                        accion(ctx, AccionReceiver.ACCION_POSPONER, info.horarioId, id * 10 + 2));

        NotificationManagerCompat.from(ctx).notify(id, b.build());
    }

    private static PendingIntent accion(Context ctx, String accion, long horarioId, int requestCode) {
        Intent i = new Intent(ctx, AccionReceiver.class)
                .setAction(accion)
                .putExtra(AlarmScheduler.EXTRA_HORARIO_ID, horarioId);
        return PendingIntent.getBroadcast(ctx, requestCode, i,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    }
}

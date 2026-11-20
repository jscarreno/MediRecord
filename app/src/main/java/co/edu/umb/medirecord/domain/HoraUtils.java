package co.edu.umb.medirecord.domain;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/** Utilidades puras de fecha y hora (sin dependencias de Android, probadas con JUnit). */
public final class HoraUtils {

    private static final String FORMATO_ISO = "yyyy-MM-dd";

    private HoraUtils() {
    }

    /** 510 → "08:30". */
    public static String formatear(int minutos) {
        if (minutos < 0 || minutos >= 24 * 60) {
            throw new IllegalArgumentException("Minutos fuera de rango: " + minutos);
        }
        return String.format(Locale.US, "%02d:%02d", minutos / 60, minutos % 60);
    }

    public static int aMinutos(int hora, int minuto) {
        return hora * 60 + minuto;
    }

    public static String fecha(long millis, TimeZone tz) {
        SimpleDateFormat f = new SimpleDateFormat(FORMATO_ISO, Locale.US);
        f.setTimeZone(tz);
        return f.format(new Date(millis));
    }

    public static String hoy() {
        return fecha(System.currentTimeMillis(), TimeZone.getDefault());
    }

    /** Fecha ISO de hace {@code dias} días respecto a {@code millis}. */
    public static String haceDias(int dias, long millis, TimeZone tz) {
        Calendar c = Calendar.getInstance(tz);
        c.setTimeInMillis(millis);
        c.add(Calendar.DAY_OF_YEAR, -dias);
        return fecha(c.getTimeInMillis(), tz);
    }

    public static String haceDias(int dias) {
        return haceDias(dias, System.currentTimeMillis(), TimeZone.getDefault());
    }

    public static int minutosDelDia(long millis, TimeZone tz) {
        Calendar c = Calendar.getInstance(tz);
        c.setTimeInMillis(millis);
        return c.get(Calendar.HOUR_OF_DAY) * 60 + c.get(Calendar.MINUTE);
    }

    public static int minutosAhora() {
        return minutosDelDia(System.currentTimeMillis(), TimeZone.getDefault());
    }

    /**
     * Próximo instante (epoch millis) en que ocurre la hora {@code minutos}.
     * Si ya pasó hoy (o es exactamente ahora) devuelve la de mañana.
     */
    public static long proximoDisparo(int minutos, long ahora, TimeZone tz) {
        Calendar c = Calendar.getInstance(tz);
        c.setTimeInMillis(ahora);
        c.set(Calendar.HOUR_OF_DAY, minutos / 60);
        c.set(Calendar.MINUTE, minutos % 60);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        if (c.getTimeInMillis() <= ahora) {
            c.add(Calendar.DAY_OF_YEAR, 1);
        }
        return c.getTimeInMillis();
    }

    /** "2026-11-20" → "20/11/2026". Si no se puede leer, devuelve el texto original. */
    public static String fechaLegible(String iso) {
        try {
            SimpleDateFormat in = new SimpleDateFormat(FORMATO_ISO, Locale.US);
            in.setLenient(false);
            Date d = in.parse(iso);
            return new SimpleDateFormat("dd/MM/yyyy", Locale.US).format(d);
        } catch (ParseException | NullPointerException e) {
            return iso;
        }
    }
}

package co.edu.umb.medirecord.domain;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import java.util.Calendar;
import java.util.TimeZone;

public class HoraUtilsTest {

    private static final TimeZone BOGOTA = TimeZone.getTimeZone("America/Bogota");

    private long millis(int anio, int mes, int dia, int hora, int minuto) {
        Calendar c = Calendar.getInstance(BOGOTA);
        c.clear();
        c.set(anio, mes - 1, dia, hora, minuto, 0);
        return c.getTimeInMillis();
    }

    @Test
    public void formatear_agregaCerosALaIzquierda() {
        assertEquals("08:05", HoraUtils.formatear(8 * 60 + 5));
        assertEquals("00:00", HoraUtils.formatear(0));
        assertEquals("23:59", HoraUtils.formatear(23 * 60 + 59));
    }

    @Test(expected = IllegalArgumentException.class)
    public void formatear_rechazaMinutosFueraDeRango() {
        HoraUtils.formatear(24 * 60);
    }

    @Test
    public void proximoDisparo_horaFuturaHoy_programaHoy() {
        long ahora = millis(2026, 11, 20, 7, 0);
        assertEquals(millis(2026, 11, 20, 8, 0), HoraUtils.proximoDisparo(480, ahora, BOGOTA));
    }

    @Test
    public void proximoDisparo_horaYaPasada_programaManana() {
        long ahora = millis(2026, 11, 20, 9, 0);
        assertEquals(millis(2026, 11, 21, 8, 0), HoraUtils.proximoDisparo(480, ahora, BOGOTA));
    }

    @Test
    public void proximoDisparo_exactamenteAhora_programaManana() {
        long ahora = millis(2026, 11, 20, 8, 0);
        assertEquals(millis(2026, 11, 21, 8, 0), HoraUtils.proximoDisparo(480, ahora, BOGOTA));
    }

    @Test
    public void proximoDisparo_finDeMes_pasaAlMesSiguiente() {
        long ahora = millis(2026, 11, 30, 22, 0);
        assertEquals(millis(2026, 12, 1, 6, 30), HoraUtils.proximoDisparo(390, ahora, BOGOTA));
    }

    @Test
    public void haceDias_calculaFechaIso() {
        long ahora = millis(2026, 11, 3, 10, 0);
        assertEquals("2026-10-28", HoraUtils.haceDias(6, ahora, BOGOTA));
    }

    @Test
    public void fechaLegible_convierteFormato_yToleraTextoInvalido() {
        assertEquals("20/11/2026", HoraUtils.fechaLegible("2026-11-20"));
        assertEquals("no-es-fecha", HoraUtils.fechaLegible("no-es-fecha"));
    }
}

package co.edu.umb.medirecord.domain;

/** Cálculo del porcentaje de cumplimiento (adherencia) al tratamiento. */
public final class AdherenciaCalculator {

    /** Valor devuelto cuando no hay tomas registradas en el periodo. */
    public static final int SIN_DATOS = -1;

    private AdherenciaCalculator() {
    }

    public static int porcentaje(int tomadas, int omitidas) {
        if (tomadas < 0 || omitidas < 0) {
            throw new IllegalArgumentException("Los conteos no pueden ser negativos");
        }
        int total = tomadas + omitidas;
        if (total == 0) return SIN_DATOS;
        return Math.round(tomadas * 100f / total);
    }
}

package co.edu.umb.medirecord.data.model;

import java.util.Objects;

/**
 * Fila de la pantalla "Hoy": un horario programado con el estado de su toma
 * para la fecha consultada (estado == null significa pendiente).
 */
public class TomaDelDia {
    public long horarioId;
    public int minutos;
    public long medicamentoId;
    public String nombre;
    public String dosis;
    public String tipo;
    public String estado;
    public Long registradaEn;

    public boolean estaPendiente() {
        return estado == null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TomaDelDia)) return false;
        TomaDelDia t = (TomaDelDia) o;
        return horarioId == t.horarioId && minutos == t.minutos && medicamentoId == t.medicamentoId
                && Objects.equals(nombre, t.nombre) && Objects.equals(dosis, t.dosis)
                && Objects.equals(tipo, t.tipo) && Objects.equals(estado, t.estado)
                && Objects.equals(registradaEn, t.registradaEn);
    }

    @Override
    public int hashCode() {
        return Objects.hash(horarioId, minutos, medicamentoId, nombre, dosis, tipo, estado, registradaEn);
    }
}

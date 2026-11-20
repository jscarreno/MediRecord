package co.edu.umb.medirecord.data.model;

import java.util.Objects;

/** Fila del historial: una toma registrada con los datos de su medicamento. */
public class RegistroHistorial {
    public long tomaId;
    public String fecha;
    public String estado;
    public long registradaEn;
    public int minutos;
    public String nombre;
    public String dosis;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RegistroHistorial)) return false;
        RegistroHistorial r = (RegistroHistorial) o;
        return tomaId == r.tomaId && registradaEn == r.registradaEn && minutos == r.minutos
                && Objects.equals(fecha, r.fecha) && Objects.equals(estado, r.estado)
                && Objects.equals(nombre, r.nombre) && Objects.equals(dosis, r.dosis);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tomaId, fecha, estado, registradaEn, minutos, nombre, dosis);
    }
}

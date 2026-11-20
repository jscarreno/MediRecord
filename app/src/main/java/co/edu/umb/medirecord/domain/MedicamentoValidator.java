package co.edu.umb.medirecord.domain;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Reglas de validación del formulario de medicamento (lógica pura, probada con JUnit). */
public final class MedicamentoValidator {

    public static final int MAX_NOMBRE = 50;
    public static final int MIN_NOMBRE = 2;
    public static final int MAX_DOSIS = 30;
    public static final int MAX_HORARIOS = 8;

    public enum Error {
        NOMBRE_VACIO, NOMBRE_CORTO, NOMBRE_LARGO,
        DOSIS_VACIA, DOSIS_LARGA,
        TIPO_VACIO,
        SIN_HORARIOS, HORARIO_INVALIDO, HORARIO_DUPLICADO, DEMASIADOS_HORARIOS
    }

    private MedicamentoValidator() {
    }

    public static Set<Error> validar(String nombre, String dosis, String tipo, List<Integer> horarios) {
        Set<Error> errores = EnumSet.noneOf(Error.class);

        String n = nombre == null ? "" : nombre.trim();
        if (n.isEmpty()) errores.add(Error.NOMBRE_VACIO);
        else if (n.length() < MIN_NOMBRE) errores.add(Error.NOMBRE_CORTO);
        else if (n.length() > MAX_NOMBRE) errores.add(Error.NOMBRE_LARGO);

        String d = dosis == null ? "" : dosis.trim();
        if (d.isEmpty()) errores.add(Error.DOSIS_VACIA);
        else if (d.length() > MAX_DOSIS) errores.add(Error.DOSIS_LARGA);

        if (tipo == null || tipo.trim().isEmpty()) errores.add(Error.TIPO_VACIO);

        if (horarios == null || horarios.isEmpty()) {
            errores.add(Error.SIN_HORARIOS);
        } else {
            if (horarios.size() > MAX_HORARIOS) errores.add(Error.DEMASIADOS_HORARIOS);
            Set<Integer> vistos = new HashSet<>();
            for (Integer h : horarios) {
                if (h == null || h < 0 || h >= 24 * 60) errores.add(Error.HORARIO_INVALIDO);
                else if (!vistos.add(h)) errores.add(Error.HORARIO_DUPLICADO);
            }
        }
        return errores;
    }
}

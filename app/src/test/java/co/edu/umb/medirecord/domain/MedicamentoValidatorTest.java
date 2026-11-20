package co.edu.umb.medirecord.domain;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import co.edu.umb.medirecord.domain.MedicamentoValidator.Error;

public class MedicamentoValidatorTest {

    @Test
    public void datosValidos_sinErrores() {
        assertTrue(MedicamentoValidator.validar("Losartán", "50 mg", "Pastilla", Arrays.asList(480, 1200)).isEmpty());
    }

    @Test
    public void nombreVacioOSoloEspacios_esError() {
        assertTrue(MedicamentoValidator.validar("   ", "50 mg", "Pastilla", Collections.singletonList(480))
                .contains(Error.NOMBRE_VACIO));
        assertTrue(MedicamentoValidator.validar(null, "50 mg", "Pastilla", Collections.singletonList(480))
                .contains(Error.NOMBRE_VACIO));
    }

    @Test
    public void nombreDeUnaLetra_esCorto() {
        assertTrue(MedicamentoValidator.validar("A", "50 mg", "Pastilla", Collections.singletonList(480))
                .contains(Error.NOMBRE_CORTO));
    }

    @Test
    public void nombreMuyLargo_esError() {
        String largo = new String(new char[51]).replace('\0', 'x');
        assertTrue(MedicamentoValidator.validar(largo, "50 mg", "Pastilla", Collections.singletonList(480))
                .contains(Error.NOMBRE_LARGO));
    }

    @Test
    public void dosisVaciaYTipoVacio_sonErrores() {
        Set<Error> e = MedicamentoValidator.validar("Losartán", "", " ", Collections.singletonList(480));
        assertTrue(e.contains(Error.DOSIS_VACIA));
        assertTrue(e.contains(Error.TIPO_VACIO));
    }

    @Test
    public void sinHorarios_esError() {
        assertTrue(MedicamentoValidator.validar("Losartán", "50 mg", "Pastilla", new ArrayList<>())
                .contains(Error.SIN_HORARIOS));
    }

    @Test
    public void horarioDuplicado_esError() {
        assertTrue(MedicamentoValidator.validar("Losartán", "50 mg", "Pastilla", Arrays.asList(480, 480))
                .contains(Error.HORARIO_DUPLICADO));
    }

    @Test
    public void horarioFueraDeRango_esInvalido() {
        assertTrue(MedicamentoValidator.validar("Losartán", "50 mg", "Pastilla", Arrays.asList(-1, 1440))
                .contains(Error.HORARIO_INVALIDO));
    }

    @Test
    public void masDeOchoHorarios_esError() {
        List<Integer> nueve = new ArrayList<>();
        for (int i = 0; i < 9; i++) nueve.add(i * 60);
        assertTrue(MedicamentoValidator.validar("Losartán", "50 mg", "Pastilla", nueve)
                .contains(Error.DEMASIADOS_HORARIOS));
    }

    @Test
    public void variosErrores_seReportanJuntos() {
        assertEquals(4, MedicamentoValidator.validar("", "", "", null).size());
    }
}

package co.edu.umb.medirecord.domain;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class AdherenciaCalculatorTest {

    @Test
    public void sinTomas_devuelveSinDatos() {
        assertEquals(AdherenciaCalculator.SIN_DATOS, AdherenciaCalculator.porcentaje(0, 0));
    }

    @Test
    public void todasTomadas_es100() {
        assertEquals(100, AdherenciaCalculator.porcentaje(14, 0));
    }

    @Test
    public void redondeaAlEnteroMasCercano() {
        assertEquals(67, AdherenciaCalculator.porcentaje(2, 1));
        assertEquals(85, AdherenciaCalculator.porcentaje(17, 3));
    }

    @Test(expected = IllegalArgumentException.class)
    public void conteoNegativo_lanzaExcepcion() {
        AdherenciaCalculator.porcentaje(-1, 2);
    }
}

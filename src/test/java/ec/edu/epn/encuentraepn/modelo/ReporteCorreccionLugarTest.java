package ec.edu.epn.encuentraepn.modelo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static ec.edu.epn.encuentraepn.DatosDePrueba.reporteActivo;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * HU-22 Corregir mi reporte: corrección del lugar.
 */
class ReporteCorreccionLugarTest {

    @Test
    @DisplayName("Corregir el lugar de un reporte lo mueve al nuevo punto del campus")
    void corregirElLugarLoMueve() {
        Reporte reporte = reporteActivo();
        Ubicacion correcta = new Ubicacion(-0.2085, -78.4880, Campus.zonaDe(-0.2085, -78.4880));

        reporte.corregirUbicacion(correcta);

        assertSame(correcta, reporte.getUbicacion());
    }
}

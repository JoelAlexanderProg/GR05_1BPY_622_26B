package ec.edu.epn.encuentraepn.modelo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static ec.edu.epn.encuentraepn.DatosDePrueba.reporteActivo;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * HU-22 Corregir mi reporte: un reporte reservado ya no se corrige, porque el reclamante ya lo vio así.
 */
class ReporteSePuedeCorregirTest {

    @Test
    @DisplayName("Solo un reporte activo se puede corregir; uno reservado ya no")
    void soloUnReporteActivoSePuedeCorregir() {
        Reporte activo = reporteActivo();
        Reporte reservado = reporteActivo();
        reservado.reservar();

        assertTrue(activo.sePuedeCorregir());
        assertFalse(reservado.sePuedeCorregir());
    }
}

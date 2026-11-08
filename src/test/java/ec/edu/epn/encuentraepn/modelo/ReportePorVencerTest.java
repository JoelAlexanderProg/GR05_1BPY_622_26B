package ec.edu.epn.encuentraepn.modelo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static ec.edu.epn.encuentraepn.DatosDePrueba.reportePublicadoEl;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * HU-23 Renovar mi reporte: aviso de que el reporte está por vencer.
 */
class ReportePorVencerTest {

    @Test
    @DisplayName("Un reporte está por vencer en sus últimos 7 días de vigencia, no antes ni después de vencer")
    void unReporteEstaPorVencerEnSusUltimos7Dias() {
        LocalDate publicacion = LocalDate.of(2026, 9, 1);
        Reporte reporte = reportePublicadoEl(publicacion);

        assertFalse(reporte.estaPorVencer(publicacion.plusDays(10)));
        assertTrue(reporte.estaPorVencer(publicacion.plusDays(55)));
        assertFalse(reporte.estaPorVencer(publicacion.plusDays(61)));
    }
}

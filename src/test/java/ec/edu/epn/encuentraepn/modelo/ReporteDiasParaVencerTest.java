package ec.edu.epn.encuentraepn.modelo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static ec.edu.epn.encuentraepn.DatosDePrueba.reportePublicadoEl;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * HU-23 Renovar mi reporte: días que le quedan a un reporte antes de vencer.
 */
class ReporteDiasParaVencerTest {

    @Test
    @DisplayName("A un reporte publicado hace 50 días le quedan 10 para vencer, y a uno vencido, 0")
    void diasQueLeQuedanParaVencer() {
        LocalDate publicacion = LocalDate.of(2026, 9, 1);
        Reporte reporte = reportePublicadoEl(publicacion);

        assertEquals(10, reporte.diasParaVencer(publicacion.plusDays(50)));
        assertEquals(0, reporte.diasParaVencer(publicacion.plusDays(75)));
    }
}

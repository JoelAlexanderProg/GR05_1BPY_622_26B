package ec.edu.epn.encuentraepn.modelo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static ec.edu.epn.encuentraepn.DatosDePrueba.reportePublicadoEl;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * HU-23 Renovar mi reporte: la renovación reinicia la vigencia.
 */
class ReporteRenovacionTest {

    @Test
    @DisplayName("Renovar un reporte reinicia su vigencia desde el día de la renovación")
    void renovarReiniciaLaVigencia() {
        Reporte reporte = reportePublicadoEl(LocalDate.of(2026, 9, 1));
        LocalDate renovacion = LocalDate.of(2026, 10, 25);

        reporte.renovar(renovacion);

        assertEquals(renovacion, reporte.getInicioVigencia());
    }
}
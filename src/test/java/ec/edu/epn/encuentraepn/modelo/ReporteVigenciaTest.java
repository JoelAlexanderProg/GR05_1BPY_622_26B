package ec.edu.epn.encuentraepn.modelo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static ec.edu.epn.encuentraepn.DatosDePrueba.reportePublicadoEl;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * HU-24 Ver solo los reportes vigentes y HU-23 Renovar mi reporte: reglas de la vigencia de un reporte.
 */
class ReporteVigenciaTest {

    @Test
    @DisplayName("Publicar un reporte inicia su vigencia ese mismo día")
    void publicarIniciaLaVigencia() {
        LocalDate publicacion = LocalDate.of(2026, 11, 9);

        Reporte reporte = reportePublicadoEl(publicacion);

        assertEquals(publicacion, reporte.getInicioVigencia());
    }
}

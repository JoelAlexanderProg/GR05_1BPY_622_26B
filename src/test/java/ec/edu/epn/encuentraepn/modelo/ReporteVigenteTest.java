
package ec.edu.epn.encuentraepn.modelo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.time.LocalDate;

import static ec.edu.epn.encuentraepn.DatosDePrueba.reportePublicadoEl;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * HU-24 Ver solo los reportes vigentes: un reporte sigue vigente durante 60 días desde su publicación.
 */
class ReporteVigenteTest {

    private static final LocalDate PUBLICACION = LocalDate.of(2026, 9, 1);

    /**
     * Prueba con parámetros: cada fila es un día distinto después de la publicación.
     */
    @ParameterizedTest(name = "{2}: {0} días después → vigente = {1}")
    @CsvSource({
            "0,  true,  el mismo día",
            "59, true,  último día de vigencia",
            "60, false, vence a los 60 días",
            "90, false, tres meses después"
    })
    @DisplayName("Un reporte sigue vigente hasta 60 días después de publicarse")
    void unReporteSigueVigenteHasta60Dias(int diasDespues, boolean vigente, String caso) {
        Reporte reporte = reportePublicadoEl(PUBLICACION);

        assertEquals(vigente, reporte.estaVigente(PUBLICACION.plusDays(diasDespues)));
    }
}

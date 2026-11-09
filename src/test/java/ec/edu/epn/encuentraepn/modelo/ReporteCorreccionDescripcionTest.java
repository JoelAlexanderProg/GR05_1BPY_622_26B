
package ec.edu.epn.encuentraepn.modelo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static ec.edu.epn.encuentraepn.DatosDePrueba.reporteActivo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * HU-22 Corregir mi reporte: corrección de la descripción.
 */
class ReporteCorreccionDescripcionTest {

    @Test
    @DisplayName("Corregir la descripción de un reporte la reemplaza por la nueva")
    void corregirLaDescripcionLaReemplaza() {
        Reporte reporte = reporteActivo("Llaves con llavero rojo");

        reporte.corregirDescripcion("Llaves con llavero azul");

        assertEquals("Llaves con llavero azul", reporte.getDescripcionPublica());
    }

    /**
     * Prueba con parámetros: cada fila es una descripción que no se puede publicar.
     */
    @ParameterizedTest(name = "{1}")
    @MethodSource("descripcionesInvalidas")
    @DisplayName("Una descripción corregida vacía o de más de 200 caracteres se rechaza y el reporte no cambia")
    void unaDescripcionInvalidaSeRechaza(String descripcion, String caso) {
        Reporte reporte = reporteActivo("Llaves con llavero rojo");

        assertThrows(IllegalArgumentException.class, () -> reporte.corregirDescripcion(descripcion));
        assertEquals("Llaves con llavero rojo", reporte.getDescripcionPublica());
    }

    static Stream<Arguments> descripcionesInvalidas() {
        return Stream.of(
                Arguments.of("", "vacía"),
                Arguments.of("   ", "solo espacios"),
                Arguments.of("x".repeat(201), "201 caracteres"));
    }
}

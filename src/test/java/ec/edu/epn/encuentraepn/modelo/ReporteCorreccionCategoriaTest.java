package ec.edu.epn.encuentraepn.modelo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static ec.edu.epn.encuentraepn.DatosDePrueba.reporteActivo;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * HU-22 Corregir mi reporte: corrección de la categoría.
 */
class ReporteCorreccionCategoriaTest {

    @Test
    @DisplayName("Corregir la categoría de un reporte la reemplaza por la nueva")
    void corregirLaCategoriaLaReemplaza() {
        Reporte reporte = reporteActivo();
        Categoria electronicos = new Categoria("Electrónicos");

        reporte.corregirCategoria(electronicos);

        assertSame(electronicos, reporte.getCategoria());
    }
}
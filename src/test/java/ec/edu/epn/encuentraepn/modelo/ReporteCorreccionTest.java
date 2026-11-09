package ec.edu.epn.encuentraepn.modelo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static ec.edu.epn.encuentraepn.DatosDePrueba.reporteActivo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * HU-22 Corregir mi reporte: reglas comunes a las tres correcciones.
 */
class ReporteCorreccionTest {

    @Test
    @DisplayName("Las correcciones no se aplican a un reporte reservado")
    void lasCorreccionesNoSeAplicanAUnReporteReservado() {
        Reporte reporte = reporteActivo("Mochila negra con el logo de la EPN");
        reporte.reservar();
        Ubicacion lugar = reporte.getUbicacion();
        Categoria categoria = reporte.getCategoria();

        assertThrows(IllegalStateException.class, () -> reporte.corregirDescripcion("Mochila azul"));
        assertThrows(IllegalStateException.class,
                () -> reporte.corregirUbicacion(new Ubicacion(-0.2085, -78.4890, Campus.ZONA_NORTE)));
        assertThrows(IllegalStateException.class, () -> reporte.corregirCategoria(new Categoria("Otros")));
        assertEquals("Mochila negra con el logo de la EPN", reporte.getDescripcionPublica());
        assertSame(lugar, reporte.getUbicacion());
        assertSame(categoria, reporte.getCategoria());
    }
}

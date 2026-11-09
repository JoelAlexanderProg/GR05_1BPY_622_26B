package ec.edu.epn.encuentraepn.modelo;

import java.util.UUID;

/**
 * HU-22: datos que el autor corrige de su reporte: la descripción, la categoría y el lugar.
 */
public record DatosCorreccion(
        String descripcionPublica,
        UUID categoriaId,
        Double latitud,
        Double longitud) {
}

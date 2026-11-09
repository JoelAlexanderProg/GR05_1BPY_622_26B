package ec.edu.epn.encuentraepn.web;

import ec.edu.epn.encuentraepn.dao.CategoriaDAO;
import ec.edu.epn.encuentraepn.dao.ReclamacionDAO;
import ec.edu.epn.encuentraepn.dao.ReporteDAO;
import ec.edu.epn.encuentraepn.dao.UsuarioDAO;
import ec.edu.epn.encuentraepn.modelo.Campus;
import ec.edu.epn.encuentraepn.modelo.Categoria;
import ec.edu.epn.encuentraepn.modelo.DatosReporte;
import ec.edu.epn.encuentraepn.modelo.Reclamacion;
import ec.edu.epn.encuentraepn.modelo.Reporte;
import ec.edu.epn.encuentraepn.modelo.TipoReporte;
import ec.edu.epn.encuentraepn.modelo.Ubicacion;
import ec.edu.epn.encuentraepn.modelo.Usuario;

import java.time.LocalDate;
import java.util.List;

/**
 * Datos de ejemplo para demostrar los escenarios de aceptación de la iteración 5. Se cargan solo si la variable
 * de entorno {@value #VARIABLE} vale {@code true} y todavía no hay reportes. Las fechas se calculan desde hoy,
 * porque la vigencia de un reporte depende de cuántos días pasaron desde que se publicó.
 */
final class DatosDemo {

    /** Variable de entorno que activa los datos de ejemplo, por ejemplo con {@code docker run -e DATOS_DEMO=true}. */
    static final String VARIABLE = "DATOS_DEMO";

    private final UsuarioDAO usuarioDAO = new UsuarioDAO();
    private final CategoriaDAO categoriaDAO = new CategoriaDAO();
    private final ReporteDAO reporteDAO = new ReporteDAO();
    private final ReclamacionDAO reclamacionDAO = new ReclamacionDAO();
    private final LocalDate hoy = LocalDate.now();

    static boolean activados() {
        return "true".equalsIgnoreCase(System.getenv(VARIABLE));
    }

    void cargarSiHaceFalta() {
        if (!reporteDAO.listarTodos().isEmpty()) {
            return;
        }
        Usuario bruno = usuarioDAO.obtenerORegistrar("Bruno Salas", "bruno.salas@epn.edu.ec");
        Usuario andrea = usuarioDAO.obtenerORegistrar("Andrea Paz", "andrea.paz@epn.edu.ec");

        // HU-24, escenario 1: un reporte reciente se ve en el mapa
        guardar(encontrado(bruno, 7, "Llaves", "Llaves con llavero rojo", -0.2085, -78.4890,
                "¿Cuántas llaves tiene el llavero?"));
        // HU-24, escenarios 2 y 3: un reporte de hace tres meses que nadie renovó ya no se ve ni se encuentra
        guardar(encontrado(bruno, 90, "Ropa y accesorios", "Chaqueta azul de jean", -0.2095, -78.4905,
                "¿Qué tiene en el bolsillo interior?"));
        // HU-23, escenario 1: a un reporte publicado hace 55 días le quedan pocos días para vencer
        guardar(perdido(bruno, 55, "Calculadoras", "Calculadora Casio fx-991 gris", -0.2105, -78.4880));
        // HU-23, escenario 2: un reporte vencido que su autor puede renovar
        guardar(perdido(bruno, 70, "Electrónicos", "Audífonos inalámbricos blancos", -0.2115, -78.4895));
        // HU-23, escenario 3: un reporte reservado porque su autor aceptó la reclamación del dueño
        Reporte mochila = encontrado(bruno, 58, "Ropa y accesorios", "Mochila negra con el logo de la EPN",
                -0.2120, -78.4875, "¿Qué hay en el bolsillo pequeño?");
        mochila.reservar();
        guardar(mochila);
        Reclamacion reclamacion = new Reclamacion(andrea, mochila);
        reclamacion.agregarRespuesta(mochila.getPreguntas().get(0), "Un cargador de celular");
        reclamacion.aceptar();
        reclamacionDAO.guardar(reclamacion);
    }

    /** Objeto encontrado y publicado hace los días indicados, con su pregunta de verificación. */
    private Reporte encontrado(Usuario autor, int diasAtras, String categoria, String descripcion, double latitud,
                               double longitud, String pregunta) {
        Reporte reporte = publicado(TipoReporte.ENCONTRADO, autor, diasAtras, categoria, descripcion, latitud,
                longitud, List.of(pregunta));
        reporte.agregarPregunta(pregunta);
        return reporte;
    }

    /** Objeto perdido y publicado hace los días indicados. */
    private Reporte perdido(Usuario autor, int diasAtras, String categoria, String descripcion, double latitud,
                            double longitud) {
        return publicado(TipoReporte.PERDIDO, autor, diasAtras, categoria, descripcion, latitud, longitud, List.of());
    }

    /** El objeto se perdió o se encontró el mismo día en que se publicó el reporte. */
    private Reporte publicado(TipoReporte tipo, Usuario autor, int diasAtras, String categoria, String descripcion,
                              double latitud, double longitud, List<String> preguntas) {
        LocalDate fecha = hoy.minusDays(diasAtras);
        Categoria encontrada = categoriaDAO.listarTodas().stream()
                .filter(c -> c.getNombre().equals(categoria)).findFirst().orElseThrow();
        DatosReporte datos = new DatosReporte(tipo, encontrada.getId(), descripcion, fecha, latitud, longitud,
                preguntas);
        Reporte reporte = new Reporte(datos, autor, encontrada,
                new Ubicacion(latitud, longitud, Campus.zonaDe(latitud, longitud)));
        reporte.publicar(fecha);
        return reporte;
    }

    private void guardar(Reporte reporte) {
        reporteDAO.guardar(reporte);
    }
}

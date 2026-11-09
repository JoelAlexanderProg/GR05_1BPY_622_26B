package ec.edu.epn.encuentraepn.controlador;

import ec.edu.epn.encuentraepn.dao.ReclamacionDAO;
import ec.edu.epn.encuentraepn.dao.ReporteDAO;
import ec.edu.epn.encuentraepn.modelo.EstadoReclamacion;
import ec.edu.epn.encuentraepn.modelo.EstadoReporte;
import ec.edu.epn.encuentraepn.modelo.Reclamacion;
import ec.edu.epn.encuentraepn.modelo.Reporte;
import ec.edu.epn.encuentraepn.modelo.Usuario;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static ec.edu.epn.encuentraepn.DatosDePrueba.reclamacionPendiente;
import static ec.edu.epn.encuentraepn.DatosDePrueba.reportePublicadoEl;
import static ec.edu.epn.encuentraepn.controlador.ControlRegistroReporte.MOTIVO_RETIRO;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * HU-11 Retirar un reporte propio y HU-23 Renovar mi reporte. Pruebas del control con el acceso a datos simulado.
 */
@ExtendWith(MockitoExtension.class)
class ControlRegistroReporteTest {

    @Mock
    private ReporteDAO reporteDAO;

    @Mock
    private ReclamacionDAO reclamacionDAO;

    @Test
    @DisplayName("Retirar un reporte rechaza sus reclamaciones pendientes y lo guarda con el DAO, sin usar la base de datos")
    void retirarReporteRechazaLasPendientesYLoGuarda() {
        Usuario encontrante = new Usuario("Bruno Salas", "bruno.salas@epn.edu.ec");
        Reclamacion pendiente = reclamacionPendiente(encontrante);
        Reporte reporte = pendiente.getObjeto();
        reporte.publicar();
        UUID id = UUID.randomUUID();
        when(reporteDAO.buscarPorId(id)).thenReturn(reporte);
        when(reclamacionDAO.listarPendientes(reporte)).thenReturn(List.of(pendiente));
        ControlRegistroReporte control = new ControlRegistroReporte(reporteDAO, reclamacionDAO);

        control.retirarReporte(encontrante, id);

        verify(reporteDAO).actualizar(reporte);
        assertEquals(EstadoReporte.RETIRADO, reporte.getEstado());
        assertEquals(EstadoReclamacion.RECHAZADA, pendiente.getEstado());
        assertEquals(MOTIVO_RETIRO, pendiente.getMotivoRechazo());
    }

    @Test
    @DisplayName("HU-23: el control renueva el reporte solo si quien lo pide es su autor, y lo guarda con el DAO")
    void renovarReporteSoloLoHaceSuAutorYLoGuarda() {
        Reporte vencido = reportePublicadoEl(LocalDate.now().minusDays(90));
        UUID id = UUID.randomUUID();
        when(reporteDAO.buscarPorId(id)).thenReturn(vencido);
        ControlRegistroReporte control = new ControlRegistroReporte(reporteDAO, reclamacionDAO);
        Usuario otraPersona = new Usuario("Andrea Paz", "andrea.paz@epn.edu.ec");

        assertThrows(OperacionInvalidaException.class, () -> control.renovarReporte(otraPersona, id));
        verify(reporteDAO, never()).actualizar(vencido);

        control.renovarReporte(vencido.getAutor(), id);

        verify(reporteDAO).actualizar(vencido);
        assertEquals(LocalDate.now(), vencido.getInicioVigencia());
    }
}

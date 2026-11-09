package ec.edu.epn.encuentraepn.modelo;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.text.Normalizer;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/**
 * Reporte de un objeto perdido o encontrado en el campus.
 * Diagrama de clases: «entity» Reporte.
 */
@Entity
@Table(name = "reporte")
public class Reporte {

    /** HU-14: distancia máxima, en metros, entre el lugar de la pérdida y el del hallazgo. */
    public static final int RADIO_COINCIDENCIA_METROS = 200;

    /** HU-14: días después de la pérdida en los que un hallazgo todavía puede ser el mismo objeto. */
    public static final int DIAS_COINCIDENCIA = 30;

    /** HU-24: días que un reporte sigue vigente desde que se publicó o se renovó. */
    public static final int DIAS_VIGENCIA = 60;

    /** HU-23: últimos días de vigencia en los que se avisa al autor que su reporte está por vencer. */
    public static final int DIAS_AVISO_VENCIMIENTO = 7;

    /** HU-22: largo máximo de la descripción pública, al registrar el reporte y al corregirlo. */
    public static final int MAX_DESCRIPCION = 200;

    /** Mensaje cuando la descripción está vacía o pasa de {@value #MAX_DESCRIPCION} caracteres. */
    public static final String MENSAJE_DESCRIPCION = "Escriba una descripción de hasta " + MAX_DESCRIPCION + " caracteres.";

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 12)
    private TipoReporte tipo;

    @Column(nullable = false, length = MAX_DESCRIPCION)
    private String descripcionPublica;

    @Column(nullable = false)
    private LocalDate fechaSuceso;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 12)
    private EstadoReporte estado;

    private LocalDateTime fechaDevolucion;

    /** HU-24: día desde el que cuenta la vigencia del reporte: su publicación o su última renovación. */
    private LocalDate inicioVigencia;

    @ManyToOne(optional = false)
    private Usuario autor;

    @ManyToOne(optional = false)
    private Categoria categoria;

    @Embedded
    private Ubicacion ubicacion;

    @OneToMany(mappedBy = "reporte", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PreguntaVerificacion> preguntas = new ArrayList<>();

    /** HU-15: en un objeto encontrado, el reporte de pérdida de su dueño, si quien lo encontró lo avisó. */
    @ManyToOne
    private Reporte reportePerdido;

    protected Reporte() {
    }

    // SEC-01 · 8: «create» Reporte(datos, usuario, categoria, ubicacion)
    public Reporte(DatosReporte datos, Usuario autor, Categoria categoria, Ubicacion ubicacion) {
        this.tipo = datos.tipo();
        this.descripcionPublica = datos.descripcionPublica();
        this.fechaSuceso = datos.fechaSuceso();
        this.autor = autor;
        this.categoria = categoria;
        this.ubicacion = ubicacion;
        this.estado = EstadoReporte.BORRADOR;
    }

    // SEC-01 · 9: «create» preguntas asociadas al reporte
    public void agregarPregunta(String texto) {
        preguntas.add(new PreguntaVerificacion(this, texto));
    }

    // SEC-01 · 10: publicar()
    public void publicar() {
        cambiarEstado(EstadoReporte.BORRADOR, EstadoReporte.ACTIVO);
    }

    /**
     * HU-24: publica el reporte el día indicado; desde ese día cuenta su vigencia.
     */
    public void publicar(LocalDate hoy) {
        publicar();
        inicioVigencia = hoy;
    }

    /**
     * HU-23: quien publicó el reporte lo renueva y vuelve a estar vigente desde ese día.
     * Solo se renueva un reporte activo; si está reservado o retirado, lanza IllegalStateException.
     */
    public void renovar(LocalDate hoy) {
        exigirEstado(EstadoReporte.ACTIVO);
        inicioVigencia = hoy;
    }

    // SEC-04 · 18: reservar()
    public void reservar() {
        cambiarEstado(EstadoReporte.ACTIVO, EstadoReporte.RESERVADO);
    }
    
    /**
     * HU-22: quien publicó el reporte corrige su descripción. No puede quedar vacía ni pasar de
     * {@value #MAX_DESCRIPCION} caracteres.
     */
    public void corregirDescripcion(String nueva) {
        exigirQueSePuedaCorregir();
        String descripcion = nueva.trim();
        if (descripcion.isEmpty() || descripcion.length() > MAX_DESCRIPCION) {
            throw new IllegalArgumentException(MENSAJE_DESCRIPCION);
        }
        descripcionPublica = descripcion;
    }

    // SEC-04 · 40: marcarEntregado(fecha)
    public void marcarEntregado(LocalDateTime fecha) {
        cambiarEstado(EstadoReporte.RESERVADO, EstadoReporte.ENTREGADO);
        fechaDevolucion = fecha;
    }

    /**
     * HU-19: si la entrega acordada no se realizó, el objeto reservado vuelve a estar disponible en el mapa.
     */
    public void liberar() {
        cambiarEstado(EstadoReporte.RESERVADO, EstadoReporte.ACTIVO);
    }

    /**
     * HU-11: quien registró el reporte lo retira y deja de aparecer en el mapa.
     */
    public void retirar() {
        cambiarEstado(EstadoReporte.ACTIVO, EstadoReporte.RETIRADO);
    }

    /**
     * HU-12: indica si la descripción del reporte contiene la palabra buscada.
     */
    public boolean contienePalabra(String palabra) {
        return normalizar(descripcionPublica).contains(normalizar(palabra));
    }

    /**
     * HU-14: indica si un objeto encontrado puede ser este objeto perdido. Coincide si está activo,
     * es de la misma categoría, se encontró a {@value #RADIO_COINCIDENCIA_METROS} m o menos y
     * entre la fecha de la pérdida y {@value #DIAS_COINCIDENCIA} días después.
     */
    public boolean coincideCon(Reporte encontrado) {
        LocalDate limite = fechaSuceso.plusDays(DIAS_COINCIDENCIA);
        return tipo == TipoReporte.PERDIDO
                && encontrado.esEncontrado()
                && encontrado.estaActivo()
                && esDeLaMismaCategoria(encontrado)
                && !encontrado.fechaSuceso.isBefore(fechaSuceso)
                && !encontrado.fechaSuceso.isAfter(limite)
                && distanciaA(encontrado) <= RADIO_COINCIDENCIA_METROS;
    }

    /**
     * HU-15: quien encontró este objeto avisa al dueño de un reporte de pérdida que lo tiene.
     * El reporte de pérdida debe estar activo y ser de otra persona.
     */
    public void vincularConPerdido(Reporte perdido) {
        if (!esEncontrado() || perdido.esEncontrado()) {
            throw new IllegalArgumentException("Solo un objeto encontrado se vincula con un reporte de pérdida.");
        }
        if (!perdido.estaActivo()) {
            throw new IllegalStateException("Este reporte de pérdida ya no está activo.");
        }
        if (perdido.esAutor(autor)) {
            throw new IllegalArgumentException("No puede avisar que encontró un objeto que usted mismo reportó.");
        }
        reportePerdido = perdido;
    }

    /**
     * HU-22: quien publicó el reporte corrige el lugar donde perdió o encontró el objeto.
     */
    public void corregirUbicacion(Ubicacion nueva) {
        exigirQueSePuedaCorregir();
        ubicacion = nueva;
    }
    
    /** HU-14: distancia en metros entre el lugar de este reporte y el de otro. */
    public double distanciaA(Reporte otro) {
        return ubicacion.distanciaA(otro.ubicacion);
    }

    /**
     * Compara las categorías por su identificador; si todavía no se guardaron, por la misma instancia.
     */
    private boolean esDeLaMismaCategoria(Reporte otro) {
        return categoria == otro.categoria
                || categoria.getId() != null && categoria.getId().equals(otro.categoria.getId());
    }

    /**
     * Texto en minúsculas, sin tildes y sin espacios al inicio ni al final, para comparar búsquedas.
     */
    private static String normalizar(String texto) {
        return Normalizer.normalize(texto.trim(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "").toLowerCase(Locale.ROOT);
    }

    /**
     * Pasa del estado esperado al nuevo. Si el reporte está en otro estado, lanza IllegalStateException.
     */
    private void cambiarEstado(EstadoReporte desde, EstadoReporte hacia) {
        exigirEstado(desde);
        estado = hacia;
    }

    private void exigirEstado(EstadoReporte esperado) {
        if (estado != esperado) {
            throw new IllegalStateException("El reporte debe estar en estado " + esperado + " y está " + estado);
        }
    }

    /**
     * HU-24: el reporte sigue vigente durante {@value #DIAS_VIGENCIA} días desde que se publicó o se renovó.
     */
    public boolean estaVigente(LocalDate hoy) {
        return diasParaVencer(hoy) > 0;
    }

    /**
     * HU-24: el reporte se muestra en el mapa y en la búsqueda mientras está activo y vigente.
     */
    public boolean esVisibleEnMapa(LocalDate hoy) {
        return estaActivo() && estaVigente(hoy);
    }

    public boolean esEncontrado() {
        return tipo == TipoReporte.ENCONTRADO;
    }

    /**
     * HU-23: días que le quedan al reporte antes de vencer; 0 si ya venció.
     */
    public int diasParaVencer(LocalDate hoy) {
        long transcurridos = hoy.toEpochDay() - inicioVigencia.toEpochDay();
        return (int) Math.max(0, DIAS_VIGENCIA - transcurridos);
    }

    public boolean estaActivo() {
        return estado == EstadoReporte.ACTIVO;
    }

    /**
     * HU-23: el reporte está por vencer en sus últimos {@value #DIAS_AVISO_VENCIMIENTO} días de vigencia.
     */
    public boolean estaPorVencer(LocalDate hoy) {
        int dias = diasParaVencer(hoy);
        return dias > 0 && dias <= DIAS_AVISO_VENCIMIENTO;
    }

    public boolean esAutor(Usuario usuario) {
        return autor.equals(usuario);
    }

    /**
     * HU-22: quien publicó el reporte corrige la categoría del objeto.
     */
    public void corregirCategoria(Categoria nueva) {
        exigirQueSePuedaCorregir();
        categoria = nueva;
    }

    public UUID getId() {
        return id;
    }

    /**
     * HU-22: un reporte se puede corregir mientras está activo.
     */
    public boolean sePuedeCorregir() {
        return estado == EstadoReporte.ACTIVO;
    }

    /**
     * HU-22: las tres correcciones exigen que el reporte se pueda corregir; si no, lanzan IllegalStateException.
     */
    private void exigirQueSePuedaCorregir() {
        if (!sePuedeCorregir()) {
            throw new IllegalStateException("Solo se puede corregir un reporte activo.");
        }
    }

    public TipoReporte getTipo() {
        return tipo;
    }

    public String getDescripcionPublica() {
        return descripcionPublica;
    }

    public LocalDate getFechaSuceso() {
        return fechaSuceso;
    }

    public EstadoReporte getEstado() {
        return estado;
    }

    public LocalDateTime getFechaDevolucion() {
        return fechaDevolucion;
    }

    public LocalDate getInicioVigencia() {
        return inicioVigencia;
    }

    public Usuario getAutor() {
        return autor;
    }

    public Categoria getCategoria() {
        return categoria;
    }

    public Ubicacion getUbicacion() {
        return ubicacion;
    }

    public List<PreguntaVerificacion> getPreguntas() {
        return Collections.unmodifiableList(preguntas);
    }

    public Reporte getReportePerdido() {
        return reportePerdido;
    }
}

<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%-- «boundary» FormularioCorreccion (HU-22): mostrarCorreccion(reporte), solicitarCorreccion(datos) --%>
<c:set var="titulo" value="Corregir mi reporte"/>
<%@ include file="/WEB-INF/vistas/comun/cabecera.jspf" %>

<h1>Corregir mi reporte</h1>
<p>Corrija la descripción, la categoría o el lugar. Así lo verán quienes buscan el objeto en el mapa.</p>

<c:if test="${not empty error}">
    <p class="error"><c:out value="${error}"/></p>
</c:if>

<dl class="datos">
    <dt>Tipo</dt><dd>${reporte.tipo.etiqueta}</dd>
    <dt>Fecha</dt><dd>${reporte.fechaSuceso}</dd>
</dl>

<%-- Si el formulario vuelve con un error, conserva lo que la persona escribió; si no, muestra los datos actuales --%>
<c:set var="descripcion" value="${empty param.descripcion ? reporte.descripcionPublica : param.descripcion}"/>
<c:set var="categoriaElegida" value="${empty param.categoria ? reporte.categoria.id.toString() : param.categoria}"/>
<c:set var="latitudElegida" value="${empty param.latitud ? reporte.ubicacion.latitud : param.latitud}"/>
<c:set var="longitudElegida" value="${empty param.longitud ? reporte.ubicacion.longitud : param.longitud}"/>

<form class="formulario ancho" id="correccion" method="post" action="${ctx}/reportes/corregir">
    <input type="hidden" name="id" value="${reporte.id}">

    <label for="categoria">Categoría</label>
    <select id="categoria" name="categoria" required>
        <c:forEach var="categoria" items="${categorias}">
            <option value="${categoria.id}" ${categoriaElegida eq categoria.id.toString() ? 'selected' : ''}>
                <c:out value="${categoria.nombre}"/>
            </option>
        </c:forEach>
    </select>

    <label for="descripcion">Descripción pública</label>
    <textarea id="descripcion" name="descripcion" maxlength="200" rows="2" required><c:out value="${descripcion}"/></textarea>
    <small>La verá cualquier persona. No incluya detalles que solo el dueño conoce.</small>

    <label>Lugar: haga clic en el mapa, dentro del área punteada, para moverlo</label>
    <%@ include file="/WEB-INF/vistas/comun/mapa.jspf" %>
    <input type="hidden" id="latitud" name="latitud" value="<c:out value='${latitudElegida}'/>">
    <input type="hidden" id="longitud" name="longitud" value="<c:out value='${longitudElegida}'/>">
    <small id="ubicacionElegida"></small>

    <div class="acciones">
        <button type="submit" class="boton">Guardar corrección</button>
        <a href="${ctx}/inicio">Cancelar</a>
    </div>
</form>

<script>
    const mapa = crearMapaCampus();
    const latitud = document.getElementById('latitud');
    const longitud = document.getElementById('longitud');
    let marcador = null;

    function marcarUbicacion(posicion) {
        if (marcador) {
            mapa.removeLayer(marcador);
        }
        marcador = crearMarcador(posicion.lat, posicion.lng, '${reporte.tipo}').addTo(mapa);
        latitud.value = posicion.lat.toFixed(6);
        longitud.value = posicion.lng.toFixed(6);
        document.getElementById('ubicacionElegida').textContent =
            'Lugar marcado: ' + latitud.value + ', ' + longitud.value;
    }

    mapa.on('click', (evento) => marcarUbicacion(evento.latlng));
    marcarUbicacion(L.latLng(Number(latitud.value), Number(longitud.value)));
</script>

<%@ include file="/WEB-INF/vistas/comun/pie.jspf" %>

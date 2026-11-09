// Pruebas de aceptación de la iteración 5 (Proyecto 1B): comprueban en el navegador los 10 escenarios de
// HU-24 Ver solo los reportes vigentes, HU-23 Renovar mi reporte y HU-22 Corregir mi reporte, con capturas.
//
// Uso:      npm install puppeteer-core
//           node casos-iteracion-5.js <carpeta de capturas> [URL base]
// Requiere: Node.js y Google Chrome. La aplicación debe estar recién iniciada con los datos de ejemplo:
//           docker run -d -p 8080:8080 -e DATOS_DEMO=true joelalexanderp/encuentraepn:2.0
//           Al final escribe resultados.json en la carpeta de capturas.
const puppeteer = require('puppeteer-core');
const fs = require('fs');
const path = require('path');

const SALIDA = process.argv[2] || 'capturas';
const BASE = process.argv[3] || 'http://localhost:8080';
const CHROME = 'C:/Program Files/Google/Chrome/Application/chrome.exe';

// Personas de los datos de ejemplo (DATOS_DEMO=true)
const BRUNO = { nombre: 'Bruno Salas', correo: 'bruno.salas@epn.edu.ec' };      // reportante
const ANDREA = { nombre: 'Andrea Paz', correo: 'andrea.paz@epn.edu.ec' };       // miembro que busca en el mapa

const resultados = [];
let casoActual = null;

function caso(id, nombre) {
  casoActual = { id, nombre, pasos: [] };
  resultados.push(casoActual);
  console.log(`\n${id} ${nombre}`);
}

function paso(numero, esperado, cumple) {
  casoActual.pasos.push({ numero, esperado, cumple: Boolean(cumple) });
  console.log(`  ${cumple ? 'ok   ' : 'FALLO'} escenario ${numero}: ${esperado}`);
}

const textoDe = (page, selector) => page.$eval(selector, (e) => e.innerText.trim()).catch(() => '');
const existe = async (page, selector) => (await page.$(selector)) !== null;
const textos = (page, selector) => page.$$eval(selector, (es) => es.map((e) => e.innerText.replace(/\s+/g, ' ').trim()));

async function esperarMapa(page) {
  if (await existe(page, '#mapa')) {
    await page.waitForNetworkIdle({ idleTime: 600, timeout: 15000 }).catch(() => {});
  }
}

async function captura(page, nombre) {
  await esperarMapa(page);
  await page.screenshot({ path: path.join(SALIDA, nombre), fullPage: true });
}

async function ir(page, ruta) {
  await page.goto(BASE + ruta, { waitUntil: 'domcontentloaded' });
}

async function enviar(page, selector) {
  await Promise.all([page.waitForNavigation({ waitUntil: 'domcontentloaded' }), page.click(selector)]);
}

async function identificar(page, usuario) {
  await ir(page, '/identificacion');
  await page.type('#nombre', usuario.nombre);
  await page.type('#correo', usuario.correo);
  await enviar(page, 'form.formulario button[type="submit"]');
}

/** Descripciones de los reportes que se ven en el mapa (la lista de resultados tiene un marcador por reporte). */
async function reportesEnMapa(page) {
  return textos(page, '#resultados li a');
}

/** Fila de «Mis reportes» con la descripción indicada, como texto. */
async function filaDe(page, descripcion) {
  return page.$$eval('#misReportes tbody tr', (filas, d) => {
    const fila = filas.find((f) => f.innerText.includes(d));
    return fila ? fila.innerText.replace(/\s+/g, ' ').trim() : '';
  }, descripcion);
}

/** Indica si la fila de «Mis reportes» con la descripción indicada ofrece la acción «Renovar». */
async function puedeRenovar(page, descripcion) {
  return page.$$eval('#misReportes tbody tr', (filas, d) => {
    const fila = filas.find((f) => f.innerText.includes(d));
    return Boolean(fila && fila.querySelector('button.renovar'));
  }, descripcion);
}

/** Marca con un borde la fila de «Mis reportes» de la descripción indicada, para que se note en la captura. */
async function resaltarFila(page, descripcion) {
  await page.$$eval('#misReportes tbody tr', (filas, d) => {
    const fila = filas.find((f) => f.innerText.includes(d));
    if (fila) fila.style.outline = '3px solid #f0a500';
  }, descripcion);
}

const SECCIONES = [];

// ---------------------------------------------------------------------------------------------- HU-24
SECCIONES.push(async ({ sesion }) => {
  caso('HU-24', 'Ver solo los reportes vigentes');
  const andrea = await sesion(ANDREA);

  await ir(andrea, '/reportes');
  const enMapa = await reportesEnMapa(andrea);
  await captura(andrea, 'hu24-e1-reporte-reciente.png');
  paso(1, 'El miembro ve en el mapa las llaves que se publicaron hace una semana',
    enMapa.some((r) => r.includes('Llaves con llavero rojo')));

  paso(2, 'El miembro no ve en el mapa la chaqueta que se publicó hace tres meses y no se renovó',
    enMapa.length > 0 && !enMapa.some((r) => r.includes('Chaqueta')));

  await andrea.type('#palabra', 'chaqueta');
  await enviar(andrea, 'form.filtros button[type="submit"]');
  const busqueda = await reportesEnMapa(andrea);
  const mensaje = await textoDe(andrea, '.dos-columnas section');
  await captura(andrea, 'hu24-e3-busqueda-sin-resultados.png');
  paso(3, 'Al buscar «chaqueta», el miembro ve que ningún reporte coincide con su búsqueda',
    busqueda.length === 0 && mensaje.includes('No hay reportes activos que cumplan los filtros.'));
});

// ---------------------------------------------------------------------------------------------- HU-23
SECCIONES.push(async ({ sesion }) => {
  caso('HU-23', 'Renovar mi reporte');
  const bruno = await sesion(BRUNO);

  await ir(bruno, '/inicio');
  const calculadora = await filaDe(bruno, 'Calculadora Casio fx-991 gris');
  await resaltarFila(bruno, 'Calculadora Casio fx-991 gris');
  await captura(bruno, 'hu23-e1-por-vencer.png');
  paso(1, 'El reportante ve que a su calculadora, publicada hace 55 días, le quedan pocos días para vencer',
    calculadora.includes('Por vencer: le quedan 5 días'));

  await ir(bruno, '/reportes');
  const antes = await reportesEnMapa(bruno);
  await ir(bruno, '/inicio');
  const vencido = await filaDe(bruno, 'Audífonos inalámbricos blancos');
  await resaltarFila(bruno, 'Audífonos inalámbricos blancos');
  await captura(bruno, 'hu23-e2-antes-vencido.png');
  const filaAudifonos = await bruno.$$eval('#misReportes tbody tr', (filas) =>
    filas.findIndex((f) => f.innerText.includes('Audífonos inalámbricos blancos')));
  await enviar(bruno, `#misReportes tbody tr:nth-child(${filaAudifonos + 1}) button.renovar`);
  const confirmacion = await textoDe(bruno, '#renovado');
  await captura(bruno, 'hu23-e2-renovado.png');
  await ir(bruno, '/reportes');
  const despues = await reportesEnMapa(bruno);
  await captura(bruno, 'hu23-e2-de-nuevo-en-el-mapa.png');
  paso(2, 'Tras renovar sus audífonos, cuyo reporte había vencido, el reportante los vuelve a ver en el mapa',
    !antes.some((r) => r.includes('Audífonos')) && vencido.includes('Venció')
    && confirmacion.includes('El reporte se renovó') && despues.some((r) => r.includes('Audífonos inalámbricos blancos')));

  await ir(bruno, '/inicio');
  const mochila = await filaDe(bruno, 'Mochila negra con el logo de la EPN');
  const renovable = await puedeRenovar(bruno, 'Mochila negra con el logo de la EPN');
  await resaltarFila(bruno, 'Mochila negra con el logo de la EPN');
  await captura(bruno, 'hu23-e3-reservado-sin-renovar.png');
  paso(3, 'El reportante no puede renovar la mochila, porque ya aceptó la reclamación de su dueño',
    mochila.includes('Reservado') && !renovable);
});

// ---------------------------------------------------------------------------------------------- HU-22
/** Lugar correcto del cuaderno: zona Norte del campus (en los datos de ejemplo está marcado en la zona Sur). */
const LUGAR_CORRECTO = { latitud: -0.2078, longitud: -78.4870 };

/** Desde «Mis reportes», abre el formulario de corrección del reporte con la descripción indicada. */
async function abrirCorreccion(page, descripcion) {
  await ir(page, '/inicio');
  const enlace = await page.$$eval('#misReportes tbody tr', (filas, d) => {
    const fila = filas.find((f) => f.innerText.includes(d));
    const corregir = fila && fila.querySelector('a.corregir');
    return corregir ? corregir.href : null;
  }, descripcion);
  await page.goto(enlace, { waitUntil: 'domcontentloaded' });
}

/** Indica si la fila de «Mis reportes» con la descripción indicada ofrece la acción «Corregir». */
async function puedeCorregir(page, descripcion) {
  return page.$$eval('#misReportes tbody tr', (filas, d) => {
    const fila = filas.find((f) => f.innerText.includes(d));
    return Boolean(fila && fila.querySelector('a.corregir'));
  }, descripcion);
}

async function marcarEnMapa(page, punto) {
  await page.$eval('#mapa', (m) => m.scrollIntoView({ block: 'center' }));
  await esperarMapa(page);
  const posicion = await page.evaluate((p) => {
    const marco = document.getElementById('mapa').getBoundingClientRect();
    const pixel = mapa.latLngToContainerPoint([p.latitud, p.longitud]);
    return { x: marco.x + pixel.x, y: marco.y + pixel.y };
  }, punto);
  await page.mouse.click(posicion.x, posicion.y);
}

/** Elige en un selector la opción con el texto indicado. */
async function elegir(page, selector, texto) {
  const valor = await page.$$eval(`${selector} option`, (opciones, t) =>
    opciones.find((o) => o.textContent.trim() === t).value, texto);
  await page.select(selector, valor);
}

/** Lugar del reporte en el mapa: coordenadas del marcador y zona que muestra la lista de resultados. */
async function lugarEnMapa(page, descripcion) {
  return page.$$eval('#resultados li', (items, d) => {
    const item = items.find((i) => i.innerText.includes(d));
    return item ? { latitud: Number(item.dataset.latitud), longitud: Number(item.dataset.longitud),
      zona: (item.querySelector('small').innerText.match(/Zona (\w+)/) || [])[1] } : null;
  }, descripcion);
}

/** Aplica en el mapa el filtro de la categoría indicada y devuelve los reportes que quedan. */
async function filtrarPorCategoria(page, categoria) {
  await ir(page, '/reportes');
  await elegir(page, 'form.filtros select[name="categoria"]', categoria);
  await enviar(page, 'form.filtros button[type="submit"]');
  return reportesEnMapa(page);
}

SECCIONES.push(async ({ sesion }) => {
  caso('HU-22', 'Corregir mi reporte');
  const bruno = await sesion(BRUNO);

  await abrirCorreccion(bruno, 'Llaves con llavero rojo');
  await bruno.$eval('#descripcion', (campo) => { campo.value = ''; });
  await bruno.type('#descripcion', 'Llaves con llavero azul');
  await captura(bruno, 'hu22-e1-formulario.png');
  await enviar(bruno, '#correccion button[type="submit"]');
  const corregido = await textoDe(bruno, '#corregido');
  const azul = await filaDe(bruno, 'Llaves con llavero azul');
  const rojo = await filaDe(bruno, 'Llaves con llavero rojo');
  await resaltarFila(bruno, 'Llaves con llavero azul');
  await captura(bruno, 'hu22-e1-descripcion-corregida.png');
  paso(1, 'Tras corregir la descripción, el reportante ve su reporte con el llavero azul',
    corregido.includes('El reporte se corrigió') && azul !== '' && rojo === '');

  await ir(bruno, '/reportes');
  const antes = await lugarEnMapa(bruno, 'Cuaderno universitario');
  await abrirCorreccion(bruno, 'Cuaderno universitario');
  await marcarEnMapa(bruno, LUGAR_CORRECTO);
  await captura(bruno, 'hu22-e2-nuevo-lugar.png');
  await enviar(bruno, '#correccion button[type="submit"]');
  await ir(bruno, '/reportes');
  const despues = await lugarEnMapa(bruno, 'Cuaderno universitario');
  await captura(bruno, 'hu22-e2-lugar-corregido.png');
  paso(2, 'Tras corregir el lugar, el reportante ve su cuaderno en el lugar correcto del mapa (zona Norte)',
    antes && antes.zona === 'Sur' && despues && despues.zona === 'Norte'
    && Math.abs(despues.latitud - LUGAR_CORRECTO.latitud) < 0.0003
    && Math.abs(despues.longitud - LUGAR_CORRECTO.longitud) < 0.0003);

  const electronicosAntes = await filtrarPorCategoria(bruno, 'Electrónicos');
  await abrirCorreccion(bruno, 'Cargador de laptop negro');
  await elegir(bruno, '#categoria', 'Electrónicos');
  await captura(bruno, 'hu22-e3-nueva-categoria.png');
  await enviar(bruno, '#correccion button[type="submit"]');
  const electronicos = await filtrarPorCategoria(bruno, 'Electrónicos');
  await captura(bruno, 'hu22-e3-filtro-electronicos.png');
  paso(3, 'Tras cambiarlo a «Electrónicos», el reportante ve su cargador al filtrar el mapa por esa categoría',
    !electronicosAntes.some((r) => r.includes('Cargador')) && electronicos.some((r) => r.includes('Cargador de laptop negro')));

  await ir(bruno, '/inicio');
  const mochila = await filaDe(bruno, 'Mochila negra con el logo de la EPN');
  const corregible = await puedeCorregir(bruno, 'Mochila negra con el logo de la EPN');
  await resaltarFila(bruno, 'Mochila negra con el logo de la EPN');
  await captura(bruno, 'hu22-e4-reservado-sin-corregir.png');
  paso(4, 'El reportante no puede corregir la mochila, porque ya aceptó la reclamación de su dueño',
    mochila.includes('Reservado') && !corregible);
});

(async () => {
  fs.mkdirSync(SALIDA, { recursive: true });
  const navegador = await puppeteer.launch({ executablePath: CHROME, headless: true, args: ['--lang=es-EC'] });
  // Cada persona tiene su propia sesión, como si usara su propio navegador
  const sesion = async (usuario) => {
    const contexto = await navegador.createBrowserContext();
    const page = await contexto.newPage();
    await page.setViewport({ width: 1200, height: 760, deviceScaleFactor: 1.5 });
    page.on('dialog', (dialogo) => dialogo.accept());
    await identificar(page, usuario);
    return page;
  };
  for (const seccion of SECCIONES) {
    await seccion({ sesion });
  }
  await navegador.close();

  const fallos = resultados.flatMap((c) => c.pasos.filter((p) => !p.cumple).map((p) => `${c.id} escenario ${p.numero}`));
  fs.writeFileSync(path.join(SALIDA, 'resultados.json'), JSON.stringify({
    fecha: new Date().toISOString().slice(0, 10), casos: resultados }, null, 1));
  console.log(fallos.length ? `\nFALLOS: ${fallos.join(', ')}` : '\nTODOS LOS ESCENARIOS SE CUMPLEN');
  process.exit(fallos.length ? 1 : 0);
})().catch((e) => { console.error(e); process.exit(1); });

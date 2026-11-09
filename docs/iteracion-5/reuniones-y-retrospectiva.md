# Iteración 5: reuniones y retrospectiva

## Reuniones y seguimiento

| Cuándo | Qué | Cómo |
|---|---|---|
| Después de la defensa del Examen 1B | Reunión breve sobre los puntos a mejorar para la siguiente iteración del proyecto | Presencial |
| 8 de noviembre de 2026 | Plan de la iteración (objetivo, historias, estimaciones, roles y ciclos TDD) e instrucciones de cada ciclo para cada integrante | Mensajes |
| 8 de noviembre de 2026 | Seguimiento del avance de los aportes de cada integrante (pull requests) | Mensajes |
| 9 de noviembre de 2026 | Revisión de la iteración y retrospectiva | En la defensa del proyecto |

## Retrospectiva

Se hace en la defensa de la iteración, el 9 de noviembre de 2026. Estos son los hechos de la iteración que se llevan a esa conversación:

- Los 8 pull requests de los ciclos TDD llegaron el mismo día. Siete se unieron sin conflictos; en uno, la rama se había creado desde `main` en lugar de `integracion-1`, y el conflicto se resolvió al unirla.
- En un ciclo, el código del Verde se pegó dentro de otro método y no compilaba. Se corrigió con un commit adicional antes de unirlo.
- En un ciclo, el commit Rojo se subió directamente a la rama de integración en lugar de a la rama propia.
- Un escenario de aceptación de HU-23 se ajustó (el objeto pasó de la calculadora a unos audífonos) porque no podía cumplirse a la vez que otro escenario de la misma historia.
- Las dos integraciones pasaron en Jenkins al primer intento (ejecuciones #2 y #3) y los 10 escenarios se cumplen con la imagen publicada en Docker Hub.
- La programación en parejas no se aplicó: cada integrante trabajó por su cuenta.

Lo que salió bien, lo que se puede mejorar y las acciones para la siguiente iteración se registran aquí después de la defensa.

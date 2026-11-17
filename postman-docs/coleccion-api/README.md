# Colecciones Postman de Agro Campos

Este directorio contiene tres colecciones Postman v2.1, separadas por rol, y un environment local compartido. Los tests están implementados con scripts `pm.test` y encadenan solicitudes guardando los IDs obtenidos en las respuestas.

## Archivos y propósito

| Archivo | Nombre en Postman | Rol / usuario | Solicitudes | Propósito |
|---|---|---|---:|---|
| `AgroCampos.postman_collection.json` | Agro Campos - AD - ADMIN | `ADMIN` / `admin1` | 126 | Pruebas funcionales completas, reglas de negocio, integraciones, bajas lógicas y preparación de datos para AG y OP. |
| `AgroCampos.AG.postman_collection.json` | Agro Campos - AG - AGRONOMO | `AGRONOMO` / `agronomo1` | 100 | Gestión del catálogo y planes, ejecución de labores, integraciones y rechazo de escrituras en campos/lotes. |
| `AgroCampos.OP.postman_collection.json` | Agro Campos - OP - OPERARIO | `OPERARIO` / `operario1` | 46 | Consultas, ejecución de labores de un plan preparado y restricciones para administrar campos, catálogo y planes. |
| `AgroCampos.local.postman_environment.json` | Agro Campos - Local (API Gateway) | Compartido | — | URLs locales y, durante la corrida, datos de apoyo publicados por AD. |

Los totales cuentan solicitudes, no aserciones: una solicitud puede ejecutar varios tests.

## Requisitos y autenticación

Con la infraestructura local del proyecto en ejecución, deben estar disponibles el API Gateway en `http://localhost:8080` y Keycloak en `http://localhost:8081`. Las llamadas a campos, agronomía y proyectos usan el Gateway; la obtención de tokens se realiza directamente contra Keycloak.

Cada colección obtiene exclusivamente el token de su usuario mediante `POST {{keycloakUrl}}/realms/bda-realm/protocol/openid-connect/token`, con `grant_type=password` y `client_id=agro-campos-client`.

| Colección | Solicitud de autenticación | Usuario | Contraseña local |
|---|---|---|---|
| AD | `00.00a` | `admin1` | `admin1` |
| AG | `00.00b` | `agronomo1` | `agronomo1` |
| OP | `00.00c` | `operario1` | `operario1` |

El token se guarda como `access_token` en las variables de **su propia colección**. El script previo lo lee explícitamente de ese ámbito y agrega `Authorization: Bearer ...` cuando la solicitud no tiene un encabezado de autorización explícito. No hay selector de rol ni tokens compartidos en el environment. Para renovar un token, volver a ejecutar la solicitud de autenticación del rol correspondiente.

## Roles y permisos

| Operación | ADMIN | AGRONOMO | OPERARIO |
|---|---|---|---|
| Consultar campos, lotes, catálogo y planes | Permitido | Permitido | Permitido |
| Crear, modificar y dar de baja campos/lotes | Permitido | Denegado (`403`) | Denegado (`403`) |
| Administrar cultivos, plagas, agroquímicos y tratamientos | Permitido | Permitido | Denegado (`403`) |
| Crear y cancelar planes de siembra | Permitido | Permitido | Denegado (`403`) |
| Iniciar y completar labores | Permitido | Permitido | Permitido |
| Acceder a endpoints protegidos sin token válido | Denegado (`401`) | Denegado (`401`) | Denegado (`401`) |

La matriz describe los permisos por rol; las colecciones prueban los casos concretos detallados abajo, sin cubrir necesariamente todas las combinaciones de método y recurso.

## Organización y tipos de tests

Ejecutar las solicitudes en el orden guardado en cada JSON, incluidos los casos negativos intercalados en el proceso principal.

| Carpeta | AD | AG | OP | Cobertura |
|---|---:|---:|---:|---|
| 00 - Preparación | 4 | 4 | 4 | Token del rol, reset de variables y healthchecks del Gateway. AG y OP también cargan sus datos de apoyo desde el environment. |
| 01 - Datos iniciales (seed) | 14 | 10 | — | AD crea campos/lotes y catálogo; AG crea su propio catálogo. |
| 02 - Campos y lotes | 14 | 14 | 6 | AD prueba consultas, modificaciones y validaciones; AG consulta y espera `403` en escrituras; OP solo consulta y comprueba recursos inexistentes. |
| 03 - Agronomía | 20 | 20 | 11 | AD/AG prueban consultas, modificación de plaga y reglas del catálogo; OP prueba consultas, filtros y `404`. |
| 04 - Proyectos: proceso principal | 31 | 31 | 19 | AD/AG crean, ejecutan, finalizan y cancelan planes; OP ejecuta labores del plan preparado y consulta resultados. |
| 05 - Integraciones | 9 | 9 | — | Relaciones entre servicios, restricción climática mediante Open-Meteo y conservación de labores copiadas al plan. |
| 06 - Limpieza (baja lógica) | 14 | 10 | — | AD da de baja sus datos y comprueba `404` de campo/lote; AG da de baja únicamente su catálogo. |
| 07 - Seguridad y Roles (RBAC) | 1 | 2 | 6 | Rechazos por permisos y petición sin token. |
| 08 - Preparar datos independientes para AG y OP | 19 | — | — | AD crea y publica los datos exclusivos de las otras dos colecciones. |
| **Total** | **126** | **100** | **46** | |

Los tres healthchecks de la carpeta 00 apuntan a `{{baseUrl}}/actuator/health`. Aunque algunos nombres mencionan microservicios, verifican el endpoint del Gateway y no la salud individual de cada servicio.

### AD: administración y pruebas funcionales completas

- **Casos positivos:** altas, consultas, actualizaciones, filtros y bajas lógicas; validación de estados, relaciones, superficies, suelos y secuencia de labores en los cuerpos JSON.
- **Validaciones y reglas de negocio (`400`):** nombres duplicados, datos obligatorios, superficie ocupada, números de lote repetidos, dosis inválidas y compatibilidad entre plaga y agroquímico.
- **Proceso de planes:** aptitud del suelo, un único plan activo por lote, transiciones de labores, tratamiento obligatorio y correspondiente al cultivo, finalización al cosechar y liberación del lote al finalizar/cancelar.
- **Recursos inexistentes (`404`):** campos, lotes, cultivos, tratamientos y referencias inválidas al crear planes.
- **Integraciones:** datos de campos y agronomía utilizados por proyectos, rechazo de fumigación con viento sobre el máximo y conservación de la secuencia original del plan después de modificar el cultivo.
- **Seguridad:** la carpeta 07 espera `401` sin token; las operaciones administrativas permitidas se verifican en las carpetas funcionales.

### AG: gestión agronómica y restricciones en campos

AG reutiliza la cobertura de catálogo, planes e integraciones de AD, pero trabaja con campos/lotes preparados por ADMIN y crea su propio catálogo.

Las escrituras de la carpeta 02 están marcadas con `[AG 403]`: esperan un rechazo de autorización, incluso cuando el nombre original menciona una validación de negocio. Por ejemplo, intentar crear un campo duplicado como AG prueba `403`, no la regla de duplicados con `400`.

La carpeta 07 verifica especéficamente `403` al crear un campo y `401` sin token. Su limpieza contiene diez bajas del catálogo y no elimina los campos/lotes de apoyo.

### OP: consultas y ejecución de labores

OP no tiene carpetas de seed, integraciones ni limpieza. Lee campos, lotes, cultivos, plagas, agroquímicos y tratamientos; comprueba filtros y recursos inexistentes; y ejecuta el plan `PLANIFICADO` creado por AD.

En la carpeta 04 recorre `Arar → Sembrar → Fumigar → Cosechar`, iniciando y completando cada labor. También prueba secuencias/transiciones inválidas, fumigación sin tratamiento o con un tratamiento de otro cultivo, advertencia por falta de coordenadas y rechazo de cambios sobre un plan finalizado. Consulta el plan, los planes finalizados y el histórico del lote.

Los seis casos de la carpeta 07 prueban: cancelación de plan denegada (`403`), creación de plan denegada (`403`), consulta de campos permitida (`200`), creación de campo denegada (`403`), creación de cultivo denegada (`403`) y petición sin token (`401`).

Un `400`, `403` o `404` esperado es un test correcto cuando coincide con la aserción del caso; no implica que la corrida haya fallado.

## Environment local y variables

Importar `AgroCampos.local.postman_environment.json` y seleccionar **Agro Campos - Local (API Gateway)** para las tres colecciones.

El archivo inicial contiene únicamente estas dos variables habilitadas:

| Variable | Valor local | Uso |
|---|---|---|
| `baseUrl` | `http://localhost:8080` | Base común de `/api/v1/campos`, `/api/v1/agronomia`, `/api/v1/proyectos` y `/actuator/health`. |
| `keycloakUrl` | `http://localhost:8081` | Obtención del token del rol. |

Las colecciones también definen estas URLs como valores locales de respaldo. Al seleccionar el environment, sus valores tienen prioridad. No se utilizan las antiguas variables `gatewayUrl`, `camposUrl`, `agronomiaUrl` ni `proyectosUrl`.

### Variables generadas durante la corrida

| ámbito | Variables | Origen y propósito |
|---|---|---|
| Colección | `access_token` | Autenticación exclusiva del rol. |
| Colección | `runId`, `fechaSiembra`, nombres de entidades | Reset de preparación y scripts de seed; fecha de siembra a siete días y nombres con marca de tiempo. |
| Colección | `campoId`, `loteId`, `loteSinCoordsId`, `loteNoAptoId` | Respuestas del seed de AD o carga de datos de apoyo en AG/OP. |
| Colección | `cultivoId`, `cultivoMaizId`, `plagaId`, IDs de agroquímicos y tratamientos | Seed propio en AD/AG; datos preparados por AD en OP. |
| Colección | `planId`, IDs de labores, `plan2Id`, `plan3Id`, `planClimaId` y labores climáticas | Respuestas de proyectos según los escenarios presentes en cada colección. |
| Environment | `AG_campoId`, `AG_campoNombre`, `AG_loteId`, `AG_loteSinCoordsId`, `AG_loteNoAptoId` | Carpeta 08 de AD: campo y tres lotes exclusivos para AG. |
| Environment | Variables con prefijo `OP_` | Carpeta 08 de AD: campo/lotes, catálogo, nombres de campo/cultivo, plan e IDs de sus cuatro labores para OP. |

Los prefijos `AG_` y `OP_` se agregan al environment durante la ejecución; no vienen precargados en el archivo inicial. La solicitud `00.01` de AG/OP reinicia sus variables y copia las correspondientes a su rol a variables de colección sin prefijo. Si falta un dato requerido, el script indica que primero debe ejecutarse AD, carpeta 08.

## Preparación de datos y limpieza

El seed de AD incluye un campo de 120 ha y tres lotes: uno con coordenadas y suelo `TIPO_2`, otro sin coordenadas con suelo `TIPO_2` y uno no apto para soja con suelo `TIPO_5`. El catálogo incluye soja y maíz, una plaga `INSECTO`, insecticidas permisivo/restrictivo, fertilizante, fungicida y tres tratamientos para probar compatibilidad y lámites climáticos.

La carpeta 08 de AD crea **otros juegos de datos independientes**: campo y lotes para AG; campo, lotes, catálogo y un plan en lote sin coordenadas para OP. Esto evita que la limpieza de AD o AG elimine los datos usados por la otra colección.

La carpeta 06 realiza bajas lógicas y puede omitirse si se necesita inspeccionar los datos al terminar. Los datos de apoyo creados en 08 permanecen disponibles y su baja corresponde a ADMIN; las colecciones no incluyen una limpieza automática de esos juegos de apoyo.

La prueba climática de AD/AG consulta Open-Meteo mediante el flujo de proyectos y espera `400` con el mensaje `No se puede fumigar` usando un lámite de viento de 0.01 km/h. Su resultado depende de la disponibilidad de la integración y de las condiciones climáticas consultadas. El flujo principal sin coordenadas verifica el caso de advertencia sin esa consulta climática.

## Orden de ejecución en Postman

1. Importar las tres colecciones y el environment local.
2. Seleccionar **Agro Campos - Local (API Gateway)**.
3. Ejecutar **AD completa**, desde 00 hasta 08, con una iteración. La carpeta 08 debe terminar correctamente antes de iniciar AG u OP.
4. Ejecutar **AG completa**, respetando el orden de sus carpetas.
5. Ejecutar **OP completa**, respetando el orden de sus carpetas.

AG y OP usan datos independientes: una vez preparada la carpeta 08, no dependen entre sí. No ejecutar carpetas aisladas sin sus datos previos. La carpeta 00 empieza obteniendo el token y luego realiza el reset/carga de variables.

Para preparar solo los datos de apoyo, ejecutar las carpetas **00 y 08 de AD**, en ese orden. Luego pueden correrse AG y OP sin ejecutar el resto de AD.

**OP debe ejecutarse una sola vez por plan preparado:** la cosecha lo deja `FINALIZADO`. Para repetir una corrida completa, volver a ejecutar 00 y 08 de AD (o AD completa) y luego la colección deseada. Reiniciar las variables de OP no reinicia el estado del plan en el servidor. Cada nueva preparación crea datos adicionales.

## Ejecución con Newman

Con Newman instalado, ejecutar desde este directorio. AD debe exportar el environment actualizado para que los procesos posteriores reciban los IDs con prefijos `AG_` y `OP_`:

```bash
newman run AgroCampos.postman_collection.json -e AgroCampos.local.postman_environment.json --export-environment AgroCampos.ejecucion.postman_environment.json
newman run AgroCampos.AG.postman_collection.json -e AgroCampos.ejecucion.postman_environment.json
newman run AgroCampos.OP.postman_collection.json -e AgroCampos.ejecucion.postman_environment.json
```

Revisar el resultado de cada comando antes de continuar. El archivo `AgroCampos.ejecucion.postman_environment.json` se genera con la corrida de AD; no es un archivo de entrada incluido en esta entrega. Cada colección obtiene su propio token al comenzar, por lo que no es necesario exportar tokens entre ejecuciones.

Para ejecutar únicamente la preparación de apoyo de AD:

```bash
newman run AgroCampos.postman_collection.json -e AgroCampos.local.postman_environment.json --folder "00 - Preparacion" --folder "08 - Preparar datos independientes para AG y OP" --export-environment AgroCampos.ejecucion.postman_environment.json
```

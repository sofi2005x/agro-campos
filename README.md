# 🌾 Agro Campos

Backend de microservicios para la **administración de campos agrícolas**, desarrollado con Spring Boot como Trabajo Práctico Integrador de Backend de Aplicaciones. El sistema gestiona campos y lotes, un catálogo agronómico (cultivos, plagas, agroquímicos y tratamientos) y los planes de siembra con sus laboreos, aplicando las reglas de negocio del dominio.

## Objetivos del proyecto

- Resolver un dominio real con **responsabilidades funcionales diferenciadas** entre microservicios (no tres CRUD independientes).
- Organizar cada microservicio en capas inspiradas en **DDD**: `application → domain ← infrastructure`, con un **dominio rico** (entidades con comportamiento) y las reglas de negocio concentradas en el dominio.
- Implementar **colaboración real** entre servicios: el registro de un plan de siembra necesita datos de lotes (`campos-service`) y de cultivos (`agronomia-service`).
- Integrar una **API pública externa** (Open-Meteo) para decidir si es posible fumigar.
- Exponer todo detrás de un **API Gateway**, con autenticación y autorización mediante **Keycloak (OIDC + JWT)** y **Spring Security**.
- Documentar las APIs con OpenAPI/Swagger y desplegar la solución completa con Docker Compose.

## Estado del proyecto

| Componente | Estado |
|---|---|
| `gateway` (ruteo a `campos-service`) | ✅ Implementado |
| `campos-service`: alta y consulta de campos y lotes | ✅ Implementado |
| `campos-service`: modificación y baja de campos y lotes | 🔜 Planificado |
| `campos-service`: refactor a capas (`application / domain / infrastructure`) | ✅ Implementado |
| `agronomia-service` | 🔄 En desarrollo (modelo de dominio migrado desde `campos-service`) |
| Integración con Open-Meteo | 🔜 Planificado |
| Keycloak, Spring Security y roles | 🔜 Planificado |
| `bda-commons` (JAR reutilizable) | 🔜 Planificado |
| `proyectos-service` | 🔄 En desarrollo (puertos y cliente REST para `campos-service` listos) |

## Arquitectura

```
                                           ┌───────────────────────┐
                                      ┌───►│    campos-service     │──┐
                                      │    │         :8082         │  │
 Cliente ──► API Gateway ─────────────┼───►│   agronomia-service   │──┼──►  MySQL 8
 (Bearer JWT)    :8080                │    │         :8083         │  │   (una base por
                                      │    │                       │  │    servicio)
                                      └───►│   proyectos-service   │──┘
                                           │         :8084         │
                                           └───┬───────┬───────┬───┘
                                               │       │       │
                       consulta lote y suelo ◄─┘       │       └─► Open-Meteo (pronóstico)
                       (campos-service)                │
                                  consulta cultivo y tratamiento
                                  (agronomia-service)

 Keycloak: emite los tokens JWT; el gateway y cada servicio los validan.
```

| Componente | Responsabilidad | Puerto |
|---|---|---|
| `gateway` | Spring Cloud Gateway. Punto de entrada único: enruta por prefijo y valida el JWT. | 8080 |
| `campos-service` | Campos y lotes: catastro, ubicación y control de superficie. | 8082 |
| `agronomia-service` | Vademécum y catálogo: cultivos, plagas, agroquímicos y tratamientos. | 8083 |
| `proyectos-service` | Planes de siembra, estados, laboreos y control climático de fumigaciones. Es el único que invoca a otros servicios. | 8084 |
| `mysql-db` | MySQL 8 con una base por servicio y volumen persistente. | 3306 |
| `keycloak` | Identity Provider (OIDC, JWT, roles). | 8081 |
| `bda-commons` | JAR Maven con el código verdaderamente reutilizable (ver [Componente común](#componente-común-bda-commons)). | n/a |

Rutas del gateway:

| Prefijo | Destino |
|---|---|
| `/api/v1/campos/**` | `campos-service:8082` |
| `/api/v1/agronomia/**` | `agronomia-service:8083` |
| `/api/v1/proyectos/**` | `proyectos-service:8084` |

**Dirección de las dependencias**: `proyectos-service` consume a `campos-service` y a `agronomia-service`. Estos dos no se llaman entre sí ni a `proyectos-service`, para evitar acoplamiento circular. Por eso `campos-service` guarda solo el tipo de suelo de cada lote (enum `TIPO_1` a `TIPO_5`) y no depende del catálogo agronómico.

## Tecnologías

- Java 21
- Spring Boot 3.3.4 (Web, Data JPA, Validation, Actuator)
- Spring Cloud Gateway (Spring Cloud 2023.0.3)
- Spring Security (OAuth2 Resource Server) y Keycloak *(planificado)*
- MySQL 8.0
- Springdoc OpenAPI / Swagger UI 2.6.0
- Lombok
- Maven (proyecto multi-módulo)
- Docker y Docker Compose (build multi-etapa, usuario sin privilegios, healthchecks)

## Estructura del repositorio

```
agro-campos/
├── compose.yml                  # Orquesta MySQL + servicios + gateway (+ Keycloak)
├── docs/
│   ├── c1-contexto.puml         # Diagrama C4 nivel 1
│   └── c2-contenedores.puml     # Diagrama C4 nivel 2
├── bda-commons/                 # JAR reutilizable (planificado)
├── gateway/
├── campos-service/
├── agronomia-service/           # catálogo agronómico (en desarrollo)
└── proyectos-service/           # planes de siembra (en desarrollo)
```

## Estructura de paquetes (inspirada en DDD)

Los tres microservicios comparten la misma organización interna. El ejemplo es `campos-service`:

```
clase.spring
├── application                          Capa de aplicación: casos de uso y API REST
│   ├── controllers
│   │   └── CampoController              Endpoints REST para campos y lotes
│   ├── dtos
│   │   ├── request
│   │   │   ├── CampoRequestDTO          Datos de entrada para crear un campo
│   │   │   └── LoteRequestDTO           Datos de entrada para registrar un lote
│   │   └── response
│   │       ├── CampoResponseDTO         Datos expuestos de campo y cantidad de lotes
│   │       └── LoteResponseDTO          Datos expuestos de lote con su tipo de suelo
│   └── services
│       └── CampoService                 Implementación de casos de uso y reglas de negocio
├── domain                               Núcleo del negocio y entidades JPA
│   ├── exceptions
│   │   ├── BusinessRuleException        Excepción 400 para reglas de negocio (RN-CAM)
│   │   ├── ResourceNotFoundException    Excepción 404 para entidades no encontradas
│   │   └── GlobalExceptionHandler       @RestControllerAdvice centralizado
│   └── models
│       ├── Campo                        Entidad de campo (id, nombre, superficieTotalHas, lotes)
│       ├── Lote                         Entidad de lote (nroLote, hectareas, tipoSuelo, coords)
│       └── TipoSuelo                    Enum con los tipos de suelo (TIPO_1 ... TIPO_5)
└── infrastructure                       Detalles técnicos y persistencia
    └── repositories
        ├── CampoRepository              Spring Data JPA (existsByNombreIgnoreCase)
        └── LoteRepository               Spring Data JPA (existsByCampoIdAndNroLote, findByCampoId)
```
Qué cambia en los otros dos servicios:

| Servicio | `domain.model` | `infrastructure` adicional |
|---|---|---|
| `agronomia-service` | `Cultivo`, `Plaga`, `TipoPlaga`, `Agroquimico`, `TipoAgroquimico`, `Tratamiento` | Repositorios de cada agregado |
| `proyectos-service` | `PlanDeSiembra`, `Labor`, `EstadoPlan`, `EstadoLabor` y *value objects* con los datos de lote, cultivo y clima que necesitan las reglas | `infrastructure.client`: `CamposClient`, `AgronomiaClient` y `ClimaClient` (Open-Meteo), todos con `RestClient` |

| Capa | Responsabilidad | Puede usar |
|---|---|---|
| `application` | Exponer la API REST y resolver los casos de uso. Traduce DTOs ↔ dominio y orquesta. | `domain`, `infrastructure.repository`, `infrastructure.client` |
| `domain` | Modelo del negocio y sus reglas. Es el corazón de la aplicación. | Solo a sí mismo (nunca a `application`) |
| `infrastructure` | Detalles técnicos: persistencia, clientes HTTP, configuración. | `domain` (para persistir sus entidades) |

Flujo de una petición: `Controller → Service (application) → entidades / DomainService (domain) → Repository o Client (infrastructure)`.

### Cómo se reparten las reglas entre capas

El dominio no accede a la base ni a otros servicios. Cuando una regla necesita información externa, el servicio de aplicación **obtiene el dato** y el dominio **toma la decisión**. Por ejemplo, para RN-CAM-01 el servicio de aplicación consulta `existsByNombreIgnoreCase(...)` al repositorio y le pasa el resultado a `CampoDomainService`, que decide si lanza `ReglaNegocioException`. Ni el controller ni el servicio de aplicación deciden reglas: solo orquestan.

Las reglas únicas se respaldan además con restricciones en la base de datos, como defensa ante peticiones concurrentes.

---

## Dominio y reglas de negocio

### `campos-service` — Catastro y lotes

| Entidad | Descripción |
|---|---|
| `Campo` | Nombre y superficie total en hectáreas. Relación 1 a N con sus `Lote`s. |
| `Lote` | Número de lote, hectáreas, coordenadas (latitud/longitud) y `TipoSuelo`. Relación N a 1 con `Campo`. |
| `TipoSuelo` | Enum con los tipos de suelo (`TIPO_1` a `TIPO_5`) que caracterizan al lote. |

| Regla | Descripción | Dónde se aplica | Estado |
|---|---|---|---|
| **RN-CAM-01** Unicidad de nombre | No pueden existir dos campos con el mismo nombre, sin distinguir mayúsculas. | `CampoService.crearCampo` (consulta `existsByNombreIgnoreCase` en `CampoRepository`) | ✅ |
| **RN-CAM-02** Control de superficie | La suma de las hectáreas de los lotes no puede exceder la superficie total del campo. | `CampoService.agregarLoteACampo` (valida suma de has de lotes vs campo) | ✅ |
| **RN-CAM-03** Unicidad de lote | No puede repetirse el número de lote dentro de un mismo campo. | `CampoService.agregarLoteACampo` (consulta `existsByCampoIdAndNroLote`) | ✅ |

> 📌 **Desacoplamiento completado:**  
> Se eliminaron del modelo de `campos-service` las entidades de catálogo (`Cultivo`, `Plaga`, `Agroquimico`, `TipoAgroquimico`, `Tratamiento`) ya que fueron migradas a `agronomia-service`. `campos-service` administra de forma autónoma campos, lotes y tipos de suelo.

### `agronomia-service` — Vademécum fitosanitario y catálogo de cultivos

| Entidad | Descripción |
|---|---|
| `Cultivo` | Nombre, **lista de tipos de suelo aptos** y **secuencia ordenada de laboreos** (por ejemplo: `ARADO`, `RASTRILLADO`, `SIEMBRA`, `FUMIGACION`, `COSECHA`). |
| `Plaga` | Nombre, descripción y `TipoPlaga` (`INSECTO`, `HONGO`, `MALEZA`). |
| `Agroquimico` | Nombre comercial, principio activo, `TipoAgroquimico` (`HERBICIDA`, `INSECTICIDA`, `FUNGICIDA`, `FERTILIZANTE`), viento máximo permitido (km/h) y horas libres de lluvia requeridas. |
| `Tratamiento` | Relaciona plaga + agroquímico + cultivo con dosis por hectárea y unidad de medida. |

| Regla | Descripción | Dónde se aplica | Estado |
|---|---|---|---|
| **RN-TRA-01** Unicidad de producto | No puede registrarse dos veces el mismo agroquímico comercial. | `AgroquimicoDomainService` (con la consulta del repositorio) | 🔜 |
| **RN-TRA-02** Dosis válida | La dosis por hectárea debe ser estrictamente positiva (> 0). | `Tratamiento.crear` | 🔜 |
| **RN-TRA-04** Coherencia de plaga y químico | `INSECTO` → `INSECTICIDA`, `HONGO` → `FUNGICIDA`, `MALEZA` → `HERBICIDA`. Un `FERTILIZANTE` no combate ninguna plaga. | `TipoPlaga.admite(TipoAgroquimico)`, invocado desde `Tratamiento.crear` | 🔜 |

Este servicio también es el dueño de los datos que otros servicios usan para validar: los `suelosAptos` y la secuencia de laboreos de cada cultivo, y los límites de viento y lluvia de cada agroquímico.

### `proyectos-service` — Planes de siembra y laboreos

| Entidad | Descripción |
|---|---|
| `PlanDeSiembra` | Un cultivo sembrado en un lote. Estados: `PLANIFICADO`, `EN_CURSO`, `FINALIZADO` y `CANCELADO`. |
| `Labor` | Cada laboreo del plan, en el orden que define el cultivo. Estados: `PENDIENTE`, `EN_EJECUCION` y `COMPLETADA`. |

Un plan está **activo** cuando su estado es `PLANIFICADO` o `EN_CURSO`. Al crearlo, las labores se copian de la secuencia del cultivo: los cambios posteriores en el catálogo no afectan a planes existentes.

| Regla | Descripción | Dónde se aplica | Estado |
|---|---|---|---|
| **RN-LAB-01** Aptitud de suelo | Un cultivo solo puede sembrarse en un lote cuyo `TipoSuelo` esté en la lista de `suelosAptos` del cultivo. | `PlanDomainService`. El suelo viene de `campos-service` y la lista de `agronomia-service`. | 🔜 |
| **RN-LAB-02** Exclusividad del lote | Un lote solo puede tener un único plan activo a la vez (`PLANIFICADO` o `EN_CURSO`). No se pueden solapar cultivos. | `PlanDomainService` (con la consulta del repositorio) | 🔜 |
| **RN-LAB-03** Secuencia estricta de labores | Los laboreos se ejecutan en el orden definido por el cultivo: una labor solo puede iniciarse si la anterior está `COMPLETADA`. | `PlanDeSiembra.iniciarLabor` | 🔜 |
| **RN-LAB-04** Cierre de campaña con cosecha | El plan pasa a `FINALIZADO` solo cuando la labor de `COSECHA` se marca como completada, y el lote queda libre para la próxima siembra. | `PlanDeSiembra.completarLabor` | 🔜 |
| **RN-LAB-05** Condiciones climáticas para fumigar | No se puede iniciar una labor de `FUMIGACION` si el pronóstico (Open-Meteo) para las coordenadas del lote supera el viento máximo del agroquímico o prevé lluvia dentro de sus horas libres requeridas. | `PlanDomainService`. El pronóstico lo trae `ClimaClient`. | 🔜 |

Notas sobre estas reglas:

- **Liberación del lote (RN-LAB-04)**: no requiere ninguna escritura en `campos-service`. El lote queda libre porque su plan deja de estar activo (`FINALIZADO` o `CANCELADO`), y RN-LAB-02 lo permite.
- **RN-LAB-05**: los umbrales no son constantes del servicio. Salen del `Agroquimico` del tratamiento aplicado (`vientoMaximoKmh` y `horasLibresLluvia`), por lo que la labor de fumigación debe indicar el `tratamientoId` al iniciarse.
- **Si Open-Meteo no responde**: se permite iniciar la fumigación y la respuesta incluye una advertencia indicando que no se pudo validar el clima. El cliente tiene timeout corto y el incidente queda registrado en el log.

---

## Contrato de la API

Base (por el gateway): `http://localhost:8080`. Directo a cada servicio, usar su puerto.

### `campos-service` — `/api/v1/campos`

| Método | URI | Body | Éxito | Errores | Estado |
|---|---|---|---|---|---|
| `GET` | `/api/v1/campos` | — | `200` + lista | — | ✅ |
| `GET` | `/api/v1/campos/{id}` | — | `200` + campo | `404` | ✅ |
| `POST` | `/api/v1/campos` | `CampoRequestDTO` | `201` + campo + header `Location` | `400` (validación, RN-CAM-01) | ✅ |
| `PUT` | `/api/v1/campos/{id}` | `CampoRequestDTO` | `200` + campo | `400`, `404` | 🔜 |
| `DELETE` | `/api/v1/campos/{id}` | — | `204` | `404` | 🔜 |
| `GET` | `/api/v1/campos/{id}/lotes` | — | `200` + lista | `404` | ✅ |
| `GET` | `/api/v1/campos/{id}/lotes/{loteId}` | — | `200` + lote | `404` | 🔜 |
| `POST` | `/api/v1/campos/{id}/lotes` | `LoteRequestDTO` | `201` + lote | `400` (RN-CAM-02, RN-CAM-03), `404` | ✅ |
| `PUT` | `/api/v1/campos/{id}/lotes/{loteId}` | `LoteRequestDTO` | `200` + lote | `400`, `404` | 🔜 |
| `PATCH` | `/api/v1/campos/{id}/lotes/{loteId}` | `LotePatchRequest` | `200` + lote | `400`, `404` | 🔜 |
| `DELETE` | `/api/v1/campos/{id}/lotes/{loteId}` | — | `204` | `404` | 🔜 |

`DELETE` es una **baja lógica** (el registro queda inactivo). Así los planes históricos de `proyectos-service` siguen referenciando un lote válido, sin que `campos-service` tenga que consultar a otros servicios. Un lote dado de baja no admite nuevos planes.

### `agronomia-service` — `/api/v1/agronomia` 🔜

| Método | URI | Body | Éxito | Errores |
|---|---|---|---|---|
| `GET` / `POST` | `/cultivos` | `CultivoRequest` | `200` lista / `201` | `400` |
| `GET` | `/cultivos/{id}` | — | `200` (incluye `suelosAptos` y `laboreos` ordenados) | `404` |
| `GET` / `POST` | `/plagas` | `PlagaRequest` | `200` lista / `201` | `400` |
| `GET` / `POST` | `/agroquimicos` | `AgroquimicoRequest` | `200` lista / `201` | `400` (RN-TRA-01) |
| `GET` | `/agroquimicos/{id}` | — | `200` | `404` |
| `GET` / `POST` | `/tratamientos` | `TratamientoRequest` | `200` lista (filtros `?plagaId=` y `?cultivoId=`) / `201` | `400` (RN-TRA-02, RN-TRA-04), `404` |
| `GET` | `/tratamientos/{id}` | — | `200` (incluye el agroquímico con sus límites climáticos) | `404` |

### `proyectos-service` — `/api/v1/proyectos` 🔜

| Método | URI | Body | Éxito | Errores |
|---|---|---|---|---|
| `POST` | `/planes` | `PlanRequest` (`loteId`, `cultivoId`, `fechaSiembraPrevista`) | `201` + plan + header `Location` | `400` (RN-LAB-01, RN-LAB-02), `404`, `503` |
| `GET` | `/planes` | — | `200` + lista (filtro `?estado=`) | `400` estado inválido |
| `GET` | `/planes/{id}` | — | `200` + plan con sus labores | `404` |
| `GET` | `/lotes/{loteId}/planes` | — | `200` + histórico de planes del lote | `404` |
| `PATCH` | `/planes/{id}` | `PlanPatchRequest` (`estado: CANCELADO`) | `200` + plan | `400`, `404` |
| `PATCH` | `/planes/{planId}/labores/{laborId}` | `LaborPatchRequest` (`estado`, `tratamientoId` opcional) | `200` + plan | `400` (RN-LAB-03, RN-LAB-04, RN-LAB-05), `404`, `503` |

Transiciones de una labor: `PENDIENTE → EN_EJECUCION → COMPLETADA`. El control climático de RN-LAB-05 se evalúa al pasar una labor de fumigación a `EN_EJECUCION`, que es el momento en que tiene sentido consultar el pronóstico.

### Ejemplos de intercambio

**Crear un campo**

```http
POST /api/v1/campos
Content-Type: application/json

{ "nombre": "La Esperanza", "superficieTotalHas": 120.5 }
```

```http
HTTP/1.1 201 Created
Location: /api/v1/campos/1

{ "id": 1, "nombre": "La Esperanza", "superficieTotalHas": 120.5, "cantidadLotes": 0 }
```

**Agregar un lote**

```http
POST /api/v1/campos/1/lotes
Content-Type: application/json

{ "nroLote": 1, "hectareas": 40, "latitud": -31.4201, "longitud": -64.1888, "tipoSuelo": "TIPO_2" }
```

```json
{ "id": 1, "nroLote": 1, "hectareas": 40.0, "latitud": -31.4201, "longitud": -64.1888, "tipoSuelo": "TIPO_2", "campoId": 1 }
```

**Iniciar una fumigación (planificado)**

```http
PATCH /api/v1/proyectos/planes/7/labores/12
Content-Type: application/json

{ "estado": "EN_EJECUCION", "tratamientoId": 3 }
```

```http
HTTP/1.1 400 Bad Request

{
  "status": 400,
  "error": "Bad Request",
  "mensaje": "No se puede fumigar: viento previsto de 28 km/h supera el máximo de 15 km/h del producto",
  "timestamp": "2026-10-26T10:20:00"
}
```

## Manejo de errores

Todos los errores usan el mismo formato (`ErrorResponse`). Los errores de validación agregan el detalle por campo.

```json
// 400: regla de negocio
{ "status": 400, "error": "Bad Request", "mensaje": "Ya existe un campo con el nombre: La Esperanza", "timestamp": "2026-10-26T10:20:00" }

// 400: validación de campos
{ "status": 400, "error": "Bad Request", "mensaje": "Datos inválidos", "errores": { "superficieTotalHas": "La superficie total debe ser mayor a 0" }, "timestamp": "2026-10-26T10:20:00" }

// 404: recurso inexistente
{ "status": 404, "error": "Not Found", "mensaje": "Campo no encontrado con ID: 99", "timestamp": "2026-10-26T10:20:00" }

// 503: otro microservicio no responde
{ "status": 503, "error": "Service Unavailable", "mensaje": "campos-service no disponible", "timestamp": "2026-10-26T10:20:00" }
```

`GlobalExceptionHandler` (en `application.controller`) traduce las excepciones del dominio: `*NoEncontradaException` → `404`, `ReglaNegocioException` → `400`. Un servicio remoto caído se traduce a `503`.

## Seguridad *(planificada)*

- **Keycloak** como Identity Provider, con OIDC y tokens JWT. Realm `bda-realm` y roles `ADMIN`, `AGRONOMO` y `OPERARIO`.
- El **gateway** valida el JWT (Security Filter) antes de rutear. Cada microservicio **vuelve a validarlo** como Resource Server y autoriza por rol. Solo el gateway se publica hacia afuera; los servicios quedan en la red interna de Docker (zona de confianza).

Roles propuestos:

| Operación | ADMIN | AGRONOMO | OPERARIO |
|---|:-:|:-:|:-:|
| Consultar campos, catálogo y planes | ✅ | ✅ | ✅ |
| Alta, modificación y baja de campos y lotes | ✅ | ❌ | ❌ |
| Administrar cultivos, plagas, agroquímicos y tratamientos | ✅ | ✅ | ❌ |
| Crear y cancelar planes de siembra | ✅ | ✅ | ❌ |
| Iniciar y completar labores | ✅ | ✅ | ✅ |

## Integración con Open-Meteo

[Open-Meteo](https://open-meteo.com) es una API pública de pronóstico que no requiere clave. `ClimaClient` (en `proyectos-service`) consulta el pronóstico horario de viento y precipitación para las coordenadas del lote y lo compara con los límites del agroquímico (RN-LAB-05).

| Propiedad | Descripción |
|---|---|
| `clima.open-meteo.base-url` | URL base de la API |
| `clima.open-meteo.timeout-ms` | Timeout de la consulta |

## Componente común: `bda-commons`

JAR Maven independiente, instalado en el repositorio local (`mvn install`) y usado como dependencia por los tres microservicios. Contenido previsto: conversor de roles de Keycloak (claim `realm_access`), configuración base de seguridad y OpenAPI, y, si se decide compartirlos, `ErrorResponse` y el manejo de errores. Lo que pertenece a un solo servicio (entidades y reglas de dominio) permanece dentro de ese servicio.

## Cómo ejecutarlo

### Opción 1: Docker Compose (recomendada)

Requisitos: Docker y Docker Compose.

```bash
docker compose -f compose.yml up --build
```

Se levantan en orden MySQL, los servicios (esperan a que MySQL esté sano) y el gateway (espera a que los servicios estén sanos). La primera vez tarda porque Maven descarga las dependencias.

Para bajar todo (agregá `-v` si querés borrar también los datos de la base):

```bash
docker compose -f compose.yml down
```

### Opción 2: Local, sin Docker

Requisitos: JDK 21, Maven y un MySQL 8 en `localhost:3306`. Cada servicio crea su base si no existe (`createDatabaseIfNotExist=true`).

```bash
# Terminal 1
cd campos-service
mvn spring-boot:run

# Terminal 2 (opcional, si querés pasar por el gateway)
cd gateway
mvn spring-boot:run
```

> Al correr el gateway fuera de Docker, los nombres de host de los servicios no se resuelven. Cambiá los `uri` por `http://localhost:<puerto>` en `gateway/src/main/resources/application.properties`, o llamá directo al puerto de cada servicio.

## Documentación de la API (Swagger)

Cada servicio publica su propia documentación OpenAPI:

| Servicio | Swagger UI |
|---|---|
| `campos-service` | http://localhost:8082/swagger-ui.html |
| `agronomia-service` | http://localhost:8083/swagger-ui.html |
| `proyectos-service` | http://localhost:8084/swagger-ui.html |

El JSON de OpenAPI está en `/v3/api-docs` de cada servicio. Swagger se consulta directo contra cada servicio, porque el gateway solo enruta `/api/v1/**`.

## Configuración

Las propiedades de cada servicio están en `application.properties`. En Docker se pisan con variables de entorno desde `compose.yml`:

| Variable | Valor en el compose |
|---|---|
| `SPRING_DATASOURCE_URL` | `jdbc:mysql://mysql-db:3306/<base_del_servicio>?...` |
| `SPRING_DATASOURCE_USERNAME` | usuario del servicio |
| `SPRING_DATASOURCE_PASSWORD` | contraseña del servicio |

Una base por servicio: `agro_campos_db`, `agro_agronomia_db` y `agro_proyectos_db`.

> ⚠️ Las credenciales `root/root` son **solo para desarrollo**. Antes de desplegar en cualquier entorno real hay que crear un usuario por servicio con permisos únicamente sobre su base, y mover las credenciales a un archivo `.env` (ya ignorado por Git) o a un gestor de secretos.

## Pruebas manuales sugeridas

```bash
# Crear un campo
curl -i -X POST http://localhost:8080/api/v1/campos \
  -H "Content-Type: application/json" \
  -d '{"nombre":"La Esperanza","superficieTotalHas":120.5}'

# RN-CAM-01: mismo nombre con otras mayúsculas -> 400
curl -i -X POST http://localhost:8080/api/v1/campos \
  -H "Content-Type: application/json" \
  -d '{"nombre":"LA ESPERANZA","superficieTotalHas":50}'

# Agregar un lote
curl -i -X POST http://localhost:8080/api/v1/campos/1/lotes \
  -H "Content-Type: application/json" \
  -d '{"nroLote":1,"hectareas":40,"latitud":-31.4201,"longitud":-64.1888,"tipoSuelo":"TIPO_2"}'

# RN-CAM-03: repetir el número de lote -> 400
curl -i -X POST http://localhost:8080/api/v1/campos/1/lotes \
  -H "Content-Type: application/json" \
  -d '{"nroLote":1,"hectareas":10,"latitud":-31.42,"longitud":-64.18,"tipoSuelo":"TIPO_2"}'

# RN-CAM-02: superar la superficie total del campo -> 400
curl -i -X POST http://localhost:8080/api/v1/campos/1/lotes \
  -H "Content-Type: application/json" \
  -d '{"nroLote":2,"hectareas":100,"latitud":-31.42,"longitud":-64.18,"tipoSuelo":"TIPO_2"}'

# Listar lotes / campo inexistente (404)
curl -i http://localhost:8080/api/v1/campos/1/lotes
curl -i http://localhost:8080/api/v1/campos/99
```

Las pruebas de `agronomia-service` y `proyectos-service` (RN-TRA-01, 02 y 04; RN-LAB-01 a 05) se agregan a medida que se implementan los servicios.

## Roadmap

- [x] `campos-service`: alta y consulta de campos y lotes (RN-CAM-01, 02 y 03)
- [x] API Gateway con ruteo a `campos-service`
- [x] Refactor de `campos-service` a la estructura `application / domain / infrastructure` y limpieza de modelos
- [ ] Modificación y baja (lógica) de campos y lotes
- [x] `agronomia-service`: migrar el modelo de dominio y exponer CRUD de cultivos, plagas, agroquímicos y tratamientos (RN-TRA-01, 02 y 04)
- [ ] `proyectos-service`: planes de siembra y laboreos (RN-LAB-01 a 04)
- [ ] Integración con Open-Meteo para controlar fumigaciones (RN-LAB-05)
- [ ] Keycloak, Spring Security en el gateway y en cada servicio, con roles
- [ ] `bda-commons` como dependencia Maven de los tres servicios
- [ ] Gateway: reemplazar la ruta preconfigurada de `labores-service` por las rutas definitivas
- [ ] Docker Compose con la solución completa (Keycloak incluido)
- [ ] Tests unitarios del dominio, de controllers (`@WebMvcTest`) y de repositorios (`@DataJpaTest`)
- [ ] **Subdivisión y unión dinámica de lotes** (mejora de una versión futura; al unir lotes habrá que exigir el mismo tipo de suelo)

## Fuera de alcance de este release

- Subdivisión y unión dinámica de lotes.
- Importación de datos de monitores de rendimiento y mapas de rendimiento.
- Gestión de compras de semillas y agroquímicos.

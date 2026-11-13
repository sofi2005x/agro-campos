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
| `campos-service`: modificación y baja lógica de campos y lotes | ✅ Implementado |
| `campos-service`: refactor a capas (`application / domain / infrastructure`) | ✅ Implementado |
| `agronomia-service` | ✅ Implementado |
| Integración con Open-Meteo | ✅ Implementado |
| Keycloak, Spring Security y roles | ✅ Implementado |
| `bda-commons` (JAR reutilizable) | ✅ Implementado |
| `proyectos-service` | ✅ Implementado |

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
- Spring Security (OAuth2 Resource Server) y Keycloak (OIDC + JWT)
- MySQL 8.0
- Springdoc OpenAPI / Swagger UI 2.6.0
- Lombok
- Maven (cada servicio es un proyecto independiente; `bda-commons` es un JAR que los tres usan como dependencia)
- Docker y Docker Compose (build multi-etapa, usuario sin privilegios, healthchecks)

## Estructura del repositorio

```
agro-campos/
├── compose.yml                  # Orquesta MySQL + servicios + gateway (+ Keycloak)
├── .dockerignore                # Excluye target/ y .git del contexto de build (los Dockerfile construyen desde la raíz)
├── postman-docs/
│   └── coleccion-api
│        ├── AgroCampos.local.postman_enviroment.json    # Enviroment para postman
│        └── AgroCampos.postman_collection.json          # Test de endpoints en postman
├── bda-commons/                 # JAR reutilizable
├── gateway/
├── campos-service/              # gestion de campos y lotes
├── agronomia-service/           # catálogo agronómico
└── proyectos-service/           # planes de siembra
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
│   └── models
│       ├── Campo                        Entidad de campo (id, nombre, superficieTotalHas, lotes)
│       ├── Lote                         Entidad de lote (nroLote, hectareas, tipoSuelo, coords)
│       └── TipoSuelo                    Enum con los tipos de suelo (TIPO_1 ... TIPO_5)
└── infrastructure                       Detalles técnicos y persistencia
    ├── repositories
    │   ├── CampoRepository              Spring Data JPA (existsByNombreIgnoreCase)
    │   └── LoteRepository               Spring Data JPA (existsByCampoIdAndNroLote, findByCampoId)
    └── security
        └── KeycloakSecurityConfig       Reglas de acceso por ruta y rol (usa bda-commons)
```

Las excepciones base, `ErrorResponse`, el manejador de errores y el conversor de roles de Keycloak no están en el servicio: vienen de [`bda-commons`](#componente-común-bda-commons).

Qué cambia en los otros dos servicios:

| Servicio | `domain` | `infrastructure` |
|---|---|---|
| `agronomia-service` | `models`: `Cultivo`, `Plaga`, `TipoPlaga`, `Agroquimico`, `TipoAgroquimico`, `Tratamiento`. `exceptions`: las `*NoEncontrad*Exception`, que extienden `RecursoNoEncontradoException` de `bda-commons` | `repositories` de cada agregado, `config` y `security` |
| `proyectos-service` | `models`: `PlanDeSiembra`, `Labor`, `EstadoPlan`, `EstadoLabor` y *value objects* con los datos de lote, cultivo y clima que necesitan las reglas. `ports`: `PlanRepository`, `CamposPort`, `AgronomiaPort`, `ClimaPort`. `services`: `PlanDomainService` | `adapters`: `PlanRepositoryAdapter`, `CamposRestAdapter`, `AgronomiaRestAdapter` y `OpenMeteoAdapter` (con `RestClient`); `repositories`, `config` y `security` |

| Capa | Responsabilidad | Puede usar |
|---|---|---|
| `application` | Exponer la API REST y resolver los casos de uso. Traduce DTOs ↔ dominio y orquesta. | `domain`. En `campos-service` y `agronomia-service` también sus repositorios de Spring Data; en `proyectos-service`, nunca `infrastructure`: pide todo mediante los *ports* |
| `domain` | Modelo del negocio y sus reglas. Es el corazón de la aplicación. | Solo a sí mismo (nunca a `application`) |
| `infrastructure` | Detalles técnicos: persistencia, seguridad, configuración y, en `proyectos-service`, los *adapters* que implementan los ports (JPA, `RestClient`, Open-Meteo). | `domain` (para persistir sus entidades e implementar sus ports) |

Flujo de una petición: `Controller → Service (application) → entidades / DomainService / ports (domain)`. En `proyectos-service`, en ejecución, Spring inyecta en cada port su adapter de `infrastructure`. En `campos-service` y `agronomia-service` el servicio usa directamente el repositorio.

### Cómo se reparten las reglas entre capas

El dominio no accede a la base ni a otros servicios. Cuando una regla necesita información externa, el servicio de aplicación **obtiene el dato** y el dominio **toma la decisión**. Por ejemplo, para RN-CAM-01 el servicio de aplicación consulta `existsByNombreIgnoreCase(...)` al repositorio y le pasa el resultado a `CampoDomainService`, que decide si lanza `ReglaNegocioException`. Ni el controller ni el servicio de aplicación deciden reglas: solo orquestan.

En `proyectos-service` esto se lleva más lejos con **ports y adapters**: todo lo que sale del servicio (base de datos, `campos-service`, `agronomia-service` y Open-Meteo) se pide mediante una interfaz del dominio y la implementa un adapter en `infrastructure`. Los adapters devuelven *value objects* del dominio (`LoteInfo`, `CultivoInfo`...), así el servicio de aplicación nunca ve los JSON de otros servicios ni conoce `RestClient`. Cambiar Open-Meteo por otro proveedor solo requiere un nuevo adapter de `ClimaPort`, y los casos de uso se prueban con implementaciones en memoria de los ports (`PlanDeSiembraServiceTest`), sin base de datos ni red.

Las reglas de unicidad de `campos-service` y `agronomia-service` se respaldan además con restricciones en la base de datos, como defensa ante peticiones concurrentes. RN-LAB-02 no tiene hoy esa defensa: dos `POST /planes` simultáneos sobre el mismo lote podrían crear dos planes activos.

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
| **RN-TRA-01** Unicidad de producto | No puede registrarse dos veces el mismo agroquímico comercial. | `AgroquimicoDomainService` (con la consulta del repositorio) | ✅ |
| **RN-TRA-02** Dosis válida | La dosis por hectárea debe ser estrictamente positiva (> 0). | `Tratamiento.crear` | ✅ |
| **RN-TRA-04** Coherencia de plaga y químico | `INSECTO` → `INSECTICIDA`, `HONGO` → `FUNGICIDA`, `MALEZA` → `HERBICIDA`. Un `FERTILIZANTE` no combate ninguna plaga. | `TipoPlaga.admite(TipoAgroquimico)`, invocado desde `Tratamiento.crear` | ✅ |

Este servicio también es el dueño de los datos que otros servicios usan para validar: los `suelosAptos` y la secuencia de laboreos de cada cultivo, y los límites de viento y lluvia de cada agroquímico.

### `proyectos-service` — Planes de siembra y laboreos

| Entidad | Descripción |
|---|---|
| `PlanDeSiembra` | Un cultivo sembrado en un lote. Estados: `PLANIFICADO`, `EN_CURSO`, `FINALIZADO` y `CANCELADO`. |
| `Labor` | Cada laboreo del plan, en el orden que define el cultivo. Estados: `PENDIENTE`, `EN_EJECUCION` y `COMPLETADA`. |

Un plan está **activo** cuando su estado es `PLANIFICADO` o `EN_CURSO`. Al crearlo, las labores se copian de la secuencia del cultivo: los cambios posteriores en el catálogo no afectan a planes existentes.

| Regla | Descripción | Dónde se aplica | Estado |
|---|---|---|---|
| **RN-LAB-01** Aptitud de suelo | Un cultivo solo puede sembrarse en un lote cuyo tipo de suelo esté en la lista de `suelosAptos` del cultivo. | `PlanDomainService`. El suelo viene de `campos-service` y la lista de `agronomia-service`. | ✅ |
| **RN-LAB-02** Exclusividad del lote | Un lote solo puede tener un único plan activo a la vez (`PLANIFICADO` o `EN_CURSO`). No se pueden solapar cultivos. | `PlanDomainService` (con la consulta del repositorio) | ✅ |
| **RN-LAB-03** Secuencia estricta de labores | Los laboreos se ejecutan en el orden definido por el cultivo: una labor solo puede iniciarse si la anterior está `COMPLETADA`. | `PlanDeSiembra.iniciarLabor` | ✅ |
| **RN-LAB-04** Cierre de campaña con cosecha | El plan pasa a `FINALIZADO` solo cuando la labor de `COSECHA` se marca como completada, y el lote queda libre para la próxima siembra. | `PlanDeSiembra.completarLabor` | ✅ |
| **RN-LAB-05** Condiciones climáticas para fumigar | No se puede iniciar una labor de `FUMIGACION` si el pronóstico (Open-Meteo) para las coordenadas del lote supera el viento máximo del agroquímico o prevé lluvia dentro de sus horas libres requeridas. | `PlanDomainService`. El pronóstico lo trae `ClimaClient`. | ✅ |

Notas sobre estas reglas:

- **Liberación del lote (RN-LAB-04)**: no requiere ninguna escritura en `campos-service`. El lote queda libre porque su plan deja de estar activo (`FINALIZADO` o `CANCELADO`), y RN-LAB-02 lo permite.
- **RN-LAB-05**: los umbrales no son constantes del servicio. Salen del `Agroquimico` del tratamiento aplicado (`vientoMaximoKmh` y `horasLibresLluvia`), por lo que la labor de fumigación debe indicar el `tratamientoId` al iniciarse.
- **Qué se evalúa en RN-LAB-05**: el pronóstico horario es la ventana que va desde la hora actual hasta `horasLibresLluvia` horas después. El **viento** se compara con el máximo del agroquímico en la primera hora (la de la aplicación). La **lluvia** se busca en toda la ventana y bloquea si alguna hora prevé 0,1 mm o más. El tratamiento debe corresponder al cultivo del plan.
- **Si Open-Meteo no responde** (o el lote no tiene coordenadas): se permite iniciar la fumigación y la respuesta incluye una advertencia indicando que no se pudo validar el clima. El cliente tiene timeout corto y el incidente queda registrado en el log.
- **Tipo de suelo**: el catálogo de suelos (`TIPO_1` a `TIPO_5`) tiene como dueño a `agronomia-service`. `proyectos-service` no define su propio enum: recibe el tipo de suelo del lote y los `suelosAptos` del cultivo como códigos de texto y los compara. Si el catálogo cambia, este servicio no se toca.
- **Cómo se reconoce una fumigación o una cosecha**: `agronomia-service` guarda las labores de un cultivo como texto libre, por eso `TipoLabor` las deduce del nombre (ignora mayúsculas y tildes): un nombre que contenga `fumig` es una fumigación y uno que contenga `cosech` es la cosecha. Un cultivo sin labor de cosecha nunca podría finalizar sus planes.
- **Plan sin labores**: no se puede crear un plan para un cultivo sin laboreos definidos.

---

## Contrato de la API

Base (por el gateway): `http://localhost:8080`. Directo a cada servicio, usar su puerto.

### `campos-service` — `/api/v1/campos`

| Método | URI | Body | Éxito | Errores | Estado |
|---|---|---|---|---|---|
| `GET` | `/api/v1/campos` | — | `200` + lista | — | ✅ |
| `GET` | `/api/v1/campos/{id}` | — | `200` + campo | `404` | ✅ |
| `POST` | `/api/v1/campos` | `CampoRequestDTO` | `201` + campo + header `Location` | `400` (validación, RN-CAM-01) | ✅ |
| `PUT` | `/api/v1/campos/{id}` | `CampoRequestDTO` | `200` + campo | `400`, `404` | ✅ |
| `DELETE` | `/api/v1/campos/{id}` | — | `204` | `404` | ✅ |
| `GET` | `/api/v1/campos/{id}/lotes` | — | `200` + lista | `404` | ✅ |
| `GET` | `/api/v1/campos/{id}/lotes/{loteId}` | — | `200` + lote | `404` | ✅ |
| `POST` | `/api/v1/campos/{id}/lotes` | `LoteRequestDTO` | `201` + lote | `400` (RN-CAM-02, RN-CAM-03), `404` | ✅ |
| `PUT` | `/api/v1/campos/{id}/lotes/{loteId}` | `LoteRequestDTO` | `200` + lote | `400`, `404` | ✅ |
| `PATCH` | `/api/v1/campos/{id}/lotes/{loteId}` | `LotePatchRequest` | `200` + lote | `400`, `404` | 🔜 |
| `DELETE` | `/api/v1/campos/{id}/lotes/{loteId}` | — | `204` | `404` | ✅ |

`DELETE` es una **baja lógica** (el registro queda inactivo). Así los planes históricos de `proyectos-service` siguen referenciando un lote válido, sin que `campos-service` tenga que consultar a otros servicios. Un lote dado de baja no admite nuevos planes.

### `agronomia-service` — `/api/v1/agronomia`

| Método | URI | Body | Éxito | Errores |
|---|---|---|---|---|
| `GET` / `POST` | `/cultivos` | `CultivoRequest` | `200` lista / `201` | `400` |
| `GET` | `/cultivos/{id}` | — | `200` (incluye `suelosAptos` y `laboreos` ordenados) | `404` |
| `GET` / `POST` | `/plagas` | `PlagaRequest` | `200` lista / `201` | `400` |
| `GET` / `POST` | `/agroquimicos` | `AgroquimicoRequest` | `200` lista / `201` | `400` (RN-TRA-01) |
| `GET` | `/agroquimicos/{id}` | — | `200` | `404` |
| `GET` / `POST` | `/tratamientos` | `TratamientoRequest` | `200` lista (filtros `?plagaId=` y `?cultivoId=`) / `201` | `400` (RN-TRA-02, RN-TRA-04), `404` |
| `GET` | `/tratamientos/{id}` | — | `200` (incluye el agroquímico con sus límites climáticos) | `404` |

### `proyectos-service` — `/api/v1/proyectos`

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

**Iniciar una fumigación**

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

// 401: falta el token o no es válido
{ "status": 401, "error": "Unauthorized", "mensaje": "Autenticación requerida: falta el token o no es válido", "timestamp": "2026-10-26T10:20:00" }

// 403: el rol no alcanza para la operación
{ "status": 403, "error": "Forbidden", "mensaje": "No tenés permisos para realizar esta operación", "timestamp": "2026-10-26T10:20:00" }
```

El formato y el manejo de errores son los mismos en los tres servicios porque vienen de [`bda-commons`](#componente-común-bda-commons). `ApiExceptionHandler` (se registra solo en cada servicio) traduce las excepciones a `ErrorResponse`: `RecursoNoEncontradoException` → `404`, `ReglaNegocioException` → `400`, `ServicioNoDisponibleException` → `503`, y los errores de validación, cuerpo ilegible o parámetros inválidos → `400`. Los `401` y `403` los responde `SecurityErrorHandler`, también en JSON. Cada servicio define sus excepciones propias extendiendo las base (por ejemplo, `CultivoNoEncontradoException extends RecursoNoEncontradoException`).

## Seguridad 

- **Keycloak** como Identity Provider, con OIDC y tokens JWT. Realm `bda-realm` y roles `ADMIN`, `AGRONOMO` y `OPERARIO`.
- El **gateway** valida el JWT (Security Filter) antes de rutear. Cada microservicio **vuelve a validarlo** como Resource Server y autoriza por rol. Solo el gateway se publica hacia afuera; los servicios quedan en la red interna de Docker (zona de confianza).
- El conversor de roles de Keycloak (claim `realm_access`), los errores 401 y 403 en JSON y la configuración común del resource server vienen de `bda-commons`. Cada servicio conserva solo sus reglas de acceso por ruta y rol, en su `KeycloakSecurityConfig`.

Roles:

| Operación | ADMIN | AGRONOMO | OPERARIO |
|---|:-:|:-:|:-:|
| Consultar campos, catálogo y planes | ✅ | ✅ | ✅ |
| Alta, modificación y baja de campos y lotes | ✅ | ❌ | ❌ |
| Administrar cultivos, plagas, agroquímicos y tratamientos | ✅ | ✅ | ❌ |
| Crear y cancelar planes de siembra | ✅ | ✅ | ❌ |
| Iniciar y completar labores | ✅ | ✅ | ✅ |

## Integración con Open-Meteo

[Open-Meteo](https://open-meteo.com) es una API pública de pronóstico que no requiere clave. `OpenMeteoAdapter` (en `proyectos-service`, implementa `ClimaPort`) consulta el pronóstico horario de viento y precipitación para las coordenadas del lote y lo compara con los límites del agroquímico (RN-LAB-05).

| Propiedad | Descripción |
|---|---|
| `clima.open-meteo.base-url` | URL base de la API |
| `clima.open-meteo.timeout-ms` | Timeout de la consulta |

## Componente común: `bda-commons`

JAR Maven independiente, usado como dependencia por los tres microservicios. Reúne el código que estaba repetido idéntico en cada servicio.

| Paquete | Contenido |
|---|---|
| `errors` | `ErrorResponse` (formato único de error), excepciones base (`ReglaNegocioException`, `RecursoNoEncontradoException`, `ServicioNoDisponibleException`) y `ApiExceptionHandler` |
| `security` | `KeycloakRoleConverter` (roles del claim `realm_access`), `SecurityErrorHandler` (401 y 403 en JSON) y `KeycloakSecuritySupport` (configuración común del resource server) |
| `autoconfigure` | Registra el handler de errores y `SecurityErrorHandler` como beans: los servicios no necesitan `@ComponentScan` ni `@Import` |

**Queda fuera a propósito**: las reglas de acceso por ruta y rol (dependen de los endpoints de cada servicio), las entidades y reglas de dominio (acoplarían los servicios entre sí) y los DTOs de las APIs. Sus dependencias de Web y Security son `optional`: usar `bda-commons` no activa Spring Security en un servicio que no la tenga.

Se agrega a cada servicio con:

```xml
<dependency>
    <groupId>clase.spring</groupId>
    <artifactId>bda-commons</artifactId>
    <version>0.0.1-SNAPSHOT</version>
</dependency>
```

Instalación en el repositorio Maven local, una vez y cada vez que cambie el módulo:

```bash
cd bda-commons
mvn clean install
```

En Docker no hace falta instalarlo a mano: los `Dockerfile` se construyen desde la raíz del repo y compilan `bda-commons` antes de cada servicio.

## Cómo ejecutarlo

### Opción 1: Docker Compose (recomendada)

Requisitos: Docker y Docker Compose.

```bash
docker compose -f compose.yml up --build
```

Se levantan en orden MySQL, los servicios (esperan a que MySQL esté sano) y el gateway (espera a que los servicios estén sanos). La primera vez tarda porque Maven descarga las dependencias. Los `Dockerfile` de los tres servicios usan la raíz del repo como contexto de build (`context: .` en `compose.yml`), porque necesitan compilar `bda-commons` primero.

Para bajar todo (agregá `-v` si querés borrar también los datos de la base):

```bash
docker compose -f compose.yml down
```

### Opción 2: Local, sin Docker

Requisitos: JDK 21, Maven y un MySQL 8 en `localhost:3306`. Cada servicio crea su base si no existe (`createDatabaseIfNotExist=true`).

```bash
# Una vez, y cada vez que cambie bda-commons
cd bda-commons
mvn clean install

# Terminal 1
cd campos-service
mvn spring-boot:run

# Terminal 2 (opcional, si querés pasar por el gateway)
cd gateway
mvn spring-boot:run
```

> Si se omite el primer paso, `mvn spring-boot:run` falla con `Could not find artifact clase.spring:bda-commons`.

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

Con todo levantado (`docker compose up --build`), todas las llamadas pasan por el gateway (`http://localhost:8080`) y llevan un token de Keycloak. La carpeta `postman-docs/coleccion-api/` tiene la colección de Postman que automatiza este mismo recorrido (se corre carpeta por carpeta, en orden, con el *Collection Runner*). Lo que sigue es la versión para terminal (bash: Git Bash, WSL, Linux o macOS).

**Corré las secciones en orden:** cada una usa variables (`$CAMPO`, `$SOJA`, `$PLAN`...) creadas por las anteriores. Los nombres de campos, cultivos, plagas y agroquímicos son únicos: si repetís el recorrido, recreá la base con `docker compose down -v` o cambiá los nombres.

### Preparación

```bash
GW=http://localhost:8080

# Token de admin (dura pocos minutos: si más adelante aparece un 401, repetí esta línea)
TOKEN=$(curl -s -X POST http://localhost:8081/realms/bda-realm/protocol/openid-connect/token \
  -d "grant_type=password" -d "client_id=agro-campos-client" \
  -d "username=admin1" -d "password=admin1" | sed -E 's/.*"access_token":"([^"]+)".*/\1/')

# Llamada con token: muestra el cuerpo de la respuesta y el código HTTP
api() { curl -s -w "\n→ HTTP %{http_code}\n" -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" "$@"; }

# Extrae los "id" de una respuesta JSON, en orden de aparición
ids() { grep -o '"id":[0-9]*' | cut -d: -f2; }
```

En los cuerpos JSON, los IDs se intercalan fuera de las comillas simples (`'$CAMPO'`) para que el shell los reemplace. Si una variable queda vacía, la creación falló: repetí ese comando sin la parte `| ids | head -1` para ver el error.

### `campos-service` (RN-CAM-01, 02 y 03)

```bash
# Crear un campo
CAMPO=$(api -X POST $GW/api/v1/campos -d '{"nombre":"La Esperanza","superficieTotalHas":120.5}' | ids | head -1)

# RN-CAM-01: mismo nombre con otras mayúsculas -> 400 "Ya existe un campo con el nombre..."
api -X POST $GW/api/v1/campos -d '{"nombre":"LA ESPERANZA","superficieTotalHas":50}'

# Validación: nombre vacío y superficie 0 -> 400 con "errores" (nombre, superficieTotalHas)
api -X POST $GW/api/v1/campos -d '{"nombre":"","superficieTotalHas":0}'

# Agregar un lote (con coordenadas, suelo TIPO_2, 40 ha)
LOTE=$(api -X POST $GW/api/v1/campos/$CAMPO/lotes \
  -d '{"nroLote":1,"hectareas":40,"latitud":-31.4201,"longitud":-64.1888,"tipoSuelo":"TIPO_2"}' | ids | head -1)

# RN-CAM-03: repetir el número de lote -> 400
api -X POST $GW/api/v1/campos/$CAMPO/lotes \
  -d '{"nroLote":1,"hectareas":10,"latitud":-31.42,"longitud":-64.18,"tipoSuelo":"TIPO_2"}'

# RN-CAM-02: superar la superficie total del campo (40 + 100 > 120,5) -> 400
api -X POST $GW/api/v1/campos/$CAMPO/lotes \
  -d '{"nroLote":2,"hectareas":100,"latitud":-31.42,"longitud":-64.18,"tipoSuelo":"TIPO_2"}'

# Consultas y 404
api $GW/api/v1/campos/$CAMPO/lotes
api $GW/api/v1/campos/999999
```

### `agronomia-service` (RN-TRA-01, 02 y 04)

**Datos de prueba**

```bash
AGR=$GW/api/v1/agronomia

# Cultivos: Soja (suelos TIPO_1 y TIPO_2) y Maíz (TIPO_2 y TIPO_3), con la secuencia de labores
SOJA=$(api -X POST $AGR/cultivos \
  -d '{"nombre":"Soja","suelosAptos":["TIPO_1","TIPO_2"],"labores":["Arar","Sembrar","Fumigar","Cosechar"]}' | ids | head -1)
MAIZ=$(api -X POST $AGR/cultivos \
  -d '{"nombre":"Maiz","suelosAptos":["TIPO_2","TIPO_3"],"labores":["Arar","Sembrar","Fumigar","Cosechar"]}' | ids | head -1)

# Plaga de tipo INSECTO
PLAGA=$(api -X POST $AGR/plagas \
  -d '{"nombre":"Oruga","descripcion":"Larva defoliadora","tipoPlaga":"INSECTO"}' | ids | head -1)

# Agroquímicos: dos insecticidas (uno permisivo con viento máximo 999 km/h y otro restrictivo con 0,01 km/h),
# un fertilizante y un fungicida
AGRO_PERM=$(api -X POST $AGR/agroquimicos \
  -d '{"nombreComercial":"Insecticida Permisivo","principioActivo":"Clorantraniliprol","tipo":"INSECTICIDA","vientoMaximoPermitidoKmH":999.0,"horasLibresDeLluviaRequeridas":1}' | ids | head -1)
AGRO_REST=$(api -X POST $AGR/agroquimicos \
  -d '{"nombreComercial":"Insecticida Restrictivo","principioActivo":"Prueba","tipo":"INSECTICIDA","vientoMaximoPermitidoKmH":0.01,"horasLibresDeLluviaRequeridas":1}' | ids | head -1)
FERTILIZANTE=$(api -X POST $AGR/agroquimicos \
  -d '{"nombreComercial":"Fertilizante NPK","principioActivo":"NPK","tipo":"FERTILIZANTE","vientoMaximoPermitidoKmH":15.0,"horasLibresDeLluviaRequeridas":4}' | ids | head -1)
FUNGICIDA=$(api -X POST $AGR/agroquimicos \
  -d '{"nombreComercial":"Fungicida Azoxistrobina","principioActivo":"Azoxistrobina","tipo":"FUNGICIDA","vientoMaximoPermitidoKmH":15.0,"horasLibresDeLluviaRequeridas":4}' | ids | head -1)

# Tratamientos: Soja + permisivo, Soja + restrictivo y Maíz + permisivo (el de Maíz sirve para probar RN-LAB-05)
TRAT_SOJA=$(api -X POST $AGR/tratamientos \
  -d '{"plagaId":'$PLAGA',"agroquimicoId":'$AGRO_PERM',"cultivoId":'$SOJA',"dosisPorHectarea":0.5,"unidadMedida":"Litros/ha"}' | ids | head -1)
TRAT_REST=$(api -X POST $AGR/tratamientos \
  -d '{"plagaId":'$PLAGA',"agroquimicoId":'$AGRO_REST',"cultivoId":'$SOJA',"dosisPorHectarea":0.5,"unidadMedida":"Litros/ha"}' | ids | head -1)
TRAT_MAIZ=$(api -X POST $AGR/tratamientos \
  -d '{"plagaId":'$PLAGA',"agroquimicoId":'$AGRO_PERM',"cultivoId":'$MAIZ',"dosisPorHectarea":0.5,"unidadMedida":"Litros/ha"}' | ids | head -1)
```

**Consultas** (todas devuelven 200)

```bash
api $AGR/cultivos/$SOJA                                     # incluye suelosAptos y labores en orden
api $AGR/tratamientos                                       # listado
api "$AGR/tratamientos?plagaId=$PLAGA&cultivoId=$SOJA"      # filtrado: dos tratamientos de Soja
api $AGR/tratamientos/$TRAT_SOJA                            # incluye el agroquímico con su límite de viento y de lluvia
```

**Reglas de negocio y errores**

```bash
# RN-TRA-01: agroquímico con nombre comercial repetido -> 400 "Ya existe un agroquímico..."
api -X POST $AGR/agroquimicos \
  -d '{"nombreComercial":"Insecticida Permisivo","principioActivo":"Otro","tipo":"INSECTICIDA","vientoMaximoPermitidoKmH":20.0,"horasLibresDeLluviaRequeridas":2}'

# RN-TRA-02: dosis 0 -> 400 (el error menciona "dosis")
api -X POST $AGR/tratamientos \
  -d '{"plagaId":'$PLAGA',"agroquimicoId":'$AGRO_PERM',"cultivoId":'$SOJA',"dosisPorHectarea":0,"unidadMedida":"Litros/ha"}'

# RN-TRA-04: un fertilizante no combate plagas -> 400 (el mensaje menciona "fertilizante")
api -X POST $AGR/tratamientos \
  -d '{"plagaId":'$PLAGA',"agroquimicoId":'$FERTILIZANTE',"cultivoId":'$SOJA',"dosisPorHectarea":1.0,"unidadMedida":"Kg/ha"}'

# RN-TRA-04: plaga INSECTO con un fungicida -> 400 (el mensaje exige INSECTICIDA)
api -X POST $AGR/tratamientos \
  -d '{"plagaId":'$PLAGA',"agroquimicoId":'$FUNGICIDA',"cultivoId":'$SOJA',"dosisPorHectarea":1.0,"unidadMedida":"Kg/ha"}'

# 404: tratamiento con una plaga inexistente -> 404 "Plaga no encontrada..."
api -X POST $AGR/tratamientos \
  -d '{"plagaId":999999,"agroquimicoId":'$AGRO_PERM',"cultivoId":'$SOJA',"dosisPorHectarea":1.0,"unidadMedida":"Litros/ha"}'

# Validaciones -> 400 con "errores"
api -X POST $AGR/cultivos -d '{"nombre":"Sin suelo","suelosAptos":[],"labores":["Arar"]}'          # suelosAptos
api -X POST $AGR/plagas -d '{"nombre":"Sin tipo","descripcion":"x"}'                              # tipoPlaga
api -X POST $AGR/tratamientos -d '{"dosisPorHectarea":1.0,"unidadMedida":"Litros/ha"}'            # plagaId, agroquimicoId, cultivoId

# 404: cultivo y tratamiento inexistentes
api $AGR/cultivos/999999
api $AGR/tratamientos/999999
```

### `proyectos-service` (RN-LAB-01 a 05)

**Datos de prueba**: faltan dos lotes en el campo (uno sin coordenadas y otro con un suelo que la Soja no tolera) y una fecha de siembra.

```bash
PRO=$GW/api/v1/proyectos

# Lote sin coordenadas (TIPO_2, 20 ha): permite fumigar sin depender de Open-Meteo
LOTE_SC=$(api -X POST $GW/api/v1/campos/$CAMPO/lotes \
  -d '{"nroLote":2,"hectareas":20,"tipoSuelo":"TIPO_2"}' | ids | head -1)
# Lote de suelo TIPO_5 (no apto para Soja)
LOTE_NA=$(api -X POST $GW/api/v1/campos/$CAMPO/lotes \
  -d '{"nroLote":3,"hectareas":10,"latitud":-31.43,"longitud":-64.19,"tipoSuelo":"TIPO_5"}' | ids | head -1)

FECHA=$(date -d "+7 days" +%F)      # en macOS: date -v+7d +%F

# Atajo para cambiar el estado de una labor: labor <plan> <labor> '<json>'
labor() { api -X PATCH $PRO/planes/$1/labores/$2 -d "$3"; }
```

**Crear un plan (RN-LAB-01 y RN-LAB-02)**

```bash
# Plan de Soja en el lote sin coordenadas -> 201, estado PLANIFICADO, 4 labores en el orden del cultivo.
# La respuesta trae el id del plan y el de cada labor (en orden): se guardan para los pasos siguientes.
IDS=($(api -X POST $PRO/planes \
  -d '{"campoId":'$CAMPO',"loteId":'$LOTE_SC',"cultivoId":'$SOJA',"fechaSiembraPrevista":"'$FECHA'"}' | ids))
PLAN=${IDS[0]}; ARAR=${IDS[1]}; SEMBRAR=${IDS[2]}; FUMIGAR=${IDS[3]}; COSECHAR=${IDS[4]}
echo "plan=$PLAN labores: $ARAR $SEMBRAR $FUMIGAR $COSECHAR"

# RN-LAB-02: un segundo plan activo en el mismo lote -> 400 "...ya tiene un plan activo..."
api -X POST $PRO/planes \
  -d '{"campoId":'$CAMPO',"loteId":'$LOTE_SC',"cultivoId":'$SOJA',"fechaSiembraPrevista":"'$FECHA'"}'

# RN-LAB-01: Soja en un lote TIPO_5 -> 400 "...no es apto..."
api -X POST $PRO/planes \
  -d '{"campoId":'$CAMPO',"loteId":'$LOTE_NA',"cultivoId":'$SOJA',"fechaSiembraPrevista":"'$FECHA'"}'
```

**Secuencia de labores (RN-LAB-03)**

```bash
# Fumigar antes de terminar las anteriores -> 400 "...primero debe completarse..."
labor $PLAN $FUMIGAR '{"estado":"EN_EJECUCION","tratamientoId":'$TRAT_SOJA'}'

labor $PLAN $ARAR '{"estado":"EN_EJECUCION"}'            # 200; el plan pasa a EN_CURSO
labor $PLAN $ARAR '{"estado":"PENDIENTE"}'               # 400 "...no puede volver a PENDIENTE"
labor $PLAN $ARAR '{"estado":"COMPLETADA"}'              # 200
labor $PLAN $SEMBRAR '{"estado":"COMPLETADA"}'           # 400 (debe estar EN_EJECUCION antes de completarse)
labor $PLAN $SEMBRAR '{"estado":"EN_EJECUCION"}'         # 200
labor $PLAN $SEMBRAR '{"estado":"COMPLETADA"}'           # 200
```

**Fumigación (RN-LAB-05, sin depender de Open-Meteo)**

```bash
# Sin tratamiento -> 400 "...debe indicar el tratamientoId"
labor $PLAN $FUMIGAR '{"estado":"EN_EJECUCION"}'

# Tratamiento de otro cultivo (Maíz) -> 400 "...no corresponde al cultivo del plan"
labor $PLAN $FUMIGAR '{"estado":"EN_EJECUCION","tratamientoId":'$TRAT_MAIZ'}'

# Tratamiento correcto en un lote sin coordenadas -> 200: la labor queda EN_EJECUCION con su tratamientoId
# y la respuesta incluye "advertencias": no se pudo validar el clima porque el lote no tiene coordenadas
labor $PLAN $FUMIGAR '{"estado":"EN_EJECUCION","tratamientoId":'$TRAT_SOJA'}'
labor $PLAN $FUMIGAR '{"estado":"COMPLETADA"}'           # 200
```

**Cierre con cosecha (RN-LAB-04)**

```bash
labor $PLAN $COSECHAR '{"estado":"EN_EJECUCION"}'        # 200
labor $PLAN $COSECHAR '{"estado":"COMPLETADA"}'          # 200: el plan queda FINALIZADO y el lote libre

# Un plan FINALIZADO no se puede cancelar ni admite más cambios -> 400 en los dos casos
api -X PATCH $PRO/planes/$PLAN -d '{"estado":"CANCELADO"}'
labor $PLAN $ARAR '{"estado":"EN_EJECUCION"}'
```

**Consultas**

```bash
api $PRO/planes/$PLAN                         # 200: plan FINALIZADO con sus 4 labores COMPLETADA
api $PRO/planes                               # 200: todos los planes
api "$PRO/planes?estado=FINALIZADO"           # 200: solo los finalizados
api "$PRO/planes?estado=XYZ"                  # 400: estado inválido
api $PRO/lotes/$LOTE_SC/planes                # 200: histórico del lote, del más reciente al más antiguo
```

**Lote libre tras finalizar o cancelar (RN-LAB-02)**

```bash
# El lote quedó libre al finalizar el plan -> 201, estado PLANIFICADO
PLAN2=$(api -X POST $PRO/planes \
  -d '{"campoId":'$CAMPO',"loteId":'$LOTE_SC',"cultivoId":'$SOJA',"fechaSiembraPrevista":"'$FECHA'"}' | ids | head -1)

# Un plan activo solo admite pasar a CANCELADO -> 400 (el mensaje menciona CANCELADO)
api -X PATCH $PRO/planes/$PLAN2 -d '{"estado":"FINALIZADO"}'

# Cancelar -> 200, y el lote vuelve a quedar libre
api -X PATCH $PRO/planes/$PLAN2 -d '{"estado":"CANCELADO"}'
PLAN3=$(api -X POST $PRO/planes \
  -d '{"campoId":'$CAMPO',"loteId":'$LOTE_SC',"cultivoId":'$SOJA',"fechaSiembraPrevista":"'$FECHA'"}' | ids | head -1)
api -X PATCH $PRO/planes/$PLAN3 -d '{"estado":"CANCELADO"}'
```

**Validaciones y 404**

```bash
# Fecha pasada -> 400 con "errores" (fechaSiembraPrevista)
api -X POST $PRO/planes \
  -d '{"campoId":'$CAMPO',"loteId":'$LOTE_SC',"cultivoId":'$SOJA',"fechaSiembraPrevista":"2020-01-01"}'

# Lote inexistente -> 404 "Lote no encontrado..."
api -X POST $PRO/planes \
  -d '{"campoId":'$CAMPO',"loteId":999999,"cultivoId":'$SOJA',"fechaSiembraPrevista":"'$FECHA'"}'

# Cultivo inexistente -> 404 "Cultivo no encontrado..."
api -X POST $PRO/planes \
  -d '{"campoId":'$CAMPO',"loteId":'$LOTE_SC',"cultivoId":999999,"fechaSiembraPrevista":"'$FECHA'"}'
```

**Integración con Open-Meteo (RN-LAB-05, necesita internet)**

Se usa el lote con coordenadas. El insecticida restrictivo tiene un viento máximo de 0,01 km/h, así que casi siempre se supera.

```bash
IDS=($(api -X POST $PRO/planes \
  -d '{"campoId":'$CAMPO',"loteId":'$LOTE',"cultivoId":'$SOJA',"fechaSiembraPrevista":"'$FECHA'"}' | ids))
PLAN_C=${IDS[0]}; ARAR_C=${IDS[1]}; SEMBRAR_C=${IDS[2]}; FUMIGAR_C=${IDS[3]}

labor $PLAN_C $ARAR_C '{"estado":"EN_EJECUCION"}';    labor $PLAN_C $ARAR_C '{"estado":"COMPLETADA"}'
labor $PLAN_C $SEMBRAR_C '{"estado":"EN_EJECUCION"}'; labor $PLAN_C $SEMBRAR_C '{"estado":"COMPLETADA"}'

# Viento previsto por encima del máximo -> 400 "No se puede fumigar: viento previsto de X km/h..."
# (si en ese momento el viento previsto fuera 0 km/h, no bloquea: repetí en otro momento)
labor $PLAN_C $FUMIGAR_C '{"estado":"EN_EJECUCION","tratamientoId":'$TRAT_REST'}'

# Insecticida permisivo (999 km/h, 1 hora sin lluvia) -> 200 sin advertencias.
# Si se prevé lluvia en la próxima hora responde 400 con el mensaje de lluvia, que también es un resultado válido.
labor $PLAN_C $FUMIGAR_C '{"estado":"EN_EJECUCION","tratamientoId":'$TRAT_SOJA'}'

api -X PATCH $PRO/planes/$PLAN_C -d '{"estado":"CANCELADO"}'
```

Para probar la caída de Open-Meteo, levantá `proyectos-service` con `clima.open-meteo.base-url` apuntando a una URL inválida y repetí la fumigación con el tratamiento correcto: responde 200 con `advertencias` ("No se pudo validar el clima...").

**El plan conserva la secuencia original**

```bash
# Agregar una labor al cultivo en el catálogo -> 200, 5 labores
api -X PUT $AGR/cultivos/$SOJA \
  -d '{"nombre":"Soja","suelosAptos":["TIPO_1","TIPO_2"],"labores":["Arar","Sembrar","Fumigar","Cosechar","Rastrillar"]}'

# El plan creado antes sigue con sus 4 labores: se copian al crear el plan
api $PRO/planes/$PLAN
```

### Qué regla cubre cada prueba

| Regla | Prueba | Resultado esperado |
|---|---|---|
| RN-CAM-01 | Campo con el mismo nombre en mayúsculas | 400 |
| RN-CAM-02 | Lote que supera la superficie del campo | 400 |
| RN-CAM-03 | Número de lote repetido en el campo | 400 |
| RN-TRA-01 | Agroquímico con nombre comercial repetido | 400 |
| RN-TRA-02 | Tratamiento con dosis 0 | 400 |
| RN-TRA-04 | Fertilizante o fungicida contra una plaga INSECTO | 400 |
| RN-LAB-01 | Soja en un lote TIPO_5 | 400 |
| RN-LAB-02 | Segundo plan activo en el mismo lote / plan nuevo tras finalizar o cancelar | 400 / 201 |
| RN-LAB-03 | Iniciar Fumigar sin completar las anteriores | 400 |
| RN-LAB-04 | Completar Cosechar | plan FINALIZADO, lote libre |
| RN-LAB-05 | Sin tratamiento / de otro cultivo / lote sin coordenadas / viento sobre el máximo | 400 / 400 / 200 con advertencias / 400 |

## Roadmap

- [x] `campos-service`: alta y consulta de campos y lotes (RN-CAM-01, 02 y 03)
- [x] API Gateway con ruteo a `campos-service`
- [x] Refactor de `campos-service` a la estructura `application / domain / infrastructure` y limpieza de modelos
- [x] Modificación y baja (lógica) de campos y lotes
- [x] `agronomia-service`: migrar el modelo de dominio y exponer CRUD de cultivos, plagas, agroquímicos y tratamientos (RN-TRA-01, 02 y 04)
- [x] `proyectos-service`: planes de siembra y laboreos (RN-LAB-01 a 04)
- [x] Integración con Open-Meteo para controlar fumigaciones (RN-LAB-05)
- [x] Keycloak, Spring Security en el gateway y en cada servicio, con roles
- [x] `bda-commons` como dependencia Maven de los tres servicios (errores unificados y seguridad Keycloak)
- [x] Gateway: reemplazar la ruta preconfigurada de `labores-service` por las rutas definitivas
- [x] Docker Compose con la solución completa (Keycloak incluido)
- [ ] Tests de controllers (`@WebMvcTest`), de repositorios (`@DataJpaTest`) y de los adapters HTTP (`MockRestServiceServer`). Los tests del dominio y de los casos de uso de `proyectos-service` ya existen
- [ ] **Subdivisión y unión dinámica de lotes** (mejora de una versión futura; al unir lotes habrá que exigir el mismo tipo de suelo)

## Fuera de alcance de este release

- Subdivisión y unión dinámica de lotes.
- Importación de datos de monitores de rendimiento y mapas de rendimiento.
- Gestión de compras de semillas y agroquímicos.

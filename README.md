# 🌾 Agro Campos

Backend de microservicios para la **gestión de campos agrícolas y sus lotes**, desarrollado con Spring Boot como trabajo práctico de la materia de Backend de aplicaciones. Hoy el sistema permite administrar campos y lotes; el modelo de dominio ya incluye las entidades necesarias para ampliarlo a cultivos, plagas, agroquímicos y tratamientos.

## Arquitectura

```
                    ┌──────────────┐        ┌──────────────────┐        ┌───────────┐
   Cliente  ──────► │ API Gateway  │ ─────► │  campos-service  │ ─────► │  MySQL 8  │
  (Postman,         │   :8080      │        │      :8082       │        │   :3306   │
   Swagger, etc.)   └──────┬───────┘        └──────────────────┘        └───────────┘
                           │
                           └ ─ ─ ─ ─ ►  labores-service :8083  (ruta preconfigurada, aún sin implementar)
```

| Componente | Descripción | Puerto |
|---|---|---|
| `gateway` | Spring Cloud Gateway. Punto de entrada único que enruta `/api/v1/campos/**` al servicio de campos. | 8080 |
| `campos-service` | Microservicio REST de campos y lotes (Spring Web + JPA + Validation). | 8082 |
| `mysql-db` | Base de datos `agro_campos_db`, con volumen persistente. | 3306 |

## Tecnologías

- Java 21
- Spring Boot 3.3.4 (Web, Data JPA, Validation, Actuator)
- Spring Cloud Gateway (Spring Cloud 2023.0.3)
- MySQL 8.0
- Springdoc OpenAPI / Swagger UI 2.6.0
- Lombok
- Maven
- Docker y Docker Compose (build multi-etapa, usuario sin privilegios, healthchecks)

## Estructura del repositorio

```
agro-campos/
├── compose.yml                # Orquesta MySQL + campos-service + gateway
├── campos-service/
│   ├── Dockerfile
│   ├── pom.xml
│   └── src/main/
│       ├── java/clase/spring/
│       │   ├── Main.java
│       │   ├── controllers/   # CampoController
│       │   ├── services/      # CampoService (reglas de negocio)
│       │   ├── repositories/  # CampoRepository, LoteRepository
│       │   ├── models/        # Entidades JPA y enums
│       │   ├── dtos/          # request/ y response/ (records)
│       │   └── exceptions/    # Excepciones propias + GlobalExceptionHandler
│       └── resources/application.yml
└── gateway/
    ├── Dockerfile
    ├── pom.xml
    └── src/main/
        ├── java/clase/spring/gateway/GatewayApplication.java
        └── resources/application.yml   # Definición de rutas
```

## Modelo de dominio

| Entidad | Estado | Descripción |
|---|---|---|
| `Campo` | ✅ con API | Nombre y superficie total en hectáreas. Tiene muchos lotes. |
| `Lote` | ✅ con API | Número de lote, hectáreas, latitud/longitud y tipo de suelo. Pertenece a un campo. |
| `Cultivo` | 🧱 modelo | Nombre y conjunto de tipos de suelo aptos (`esAptoParaSuelo`). |
| `Plaga` | 🧱 modelo | Nombre y descripción. |
| `Agroquimico` | 🧱 modelo | Nombre comercial, principio activo, tipo, viento máximo permitido (km/h) y horas libres de lluvia requeridas. |
| `Tratamiento` | 🧱 modelo | Relaciona plaga + agroquímico + cultivo con dosis por hectárea y unidad de medida. |

Enums: `TipoSuelo` (`TIPO_1` a `TIPO_5`) y `TipoAgroquimico` (`HERBICIDA`, `INSECTICIDA`, `FUNGICIDA`, `FERTILIZANTE`).

Las coordenadas de cada lote y los límites de viento y lluvia de los agroquímicos están pensados para consultar un servicio de clima externo (Open-Meteo) y decidir si es buen momento para aplicar un tratamiento. Esa integración todavía no está implementada.

## Reglas de negocio implementadas

- No pueden existir dos campos con el mismo nombre (sin distinguir mayúsculas).
- No puede repetirse el número de lote dentro de un mismo campo.
- La suma de las hectáreas de los lotes no puede superar la superficie total del campo.

## Cómo ejecutarlo

### Opción 1: Docker Compose (recomendada)

Requisitos: Docker y Docker Compose.

```bash
docker compose -f compose.yml up --build
```

Se levantan en orden MySQL, `campos-service` (espera a que MySQL esté sano) y el gateway (espera a que `campos-service` esté sano). La primera vez tarda porque Maven descarga las dependencias.

Para bajar todo (agregá `-v` si querés borrar también los datos de la base):

```bash
docker compose -f compose.yml down
```

### Opción 2: Local, sin Docker

Requisitos: JDK 21, Maven y un MySQL 8 en `localhost:3306` con usuario `root` / contraseña `root`. La base se crea sola (`createDatabaseIfNotExist=true`).

```bash
# Terminal 1
cd campos-service
mvn spring-boot:run

# Terminal 2 (opcional, si querés pasar por el gateway)
cd gateway
mvn spring-boot:run
```

> Al correr el gateway fuera de Docker, la ruta `http://campos-service:8082` no se resuelve. Cambiá el `uri` por `http://localhost:8082` en `gateway/src/main/resources/application.yml`, o llamá directo al puerto 8082.

## Documentación de la API (Swagger)

Con el servicio levantado, abrí: **http://localhost:8082/swagger-ui.html**

El JSON de OpenAPI está en `http://localhost:8082/v3/api-docs`. Swagger se consulta directo contra `campos-service`, porque el gateway solo enruta `/api/v1/campos/**`.

## Endpoints

Base: `http://localhost:8080/api/v1/campos` (por el gateway) o `http://localhost:8082/api/v1/campos` (directo).

| Método | Ruta | Descripción | Respuestas |
|---|---|---|---|
| `GET` | `/` | Lista todos los campos | 200 |
| `GET` | `/{id}` | Obtiene un campo por ID | 200, 404 |
| `POST` | `/` | Crea un campo | 201, 400 |
| `POST` | `/{id}/lotes` | Agrega un lote a un campo | 201, 400, 404 |
| `GET` | `/{id}/lotes` | Lista los lotes de un campo | 200, 404 |

### Ejemplos

**Crear un campo**

```bash
curl -X POST http://localhost:8080/api/v1/campos \
  -H "Content-Type: application/json" \
  -d '{"nombre": "La Esperanza", "superficieTotalHas": 120.5}'
```

```json
{ "id": 1, "nombre": "La Esperanza", "superficieTotalHas": 120.5, "cantidadLotes": 0 }
```

**Agregar un lote**

```bash
curl -X POST http://localhost:8080/api/v1/campos/1/lotes \
  -H "Content-Type: application/json" \
  -d '{"nroLote": 1, "hectareas": 40, "latitud": -31.4201, "longitud": -64.1888, "tipoSuelo": "TIPO_2"}'
```

```json
{ "id": 1, "nroLote": 1, "hectareas": 40.0, "latitud": -31.4201, "longitud": -64.1888, "tipoSuelo": "TIPO_2", "campoId": 1 }
```

### Manejo de errores

Todos los errores devuelven JSON con `timestamp`, `status` y el detalle.

```json
// 400: validación de campos
{ "timestamp": "...", "status": 400, "errors": { "superficieTotalHas": "La superficie total debe ser mayor a 0" } }

// 400: regla de negocio
{ "timestamp": "...", "status": 400, "message": "Ya existe un campo con el nombre: La Esperanza" }

// 404: recurso inexistente
{ "timestamp": "...", "status": 404, "message": "Campo no encontrado con ID: 99" }
```

## Configuración

Las propiedades de `campos-service` están en `application.yml`. En Docker se pisan con variables de entorno desde `compose.yml`:

| Variable | Valor en el compose |
|---|---|
| `SPRING_DATASOURCE_URL` | `jdbc:mysql://mysql-db:3306/agro_campos_db?...` |
| `SPRING_DATASOURCE_USERNAME` | `root` |
| `SPRING_DATASOURCE_PASSWORD` | `root` |

> ⚠️ Las credenciales `root/root` son **solo para desarrollo**. Antes de desplegar en cualquier entorno real hay que moverlas a un archivo `.env` (ya ignorado por Git) o a un gestor de secretos.

## Roadmap

- [ ] Endpoints CRUD para `Cultivo`, `Plaga`, `Agroquimico` y `Tratamiento`
- [ ] Validar que un cultivo sea apto para el tipo de suelo del lote
- [ ] Integración con Open-Meteo para recomendar aplicaciones según viento y lluvia
- [ ] `labores-service` (la ruta ya está en el gateway)
- [ ] Autenticación y autorización (Spring Security)
- [ ] Tests unitarios y de integración
- [ ] Endpoints de modificación y baja de campos y lotes
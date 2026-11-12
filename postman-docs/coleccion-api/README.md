# Colección ejecutable de endpoints y escenarios de prueba

Este directorio contiene la entrega de la colección de llamadas a las APIs de **Agro Campos**:

| Archivo | Contenido |
|---|---|
| `AgroCampos.postman_collection.json` | Colección Postman v2.1 con 105 solicitudes organizadas por carpeta |
| `AgroCampos.local.postman_environment.json` | Variables de entorno para ejecución local |
| `README.md` | Este documento: organización, variables, autenticación, datos iniciales, orden y escenarios |

La colección es importable en Postman, Newman o Bruno. En Bruno se puede importar el formato Postman; si se prefiere una colección nativa, se migran las mismas carpetas y variables conservando el orden.

---

## 1. Requisitos y URLs

La colección usa por defecto las URLs directas de cada microservicio, porque el gateway actual solo enruta `/api/v1/campos/**`.

| Variable | Valor local | Servicio |
|---|---|---|
| `gatewayUrl` | `http://localhost:8080` | API Gateway |
| `keycloackUrl` | `http://localhost:8081` | Keycloack |
| `camposUrl` | `http://localhost:8082` | `campos-service` |
| `agronomiaUrl` | `http://localhost:8083` | `agronomia-service` |
| `proyectosUrl` | `http://localhost:8084` | `proyectos-service` |
| `access_token` | vacío | Token JWT opcional |

Prerrequisitos:

1. MySQL 8 disponible.
2. `campos-service` en el puerto `8082`.
3. `agronomia-service` en el puerto `8083`.
4. `proyectos-service` en el puerto `8084`.

El `compose.yml` actual levanta MySQL, `campos-service` y el gateway. Para ejecutar la colección completa hay que levantar también `agronomia-service` y `proyectos-service` con Maven, o agregarlos al compose.

Cuando se agreguen al gateway las rutas de agronomía y proyectos, se pueden cambiar las variables para ejecutar todo a través de `http://localhost:8080`.

---

## 2. Organización de la colección

Las carpetas se ejecutan en este orden:

| Orden | Carpeta | Solicitudes | Propósito |
|---|---|---|---|
| 00 | Preparación | 3 | Healthchecks y reset de variables de ejecución |
| 01 | Datos iniciales (seed) | 14 | Campo, lotes, cultivos, plaga, agroquímicos y tratamientos |
| 02 | Campos y lotes | 14 | Escenarios de `campos-service` |
| 03 | Agronomía | 20 | Escenarios de catálogo y tratamientos |
| 04 | Proyectos: proceso principal | 31 | Plan de siembra, labores, cosecha y liberación del lote |
| 05 | Integraciones | 9 | Comunicación entre servicios, Open-Meteo y copia de labores |
| 06 | Limpieza (baja lógica) | 14 | Bajas lógicas opcionales |

Cada solicitud lleva un código en el nombre, por ejemplo:

- `02.N01 [RN-CAM-01] Crear campo con nombre duplicado (case-insensitive)`
- `04.09 [RN-LAB-04+] Completar Cosechar y finalizar plan`
- `05.N01 [RN-LAB-05 / INT-OpenMeteo] Fumigar con viento sobre el máximo`

Los tests guardan los IDs en variables de colección para que las solicitudes siguientes usen datos reales y no IDs fijos.

---

## 3. Variables de entorno

### Variables de configuración

| Variable | Descripción |
|---|---|
| `gatewayUrl` | URL del Gateway |
| `keycloackUrl` | URL de Keycloack |
| `camposUrl` | URL de `campos-service` |
| `agronomiaUrl` | URL de `agronomia-service` |
| `proyectosUrl` | URL de `proyectos-service` |
| `access_token` | Token JWT cuando se habilite Keycloak |

### Variables generadas durante la ejecución

| Variable | Origen |
|---|---|
| `campoId` | `POST /api/v1/campos` |
| `loteId`, `loteSinCoordsId`, `loteNoAptoId` | `POST /api/v1/campos/{id}/lotes` |
| `cultivoId`, `cultivoMaizId` | `POST /api/v1/agronomia/cultivos` |
| `plagaId` | `POST /api/v1/agronomia/plagas` |
| `agroquimicoId`, `agroquimicoRestrictivoId`, `fertilizanteId`, `fungicidaId` | `POST /api/v1/agronomia/agroquimicos` |
| `tratamientoId`, `tratamientoRestrictivoId`, `tratamientoMaizId` | `POST /api/v1/agronomia/tratamientos` |
| `planId`, `laborArarId`, `laborSembrarId`, `laborFumigarId`, `laborCosecharId` | `POST /api/v1/proyectos/planes` |
| `plan2Id`, `plan3Id`, `planClimaId` y sus labores | Solicitudes de proyectos |
| `runId` | Generado en la carpeta 00 |
| `fechaSiembra` | Fecha futura generada automáticamente |

Los nombres de las entidades se generan con marca de tiempo para evitar conflictos con las reglas de unicidad entre ejecuciones.

La solicitud `00.01` reinicia los IDs y regenera `runId` y `fechaSiembra`. Se debe ejecutar siempre antes de una nueva corrida.

---

## 4. Autenticación

### Estado actual

El repositorio todavía no tiene Keycloak ni Spring Security implementados. La colección funciona sin token.

A nivel de colección hay un script de pre-request que agrega el header `Authorization: Bearer {{access_token}}` solamente cuando la variable tiene valor. Esto permite ejecutar la colección hoy y reutilizarla cuando se habilite la seguridad.

### Cuando se habilite Keycloak

Configuración propuesta:

- Realm: `bda-realm`
- Roles: `ADMIN`, `AGRONOMO`, `OPERARIO`
- Token endpoint:

```http
POST {{keycloakUrl}}/realms/bda-realm/protocol/openid-connect/token
Content-Type: application/x-www-form-urlencoded

grant_type=password
client_id=agro-campos-client
username=<usuario>
password=<password>
```

El token se guarda en `access_token`. El gateway valida el JWT antes de rutear y cada microservicio lo valida nuevamente como Resource Server.

| Operación | ADMIN | AGRONOMO | OPERARIO |
|---|:-:|:-:|:-:|
| Consultar campos, catálogo y planes | Sí | Sí | Sí |
| Alta, modificación y baja de campos y lotes | Sí | No | No |
| Administrar catálogo agronómico | Sí | Sí | No |
| Crear y cancelar planes | Sí | Sí | No |
| Iniciar y completar labores | Sí | Sí | Sí |

Cuando Keycloak esté operativo se agregarán los escenarios negativos de seguridad: token ausente, token vencido, rol insuficiente (`403`) y acceso directo a servicios internos.

---

## 5. Datos iniciales

La carpeta 01 crea un juego de datos controlado:

| Dato | Valores |
|---|---|
| Campo | `Campo E2E <timestamp>`, 120 ha |
| Lote con coordenadas | Nro. 1, 40 ha, `TIPO_2`, lat. `-31.4201`, long. `-64.1888` |
| Lote sin coordenadas | Nro. 2, 20 ha, `TIPO_2` |
| Lote no apto | Nro. 3, 10 ha, `TIPO_5` |
| Cultivo Soja | Suelos `TIPO_1`, `TIPO_2`; labores `Arar`, `Sembrar`, `Fumigar`, `Cosechar` |
| Cultivo Maíz | Suelos `TIPO_2`, `TIPO_3`; misma secuencia |
| Plaga | `Oruga E2E`, tipo `INSECTO` |
| Agroquímico permisivo | `INSECTICIDA`, viento máximo 999 km/h, 1 hora libre de lluvia |
| Agroquímico restrictivo | `INSECTICIDA`, viento máximo 0.01 km/h, 1 hora libre de lluvia |
| Fertilizante | Tipo `FERTILIZANTE` |
| Fungicida | Tipo `FUNGICIDA` |
| Tratamientos | Soja + permisivo; Soja + restrictivo; Maíz + permisivo |

La carpeta 06 realiza **baja lógica**: los registros quedan inactivos, no se borran físicamente. Así se verifica que un recurso dado de baja devuelve `404` y que el histórico se conserva.

---

## 6. Orden de ejecución

1. **00 - Preparación**
   - Reinicia variables.
   - Genera `runId` y `fechaSiembra`.
   - Verifica healthchecks de los tres servicios.

2. **01 - Datos iniciales**
   - Crea todos los datos base.
   - Guarda los IDs en variables de colección.

3. **02 - Campos y lotes**
   - Escenarios positivos y negativos de `campos-service`.

4. **03 - Agronomía**
   - Escenarios de cultivos, plagas, agroquímicos y tratamientos.

5. **04 - Proyectos: proceso principal**
   - Crea un plan.
   - Recorre la secuencia completa de labores.
   - Finaliza con la cosecha.
   - Verifica la liberación del lote.

6. **05 - Integraciones**
   - Prueba `proyectos -> campos` y `proyectos -> agronomía`.
   - Prueba Open-Meteo.
   - Verifica que los cambios del catálogo no alteren planes existentes.

7. **06 - Limpieza (opcional)**
   - Da de baja lógica los datos creados.

No se deben ejecutar las carpetas 04 y 05 sin haber ejecutado antes 01. Si se ejecuta una carpeta aislada, primero deben correrse 00 y 01.

### Ejecución con Postman

1. Importar `AgroCampos.postman_collection.json`.
2. Importar `AgroCampos.local.postman_environment.json`.
3. Seleccionar el environment `Agro Campos - Local (servicios directos)`.
4. Abrir el Collection Runner y ejecutar las carpetas en orden.
5. Usar una iteración y, si es necesario, un delay de 100 ms.

### Ejecución con Newman

```bash
newman run AgroCampos.postman_collection.json \
  -e AgroCampos.local.postman_environment.json \
  --reporters cli,htmlextra
```

---

## 7. Endpoints cubiertos

### `campos-service` — `/api/v1/campos`

| Método | URI | Escenario |
|---|---|---|
| `GET` | `/campos` | Listar campos activos |
| `GET` | `/campos/{id}` | Obtener campo |
| `POST` | `/campos` | Crear campo |
| `PUT` | `/campos/{id}` | Actualizar campo |
| `DELETE` | `/campos/{id}` | Baja lógica de campo |
| `GET` | `/campos/{id}/lotes` | Listar lotes activos |
| `GET` | `/campos/{id}/lotes/{loteId}` | Obtener lote |
| `POST` | `/campos/{id}/lotes` | Agregar lote |
| `PUT` | `/campos/{id}/lotes/{loteId}` | Actualizar lote |
| `DELETE` | `/campos/{id}/lotes/{loteId}` | Baja lógica de lote |

### `agronomia-service` — `/api/v1/agronomia`

| Método | URI |
|---|---|
| `GET` / `POST` | `/cultivos` |
| `GET` / `PUT` / `DELETE` | `/cultivos/{id}` |
| `GET` / `POST` | `/plagas` |
| `GET` / `PUT` / `DELETE` | `/plagas/{id}` |
| `GET` / `POST` | `/agroquimicos` |
| `GET` / `PUT` / `DELETE` | `/agroquimicos/{id}` |
| `GET` / `POST` | `/tratamientos` |
| `GET` / `PUT` / `DELETE` | `/tratamientos/{id}` |
| `GET` | `/tratamientos?plagaId=&cultivoId=` |

### `proyectos-service` — `/api/v1/proyectos`

| Método | URI |
|---|---|
| `POST` | `/planes` |
| `GET` | `/planes` |
| `GET` | `/planes?estado=` |
| `GET` | `/planes/{id}` |
| `GET` | `/lotes/{loteId}/planes` |
| `PATCH` | `/planes/{id}` |
| `PATCH` | `/planes/{planId}/labores/{laborId}` |

---

## 8. Escenarios positivos y negativos

### 8.1 Campos y lotes

| Código | Escenario | Resultado esperado | Regla |
|---|---|---|---|
| CAM-P01 | Crear campo válido | `201`, campo activo, `cantidadLotes = 0` | — |
| CAM-P02 | Listar y obtener campo | `200`, incluye el campo creado | — |
| CAM-P03 | Actualizar campo | `200`, superficie actualizada | — |
| CAM-P04 | Agregar lote válido | `201`, lote asociado al campo | — |
| CAM-P05 | Listar y obtener lote | `200`, tipo de suelo correcto | — |
| CAM-P06 | Actualizar lote | `200`, hectáreas actualizadas | — |
| CAM-P07 | Baja lógica de lote | `204` y luego `404` | — |
| CAM-P08 | Baja lógica de campo | `204` y luego `404` | — |
| CAM-N01 | Crear campo con nombre duplicado, distinta capitalización | `400` “Ya existe un campo” | RN-CAM-01 |
| CAM-N02 | Crear campo con nombre vacío y superficie 0 | `400` con errores por campo | Validación |
| CAM-N03 | Repetir número de lote en el mismo campo | `400` “ya existe” | RN-CAM-03 |
| CAM-N04 | Agregar lote que excede la superficie total | `400` “Superficie excedida” | RN-CAM-02 |
| CAM-N05 | Actualizar campo con superficie menor a la ocupada por lotes | `400` “Superficie inválida” | RN-CAM-02 |
| CAM-N06 | Actualizar lote con número duplicado | `400` “ya existe” | RN-CAM-03 |
| CAM-N07 | Obtener campo inexistente | `404` | — |
| CAM-N08 | Obtener lote inexistente | `404` | — |

### 8.2 Agronomía

| Código | Escenario | Resultado esperado | Regla |
|---|---|---|---|
| AGR-P01 | Listar y obtener cultivos | `200`, incluye `suelosAptos` y `labores` ordenadas | — |
| AGR-P02 | Crear y actualizar plaga | `201` / `200` | — |
| AGR-P03 | Listar, obtener y actualizar agroquímicos | `200`, conserva límites climáticos | — |
| AGR-P04 | Crear tratamiento válido | `201`, incluye plaga, agroquímico y cultivo | — |
| AGR-P05 | Listar tratamientos y filtrar por plaga/cultivo | `200`, devuelve el tratamiento esperado | — |
| AGR-P06 | Obtener tratamiento | `200`, incluye límites del agroquímico | — |
| AGR-N01 | Crear agroquímico con nombre comercial duplicado | `400` “Ya existe un agroquímico” | RN-TRA-01 |
| AGR-N02 | Crear tratamiento con dosis 0 | `400` error de dosis | RN-TRA-02 |
| AGR-N03 | Crear tratamiento con fertilizante | `400` “Un fertilizante no combate ninguna plaga” | RN-TRA-04 |
| AGR-N04 | Plaga `INSECTO` con agroquímico `FUNGICIDA` | `400` “debe utilizarse un INSECTICIDA” | RN-TRA-04 |
| AGR-N05 | Tratamiento con plaga inexistente | `404` | — |
| AGR-N06 | Cultivo sin suelos aptos | `400` con error en `suelosAptos` | Validación |
| AGR-N07 | Plaga sin tipo | `400` con error en `tipoPlaga` | Validación |
| AGR-N08 | Tratamiento sin IDs obligatorios | `400` con errores en `plagaId`, `agroquimicoId`, `cultivoId` | Validación |
| AGR-N09 | Obtener cultivo o tratamiento inexistente | `404` | — |

### 8.3 Proyectos: proceso principal

| Código | Escenario | Resultado esperado | Regla |
|---|---|---|---|
| PRO-P01 | Crear plan en lote apto | `201`, estado `PLANIFICADO`, labores copiadas del cultivo | RN-LAB-01 |
| PRO-P02 | Iniciar y completar `Arar` | `200`, plan pasa a `EN_CURSO` | RN-LAB-03 |
| PRO-P03 | Iniciar y completar `Sembrar` | `200`, labor `COMPLETADA` | RN-LAB-03 |
| PRO-P04 | Iniciar `Fumigar` en lote sin coordenadas | `200`, labor `EN_EJECUCION` y advertencia climática | RN-LAB-05 |
| PRO-P05 | Completar `Fumigar` | `200` | RN-LAB-03 |
| PRO-P06 | Iniciar y completar `Cosechar` | `200`, plan `FINALIZADO` | RN-LAB-04 |
| PRO-P07 | Crear nuevo plan tras finalizar | `201`, el lote quedó libre | RN-LAB-02 |
| PRO-P08 | Cancelar plan y crear otro en el mismo lote | `200` / `201`, el lote quedó libre | RN-LAB-02 |
| PRO-P09 | Consultar plan, planes por estado e histórico del lote | `200` | — |
| PRO-N01 | Crear plan en lote `TIPO_5` para cultivo de `TIPO_1/TIPO_2` | `400` “no es apto” | RN-LAB-01 |
| PRO-N02 | Crear segundo plan activo en el mismo lote | `400` “ya tiene un plan activo” | RN-LAB-02 |
| PRO-N03 | Iniciar `Fumigar` sin completar `Sembrar` | `400` “primero debe completarse” | RN-LAB-03 |
| PRO-N04 | Volver una labor a `PENDIENTE` | `400` transición no permitida | RN-LAB-03 |
| PRO-N05 | Completar una labor sin iniciarla | `400` “debe estar EN_EJECUCION” | RN-LAB-03 |
| PRO-N06 | Iniciar fumigación sin `tratamientoId` | `400` “debe indicar el tratamientoId” | RN-LAB-05 |
| PRO-N07 | Fumigar con tratamiento de otro cultivo | `400` “no corresponde al cultivo del plan” | RN-LAB-05 |
| PRO-N08 | Fumigar con viento superior al máximo | `400` “No se puede fumigar” | RN-LAB-05 |
| PRO-N09 | Cancelar un plan `FINALIZADO` | `400` “no puede cancelarse” | RN-LAB-04 |
| PRO-N10 | Iniciar labor de un plan `FINALIZADO` | `400` “no admite cambios” | RN-LAB-03 |
| PRO-N11 | Listar planes con estado inválido | `400` parámetro inválido | — |
| PRO-N12 | Crear plan con fecha pasada | `400` error en `fechaSiembraPrevista` | Validación |
| PRO-N13 | Crear plan con lote o cultivo inexistente | `404` | — |

### 8.4 Integraciones

| Código | Escenario | Resultado esperado |
|---|---|---|
| INT-P01 | `proyectos-service` consulta el lote en `campos-service` al crear un plan | Plan creado con `campoId` y `loteId` correctos |
| INT-P02 | `proyectos-service` consulta el cultivo y sus labores en `agronomia-service` | Labores copiadas en el orden del cultivo |
| INT-P03 | `proyectos-service` consulta el tratamiento y los límites del agroquímico | Se aplica RN-LAB-05 con los límites del producto |
| INT-P04 | Cambiar la secuencia de labores del cultivo después de crear el plan | El plan existente conserva sus labores originales |
| INT-N01 | Open-Meteo devuelve viento superior al máximo | `400`, la labor queda `PENDIENTE` |
| INT-N02 | Open-Meteo prevé lluvia dentro de las horas requeridas | `400` |
| INT-N03 | Open-Meteo no responde | `200` con advertencia; la fumigación se habilita |
| INT-N04 | Lote sin coordenadas | `200` con advertencia; no se consulta el clima |
| INT-N05 | `campos-service` caído al crear un plan | `503` |
| INT-N06 | `agronomia-service` caído al crear o fumigar | `503` |
| INT-N07 | Gateway enruta `/api/v1/campos/**` | `200` a través de `http://localhost:8080` |

Los escenarios INT-N05, INT-N06 e INT-N03 requieren una prueba manual controlada: detener el servicio correspondiente o configurar una URL inválida. En la entrega se documenta el procedimiento y el resultado esperado.

---

## 9. Consideraciones sobre Open-Meteo

Los escenarios de clima dependen de un proveedor externo y no son completamente deterministas.

La colección incluye:

- Un escenario negativo de viento con un agroquímico de umbral `0.01 km/h`, que en condiciones normales devuelve `400`.
- Un escenario positivo sin coordenadas, que no consulta Open-Meteo y devuelve `200` con advertencia.

Para pruebas deterministas se propone:

1. Levantar un stub local de Open-Meteo.
2. Configurar `clima.open-meteo.base-url` para apuntar al stub.
3. Definir dos respuestas:
   - favorable: viento bajo y sin lluvia;
   - desfavorable: viento alto o lluvia dentro de la ventana.
4. Ejecutar los escenarios de clima contra el stub.
5. Mantener el pronóstico real como smoke test, registrando fecha y resultado.

Si Open-Meteo no responde, `proyectos-service` habilita la fumigación y devuelve una advertencia. El test de viento fallará en ese caso, lo que indica que debe repetirse o ejecutarse contra el stub.

---

## 10. Criterios de aceptación

Se considera que la colección está correctamente entregada cuando:

1. Se importa en Postman sin errores y el environment se selecciona correctamente.
2. Las carpetas 00 y 01 crean los datos iniciales y capturan los IDs.
3. Las carpetas 02, 03 y 04 ejecutan todos sus tests con resultado exitoso en un entorno local con los tres servicios levantados.
4. Los escenarios de error devuelven los códigos HTTP y mensajes esperados.
5. La carpeta 05 verifica las integraciones y documenta el resultado de Open-Meteo.
6. La carpeta 06 deja los datos dados de baja lógicamente.
7. Se entrega el reporte de Newman o la captura del Collection Runner como evidencia.

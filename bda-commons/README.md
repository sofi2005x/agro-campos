# bda-commons

Componente común del backend Agro Campos: un JAR Maven que usan `campos-service`, `agronomia-service` y `proyectos-service`.
Reúne el código que hoy está **repetido idéntico en los tres servicios** (manejo de errores y seguridad JWT con Keycloak).

## Qué contiene

| Paquete | Clase | Qué hace |
|---|---|---|
| `errors` | `ErrorResponse` | Formato único de error: `status`, `error`, `mensaje`, `timestamp`, `errores` (solo en validaciones). |
| `errors` | `ReglaNegocioException` (400), `RecursoNoEncontradoException` (404), `ServicioNoDisponibleException` (503) | Excepciones base. Las de cada servicio las extienden (`CultivoNoEncontradoException extends RecursoNoEncontradoException`). |
| `errors` | `ApiExceptionHandler` | `@RestControllerAdvice` que traduce esas excepciones y los errores de validación a `ErrorResponse`. |
| `security` | `KeycloakRoleConverter` | Convierte el claim `realm_access.roles` del JWT en autoridades `ROLE_*`. La lógica está en `KeycloakRoles` (sin Spring). |
| `security` | `SecurityErrorHandler` | 401 y 403 en JSON con el mismo `ErrorResponse`. |
| `security` | `KeycloakSecuritySupport` | Configuración común del resource server (sin CSRF, stateless, JWT + errores JSON) y la lista `RUTAS_PUBLICAS`. |
| `autoconfigure` | | Registra `ApiExceptionHandler` y `SecurityErrorHandler` como beans. No hace falta `@ComponentScan` ni `@Import`. |

## Qué NO contiene (a propósito)

- Las **reglas de acceso por ruta y rol** (`requestMatchers(...).hasRole(...)`): dependen de los endpoints de cada servicio, quedan en su `KeycloakSecurityConfig`.
- Entidades, enums ni reglas de dominio (por ejemplo `TipoSuelo`): acoplarían los servicios entre sí.
- DTOs de las APIs: cada servicio evoluciona su contrato por separado.
- El reenvío del token en las llamadas entre servicios (`RestClient` de `proyectos-service`): hoy lo usa un solo servicio. Si otro servicio empieza a consumir a otro, ahí sí convendría moverlo.

## Instalación (repositorio Maven local)

```bash
cd bda-commons
mvn clean install
```

Deja el JAR en `~/.m2/repository/clase/spring/bda-commons/0.0.1-SNAPSHOT/` (en Windows: `C:\Users\<usuario>\.m2\...`). Hay que repetirlo cada vez que cambie este módulo, antes de compilar los servicios.

## Uso en un servicio

```xml
<dependency>
    <groupId>clase.spring</groupId>
    <artifactId>bda-commons</artifactId>
    <version>0.0.1-SNAPSHOT</version>
</dependency>
```

Los paquetes del módulo empiezan con `clase.bda.commons`, **fuera** de `clase.spring`, para que el escaneo de componentes de los servicios no los registre dos veces.

Las dependencias de Web y Security de este módulo son `optional`: no se transmiten. Cada servicio sigue declarando las suyas, y usar bda-commons no activa Spring Security en un servicio que no la tenga.

### Seguridad: `KeycloakSecurityConfig` de cada servicio

```java
@Configuration
@EnableWebSecurity
public class KeycloakSecurityConfig {

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http, SecurityErrorHandler errorHandler) throws Exception {
        KeycloakSecuritySupport.configurarResourceServer(http, errorHandler);

        http.authorizeHttpRequests(auth -> auth
                .requestMatchers(KeycloakSecuritySupport.RUTAS_PUBLICAS).permitAll()
                // ... reglas propias del servicio ...
                .anyRequest().authenticated());

        return http.build();
    }
}
```

## Nota sobre el empaquetado

Este `pom.xml` no declara `spring-boot-maven-plugin`: ese plugin genera un JAR ejecutable que no puede usarse como dependencia.

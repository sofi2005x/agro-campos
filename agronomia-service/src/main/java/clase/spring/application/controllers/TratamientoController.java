package clase.spring.application.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import clase.spring.application.dtos.request.TratamientoRequest;
import clase.spring.application.dtos.response.TratamientoResponse;
import clase.spring.application.services.TratamientoService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/agronomia/tratamientos")
@RequiredArgsConstructor
@Tag(name = "Tratamientos", description = "Endpoints para la gestión de tratamientos")
public class TratamientoController {

    private final TratamientoService tratamientoService;

    @GetMapping
    @Operation(summary = "Listar tratamientos, opcionalmente filtrados por plaga o cultivo")
    public ResponseEntity<List<TratamientoResponse>> listarTratamientos(
            @RequestParam(required = false) Long plagaId,
            @RequestParam(required = false) Long cultivoId) {
        return ResponseEntity.ok(tratamientoService.listar(plagaId, cultivoId));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener un tratamiento por ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tratamiento encontrado"),
            @ApiResponse(responseCode = "404", description = "Tratamiento no encontrado")
    })
    public ResponseEntity<TratamientoResponse> obtenerTratamientoPorId(@PathVariable Long id) {
        return ResponseEntity.ok(tratamientoService.obtenerPorId(id));
    }

    @PostMapping
    @Operation(summary = "Crear un nuevo tratamiento")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Tratamiento creado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos o regla de negocio incumplida"),
            @ApiResponse(responseCode = "404", description = "Plaga, agroquímico o cultivo no encontrado")
    })
    public ResponseEntity<TratamientoResponse> crearTratamiento(@Valid @RequestBody TratamientoRequest request) {
        TratamientoResponse nuevo = tratamientoService.crear(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(nuevo);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar un tratamiento existente")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tratamiento actualizado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos o regla de negocio incumplida"),
            @ApiResponse(responseCode = "404", description = "Tratamiento, plaga, agroquímico o cultivo no encontrado")
    })
    public ResponseEntity<TratamientoResponse> actualizarTratamiento(
            @PathVariable Long id,
            @Valid @RequestBody TratamientoRequest request) {
        return ResponseEntity.ok(tratamientoService.actualizar(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Baja lógica de un tratamiento")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Tratamiento dado de baja exitosamente"),
            @ApiResponse(responseCode = "404", description = "Tratamiento no encontrado")
    })
    public ResponseEntity<Void> eliminarTratamiento(@PathVariable Long id) {
        tratamientoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}

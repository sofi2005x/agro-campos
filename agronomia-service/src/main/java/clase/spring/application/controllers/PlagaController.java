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
import clase.spring.application.dtos.request.PlagaRequest;
import clase.spring.application.dtos.response.PlagaResponse;
import clase.spring.application.services.PlagaService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/agronomia/plagas")
@RequiredArgsConstructor
@Tag(name = "Plagas", description = "Endpoints para la gestión de plagas")
public class PlagaController {

    private final PlagaService plagaService;

    @GetMapping
    @Operation(summary = "Listar todas las plagas")
    public ResponseEntity<List<PlagaResponse>> listarPlagas() {
        return ResponseEntity.ok(plagaService.listar());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener una plaga por ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Plaga encontrada"),
            @ApiResponse(responseCode = "404", description = "Plaga no encontrada")
    })
    public ResponseEntity<PlagaResponse> obtenerPlagaPorId(@PathVariable Long id) {
        return ResponseEntity.ok(plagaService.obtenerPorId(id));
    }

    @PostMapping
    @Operation(summary = "Crear una nueva plaga")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Plaga creada exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos o nombre duplicado")
    })
    public ResponseEntity<PlagaResponse> crearPlaga(@Valid @RequestBody PlagaRequest request) {
        PlagaResponse nueva = plagaService.crear(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(nueva);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar una plaga existente")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Plaga actualizada exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos o nombre duplicado"),
            @ApiResponse(responseCode = "404", description = "Plaga no encontrada")
    })
    public ResponseEntity<PlagaResponse> actualizarPlaga(
            @PathVariable Long id,
            @Valid @RequestBody PlagaRequest request) {
        return ResponseEntity.ok(plagaService.actualizar(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Baja lógica de una plaga")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Plaga dada de baja exitosamente"),
            @ApiResponse(responseCode = "404", description = "Plaga no encontrada")
    })
    public ResponseEntity<Void> eliminarPlaga(@PathVariable Long id) {
        plagaService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}

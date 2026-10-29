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
import clase.spring.application.dtos.request.CultivoRequest;
import clase.spring.application.dtos.response.CultivoResponse;
import clase.spring.application.services.CultivoService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/agronomia/cultivos")
@RequiredArgsConstructor
@Tag(name = "Cultivos", description = "Endpoints para la gestión de cultivos")
public class CultivoController {

    private final CultivoService cultivoService;

    @GetMapping
    @Operation(summary = "Listar todos los cultivos")
    public ResponseEntity<List<CultivoResponse>> listarCultivos() {
        return ResponseEntity.ok(cultivoService.listar());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener un cultivo por ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cultivo encontrado"),
            @ApiResponse(responseCode = "404", description = "Cultivo no encontrado")
    })
    public ResponseEntity<CultivoResponse> obtenerCultivoPorId(@PathVariable Long id) {
        return ResponseEntity.ok(cultivoService.obtenerPorId(id));
    }

    @PostMapping
    @Operation(summary = "Crear un nuevo cultivo")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Cultivo creado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos o nombre duplicado")
    })
    public ResponseEntity<CultivoResponse> crearCultivo(@Valid @RequestBody CultivoRequest request) {
        CultivoResponse nuevo = cultivoService.crear(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(nuevo);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar un cultivo existente")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cultivo actualizado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos o nombre duplicado"),
            @ApiResponse(responseCode = "404", description = "Cultivo no encontrado")
    })
    public ResponseEntity<CultivoResponse> actualizarCultivo(
            @PathVariable Long id,
            @Valid @RequestBody CultivoRequest request) {
        return ResponseEntity.ok(cultivoService.actualizar(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Baja lógica de un cultivo")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Cultivo dado de baja exitosamente"),
            @ApiResponse(responseCode = "404", description = "Cultivo no encontrado")
    })
    public ResponseEntity<Void> eliminarCultivo(@PathVariable Long id) {
        cultivoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}

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
import clase.spring.application.dtos.request.AgroquimicoRequest;
import clase.spring.application.dtos.response.AgroquimicoResponse;
import clase.spring.application.services.AgroquimicoService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/agronomia/agroquimicos")
@RequiredArgsConstructor
@Tag(name = "Agroquímicos", description = "Endpoints para la gestión de agroquímicos")
public class AgroquimicoController {

    private final AgroquimicoService agroquimicoService;

    @GetMapping
    @Operation(summary = "Listar todos los agroquímicos")
    public ResponseEntity<List<AgroquimicoResponse>> listarAgroquimicos() {
        return ResponseEntity.ok(agroquimicoService.listar());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener un agroquímico por ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Agroquímico encontrado"),
            @ApiResponse(responseCode = "404", description = "Agroquímico no encontrado")
    })
    public ResponseEntity<AgroquimicoResponse> obtenerAgroquimicoPorId(@PathVariable Long id) {
        return ResponseEntity.ok(agroquimicoService.obtenerPorId(id));
    }

    @PostMapping
    @Operation(summary = "Crear un nuevo agroquímico")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Agroquímico creado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos o nombre comercial duplicado")
    })
    public ResponseEntity<AgroquimicoResponse> crearAgroquimico(@Valid @RequestBody AgroquimicoRequest request) {
        AgroquimicoResponse nuevo = agroquimicoService.crear(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(nuevo);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar un agroquímico existente")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Agroquímico actualizado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos o nombre comercial duplicado"),
            @ApiResponse(responseCode = "404", description = "Agroquímico no encontrado")
    })
    public ResponseEntity<AgroquimicoResponse> actualizarAgroquimico(
            @PathVariable Long id,
            @Valid @RequestBody AgroquimicoRequest request) {
        return ResponseEntity.ok(agroquimicoService.actualizar(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Baja lógica de un agroquímico")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Agroquímico dado de baja exitosamente"),
            @ApiResponse(responseCode = "404", description = "Agroquímico no encontrado")
    })
    public ResponseEntity<Void> eliminarAgroquimico(@PathVariable Long id) {
        agroquimicoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}

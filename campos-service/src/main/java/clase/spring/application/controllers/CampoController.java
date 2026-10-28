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
import clase.spring.application.dtos.request.CampoRequestDTO;
import clase.spring.application.dtos.request.LoteRequestDTO;
import clase.spring.application.dtos.response.CampoResponseDTO;
import clase.spring.application.dtos.response.LoteResponseDTO;
import clase.spring.application.services.CampoService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/campos")
@RequiredArgsConstructor
@Tag(name = "Campos", description = "Endpoints para la gestión de campos y sus lotes")
public class CampoController {

    private final CampoService campoService;

    @GetMapping
    @Operation(summary = "Listar todos los campos activos")
    public ResponseEntity<List<CampoResponseDTO>> listarCampos() {
        return ResponseEntity.ok(campoService.listarTodos());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener un campo por ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Campo encontrado"),
            @ApiResponse(responseCode = "404", description = "Campo no encontrado o inactivo")
    })
    public ResponseEntity<CampoResponseDTO> obtenerCampoPorId(@PathVariable Long id) {
        return ResponseEntity.ok(campoService.buscarPorId(id));
    }

    @PostMapping
    @Operation(summary = "Crear un nuevo campo")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Campo creado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos o nombre duplicado (RN-CAM-01)")
    })
    public ResponseEntity<CampoResponseDTO> crearCampo(@Valid @RequestBody CampoRequestDTO request) {
        CampoResponseDTO nuevo = campoService.crearCampo(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(nuevo);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar un campo existente")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Campo actualizado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos, nombre duplicado o superficie menor a lotes"),
            @ApiResponse(responseCode = "404", description = "Campo no encontrado")
    })
    public ResponseEntity<CampoResponseDTO> actualizarCampo(
            @PathVariable Long id,
            @Valid @RequestBody CampoRequestDTO request) {
        return ResponseEntity.ok(campoService.actualizarCampo(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Baja lógica de un campo y sus lotes")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Campo dado de baja exitosamente"),
            @ApiResponse(responseCode = "404", description = "Campo no encontrado")
    })
    public ResponseEntity<Void> eliminarCampo(@PathVariable Long id) {
        campoService.eliminarCampo(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/lotes")
    @Operation(summary = "Agregar un lote a un campo")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Lote agregado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Superficie excedida (RN-CAM-02) o número duplicado (RN-CAM-03)"),
            @ApiResponse(responseCode = "404", description = "Campo no encontrado")
    })
    public ResponseEntity<LoteResponseDTO> agregarLote(
            @PathVariable Long id,
            @Valid @RequestBody LoteRequestDTO request) {
        LoteResponseDTO lote = campoService.agregarLoteACampo(id, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(lote);
    }

    @GetMapping("/{id}/lotes")
    @Operation(summary = "Listar los lotes activos de un campo")
    public ResponseEntity<List<LoteResponseDTO>> listarLotesDeCampo(@PathVariable Long id) {
        return ResponseEntity.ok(campoService.listarLotesDeCampo(id));
    }

    @GetMapping("/{id}/lotes/{loteId}")
    @Operation(summary = "Obtener un lote específico de un campo")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lote encontrado"),
            @ApiResponse(responseCode = "404", description = "Campo o lote no encontrado")
    })
    public ResponseEntity<LoteResponseDTO> obtenerLote(
            @PathVariable Long id,
            @PathVariable Long loteId) {
        return ResponseEntity.ok(campoService.buscarLotePorId(id, loteId));
    }

    @PutMapping("/{id}/lotes/{loteId}")
    @Operation(summary = "Actualizar un lote existente")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lote actualizado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Superficie excedida o número de lote duplicado"),
            @ApiResponse(responseCode = "404", description = "Campo o lote no encontrado")
    })
    public ResponseEntity<LoteResponseDTO> actualizarLote(
            @PathVariable Long id,
            @PathVariable Long loteId,
            @Valid @RequestBody LoteRequestDTO request) {
        return ResponseEntity.ok(campoService.actualizarLote(id, loteId, request));
    }

    @DeleteMapping("/{id}/lotes/{loteId}")
    @Operation(summary = "Baja lógica de un lote")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Lote dado de baja exitosamente"),
            @ApiResponse(responseCode = "404", description = "Campo o lote no encontrado")
    })
    public ResponseEntity<Void> eliminarLote(
            @PathVariable Long id,
            @PathVariable Long loteId) {
        campoService.eliminarLote(id, loteId);
        return ResponseEntity.noContent().build();
    }
}

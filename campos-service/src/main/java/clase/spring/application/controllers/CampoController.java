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
    @Operation(summary = "Listar todos los campos")
    public ResponseEntity<List<CampoResponseDTO>> listarCampos() {
        return ResponseEntity.ok(campoService.listarTodos());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener un campo por ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Campo encontrado"),
            @ApiResponse(responseCode = "404", description = "Campo no encontrado")
    })
    public ResponseEntity<CampoResponseDTO> obtenerCampoPorId(@PathVariable Long id) {
        return ResponseEntity.ok(campoService.buscarPorId(id));
    }

    @PostMapping
    @Operation(summary = "Crear un nuevo campo")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Campo creado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos o nombre duplicado")
    })
    public ResponseEntity<CampoResponseDTO> crearCampo(@Valid @RequestBody CampoRequestDTO request) {
        CampoResponseDTO nuevo = campoService.crearCampo(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(nuevo);
    }

    @PostMapping("/{id}/lotes")
    @Operation(summary = "Agregar un lote a un campo")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Lote agregado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Superficie excedida o número de lote duplicado"),
            @ApiResponse(responseCode = "404", description = "Campo no encontrado")
    })
    public ResponseEntity<LoteResponseDTO> agregarLote(
            @PathVariable Long id,
            @Valid @RequestBody LoteRequestDTO request) {
        LoteResponseDTO lote = campoService.agregarLoteACampo(id, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(lote);
    }

    @GetMapping("/{id}/lotes")
    @Operation(summary = "Listar todos los lotes de un campo")
    public ResponseEntity<List<LoteResponseDTO>> listarLotesDeCampo(@PathVariable Long id) {
        return ResponseEntity.ok(campoService.listarLotesDeCampo(id));
    }
}

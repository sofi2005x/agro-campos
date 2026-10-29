package clase.spring.application.controllers;

import clase.spring.application.dtos.request.LaborPatchRequest;
import clase.spring.application.dtos.request.PlanPatchRequest;
import clase.spring.application.dtos.request.PlanRequest;
import clase.spring.application.dtos.response.PlanResponse;
import clase.spring.application.services.PlanDeSiembraService;
import clase.spring.domain.models.EstadoPlan;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/proyectos")
@RequiredArgsConstructor
@Tag(name = "Planes de siembra", description = "Planes de siembra de un lote, sus estados y sus laboreos")
public class PlanDeSiembraController {

    private final PlanDeSiembraService planService;

    @PostMapping("/planes")
    @Operation(summary = "Crear un plan de siembra",
            description = "Valida la aptitud del suelo (RN-LAB-01) y que el lote no tenga otro plan activo (RN-LAB-02). "
                    + "Las labores se generan con la secuencia del cultivo.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Plan creado"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos, suelo no apto o lote con plan activo"),
            @ApiResponse(responseCode = "404", description = "Campo, lote o cultivo inexistente"),
            @ApiResponse(responseCode = "503", description = "campos-service o agronomia-service no disponible")
    })
    public ResponseEntity<PlanResponse> crear(@Valid @RequestBody PlanRequest request) {
        PlanResponse plan = planService.crear(request);
        return ResponseEntity.created(URI.create("/api/v1/proyectos/planes/" + plan.getId())).body(plan);
    }

    @GetMapping("/planes")
    @Operation(summary = "Listar planes de siembra", description = "Opcionalmente filtrados por estado")
    @ApiResponse(responseCode = "400", description = "Estado inválido")
    public ResponseEntity<List<PlanResponse>> listar(@RequestParam(required = false) EstadoPlan estado) {
        return ResponseEntity.ok(planService.listar(estado));
    }

    @GetMapping("/planes/{id}")
    @Operation(summary = "Obtener un plan de siembra con sus labores")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Plan encontrado"),
            @ApiResponse(responseCode = "404", description = "Plan no encontrado")
    })
    public ResponseEntity<PlanResponse> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(planService.obtenerPorId(id));
    }

    @GetMapping("/lotes/{loteId}/planes")
    @Operation(summary = "Histórico de planes de un lote", description = "Del más reciente al más antiguo; vacío si el lote no tuvo planes")
    public ResponseEntity<List<PlanResponse>> listarPorLote(@PathVariable Long loteId) {
        return ResponseEntity.ok(planService.listarPorLote(loteId));
    }

    @PatchMapping("/planes/{id}")
    @Operation(summary = "Cancelar un plan", description = "Única transición manual: estado = CANCELADO. Libera el lote.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Plan actualizado"),
            @ApiResponse(responseCode = "400", description = "Transición no permitida"),
            @ApiResponse(responseCode = "404", description = "Plan no encontrado")
    })
    public ResponseEntity<PlanResponse> actualizarEstado(@PathVariable Long id,
                                                         @Valid @RequestBody PlanPatchRequest request) {
        return ResponseEntity.ok(planService.actualizarEstado(id, request));
    }

    @PatchMapping("/planes/{planId}/labores/{laborId}")
    @Operation(summary = "Iniciar o completar una labor",
            description = "PENDIENTE -> EN_EJECUCION (valida RN-LAB-03 y, en fumigaciones, RN-LAB-05; requiere tratamientoId) "
                    + "y EN_EJECUCION -> COMPLETADA (completar la cosecha finaliza el plan, RN-LAB-04).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Labor actualizada; puede incluir advertencias"),
            @ApiResponse(responseCode = "400", description = "Secuencia incorrecta, clima desfavorable o transición no permitida"),
            @ApiResponse(responseCode = "404", description = "Plan, labor o tratamiento inexistente"),
            @ApiResponse(responseCode = "503", description = "agronomia-service o campos-service no disponible")
    })
    public ResponseEntity<PlanResponse> actualizarLabor(@PathVariable Long planId,
                                                        @PathVariable Long laborId,
                                                        @Valid @RequestBody LaborPatchRequest request) {
        return ResponseEntity.ok(planService.actualizarLabor(planId, laborId, request));
    }
}

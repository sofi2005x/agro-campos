package clase.spring.application.controllers;

import clase.spring.application.dtos.request.TratamientoRequest;
import clase.spring.application.dtos.response.TratamientoResponse;
import clase.spring.application.services.TratamientoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/agronomia/tratamientos")
@RequiredArgsConstructor
public class TratamientoController {

    private final TratamientoService tratamientoService;

    @GetMapping
    public ResponseEntity<List<TratamientoResponse>> listar(@RequestParam(required = false) Long plagaId,
                                                            @RequestParam(required = false) Long cultivoId) {
        return ResponseEntity.ok(tratamientoService.listar(plagaId, cultivoId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TratamientoResponse> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(tratamientoService.obtenerPorId(id));
    }

    @PostMapping
    public ResponseEntity<TratamientoResponse> crear(@Valid @RequestBody TratamientoRequest request) {
        TratamientoResponse response = tratamientoService.crear(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}

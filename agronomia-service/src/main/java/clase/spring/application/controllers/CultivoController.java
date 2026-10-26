package clase.spring.application.controllers;

import clase.spring.application.dtos.request.CultivoRequest;
import clase.spring.application.dtos.response.CultivoResponse;
import clase.spring.application.services.CultivoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/agronomia/cultivos")
@RequiredArgsConstructor
public class CultivoController {

    private final CultivoService cultivoService;

    @GetMapping
    public ResponseEntity<List<CultivoResponse>> listar() {
        return ResponseEntity.ok(cultivoService.listar());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CultivoResponse> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(cultivoService.obtenerPorId(id));
    }

    @PostMapping
    public ResponseEntity<CultivoResponse> crear(@Valid @RequestBody CultivoRequest request) {
        CultivoResponse response = cultivoService.crear(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}

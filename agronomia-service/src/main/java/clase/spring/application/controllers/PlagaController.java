package clase.spring.application.controllers;

import clase.spring.application.dtos.request.PlagaRequest;
import clase.spring.application.dtos.response.PlagaResponse;
import clase.spring.application.services.PlagaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/agronomia/plagas")
@RequiredArgsConstructor
public class PlagaController {

    private final PlagaService plagaService;

    @GetMapping
    public ResponseEntity<List<PlagaResponse>> listar() {
        return ResponseEntity.ok(plagaService.listar());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PlagaResponse> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(plagaService.obtenerPorId(id));
    }

    @PostMapping
    public ResponseEntity<PlagaResponse> crear(@Valid @RequestBody PlagaRequest request) {
        PlagaResponse response = plagaService.crear(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}

package clase.spring.application.controllers;

import clase.spring.application.dtos.request.AgroquimicoRequest;
import clase.spring.application.dtos.response.AgroquimicoResponse;
import clase.spring.application.services.AgroquimicoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/agronomia/agroquimicos")
@RequiredArgsConstructor
public class AgroquimicoController {

    private final AgroquimicoService agroquimicoService;

    @GetMapping
    public ResponseEntity<List<AgroquimicoResponse>> listar() {
        return ResponseEntity.ok(agroquimicoService.listar());
    }

    @GetMapping("/{id}")
    public ResponseEntity<AgroquimicoResponse> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(agroquimicoService.obtenerPorId(id));
    }

    @PostMapping
    public ResponseEntity<AgroquimicoResponse> crear(@Valid @RequestBody AgroquimicoRequest request) {
        AgroquimicoResponse response = agroquimicoService.crear(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}

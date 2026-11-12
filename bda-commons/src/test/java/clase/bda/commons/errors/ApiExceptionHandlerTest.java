package clase.bda.commons.errors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ApiExceptionHandlerTest {

    @RestController
    static class ControladorDePrueba {
        @GetMapping("/regla")
        String regla() {
            throw new ReglaNegocioException("regla violada");
        }

        @GetMapping("/inexistente")
        String inexistente() {
            throw new RecursoNoEncontradoException("no existe");
        }

        @GetMapping("/caido")
        String caido() {
            throw new ServicioNoDisponibleException("agronomia-service", "timeout");
        }

        // Una excepción propia de un servicio que extiende la base debe recibir el mismo trato
        @GetMapping("/propia")
        String propia() {
            throw new CampoNoEncontradoException(99L);
        }

        @GetMapping("/numero/{n}")
        String numero(@org.springframework.web.bind.annotation.PathVariable Integer n) {
            return "ok";
        }
    }

    static class CampoNoEncontradoException extends RecursoNoEncontradoException {
        CampoNoEncontradoException(Long id) {
            super("Campo no encontrado con ID: " + id);
        }
    }

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new ControladorDePrueba())
                .setControllerAdvice(new ApiExceptionHandler())
                .build();
    }

    @Test
    void reglaDeNegocioEs400() throws Exception {
        mockMvc.perform(get("/regla"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.mensaje").value("regla violada"));
    }

    @Test
    void recursoNoEncontradoEs404() throws Exception {
        mockMvc.perform(get("/inexistente"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensaje").value("no existe"));
    }

    @Test
    void servicioNoDisponibleEs503() throws Exception {
        mockMvc.perform(get("/caido"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.status").value(503));
    }

    @Test
    void lasExcepcionesPropiasDeUnServicioHeredanElComportamiento() throws Exception {
        mockMvc.perform(get("/propia"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensaje").value("Campo no encontrado con ID: 99"));
    }

    @Test
    void unParametroConTipoInvalidoEs400() throws Exception {
        mockMvc.perform(get("/numero/abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }
}

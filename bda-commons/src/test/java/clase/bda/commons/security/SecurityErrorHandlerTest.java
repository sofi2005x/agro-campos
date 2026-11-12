package clase.bda.commons.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SecurityErrorHandlerTest {

    private final ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());
    private final SecurityErrorHandler handler = new SecurityErrorHandler(mapper);

    @Test
    void sinTokenResponde401EnJson() throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();

        handler.commence(new MockHttpServletRequest(), response, new BadCredentialsException("sin token"));

        assertEquals(401, response.getStatus());
        assertTrue(response.getContentType().startsWith("application/json"));
        var json = mapper.readTree(response.getContentAsString());
        assertEquals(401, json.get("status").asInt());
        assertEquals("Unauthorized", json.get("error").asText());
        assertTrue(json.get("mensaje").asText().contains("Autenticación requerida"));
    }

    @Test
    void conRolInsuficienteResponde403EnJson() throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();

        handler.handle(new MockHttpServletRequest(), response, new AccessDeniedException("denegado"));

        assertEquals(403, response.getStatus());
        var json = mapper.readTree(response.getContentAsString());
        assertEquals(403, json.get("status").asInt());
        assertEquals("Forbidden", json.get("error").asText());
    }
}

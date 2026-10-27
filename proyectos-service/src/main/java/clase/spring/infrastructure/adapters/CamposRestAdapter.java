package clase.spring.infrastructure.adapters;

import clase.spring.domain.ports.CamposPort;
import clase.spring.infrastructure.clients.CampoResponse;
import clase.spring.infrastructure.exceptions.CampoNoEncontradoException;
import clase.spring.infrastructure.exceptions.CamposServiceUnavailableException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class CamposRestAdapter implements CamposPort {

    private final RestClient restClient;

    public CamposRestAdapter(
            RestClient.Builder restClientBuilder,
            @Value("${services.campos.url}") String camposServiceUrl
    ) {
        this.restClient = restClientBuilder
                .baseUrl(camposServiceUrl)
                .build();
    }

    @Override
    public CampoInfo buscarCampo(Long campoId) {
        try {
            CampoResponse response = restClient.get()
                    .uri("/api/v1/campos/{id}", campoId)
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError,
                            (request, responseError) -> {
                                if (responseError.getStatusCode().value() == 404) {
                                    throw new CampoNoEncontradoException(campoId);
                                }
                            })
                    .body(CampoResponse.class);

            if (response == null) {
                throw new IllegalStateException(
                        "campos-service devolvió una respuesta vacía"
                );
            }

            return new CampoInfo(
                    response.id(),
                    response.nombre(),
                    response.superficieTotalHas(),
                    response.cantidadLotes()
            );
        } catch (RestClientException exception) {
            throw new CamposServiceUnavailableException(
                    "No se pudo consultar campos-service", exception
            );
        }
    }
}

package fr.plop.integration;

import fr.plop.contexts.connect.presenter.ConnectionController;
import fr.plop.contexts.game.config.template.domain.TemplateInitUseCase;
import fr.plop.contexts.game.config.template.domain.model.Template;
import fr.plop.contexts.game.session.core.presenter.GameSessionController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class GameFirstStartIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @LocalServerPort
    int randomServerPort;

    @Autowired
    private TemplateInitUseCase.OutPort templateInitUseCase;

    @BeforeEach
    void setUp() {
        templateInitUseCase.deleteAll();
        Template.Code code = new Template.Code("TEST_FIRST");
        Template template = new Template(code, "Mon premier jeu");
        templateInitUseCase.create(template);
    }

    @Test
    public void createSession() throws URISyntaxException {

        ConnectionController.ResponseDTO connection = createAuth();

        GameSessionController.GameSessionCreateResponse session = createGameSession(connection.token());

        assertThat(session.id()).isNotNull();
        assertThat(session.label()).isEqualTo("Mon premier jeu");

        //TODO -> Get Session ? Get User one ??

    }

    private ConnectionController.ResponseDTO createAuth() throws URISyntaxException {
        final String baseUrl = "http://localhost:" + randomServerPort + "/connect/";
        URI uri = new URI(baseUrl);

        ConnectionController.RequestDTO request = new ConnectionController.RequestDTO("anyDeviceId");

        ResponseEntity<ConnectionController.ResponseDTO> result = this.restTemplate.postForEntity(uri, request, ConnectionController.ResponseDTO.class);
        return result.getBody();
    }

    private GameSessionController.GameSessionCreateResponse createGameSession(String token) throws URISyntaxException {
        final String baseUrl = "http://localhost:" + randomServerPort + "/sessions/";
        URI uri = new URI(baseUrl);

        GameSessionController.GameSessionCreateRequest request = new GameSessionController.GameSessionCreateRequest("TEST_FIRST");
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.add("Authorization", token);

        ResponseEntity<GameSessionController.GameSessionCreateResponse> result = this.restTemplate
                .exchange(uri, HttpMethod.POST, new HttpEntity<>(request, headers), GameSessionController.GameSessionCreateResponse.class);
        return result.getBody();
    }


}

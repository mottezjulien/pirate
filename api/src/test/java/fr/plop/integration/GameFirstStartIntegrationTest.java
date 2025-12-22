package fr.plop.integration;

import fr.plop.contexts.connect.presenter.ConnectionController;
import fr.plop.contexts.game.config.scenario.domain.model.ScenarioConfig;
import fr.plop.contexts.game.config.template.domain.usecase.TemplateInitUseCase;
import fr.plop.contexts.game.config.template.domain.model.Template;
import fr.plop.contexts.game.session.core.domain.port.GameSessionClearPort;
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
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class GameFirstStartIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @LocalServerPort
    int randomServerPort;

    @Autowired
    private GameSessionClearPort sessionClear;

    @Autowired
    private TemplateInitUseCase.OutPort templateInitUseCase;

    @BeforeEach
    void setUp() {
        sessionClear.clearAll();
        templateInitUseCase.deleteAll();
        Template.Code code = new Template.Code("TEST_FIRST");
        ScenarioConfig scenario = new ScenarioConfig(List.of(new ScenarioConfig.Step()));
        Template template = new Template(code, "Mon premier jeu", scenario);
        templateInitUseCase.create(template);
    }

    @Test
    public void createSession() throws URISyntaxException {
        ConnectionController.ResponseDTO connection = createAuth();
        GameSessionController.GameSessionResponseDTO session = createGameSession(connection.token());
        assertThat(session.id()).isNotNull();
    }

    private ConnectionController.ResponseDTO createAuth() throws URISyntaxException {
        final String baseUrl = "http://localhost:" + randomServerPort + "/connect/";
        URI uri = new URI(baseUrl);

        ConnectionController.RequestDTO request = new ConnectionController.RequestDTO("anyDeviceId");

        ResponseEntity<ConnectionController.ResponseDTO> result = this.restTemplate.postForEntity(uri, request, ConnectionController.ResponseDTO.class);
        return result.getBody();
    }

    private GameSessionController.GameSessionResponseDTO createGameSession(String token) throws URISyntaxException {
        final String baseUrl = "http://localhost:" + randomServerPort + "/sessions/";
        URI uri = new URI(baseUrl);

        GameSessionController.GameSessionCreateRequest request = new GameSessionController.GameSessionCreateRequest("TEST_FIRST");
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.add("Authorization", token);

        ResponseEntity<GameSessionController.GameSessionResponseDTO> result = this.restTemplate
                .exchange(uri, HttpMethod.POST, new HttpEntity<>(request, headers), GameSessionController.GameSessionResponseDTO.class);
        return result.getBody();
    }


}

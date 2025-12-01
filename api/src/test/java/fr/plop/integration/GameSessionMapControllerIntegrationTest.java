package fr.plop.integration;

import fr.plop.contexts.connect.presenter.ConnectionController;
import fr.plop.contexts.game.config.Image.domain.ImageGeneric;
import fr.plop.contexts.game.config.board.domain.model.BoardConfig;
import fr.plop.contexts.game.config.board.domain.model.BoardSpace;
import fr.plop.contexts.game.config.condition.Condition;
import fr.plop.contexts.game.config.map.domain.MapConfig;
import fr.plop.contexts.game.config.map.domain.MapItem;
import fr.plop.contexts.game.config.scenario.domain.model.ScenarioConfig;
import fr.plop.contexts.game.config.template.domain.model.Template;
import fr.plop.contexts.game.config.template.domain.usecase.TemplateInitUseCase;
import fr.plop.contexts.game.session.core.domain.port.GameSessionClearPort;
import fr.plop.contexts.game.session.core.domain.usecase.GameMoveUseCase;
import fr.plop.contexts.game.session.core.presenter.GameSessionController;
import fr.plop.contexts.game.session.core.presenter.GameSessionMoveController;
import fr.plop.generic.enumerate.Priority;
import fr.plop.generic.position.Point;
import fr.plop.generic.position.Rect;
import fr.plop.subs.image.Image;
import fr.plop.subs.image.ImageDetailsResponseDTO;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DisplayName("GameSessionMapController Integration Tests")
public class GameSessionMapControllerIntegrationTest {

    public static final String TEMPLATE_CODE = "TEST_MAPS";
    @Autowired
    private TestRestTemplate restTemplate;

    @LocalServerPort
    int randomServerPort;

    @Autowired
    private GameSessionClearPort sessionClear;
    @Autowired
    private TemplateInitUseCase.OutPort templateInitUseCase;
    @Autowired
    private GameMoveUseCase gameMoveUseCase;

    private final ScenarioConfig scenario = new ScenarioConfig(List.of(new ScenarioConfig.Step(), new ScenarioConfig.Step()));

    private final Rect rect = new Rect(new Point(0.0, 0.0), new Point(20.0, 20.0));
    private final BoardConfig board = new BoardConfig(List.of(new BoardSpace("space", Priority.LOW, List.of(rect))));


    @BeforeEach
    void setUp() {
        sessionClear.clearAll();
        templateInitUseCase.deleteAll();
    }

    @Test
    public void getMapsNoStepNoSpace() throws URISyntaxException {
        final MapItem noSpaceNoStep = new MapItem(imageGeneric(Image.Type.WEB, "siteABC"));
        createTemplateWithMaps(new MapConfig(List.of(noSpaceNoStep)));

        List<ImageDetailsResponseDTO> maps = createSessionAndFindMaps();
        assertThat(maps)
                .hasSize(1)
                .anySatisfy(map -> assertThat(map.id()).isEqualTo(noSpaceNoStep.id().value()));
    }

    @Test
    public void getMapsNoStepNoSpaceAndOneActiveStep_firstStepActiveInBeginning() throws URISyntaxException {
        final MapItem noSpaceNoStep = new MapItem(imageGeneric(Image.Type.WEB, "siteABC"));
        final Condition inFirstStep = new Condition.Step(new Condition.Id(), scenario.steps().getFirst().id());
        final MapItem firstStep = new MapItem(imageGeneric(Image.Type.ASSET, "asset/plop.png"), inFirstStep);
        createTemplateWithMaps(new MapConfig(List.of(noSpaceNoStep, firstStep)));

        List<ImageDetailsResponseDTO> maps = createSessionAndFindMaps();
        assertThat(maps)
                .hasSize(2)
                .anySatisfy(map -> assertThat(map.id()).isEqualTo(noSpaceNoStep.id().value()))
                .anySatisfy(map -> assertThat(map.id()).isEqualTo(firstStep.id().value()));
    }

    @Test
    public void getMapsNoStepNoSpaceButNotNotInvalideStep() throws URISyntaxException {
        final MapItem noSpaceNoStep = new MapItem(imageGeneric(Image.Type.WEB, "siteABC"));
        final Condition inSecondStep = new Condition.Step(new Condition.Id(), scenario.steps().get(1).id());
        final MapItem secondStep = new MapItem(imageGeneric(Image.Type.ASSET, "asset/plop.png"), inSecondStep);
        createTemplateWithMaps(new MapConfig(List.of(noSpaceNoStep, secondStep)));

        List<ImageDetailsResponseDTO> maps = createSessionAndFindMaps();
        assertThat(maps)
                .hasSize(1)
                .anySatisfy(map -> assertThat(map.id()).isEqualTo(noSpaceNoStep.id().value()));
    }


    @Test
    public void getMapsNotActiveSpace() throws URISyntaxException {
        final Condition inSpace = new Condition.InsideSpace(new Condition.Id(), board.spaces().getFirst().id());
        final MapItem inSpaceMap = new MapItem(imageGeneric(Image.Type.ASSET, "asset/plop.png"), inSpace);
        createTemplateWithMaps(new MapConfig(List.of(inSpaceMap)));

        List<ImageDetailsResponseDTO> maps = createSessionAndFindMaps();
        assertThat(maps)
                .hasSize(0);
    }


    @Test
    public void getMapActiveSpace() throws URISyntaxException {
        final Condition inSpace = new Condition.InsideSpace(new Condition.Id(), board.spaces().getFirst().id());
        final MapItem inSpaceMap = new MapItem(imageGeneric(Image.Type.ASSET, "asset/plop.png"), inSpace);
        createTemplateWithMaps(new MapConfig(List.of(inSpaceMap)));

        ConnectionController.ResponseDTO connection = connect();
        GameSessionController.GameSessionResponseDTO session = createGameSession(connection.token());

        move(connection.token(), session.id());

        List<ImageDetailsResponseDTO> maps = getMaps(connection.token(), session.id());

        assertThat(maps)
                .hasSize(1)
                .anySatisfy(map -> assertThat(map.id()).isEqualTo(inSpaceMap.id().value()));
    }


    private ConnectionController.ResponseDTO connect() throws URISyntaxException {
        final String baseUrl = "http://localhost:" + randomServerPort + "/connect/";
        ConnectionController.RequestDTO request = new ConnectionController.RequestDTO("anyDeviceId");
        ResponseEntity<ConnectionController.ResponseDTO> result = this.restTemplate.postForEntity(new URI(baseUrl), request, ConnectionController.ResponseDTO.class);
        return result.getBody();
    }

    private GameSessionController.GameSessionResponseDTO createGameSession(String token) throws URISyntaxException {
        final String baseUrl = "http://localhost:" + randomServerPort + "/sessions/";

        GameSessionController.GameSessionCreateRequest request = new GameSessionController.GameSessionCreateRequest(TEMPLATE_CODE);
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.add("Authorization", token);

        ResponseEntity<GameSessionController.GameSessionResponseDTO> result = restTemplate.exchange(new URI(baseUrl), HttpMethod.POST, new HttpEntity<>(request, headers), GameSessionController.GameSessionResponseDTO.class);
        return result.getBody();
    }

    private void createTemplateWithMaps(MapConfig map) {
        Template template = new Template(new Template.Code(TEMPLATE_CODE), "Test Maps Game", scenario, board, map);
        templateInitUseCase.create(template);
    }

    private List<ImageDetailsResponseDTO> createSessionAndFindMaps() throws URISyntaxException {
        ConnectionController.ResponseDTO connection = connect();
        GameSessionController.GameSessionResponseDTO session = createGameSession(connection.token());
        return getMaps(connection.token(), session.id());
    }

    private List<ImageDetailsResponseDTO> getMaps(String token, String sessionId) throws URISyntaxException {
        final String baseUrl = "http://localhost:" + randomServerPort + "/sessions/" + sessionId + "/maps";
        URI uri = new URI(baseUrl);

        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.add("Authorization", token);
        headers.add("Language", "FR");

        ResponseEntity<ImageDetailsResponseDTO[]> result = this.restTemplate
                .exchange(uri, HttpMethod.GET, new HttpEntity<>(headers), ImageDetailsResponseDTO[].class);
        Assertions.assertNotNull(result.getBody());
        return List.of(result.getBody());
    }

    private void move(String token, String sessionId) throws URISyntaxException {
        final String baseUrl = "http://localhost:" + randomServerPort + "/sessions/" + sessionId + "/move";
        URI uri = new URI(baseUrl);

        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.add("Authorization", token);
        headers.add("Language", "FR");

        GameSessionMoveController.GameMoveRequestDTO request = new GameSessionMoveController.GameMoveRequestDTO(10, 10);

        this.restTemplate.exchange(uri, HttpMethod.POST, new HttpEntity<>(request, headers), Void.class);
    }

    private ImageGeneric imageGeneric(Image.Type type, String imageValue) {
        return new ImageGeneric("", new Image(type, imageValue), List.of());
    }

}

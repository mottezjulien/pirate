package fr.plop.integration;

import fr.plop.contexts.connect.presenter.ConnectionController;
import fr.plop.contexts.game.commun.domain.Game;
import fr.plop.contexts.game.commun.domain.GameProject;
import fr.plop.contexts.game.config.Image.domain.ImageGeneric;
import fr.plop.contexts.game.config.Image.domain.ImageObject;
import fr.plop.contexts.game.config.board.domain.model.BoardConfig;
import fr.plop.contexts.game.config.board.domain.model.BoardSpace;
import fr.plop.contexts.game.config.condition.Condition;
import fr.plop.contexts.game.config.map.domain.MapConfig;
import fr.plop.contexts.game.config.map.domain.MapItem;
import fr.plop.contexts.game.config.scenario.domain.model.ScenarioConfig;
import fr.plop.contexts.game.config.template.domain.model.Template;
import fr.plop.contexts.game.config.template.domain.usecase.TemplateInitUseCase;
import fr.plop.contexts.game.instance.core.domain.port.GameInstanceClearPort;
import fr.plop.contexts.game.instance.core.presenter.GameInstanceController;
import fr.plop.contexts.game.instance.core.presenter.GameInstanceMoveController;
import fr.plop.contexts.game.instance.map.presenter.GameInstanceMapController;
import fr.plop.generic.ImagePoint;
import fr.plop.generic.enumerate.Priority;
import fr.plop.generic.position.Point;
import fr.plop.generic.position.Rectangle;
import fr.plop.subs.image.Image;
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
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DisplayName("GameInstanceMapController Integration Tests")
public class GameInstanceMapControllerIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @LocalServerPort
    int randomServerPort;

    @Autowired
    private GameInstanceClearPort sessionClear;
    @Autowired
    private TemplateInitUseCase.OutPort templateInitUseCase;



    private final Template.Id templateId = new Template.Id();
    private final ScenarioConfig scenario = new ScenarioConfig(List.of(new ScenarioConfig.Step(), new ScenarioConfig.Step()));
    private final Rectangle rectangle = new Rectangle(Point.from(0.0, 0.0), Point.from(20.0, 20.0));
    private final BoardConfig board = new BoardConfig(List.of(new BoardSpace("space", Priority.LOW, List.of(rectangle))));


    @BeforeEach
    void setUp() {
        sessionClear.clearAll();
        templateInitUseCase.deleteAll();
    }

    @Test
    public void getMapsNoStepNoSpace() throws URISyntaxException {
        final MapItem noSpaceNoStep = new MapItem(imageGeneric(Image.Type.WEB, "siteABC"));
        createTemplateWithMaps(new MapConfig(List.of(noSpaceNoStep)));

        List<GameInstanceMapController.ResponseDTO> maps = startSessionAndFindMaps();
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

        List<GameInstanceMapController.ResponseDTO> maps = startSessionAndFindMaps();
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

        List<GameInstanceMapController.ResponseDTO> maps = startSessionAndFindMaps();
        assertThat(maps)
                .hasSize(1)
                .anySatisfy(map -> assertThat(map.id()).isEqualTo(noSpaceNoStep.id().value()));
    }


    @Test
    public void getMapsNotActiveSpace() throws URISyntaxException {
        final Condition inSpace = new Condition.InsideSpace(new Condition.Id(), board.spaces().getFirst().id());
        final MapItem inSpaceMap = new MapItem(imageGeneric(Image.Type.ASSET, "asset/plop.png"), inSpace);
        createTemplateWithMaps(new MapConfig(List.of(inSpaceMap)));

        List<GameInstanceMapController.ResponseDTO> maps = startSessionAndFindMaps();
        assertThat(maps)
                .hasSize(0);
    }


    @Test
    public void getMapWithActiveSpace() throws URISyntaxException {
        final Condition inSpace = new Condition.InsideSpace(new Condition.Id(), board.spaces().getFirst().id());
        final MapItem mapItem = new MapItem(imageGeneric(Image.Type.ASSET, "asset/plop.png"), inSpace);
        createTemplateWithMaps(new MapConfig(List.of(mapItem)));

        ConnectionController.ResponseDTO connection = connect();
        GameInstanceController.ResponseDTO connectionSession = createGame(connection.token());

        assertThat(getMaps(connectionSession.auth().token(), connectionSession.id())).hasSize(0);

        move(connectionSession.auth().token(), connectionSession.id());

        assertThat(getMaps(connectionSession.auth().token(), connectionSession.id())).hasSize(1)
                .anySatisfy(map -> assertThat(map.id()).isEqualTo(mapItem.id().value()));
    }


    @Test
    public void getMapObjectsPostionsAndPointer() throws URISyntaxException {


        List<ImageObject> objects = List.of(
                new ImageObject._Image(new ImageObject.Atom("", new ImagePoint(0.1, 0.9), Optional.empty()), new Image(Image.Type.ASSET, "asset/object1.png")),
                new ImageObject.Point(new ImageObject.Atom("", new ImagePoint(0.8, 0.2), Optional.empty()), "blue")
        );
        ImageGeneric imageGeneric = new ImageGeneric("", new Image(Image.Type.ASSET, "asset/plop.png"), objects);

        final List<MapItem.Position> positions = List.of(
                new MapItem.Position(board.spaces().getFirst().id(), new ImagePoint(0.6, 0.4), Priority.MEDIUM));
        final MapItem mapItem = new MapItem(imageGeneric,
                Optional.of(new Image(Image.Type.ASSET, "asset/pointer.png")), positions);
        createTemplateWithMaps(new MapConfig(List.of(mapItem)));

        ConnectionController.ResponseDTO connection = connect();
        GameInstanceController.ResponseDTO connectionSession = createGame(connection.token());

        move(connectionSession.auth().token(), connectionSession.id());

        assertThat(getMaps(connectionSession.auth().token(), connectionSession.id()))
                .hasSize(1)
                .anySatisfy(map -> {
                    assertThat(map.id()).isEqualTo(mapItem.id().value());
                    GameInstanceMapController.ResponseDTO.Pointer pointer = map.pointer();
                    assertThat(pointer).isNotNull();
                    assertThat(pointer.image().type()).isEqualTo("ASSET");
                    assertThat(pointer.image().value()).isEqualTo("asset/pointer.png");
                    assertThat(pointer.position().top()).isEqualTo(0.6);
                    assertThat(pointer.position().left()).isEqualTo(0.4);
                    assertThat(map.image().image().type()).isEqualTo("ASSET");
                    assertThat(map.image().image().value()).isEqualTo("asset/plop.png");
                    assertThat(map.image().objects()).hasSize(2)
                            .anySatisfy(object -> {
                                assertThat(object.type()).isEqualTo("IMAGE");
                                assertThat(object.image().type()).isEqualTo("ASSET");
                                assertThat(object.image().value()).isEqualTo("asset/object1.png");
                                assertThat(object.position().top()).isEqualTo(0.1);
                                assertThat(object.position().left()).isEqualTo(0.9);
                            })
                            .anySatisfy(object -> {
                                assertThat(object.type()).isEqualTo("POINT");
                                assertThat(object.point().color()).isEqualTo("blue");
                                assertThat(object.position().top()).isEqualTo(0.8);
                                assertThat(object.position().left()).isEqualTo(0.2);
                            });

                });
    }

    @Test
    public void getMapObjects() throws URISyntaxException {

        final List<MapItem.Position> positions = List.of(
                new MapItem.Position(board.spaces().getFirst().id(), new ImagePoint(0.6, 0.4), Priority.MEDIUM));
        final MapItem mapItem = new MapItem(imageGeneric(Image.Type.ASSET, "asset/plop.png"),
                Optional.of(new Image(Image.Type.ASSET, "asset/pointer.png")), positions);
        createTemplateWithMaps(new MapConfig(List.of(mapItem)));

        ConnectionController.ResponseDTO connection = connect();
        GameInstanceController.ResponseDTO connectionSession = createGame(connection.token());

        move(connectionSession.auth().token(), connectionSession.id());

        assertThat(getMaps(connectionSession.auth().token(), connectionSession.id()))
                .hasSize(1)
                .anySatisfy(map -> {
                    assertThat(map.id()).isEqualTo(mapItem.id().value());
                    GameInstanceMapController.ResponseDTO.Pointer pointer = map.pointer();
                    assertThat(pointer).isNotNull();
                    assertThat(pointer.image().type()).isEqualTo("ASSET");
                    assertThat(pointer.image().value()).isEqualTo("asset/pointer.png");
                    assertThat(pointer.position().top()).isEqualTo(0.6);
                    assertThat(pointer.position().left()).isEqualTo(0.4);
                });
    }


    private ConnectionController.ResponseDTO connect() throws URISyntaxException {
        final String baseUrl = "http://localhost:" + randomServerPort + "/connect/";
        ConnectionController.RequestDTO request = new ConnectionController.RequestDTO("anyDeviceId");
        ResponseEntity<ConnectionController.ResponseDTO> result = this.restTemplate.postForEntity(new URI(baseUrl), request, ConnectionController.ResponseDTO.class);
        return result.getBody();
    }

    private GameInstanceController.ResponseDTO createGame(String token) throws URISyntaxException {
        final String baseUrl = "http://localhost:" + randomServerPort + "/instances/";

        GameInstanceController.CreateRequestDTO request = new GameInstanceController.CreateRequestDTO(templateId.value());
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.add("Authorization", token);

        ResponseEntity<GameInstanceController.ResponseDTO> result = restTemplate.exchange(new URI(baseUrl), HttpMethod.POST, new HttpEntity<>(request, headers), GameInstanceController.ResponseDTO.class);
        return result.getBody();
    }

    private void startGameInstance(String token, String sessionId) throws URISyntaxException {
        final String baseUrl = "http://localhost:" + randomServerPort +  "/instances/" + sessionId + "/start";

        GameInstanceController.CreateRequestDTO request = new GameInstanceController.CreateRequestDTO(templateId.value());
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.add("Authorization", token);

        restTemplate.exchange(new URI(baseUrl), HttpMethod.POST, new HttpEntity<>(request, headers), GameInstanceController.ResponseDTO.class);
    }

    private void createTemplateWithMaps(MapConfig map) {
        Template template = Template.builder()
                .id(templateId)
                .scenario(scenario)
                .board(board)
                .map(map)
                .build();
        Game.Id gameId = templateInitUseCase.findOrCreateGame(new GameProject.Code("map-test"), new Game.Version("1.0.0"));
        templateInitUseCase.createOrUpdate(gameId, template);
    }

    private List<GameInstanceMapController.ResponseDTO> startSessionAndFindMaps() throws URISyntaxException {
        ConnectionController.ResponseDTO connection = connect();
        GameInstanceController.ResponseDTO connectionSession = createGame(connection.token());
        startGameInstance(connectionSession.auth().token(), connectionSession.id());
        return getMaps(connectionSession.auth().token(), connectionSession.id());
    }

    private List<GameInstanceMapController.ResponseDTO> getMaps(String token, String sessionId) throws URISyntaxException {
        final String baseUrl = "http://localhost:" + randomServerPort + "/instances/" + sessionId + "/maps";
        URI uri = new URI(baseUrl);

        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.add("Authorization", token);
        headers.add("Language", "FR");

        ResponseEntity<GameInstanceMapController.ResponseDTO[]> result = this.restTemplate
                .exchange(uri, HttpMethod.GET, new HttpEntity<>(headers), GameInstanceMapController.ResponseDTO[].class);
        Assertions.assertNotNull(result.getBody());
        return List.of(result.getBody());
    }

    private void move(String token, String sessionId) throws URISyntaxException {
        final String baseUrl = "http://localhost:" + randomServerPort + "/instances/" + sessionId + "/move";
        URI uri = new URI(baseUrl);

        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.add("Authorization", token);
        headers.add("Language", "FR");

        GameInstanceMoveController.GameMoveRequestDTO request = new GameInstanceMoveController.GameMoveRequestDTO(10, 10);

        this.restTemplate.exchange(uri, HttpMethod.POST, new HttpEntity<>(request, headers), Void.class);
    }

    private ImageGeneric imageGeneric(Image.Type type, String imageValue) {
        return new ImageGeneric("", new Image(type, imageValue), List.of());
    }

}

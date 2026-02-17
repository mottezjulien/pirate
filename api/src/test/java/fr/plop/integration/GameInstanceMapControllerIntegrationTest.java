package fr.plop.integration;

import fr.plop.contexts.connect.presenter.ConnectionController;
import fr.plop.contexts.game.commun.domain.Game;
import fr.plop.contexts.game.commun.domain.GameProject;
import fr.plop.contexts.game.config.board.domain.model.BoardConfig;
import fr.plop.contexts.game.config.board.domain.model.BoardSpace;
import fr.plop.contexts.game.config.condition.Condition;
import fr.plop.contexts.game.config.map.domain.MapConfig;
import fr.plop.contexts.game.config.map.domain.MapItem;
import fr.plop.contexts.game.config.map.domain.MapObject;
import fr.plop.contexts.game.config.scenario.domain.model.ScenarioConfig;
import fr.plop.contexts.game.config.template.domain.model.Template;
import fr.plop.contexts.game.config.template.domain.usecase.TemplateInitUseCase;
import fr.plop.contexts.game.instance.core.domain.port.GameInstanceClearPort;
import fr.plop.contexts.game.instance.core.presenter.GameInstanceController;
import fr.plop.contexts.game.instance.core.presenter.GameInstanceMoveController;
import fr.plop.contexts.game.instance.map.presenter.GameInstanceMapController;
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

import java.math.BigDecimal;
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

    // Bounds for maps (different from board rectangles - this is the visual area of the map image)
    private final Rectangle mapBounds = new Rectangle(Point.from(0.0, 0.0), Point.from(20.0, 20.0));


    @BeforeEach
    void setUp() {
        sessionClear.clearAll();
        templateInitUseCase.deleteAll();
    }

    @Test
    public void getMapsNoStepNoSpace() throws URISyntaxException {
        final MapItem noSpaceNoStep = new MapItem("Map", new Image(fr.plop.subs.image.Image.Type.WEB, "siteABC"), mapBounds);
        createTemplateWithMaps(new MapConfig(List.of(noSpaceNoStep)));

        List<GameInstanceMapController.ResponseDTO> maps = startAndFindMaps();
        assertThat(maps)
                .hasSize(1)
                .anySatisfy(map -> assertThat(map.id()).isEqualTo(noSpaceNoStep.id().value()));
    }

    @Test
    public void getMapsNoStepNoSpaceAndOneActiveStep_firstStepActiveInBeginning() throws URISyntaxException {
        final MapItem noSpaceNoStep = new MapItem("Map1", new Image(fr.plop.subs.image.Image.Type.WEB, "siteABC"), mapBounds);
        final Condition inFirstStep = new Condition.Step(new Condition.Id(), scenario.steps().getFirst().id());
        final MapItem firstStep = new MapItem("Map2", new Image(fr.plop.subs.image.Image.Type.ASSET, "asset/plop.png"), mapBounds, inFirstStep);
        createTemplateWithMaps(new MapConfig(List.of(noSpaceNoStep, firstStep)));

        List<GameInstanceMapController.ResponseDTO> maps = startAndFindMaps();
        assertThat(maps)
                .hasSize(2)
                .anySatisfy(map -> assertThat(map.id()).isEqualTo(noSpaceNoStep.id().value()))
                .anySatisfy(map -> assertThat(map.id()).isEqualTo(firstStep.id().value()));
    }

    @Test
    public void getMapsNoStepNoSpaceButNotNotInvalideStep() throws URISyntaxException {
        final MapItem noSpaceNoStep = new MapItem("Map1", new Image(fr.plop.subs.image.Image.Type.WEB, "siteABC"), mapBounds);
        final Condition inSecondStep = new Condition.Step(new Condition.Id(), scenario.steps().get(1).id());
        final MapItem secondStep = new MapItem("Map2", new Image(fr.plop.subs.image.Image.Type.ASSET, "asset/plop.png"), mapBounds, inSecondStep);
        createTemplateWithMaps(new MapConfig(List.of(noSpaceNoStep, secondStep)));

        List<GameInstanceMapController.ResponseDTO> maps = startAndFindMaps();
        assertThat(maps)
                .hasSize(1)
                .anySatisfy(map -> assertThat(map.id()).isEqualTo(noSpaceNoStep.id().value()));
    }


    @Test
    public void getMapsNotActiveSpace() throws URISyntaxException {
        final Condition inSpace = new Condition.InsideSpace(new Condition.Id(), board.spaces().getFirst().id());
        final MapItem inSpaceMap = new MapItem("Map", new Image(fr.plop.subs.image.Image.Type.ASSET, "asset/plop.png"), mapBounds, inSpace);
        createTemplateWithMaps(new MapConfig(List.of(inSpaceMap)));

        List<GameInstanceMapController.ResponseDTO> maps = startAndFindMaps();
        assertThat(maps)
                .hasSize(0);
    }


    @Test
    public void getMapWithActiveSpace() throws URISyntaxException {
        final Condition inSpace = new Condition.InsideSpace(new Condition.Id(), board.spaces().getFirst().id());
        final MapItem mapItem = new MapItem("Map", new Image(fr.plop.subs.image.Image.Type.ASSET, "asset/plop.png"), mapBounds, inSpace);
        createTemplateWithMaps(new MapConfig(List.of(mapItem)));

        ConnectionController.ResponseDTO connection = connect();
        GameInstanceController.ResponseDTO responseDTO = createGame(connection.token());

        assertThat(getMaps(responseDTO.auth().token(), responseDTO.id())).hasSize(0);

        move(responseDTO.auth().token(), responseDTO.id());

        assertThat(getMaps(responseDTO.auth().token(), responseDTO.id())).hasSize(1)
                .anySatisfy(map -> assertThat(map.id()).isEqualTo(mapItem.id().value()));
    }


    @Test
    public void getMapObjectsAndPointer() throws URISyntaxException {
        // Create map objects with lat/lng positions (within the map bounds)
        Point objectPosition1 = fr.plop.generic.position.Point.from(2.0, 18.0); // lat 2, lng 18
        Point objectPosition2 = fr.plop.generic.position.Point.from(16.0, 4.0); // lat 16, lng 4

        List<MapObject> objects = List.of(
                new MapObject._Image(
                        new MapObject.Atom("object1", objectPosition1, Optional.empty()),
                        new Image(fr.plop.subs.image.Image.Type.ASSET, "asset/object1.png")),
                new MapObject.Point(
                        new MapObject.Atom("object2", objectPosition2, Optional.empty()),
                        "blue")
        );

        final MapItem mapItem = new MapItem(
                "MapWithObjects",
                new Image(fr.plop.subs.image.Image.Type.ASSET, "asset/plop.png"),
                mapBounds,
                Optional.of(new Image(fr.plop.subs.image.Image.Type.ASSET, "asset/pointer.png")),
                objects);
        createTemplateWithMaps(new MapConfig(List.of(mapItem)));

        ConnectionController.ResponseDTO connection = connect();
        GameInstanceController.ResponseDTO responseDTO = createGame(connection.token());

        List<GameInstanceMapController.ResponseDTO> maps = getMaps(responseDTO.auth().token(), responseDTO.id());

        assertThat(maps)
                .hasSize(1)
                .anySatisfy(map -> {
                    assertThat(map.id()).isEqualTo(mapItem.id().value());

                    // Check pointer
                    GameInstanceMapController.ImageDTO pointer = map.pointer();
                    assertThat(pointer).isNotNull();
                    assertThat(pointer.type()).isEqualTo("ASSET");
                    assertThat(pointer.value()).isEqualTo("asset/pointer.png");

                    // Check image
                    assertThat(map.image().type()).isEqualTo("ASSET");
                    assertThat(map.image().value()).isEqualTo("asset/plop.png");

                    // Check bounds
                    assertThat(map.bounds()).isNotNull();
                    assertThat(map.bounds().bottomLeft().lat()).isEqualByComparingTo(BigDecimal.ZERO);
                    assertThat(map.bounds().bottomLeft().lng()).isEqualByComparingTo(BigDecimal.ZERO);
                    assertThat(map.bounds().topRight().lat()).isEqualByComparingTo(BigDecimal.valueOf(20.0));
                    assertThat(map.bounds().topRight().lng()).isEqualByComparingTo(BigDecimal.valueOf(20.0));

                    // Check objects with lat/lng positions
                    assertThat(map.objects()).hasSize(2)
                            .anySatisfy(object -> {
                                assertThat(object.type()).isEqualTo("IMAGE");
                                assertThat(object.image().type()).isEqualTo("ASSET");
                                assertThat(object.image().value()).isEqualTo("asset/object1.png");
                                assertThat(object.position().lat()).isEqualByComparingTo(BigDecimal.valueOf(2.0));
                                assertThat(object.position().lng()).isEqualByComparingTo(BigDecimal.valueOf(18.0));
                            })
                            .anySatisfy(object -> {
                                assertThat(object.type()).isEqualTo("POINT");
                                assertThat(object.color()).isEqualTo("blue");
                                assertThat(object.position().lat()).isEqualByComparingTo(BigDecimal.valueOf(16.0));
                                assertThat(object.position().lng()).isEqualByComparingTo(BigDecimal.valueOf(4.0));
                            });

                });
    }

    @Test
    public void getMapWithPointerOnly() throws URISyntaxException {
        final MapItem mapItem = new MapItem(
                "MapWithPointer",
                new Image(fr.plop.subs.image.Image.Type.ASSET, "asset/plop.png"),
                mapBounds,
                Optional.of(new Image(fr.plop.subs.image.Image.Type.ASSET, "asset/pointer.png")),
                List.of());
        createTemplateWithMaps(new MapConfig(List.of(mapItem)));

        ConnectionController.ResponseDTO connection = connect();
        GameInstanceController.ResponseDTO responseDTO = createGame(connection.token());

        List<GameInstanceMapController.ResponseDTO> maps = getMaps(responseDTO.auth().token(), responseDTO.id());

        assertThat(maps)
                .hasSize(1)
                .anySatisfy(map -> {
                    assertThat(map.id()).isEqualTo(mapItem.id().value());
                    GameInstanceMapController.ImageDTO pointer = map.pointer();
                    assertThat(pointer).isNotNull();
                    assertThat(pointer.type()).isEqualTo("ASSET");
                    assertThat(pointer.value()).isEqualTo("asset/pointer.png");
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

    private void startGameInstance(String token, String instanceId) throws URISyntaxException {
        final String baseUrl = "http://localhost:" + randomServerPort +  "/instances/" + instanceId + "/start";

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

    private List<GameInstanceMapController.ResponseDTO> startAndFindMaps() throws URISyntaxException {
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

        // Send the spaceIds that the player is now in
        List<String> spaceIds = List.of(board.spaces().getFirst().id().value());
        GameInstanceMoveController.GameMoveRequestDTO request = new GameInstanceMoveController.GameMoveRequestDTO(spaceIds);

        this.restTemplate.exchange(uri, HttpMethod.POST, new HttpEntity<>(request, headers), Void.class);
    }

}

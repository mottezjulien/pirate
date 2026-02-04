package fr.plop.integration;

import fr.plop.contexts.connect.presenter.ConnectionController;
import fr.plop.contexts.game.commun.domain.Game;
import fr.plop.contexts.game.commun.domain.GameProject;
import fr.plop.contexts.game.config.board.domain.model.BoardConfig;
import fr.plop.contexts.game.config.board.domain.model.BoardSpace;
import fr.plop.contexts.game.config.condition.Condition;
import fr.plop.contexts.game.config.consequence.Consequence;
import fr.plop.contexts.game.config.inventory.domain.model.GameConfigInventoryItem;
import fr.plop.contexts.game.config.inventory.domain.model.InventoryConfig;
import fr.plop.contexts.game.config.scenario.domain.model.Possibility;
import fr.plop.contexts.game.config.scenario.domain.model.PossibilityRecurrence;
import fr.plop.contexts.game.config.scenario.domain.model.PossibilityTrigger;
import fr.plop.contexts.game.config.scenario.domain.model.ScenarioConfig;
import fr.plop.contexts.game.config.template.domain.model.Template;
import fr.plop.contexts.game.config.template.domain.usecase.TemplateInitUseCase;
import fr.plop.contexts.game.instance.core.domain.port.GameInstanceClearPort;
import fr.plop.contexts.game.instance.core.presenter.GameInstanceController;
import fr.plop.contexts.game.instance.push.PushPort;
import fr.plop.contexts.game.instance.scenario.domain.model.ScenarioSessionState;
import fr.plop.contexts.game.instance.scenario.presenter.GameInstanceScenarioController;
import fr.plop.generic.enumerate.Priority;
import fr.plop.generic.position.Point;
import fr.plop.generic.position.Rectangle;
import fr.plop.subs.i18n.domain.I18n;
import fr.plop.subs.i18n.domain.Language;
import fr.plop.subs.image.Image;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test for ACTION OBJECT WITH functionality.
 *
 * Tests that using an equipped item (like a shovel) only triggers consequences
 * when the player is in the correct location (condition InsideSpace).
 *
 * Scenario: "Creuser (ACTION OBJECT WITH PELLE) au bon endroit (TREASURE_ZONE)"
 * - Using shovel OUTSIDE treasure zone → NO consequence
 * - Using shovel INSIDE treasure zone → SUCCESS consequence
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class InventoryActionWithConditionIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @LocalServerPort
    int randomServerPort;

    @Autowired
    private GameInstanceClearPort sessionClear;

    @Autowired
    private TemplateInitUseCase.OutPort templateInitUseCase;

    @MockitoBean
    private PushPort pushPort;

    private Template template;

    // IDs used in the template
    private static final BoardSpace.Id TREASURE_ZONE_ID = new BoardSpace.Id("TREASURE_ZONE");
    private static final GameConfigInventoryItem.Id SHOVEL_ID = new GameConfigInventoryItem.Id("PELLE");
    private static final ScenarioConfig.Target.Id TARGET_TREASURE_ID = new ScenarioConfig.Target.Id("TARGET_TREASURE_FOUND");
    private static final ScenarioConfig.Step.Id STEP_DIG_ID = new ScenarioConfig.Step.Id("STEP_DIG");

    // Coordinates
    private static final double TREASURE_LAT_MIN = 48.8580;
    private static final double TREASURE_LAT_MAX = 48.8584;
    private static final double TREASURE_LNG_MIN = 2.2945;
    private static final double TREASURE_LNG_MAX = 2.2950;

    // Inside treasure zone
    private static final double INSIDE_LAT = 48.8582;
    private static final double INSIDE_LNG = 2.2947;

    // Outside treasure zone (north of the zone)
    private static final double OUTSIDE_LAT = 48.8590;
    private static final double OUTSIDE_LNG = 2.2947;

    @BeforeEach
    void setUp() {
        template = buildTemplate();
        templateInitUseCase.createOrUpdate(
                templateInitUseCase.findOrCreateGame(
                        new GameProject.Code("ACTION_CONDITION_TEST_" + System.currentTimeMillis()),
                        new Game.Version("1.0.0")
                ),
                template
        );
    }

    private Template buildTemplate() {
        // 1. Board - treasure zone
        BoardSpace treasureZone = new BoardSpace(
                TREASURE_ZONE_ID,
                "Zone du trésor",
                Priority.MEDIUM,
                List.of(new Rectangle(
                        Point.from(TREASURE_LAT_MIN, TREASURE_LNG_MIN),
                        Point.from(TREASURE_LAT_MAX, TREASURE_LNG_MAX)
                ))
        );
        BoardConfig boardConfig = new BoardConfig(List.of(treasureZone));

        // 2. Inventory - shovel (equippable)
        GameConfigInventoryItem shovel = new GameConfigInventoryItem(
                SHOVEL_ID,
                i18n("Pelle", "Shovel"),
                new Image(Image.Type.ASSET, "items/shovel.png"),
                Optional.of(i18n("Une pelle pour creuser", "A shovel for digging")),
                GameConfigInventoryItem.Type.UNIQUE,
                1, // Start with 1 shovel
                Optional.empty(),
                GameConfigInventoryItem.ActionType.EQUIPPABLE
        );
        InventoryConfig inventoryConfig = new InventoryConfig(
                new InventoryConfig.Id(),
                List.of(shovel),
                List.of()
        );

        // 3. Scenario
        ScenarioConfig.Target targetTreasure = new ScenarioConfig.Target(
                TARGET_TREASURE_ID,
                i18n("Trouver le trésor", "Find the treasure"),
                Optional.empty(),
                false,
                List.of(),
                Optional.empty()
        );

        // Possibility 1: Give shovel when step becomes active
        Possibility giveShovelPossibility = new Possibility(
                new PossibilityRecurrence.Times(1),
                new PossibilityTrigger.StepActive(new PossibilityTrigger.Id(), STEP_DIG_ID),
                List.of(new Consequence.InventoryAddItem(new Consequence.Id(), SHOVEL_ID))
        );

        // Possibility 2: Use shovel (InventoryItemAction) + Condition: Inside treasure zone
        Condition insideZoneCondition = new Condition.InsideSpace(new Condition.Id(), TREASURE_ZONE_ID);

        Possibility digTreasurePossibility = new Possibility(
                new PossibilityRecurrence.Always(),
                new PossibilityTrigger.InventoryItemAction(new PossibilityTrigger.Id(), SHOVEL_ID),
                insideZoneCondition, // CONDITION: must be inside treasure zone
                List.of(new Consequence.ScenarioTarget(new Consequence.Id(), TARGET_TREASURE_ID, ScenarioSessionState.SUCCESS))
        );

        ScenarioConfig.Step step = new ScenarioConfig.Step(
                STEP_DIG_ID,
                i18n("Creuser le trésor", "Dig for treasure"),
                Optional.empty(),
                0,
                List.of(targetTreasure),
                List.of(giveShovelPossibility, digTreasurePossibility)
        );

        ScenarioConfig scenarioConfig = new ScenarioConfig(List.of(step));

        // 4. Build template
        return Template.builder()
                .board(boardConfig)
                .inventory(inventoryConfig)
                .scenario(scenarioConfig)
                .build();
    }

    private static I18n i18n(String fr, String en) {
        return new I18n(Map.of(Language.FR, fr, Language.EN, en));
    }

    @Test
    public void useEquippedItem_outsideZone_doesNotTriggerConsequence() throws URISyntaxException, InterruptedException {
        // 1. Create connection and session
        ConnectionController.ResponseDTO connection = createAuth();
        GameInstanceController.ResponseDTO session = createGame(connection.token());
        assertThat(session.id()).isNotNull();

        // 2. Start the session
        startGame(session.auth().token(), session.id());
        Thread.sleep(500);

        // 3. Move OUTSIDE the treasure zone
        sendPosition(session.auth().token(), session.id(), OUTSIDE_LAT, OUTSIDE_LNG);
        Thread.sleep(500);

        // 4. Get the actual session item ID for the shovel
        String shovelSessionId = findInventoryItemSessionId(session.auth().token(), session.id(), "Pelle");

        // 5. Equip the shovel
        equipItem(session.auth().token(), session.id(), shovelSessionId);
        Thread.sleep(200);

        // 6. Use the equipped shovel (outside zone)
        useEquippedItem(session.auth().token(), session.id(), shovelSessionId);
        Thread.sleep(500);

        // 7. Check goals - TARGET_TREASURE should NOT be done (condition not met)
        GameInstanceScenarioController.GameGoalResponseDTO[] goals = getGoals(session.auth().token(), session.id());
        assertThat(goals).isNotEmpty();

        boolean treasureFound = Arrays.stream(goals)
                .flatMap(goal -> goal.targets().stream())
                .anyMatch(target -> target.id().contains("TARGET_TREASURE_FOUND") && target.done());

        assertThat(treasureFound)
                .as("Treasure should NOT be found when using shovel OUTSIDE the zone")
                .isFalse();
    }

    @Test
    public void useEquippedItem_insideZone_triggersConsequence() throws URISyntaxException, InterruptedException {
        // 1. Create connection and session
        ConnectionController.ResponseDTO connection = createAuth();
        GameInstanceController.ResponseDTO session = createGame(connection.token());
        assertThat(session.id()).isNotNull();

        // 2. Start the session
        startGame(session.auth().token(), session.id());
        Thread.sleep(500);

        // 3. Move INSIDE the treasure zone
        sendPosition(session.auth().token(), session.id(), INSIDE_LAT, INSIDE_LNG);
        Thread.sleep(500);

        // 4. Get the actual session item ID for the shovel
        String shovelSessionId = findInventoryItemSessionId(session.auth().token(), session.id(), "Pelle");

        // 5. Equip the shovel
        equipItem(session.auth().token(), session.id(), shovelSessionId);
        Thread.sleep(200);

        // 6. Use the equipped shovel (inside zone)
        useEquippedItem(session.auth().token(), session.id(), shovelSessionId);
        Thread.sleep(500);

        // 7. Check goals - TARGET_TREASURE should be SUCCESS
        GameInstanceScenarioController.GameGoalResponseDTO[] goals = getGoals(session.auth().token(), session.id());
        assertThat(goals).isNotEmpty();

        boolean treasureFound = Arrays.stream(goals)
                .flatMap(goal -> goal.targets().stream())
                .anyMatch(target -> target.id().contains("TARGET_TREASURE_FOUND") && target.done());

        assertThat(treasureFound)
                .as("Treasure SHOULD be found when using shovel INSIDE the zone")
                .isTrue();
    }

    @Test
    public void useEquippedItem_moveInsideThenUse_triggersConsequence() throws URISyntaxException, InterruptedException {
        // Test: Start outside, equip, move inside, then use
        ConnectionController.ResponseDTO connection = createAuth();
        GameInstanceController.ResponseDTO session = createGame(connection.token());

        startGame(session.auth().token(), session.id());
        Thread.sleep(500);

        // Get the actual session item ID for the shovel
        String shovelSessionId = findInventoryItemSessionId(session.auth().token(), session.id(), "Pelle");

        // Start OUTSIDE
        sendPosition(session.auth().token(), session.id(), OUTSIDE_LAT, OUTSIDE_LNG);
        Thread.sleep(300);

        // Equip the shovel while outside
        equipItem(session.auth().token(), session.id(), shovelSessionId);
        Thread.sleep(200);

        // Move INSIDE the zone
        sendPosition(session.auth().token(), session.id(), INSIDE_LAT, INSIDE_LNG);
        Thread.sleep(500);

        // Now use the shovel (should trigger because we're inside)
        useEquippedItem(session.auth().token(), session.id(), shovelSessionId);
        Thread.sleep(500);

        // Check result
        GameInstanceScenarioController.GameGoalResponseDTO[] goals = getGoals(session.auth().token(), session.id());
        boolean treasureFound = Arrays.stream(goals)
                .flatMap(goal -> goal.targets().stream())
                .anyMatch(target -> target.id().contains("TARGET_TREASURE_FOUND") && target.done());

        assertThat(treasureFound).isTrue();
    }

    // ========== Helper methods ==========

    private ConnectionController.ResponseDTO createAuth() throws URISyntaxException {
        final String baseUrl = "http://localhost:" + randomServerPort + "/connect/";
        URI uri = new URI(baseUrl);

        ConnectionController.RequestDTO request = new ConnectionController.RequestDTO("action-condition-device-" + System.currentTimeMillis());
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        ResponseEntity<ConnectionController.ResponseDTO> result = this.restTemplate
                .exchange(uri, HttpMethod.POST, new HttpEntity<>(request, headers), ConnectionController.ResponseDTO.class);
        return result.getBody();
    }

    private GameInstanceController.ResponseDTO createGame(String token) throws URISyntaxException {
        final String baseUrl = "http://localhost:" + randomServerPort + "/instances/";
        URI uri = new URI(baseUrl);

        GameInstanceController.CreateRequestDTO request = new GameInstanceController.CreateRequestDTO(template.id().value());
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.add("Authorization", token);

        ResponseEntity<GameInstanceController.ResponseDTO> result = this.restTemplate
                .exchange(uri, HttpMethod.POST, new HttpEntity<>(request, headers), GameInstanceController.ResponseDTO.class);
        return result.getBody();
    }

    private void startGame(String gameToken, String sessionId) throws URISyntaxException {
        final String baseUrl = "http://localhost:" + randomServerPort + "/instances/" + sessionId + "/start/";
        URI uri = new URI(baseUrl);

        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.add("Authorization", gameToken);

        this.restTemplate.exchange(uri, HttpMethod.POST, new HttpEntity<>(headers), GameInstanceController.ResponseDTO.class);
    }

    private void sendPosition(String gameToken, String sessionId, double lat, double lng) throws URISyntaxException {
        final String baseUrl = "http://localhost:" + randomServerPort + "/instances/" + sessionId + "/move/";
        URI uri = new URI(baseUrl);

        record PositionRequest(double lat, double lng) {}
        PositionRequest request = new PositionRequest(lat, lng);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.add("Authorization", gameToken);

        this.restTemplate.exchange(uri, HttpMethod.POST, new HttpEntity<>(request, headers), Void.class);
    }

    private void equipItem(String gameToken, String sessionId, String itemId) throws URISyntaxException {
        final String baseUrl = "http://localhost:" + randomServerPort + "/instances/" + sessionId + "/inventory/" + itemId + "/equip";
        URI uri = new URI(baseUrl);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", gameToken);

        this.restTemplate.exchange(uri, HttpMethod.POST, new HttpEntity<>(headers), Void.class);
    }

    private void useEquippedItem(String gameToken, String sessionId, String itemId) throws URISyntaxException {
        final String baseUrl = "http://localhost:" + randomServerPort + "/instances/" + sessionId + "/inventory/" + itemId + "/equip/use";
        URI uri = new URI(baseUrl);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", gameToken);

        this.restTemplate.exchange(uri, HttpMethod.POST, new HttpEntity<>(headers), Void.class);
    }

    private InventoryItemDTO[] getInventory(String gameToken, String sessionId) throws URISyntaxException {
        final String baseUrl = "http://localhost:" + randomServerPort + "/instances/" + sessionId + "/inventory/";
        URI uri = new URI(baseUrl);

        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.add("Authorization", gameToken);
        headers.add("Language", "FR");

        ResponseEntity<InventoryItemDTO[]> result = this.restTemplate
                .exchange(uri, HttpMethod.GET, new HttpEntity<>(headers), InventoryItemDTO[].class);
        return result.getBody();
    }

    private String findInventoryItemSessionId(String gameToken, String sessionId, String itemLabel) throws URISyntaxException {
        InventoryItemDTO[] items = getInventory(gameToken, sessionId);
        return Arrays.stream(items)
                .filter(item -> item.label().equals(itemLabel))
                .findFirst()
                .map(InventoryItemDTO::id)
                .orElseThrow(() -> new RuntimeException("Item not found: " + itemLabel));
    }

    record InventoryItemDTO(String id, String label, List<String> actions, int count) {}

    private GameInstanceScenarioController.GameGoalResponseDTO[] getGoals(String gameToken, String sessionId) throws URISyntaxException {
        final String baseUrl = "http://localhost:" + randomServerPort + "/instances/" + sessionId + "/goals/";
        URI uri = new URI(baseUrl);

        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.add("Authorization", gameToken);
        headers.add("Language", "FR");

        ResponseEntity<GameInstanceScenarioController.GameGoalResponseDTO[]> result = this.restTemplate
                .exchange(uri, HttpMethod.GET, new HttpEntity<>(headers), GameInstanceScenarioController.GameGoalResponseDTO[].class);
        return result.getBody();
    }
}

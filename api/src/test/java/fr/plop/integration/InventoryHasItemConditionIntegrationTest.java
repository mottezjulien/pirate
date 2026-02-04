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
import fr.plop.contexts.game.config.scenario.domain.model.PossibilityTrigger;
import fr.plop.contexts.game.config.scenario.domain.model.ScenarioConfig;
import fr.plop.contexts.game.config.talk.domain.*;
import fr.plop.contexts.game.config.template.domain.model.Template;
import fr.plop.contexts.game.config.template.domain.usecase.TemplateInitUseCase;
import fr.plop.contexts.game.instance.core.domain.port.GameInstanceClearPort;
import fr.plop.contexts.game.instance.core.presenter.GameInstanceController;
import fr.plop.contexts.game.instance.push.PushPort;
import fr.plop.contexts.game.instance.scenario.domain.model.ScenarioSessionState;
import fr.plop.contexts.game.instance.talk.GameInstanceTalkController;
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
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test for InventoryHasItem condition.
 *
 * Tests that a talk with a conditional message:
 * - Shows one message when the player does NOT have the item
 * - Shows a different message when the player HAS the item
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class InventoryHasItemConditionIntegrationTest {

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
    private static final BoardSpace.Id SPACE_TREASURE_ID = new BoardSpace.Id("SPACE_TREASURE");
    private static final GameConfigInventoryItem.Id ITEM_KEY_ID = new GameConfigInventoryItem.Id("ITEM_KEY");
    private static final TalkCharacter.Id CHARACTER_GUARD_ID = new TalkCharacter.Id("PNJ_GUARD");
    private static final TalkItem.Id TALK_GUARD_CHECK_ID = new TalkItem.Id("TALK_GUARD_CHECK");
    private static final ScenarioConfig.Target.Id TARGET_FIND_KEY_ID = new ScenarioConfig.Target.Id("TARGET_FIND_KEY");

    @BeforeEach
    void setUp() {
        sessionClear.clearAll();
        templateInitUseCase.deleteAll();

        // Build template directly without JSON parsing
        template = buildTemplate();
        templateInitUseCase.createOrUpdate(
                templateInitUseCase.findOrCreateGame(new GameProject.Code("INVENTORY_CONDITION_TEST"), new Game.Version("1.0.0")),
                template
        );
    }

    private Template buildTemplate() {
        // 1. Board - one space for getting the item
        BoardSpace treasureSpace = new BoardSpace(
                SPACE_TREASURE_ID,
                "Treasure Zone",
                Priority.MEDIUM,
                List.of(new Rectangle(
                        Point.from(48.8580, 2.2945),  // bottomLeft
                        Point.from(48.8584, 2.2950)   // topRight
                ))
        );
        BoardConfig boardConfig = new BoardConfig(List.of(treasureSpace));

        // 2. Inventory - one item (golden key)
        GameConfigInventoryItem key = new GameConfigInventoryItem(
                ITEM_KEY_ID,
                i18n("Clef dorée", "Golden key"),
                new Image(Image.Type.ASSET, "items/key.png"),
                Optional.of(i18n("Une clef en or", "A golden key")),
                GameConfigInventoryItem.Type.UNIQUE,
                0,
                Optional.empty(),
                GameConfigInventoryItem.ActionType.NONE
        );
        InventoryConfig inventoryConfig = new InventoryConfig(
                new InventoryConfig.Id(),
                List.of(key),
                List.of()
        );

        // 3. Talk - one character with conditional check for item
        TalkCharacter guard = new TalkCharacter(CHARACTER_GUARD_ID, "Guard");
        Image guardImage = new Image(Image.Type.ASSET, "guard/default.png");
        TalkCharacter.Reference guardRef = new TalkCharacter.Reference(guard, "DEFAULT", guardImage);

        // Condition: has the key
        Condition hasKeyCondition = new Condition.InventoryHasItem(new Condition.Id(), ITEM_KEY_ID);

        // Conditional talk output:
        // - Default (no key): "Vous n'avez pas la clef"
        // - Branch (has key): "Vous avez la clef"
        TalkItemOut.Conditional.Branch hasKeyBranch = new TalkItemOut.Conditional.Branch(
                0,
                hasKeyCondition,
                i18n("Vous avez la clef", "You have the key")
        );

        TalkItemOut conditionalOutput = new TalkItemOut.Conditional(
                i18n("Vous n'avez pas la clef", "You don't have the key"),
                List.of(hasKeyBranch)
        );

        TalkItem talkItem = new TalkItem(
                TALK_GUARD_CHECK_ID,
                conditionalOutput,
                guardRef,
                new TalkItemNext.Empty()
        );

        TalkConfig talkConfig = new TalkConfig(List.of(talkItem));

        // 4. Scenario - one possibility to add item when entering space
        ScenarioConfig.Target target = new ScenarioConfig.Target(
                TARGET_FIND_KEY_ID,
                i18n("Trouver la clef", "Find the key"),
                Optional.empty(),
                false,
                List.of(),
                Optional.empty()
        );

        Possibility addItemPossibility = new Possibility(
                new PossibilityTrigger.SpaceGoIn(new PossibilityTrigger.Id(), SPACE_TREASURE_ID),
                List.of(
                        new Consequence.InventoryAddItem(new Consequence.Id(), ITEM_KEY_ID),
                        new Consequence.ScenarioTarget(new Consequence.Id(), TARGET_FIND_KEY_ID, ScenarioSessionState.SUCCESS)
                )
        );

        ScenarioConfig.Step step = new ScenarioConfig.Step(
                new ScenarioConfig.Step.Id("STEP_1"),
                i18n("Etape 1", "Step 1"),
                Optional.empty(),
                0,
                List.of(target),
                List.of(addItemPossibility)
        );

        ScenarioConfig scenarioConfig = new ScenarioConfig(List.of(step));

        // 5. Build and return template
        return Template.builder()
                .board(boardConfig)
                .inventory(inventoryConfig)
                .talk(talkConfig)
                .scenario(scenarioConfig)
                .build();
    }

    private static I18n i18n(String fr, String en) {
        return new I18n(Map.of(Language.FR, fr, Language.EN, en));
    }

    @Test
    public void inventoryCondition_withoutItem_showsNoItemMessage() throws URISyntaxException, InterruptedException {
        // 1. Create connection and session
        ConnectionController.ResponseDTO connection = createAuth();
        GameInstanceController.ResponseDTO session = createGame(connection.token());
        assertThat(session.id()).isNotNull();

        // 2. Start the session
        startGame(session.auth().token(), session.id());
        Thread.sleep(500); // Wait for async processing

        // 3. Check talk - player does NOT have the key
        GameInstanceTalkController.ResponseDTO response = getTalk(session.auth().token(), session.id(), TALK_GUARD_CHECK_ID.value());

        // Should show "no key" message (default)
        assertThat(response.text()).isEqualTo("Vous n'avez pas la clef");
    }

    @Test
    public void inventoryCondition_withItem_showsHasItemMessage() throws URISyntaxException, InterruptedException {
        // 1. Create connection and session
        ConnectionController.ResponseDTO connection = createAuth();
        GameInstanceController.ResponseDTO session = createGame(connection.token());
        assertThat(session.id()).isNotNull();

        // 2. Start the session
        startGame(session.auth().token(), session.id());
        Thread.sleep(500); // Wait for async processing

        // 3. Move into the treasure space to get the key
        sendPosition(session.auth().token(), session.id(), 48.8582, 2.2947);
        Thread.sleep(500); // Wait for consequence processing (INVENTORY_ADD)

        // 4. Check talk - player NOW has the key
        GameInstanceTalkController.ResponseDTO response = getTalk(session.auth().token(), session.id(), TALK_GUARD_CHECK_ID.value());

        // Should show "has key" message (branch condition matched)
        assertThat(response.text()).isEqualTo("Vous avez la clef");
    }

    // ========== Helper methods ==========

    private ConnectionController.ResponseDTO createAuth() throws URISyntaxException {
        final String baseUrl = "http://localhost:" + randomServerPort + "/connect/";
        URI uri = new URI(baseUrl);

        ConnectionController.RequestDTO request = new ConnectionController.RequestDTO("test-device-inventory-" + System.currentTimeMillis());
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

    private GameInstanceTalkController.ResponseDTO getTalk(String gameToken, String sessionId, String talkId) throws URISyntaxException {
        final String baseUrl = "http://localhost:" + randomServerPort + "/instances/" + sessionId + "/talks/" + talkId;
        URI uri = new URI(baseUrl);

        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.add("Authorization", gameToken);
        headers.add("Language", "FR");

        ResponseEntity<GameInstanceTalkController.ResponseDTO> result = this.restTemplate
                .exchange(uri, HttpMethod.GET, new HttpEntity<>(headers), GameInstanceTalkController.ResponseDTO.class);
        return result.getBody();
    }
}

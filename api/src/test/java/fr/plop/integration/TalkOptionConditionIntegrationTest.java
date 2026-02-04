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
 * Integration test for conditions on talk options.
 *
 * Tests that talk options with conditions are correctly filtered:
 * - Option WITHOUT condition -> always visible
 * - Option WITH condition INVENTORY_HAS -> visible only if player HAS the item
 * - Option WITH condition NOT(INVENTORY_HAS) -> visible only if player does NOT have the item
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class TalkOptionConditionIntegrationTest {

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
    private static final GameConfigInventoryItem.Id ITEM_CARTE_ID = new GameConfigInventoryItem.Id("ITEM_CARTE");
    private static final TalkCharacter.Id CHARACTER_MARCEL_ID = new TalkCharacter.Id("PNJ_MARCEL");
    private static final TalkItem.Id TALK_OPTIONS_ID = new TalkItem.Id("TALK_MARCEL_OPTIONS");
    private static final TalkItemNext.Options.Option.Id OPT_BONJOUR_ID = new TalkItemNext.Options.Option.Id("OPT_BONJOUR");
    private static final TalkItemNext.Options.Option.Id OPT_HAS_CARTE_ID = new TalkItemNext.Options.Option.Id("OPT_HAS_CARTE");
    private static final TalkItemNext.Options.Option.Id OPT_NEED_CARTE_ID = new TalkItemNext.Options.Option.Id("OPT_NEED_CARTE");

    @BeforeEach
    void setUp() {
        sessionClear.clearAll();
        templateInitUseCase.deleteAll();

        // Build template directly without JSON parsing
        template = buildTemplate();
        templateInitUseCase.createOrUpdate(
                templateInitUseCase.findOrCreateGame(new GameProject.Code("OPTION_CONDITION_TEST"), new Game.Version("1.0.0")),
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

        // 2. Inventory - one item
        GameConfigInventoryItem carte = new GameConfigInventoryItem(
                ITEM_CARTE_ID,
                i18n("Carte au trésor", "Treasure map"),
                new Image(Image.Type.ASSET, "items/carte.png"),
                Optional.of(i18n("Une vieille carte", "An old map")),
                GameConfigInventoryItem.Type.UNIQUE,
                0,
                Optional.empty(),
                GameConfigInventoryItem.ActionType.NONE
        );
        InventoryConfig inventoryConfig = new InventoryConfig(
                new InventoryConfig.Id(),
                List.of(carte),
                List.of()
        );

        // 3. Talk - one character, one item with 3 options
        TalkCharacter marcel = new TalkCharacter(CHARACTER_MARCEL_ID, "Marcel");
        Image marcelImage = new Image(Image.Type.ASSET, "marcel/default.png");
        TalkCharacter.Reference marcelRef = new TalkCharacter.Reference(marcel, "DEFAULT", marcelImage);

        // Conditions
        Condition hasCarteCondition = new Condition.InventoryHasItem(new Condition.Id(), ITEM_CARTE_ID);
        Condition notHasCarteCondition = new Condition.Not(new Condition.Id(), hasCarteCondition);

        // Options
        TalkItemNext.Options.Option optBonjour = new TalkItemNext.Options.Option(
                OPT_BONJOUR_ID,
                1,
                i18n("Bonjour", "Hello"),
                Optional.empty(),
                Optional.empty()  // No condition - always visible
        );

        TalkItemNext.Options.Option optHasCarte = new TalkItemNext.Options.Option(
                OPT_HAS_CARTE_ID,
                2,
                i18n("J'ai la carte", "I have the map"),
                Optional.empty(),
                Optional.of(hasCarteCondition)  // Visible only if has item
        );

        TalkItemNext.Options.Option optNeedCarte = new TalkItemNext.Options.Option(
                OPT_NEED_CARTE_ID,
                3,
                i18n("Donnez-moi la carte", "Give me the map"),
                Optional.empty(),
                Optional.of(notHasCarteCondition)  // Visible only if does NOT have item
        );

        TalkItem talkItem = TalkItem.options(
                TALK_OPTIONS_ID,
                TalkItemOut.fixed(i18n("Que voulez-vous ?", "What do you want?")),
                marcelRef,
                List.of(optBonjour, optHasCarte, optNeedCarte)
        );

        TalkConfig talkConfig = new TalkConfig(List.of(talkItem));

        // 4. Scenario - one possibility to add item when entering space
        ScenarioConfig.Target target = new ScenarioConfig.Target(
                new ScenarioConfig.Target.Id("TARGET_1"),
                i18n("Objectif", "Goal"),
                Optional.empty(),
                false,
                List.of(),
                Optional.empty()
        );

        Possibility addItemPossibility = new Possibility(
                new PossibilityTrigger.SpaceGoIn(new PossibilityTrigger.Id(), SPACE_TREASURE_ID),
                List.of(new Consequence.InventoryAddItem(new Consequence.Id(), ITEM_CARTE_ID))
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
    public void optionCondition_withoutItem_showsCorrectOptions() throws URISyntaxException, InterruptedException {
        // 1. Create connection and session
        ConnectionController.ResponseDTO connection = createAuth();
        GameInstanceController.ResponseDTO session = createGame(connection.token());
        assertThat(session.id()).isNotNull();

        // 2. Start the session
        startGame(session.auth().token(), session.id());
        Thread.sleep(500);

        // 3. Get talk options - player does NOT have the carte
        GameInstanceTalkController.ResponseDTO response = getTalk(session.auth().token(), session.id(), TALK_OPTIONS_ID.value());

        // Should show:
        // - OPT_BONJOUR (no condition)
        // - OPT_NEED_CARTE (NOT has item - condition TRUE because player doesn't have item)
        // Should NOT show:
        // - OPT_HAS_CARTE (has item - condition FALSE because player doesn't have item)
        List<String> optionIds = response.next().options().stream()
                .map(GameInstanceTalkController.ResponseDTO.Next.Option::id)
                .toList();

        assertThat(optionIds).containsExactlyInAnyOrder("OPT_BONJOUR", "OPT_NEED_CARTE");
        assertThat(optionIds).doesNotContain("OPT_HAS_CARTE");
    }

    @Test
    public void optionCondition_withItem_showsCorrectOptions() throws URISyntaxException, InterruptedException {
        // 1. Create connection and session
        ConnectionController.ResponseDTO connection = createAuth();
        GameInstanceController.ResponseDTO session = createGame(connection.token());
        assertThat(session.id()).isNotNull();

        // 2. Start the session
        startGame(session.auth().token(), session.id());
        Thread.sleep(500);

        // 3. Move into the treasure space to get the carte
        sendPosition(session.auth().token(), session.id(), 48.8582, 2.2947);
        Thread.sleep(500);

        // 4. Get talk options - player NOW has the carte
        GameInstanceTalkController.ResponseDTO response = getTalk(session.auth().token(), session.id(), TALK_OPTIONS_ID.value());

        // Should show:
        // - OPT_BONJOUR (no condition)
        // - OPT_HAS_CARTE (has item - condition TRUE because player has item)
        // Should NOT show:
        // - OPT_NEED_CARTE (NOT has item - condition FALSE because player has item)
        List<String> optionIds = response.next().options().stream()
                .map(GameInstanceTalkController.ResponseDTO.Next.Option::id)
                .toList();

        assertThat(optionIds).containsExactlyInAnyOrder("OPT_BONJOUR", "OPT_HAS_CARTE");
        assertThat(optionIds).doesNotContain("OPT_NEED_CARTE");
    }

    @Test
    public void optionCondition_optionWithoutCondition_alwaysVisible() throws URISyntaxException, InterruptedException {
        // 1. Create connection and session
        ConnectionController.ResponseDTO connection = createAuth();
        GameInstanceController.ResponseDTO session = createGame(connection.token());

        // 2. Start the session
        startGame(session.auth().token(), session.id());
        Thread.sleep(500);

        // 3. Check option is visible WITHOUT item
        GameInstanceTalkController.ResponseDTO response1 = getTalk(session.auth().token(), session.id(), TALK_OPTIONS_ID.value());
        assertThat(response1.next().options().stream()
                .anyMatch(opt -> opt.id().equals("OPT_BONJOUR"))).isTrue();

        // 4. Get item and check option is still visible WITH item
        sendPosition(session.auth().token(), session.id(), 48.8582, 2.2947);
        Thread.sleep(500);

        GameInstanceTalkController.ResponseDTO response2 = getTalk(session.auth().token(), session.id(), TALK_OPTIONS_ID.value());
        assertThat(response2.next().options().stream()
                .anyMatch(opt -> opt.id().equals("OPT_BONJOUR"))).isTrue();
    }

    @Test
    public void optionCondition_optionValues_areCorrect() throws URISyntaxException, InterruptedException {
        // 1. Create connection and session
        ConnectionController.ResponseDTO connection = createAuth();
        GameInstanceController.ResponseDTO session = createGame(connection.token());

        // 2. Start the session
        startGame(session.auth().token(), session.id());
        Thread.sleep(500);

        // 3. Get talk and verify option values
        GameInstanceTalkController.ResponseDTO response = getTalk(session.auth().token(), session.id(), TALK_OPTIONS_ID.value());

        // Verify the text of the message
        assertThat(response.text()).isEqualTo("Que voulez-vous ?");

        // Verify option values
        var optBonjour = response.next().options().stream()
                .filter(opt -> opt.id().equals("OPT_BONJOUR"))
                .findFirst();
        assertThat(optBonjour).isPresent();
        assertThat(optBonjour.get().value()).isEqualTo("Bonjour");

        var optNeedCarte = response.next().options().stream()
                .filter(opt -> opt.id().equals("OPT_NEED_CARTE"))
                .findFirst();
        assertThat(optNeedCarte).isPresent();
        assertThat(optNeedCarte.get().value()).isEqualTo("Donnez-moi la carte");
    }

    // ========== Helper methods ==========

    private ConnectionController.ResponseDTO createAuth() throws URISyntaxException {
        final String baseUrl = "http://localhost:" + randomServerPort + "/connect/";
        URI uri = new URI(baseUrl);

        ConnectionController.RequestDTO request = new ConnectionController.RequestDTO("test-device-option-" + System.currentTimeMillis());
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

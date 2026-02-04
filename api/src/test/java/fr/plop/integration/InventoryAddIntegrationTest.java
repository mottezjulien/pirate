package fr.plop.integration;

import fr.plop.contexts.connect.presenter.ConnectionController;
import fr.plop.contexts.game.commun.domain.Game;
import fr.plop.contexts.game.commun.domain.GameProject;
import fr.plop.contexts.game.config.consequence.Consequence;
import fr.plop.contexts.game.config.inventory.domain.model.GameConfigInventoryItem;
import fr.plop.contexts.game.config.inventory.domain.model.InventoryConfig;
import fr.plop.contexts.game.config.scenario.domain.model.Possibility;
import fr.plop.contexts.game.config.scenario.domain.model.PossibilityTrigger;
import fr.plop.contexts.game.config.scenario.domain.model.ScenarioConfig;
import fr.plop.contexts.game.config.template.domain.model.Template;
import fr.plop.contexts.game.config.template.domain.usecase.TemplateInitUseCase;
import fr.plop.contexts.game.instance.core.domain.port.GameInstanceClearPort;
import fr.plop.contexts.game.instance.core.presenter.GameInstanceController;
import fr.plop.contexts.game.instance.inventory.presenter.GameInstanceInventoryController;
import fr.plop.contexts.game.instance.push.PushPort;
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
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class InventoryAddIntegrationTest {

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

    @BeforeEach
    void setUp() throws InterruptedException {
        Thread.sleep(200);
        sessionClear.clearAll();
        templateInitUseCase.deleteAll();

        // Item IDs
        var itemKeyId = new GameConfigInventoryItem.Id("ITEM_KEY");
        var itemCoinId = new GameConfigInventoryItem.Id("ITEM_COIN");

        // Step & Target IDs
        var stepId = new ScenarioConfig.Step.Id("STEP_1");
        var target1Id = new ScenarioConfig.Target.Id("TARGET_1");

        // Inventory items
        var items = List.of(
                new GameConfigInventoryItem(itemKeyId, i18n("Cle doree", "Golden key"),
                        new Image(Image.Type.ASSET, "items/key.png"),
                        Optional.of(i18n("Une cle brillante", "A shiny key")),
                        GameConfigInventoryItem.Type.UNIQUE, 0, Optional.empty(), GameConfigInventoryItem.ActionType.NONE),
                new GameConfigInventoryItem(itemCoinId, i18n("Piece d'or", "Gold coin"),
                        new Image(Image.Type.ASSET, "items/coin.png"),
                        Optional.empty(),
                        GameConfigInventoryItem.Type.COLLECTION, 0, Optional.empty(), GameConfigInventoryItem.ActionType.NONE)
        );

        // StepActive → add ITEM_KEY
        var stepActivePossibility = new Possibility(
                new PossibilityTrigger.StepActive(new PossibilityTrigger.Id(), stepId),
                List.of(new Consequence.InventoryAddItem(new Consequence.Id(), itemKeyId)));

        // Step with target and possibility
        var step = new ScenarioConfig.Step(stepId, i18n("Etape 1", "Step 1"), Optional.empty(), 0,
                List.of(new ScenarioConfig.Target(target1Id, i18n("Objectif", "Goal"), Optional.empty(), false, List.of(), Optional.empty())),
                List.of(stepActivePossibility));

        // Build template
        template = Template.builder()
                .scenario(new ScenarioConfig(List.of(step)))
                .inventory(new InventoryConfig(new InventoryConfig.Id(), items, List.of()))
                .build();

        Game.Id gameId = templateInitUseCase.findOrCreateGame(new GameProject.Code("INVENTORY_ADD_TEST"), new Game.Version("1.0.0"));
        templateInitUseCase.createOrUpdate(gameId, template);
    }

    @Test
    public void inventoryParsing_createsItems() {
        // Verify inventory items were parsed
        assertThat(template.inventory().items()).hasSize(2);
        assertThat(template.inventory().items().get(0).label().value(fr.plop.subs.i18n.domain.Language.FR)).isEqualTo("Cle doree");
        assertThat(template.inventory().items().get(1).label().value(fr.plop.subs.i18n.domain.Language.FR)).isEqualTo("Piece d'or");
    }

    @Test
    public void createSession_triggersInventoryAdd_itemAppearsInList() throws URISyntaxException, InterruptedException {
        // 1. Create auth
        ConnectionController.ResponseDTO connection = createAuth();

        // 2. Create game session
        GameInstanceController.ResponseDTO session = createGame(connection.token());
        assertThat(session.id()).isNotNull();

        // 3. Start the session - this triggers StepActive and adds the item
        startGameInstance(session.auth().token(), session.id());

        // 4. Wait for async event processing
        Thread.sleep(500);

        // 5. Get inventory list
        List<GameInstanceInventoryController.SimpleResponseDTO> inventory = getInventoryList(session.auth().token(), session.id());

        // 6. Verify the item was added
        assertThat(inventory).hasSize(1);
        assertThat(inventory.get(0).label()).isEqualTo("Cle doree");
        assertThat(inventory.get(0).count()).isEqualTo(1);
    }

    @Test
    public void collectionItem_canBeAddedMultipleTimes() throws URISyntaxException, InterruptedException {
        // Create a template that adds the same COLLECTION item 3 times
        sessionClear.clearAll();
        templateInitUseCase.deleteAll();

        var itemCoinId = new GameConfigInventoryItem.Id("ITEM_COIN");
        var stepId = new ScenarioConfig.Step.Id("STEP_1");
        var target1Id = new ScenarioConfig.Target.Id("TARGET_1");

        var coinItem = new GameConfigInventoryItem(itemCoinId, i18n("Piece d'or", "Gold coin"),
                new Image(Image.Type.ASSET, "items/coin.png"), Optional.empty(),
                GameConfigInventoryItem.Type.COLLECTION, 0, Optional.empty(), GameConfigInventoryItem.ActionType.NONE);

        var stepActivePossibility = new Possibility(
                new PossibilityTrigger.StepActive(new PossibilityTrigger.Id(), stepId),
                List.of(
                        new Consequence.InventoryAddItem(new Consequence.Id(), itemCoinId),
                        new Consequence.InventoryAddItem(new Consequence.Id(), itemCoinId),
                        new Consequence.InventoryAddItem(new Consequence.Id(), itemCoinId)
                ));

        var step = new ScenarioConfig.Step(stepId, i18n("Etape 1", "Step 1"), Optional.empty(), 0,
                List.of(new ScenarioConfig.Target(target1Id, i18n("Objectif", "Goal"), Optional.empty(), false, List.of(), Optional.empty())),
                List.of(stepActivePossibility));

        Template templateMultiAdd = Template.builder()
                .scenario(new ScenarioConfig(List.of(step)))
                .inventory(new InventoryConfig(new InventoryConfig.Id(), List.of(coinItem), List.of()))
                .build();

        Game.Id gameId = templateInitUseCase.findOrCreateGame(new GameProject.Code("INVENTORY_MULTI_ADD_TEST"), new Game.Version("1.0.0"));
        templateInitUseCase.createOrUpdate(gameId, templateMultiAdd);

        // Create session
        ConnectionController.ResponseDTO connection = createAuth();
        GameInstanceController.ResponseDTO session = createGameForTemplate(connection.token(), templateMultiAdd.id().value());

        // Start the session
        startGameInstance(session.auth().token(), session.id());

        // Wait for async event processing
        Thread.sleep(500);

        // Get inventory
        List<GameInstanceInventoryController.SimpleResponseDTO> inventory = getInventoryList(session.auth().token(), session.id());

        // Verify count is 3
        assertThat(inventory).hasSize(1);
        assertThat(inventory.getFirst().label()).isEqualTo("Piece d'or");
        assertThat(inventory.getFirst().count()).isEqualTo(3);
    }

    private ConnectionController.ResponseDTO createAuth() throws URISyntaxException {
        final String baseUrl = "http://localhost:" + randomServerPort + "/connect/";
        URI uri = new URI(baseUrl);

        ConnectionController.RequestDTO request = new ConnectionController.RequestDTO("inventoryTestDeviceId" + System.currentTimeMillis());

        ResponseEntity<ConnectionController.ResponseDTO> result = this.restTemplate.postForEntity(uri, request, ConnectionController.ResponseDTO.class);
        return result.getBody();
    }

    private GameInstanceController.ResponseDTO createGame(String token) throws URISyntaxException {
        return createGameForTemplate(token, template.id().value());
    }

    private GameInstanceController.ResponseDTO createGameForTemplate(String token, String templateId) throws URISyntaxException {
        final String baseUrl = "http://localhost:" + randomServerPort + "/instances/";
        URI uri = new URI(baseUrl);

        GameInstanceController.CreateRequestDTO request = new GameInstanceController.CreateRequestDTO(templateId);
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.add("Authorization", token);

        ResponseEntity<GameInstanceController.ResponseDTO> result = this.restTemplate
                .exchange(uri, HttpMethod.POST, new HttpEntity<>(request, headers), GameInstanceController.ResponseDTO.class);
        return result.getBody();
    }

    private void startGameInstance(String gameToken, String sessionId) throws URISyntaxException {
        final String baseUrl = "http://localhost:" + randomServerPort + "/instances/" + sessionId + "/start/";
        URI uri = new URI(baseUrl);

        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.add("Authorization", gameToken);

        this.restTemplate.exchange(uri, HttpMethod.POST, new HttpEntity<>(headers), GameInstanceController.ResponseDTO.class);
    }

    private static I18n i18n(String fr, String en) {
        return new I18n(Map.of(Language.FR, fr, Language.EN, en));
    }

    private List<GameInstanceInventoryController.SimpleResponseDTO> getInventoryList(String gameToken, String sessionId) throws URISyntaxException {
        final String baseUrl = "http://localhost:" + randomServerPort + "/instances/" + sessionId + "/inventory/";
        URI uri = new URI(baseUrl);

        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.add("Authorization", gameToken);
        headers.add("Language", "FR");

        ResponseEntity<List<GameInstanceInventoryController.SimpleResponseDTO>> result = this.restTemplate
                .exchange(uri, HttpMethod.GET, new HttpEntity<>(headers),
                        new ParameterizedTypeReference<List<GameInstanceInventoryController.SimpleResponseDTO>>() {});
        return result.getBody();
    }
}

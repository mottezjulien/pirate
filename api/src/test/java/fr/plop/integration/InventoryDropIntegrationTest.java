package fr.plop.integration;

import fr.plop.contexts.connect.presenter.ConnectionController;
import fr.plop.contexts.game.commun.domain.Game;
import fr.plop.contexts.game.commun.domain.GameProject;
import fr.plop.contexts.game.config.consequence.Consequence;
import fr.plop.contexts.game.config.inventory.domain.model.GameConfigInventoryItem;
import fr.plop.contexts.game.config.inventory.domain.model.InventoryConfig;
import fr.plop.contexts.game.config.inventory.domain.model.InventoryMergedRule;
import fr.plop.contexts.game.config.scenario.domain.model.Possibility;
import fr.plop.contexts.game.config.scenario.domain.model.PossibilityTrigger;
import fr.plop.contexts.game.config.scenario.domain.model.ScenarioConfig;
import fr.plop.contexts.game.config.template.domain.model.Template;
import fr.plop.contexts.game.config.template.domain.usecase.TemplateInitUseCase;
import fr.plop.contexts.game.instance.core.domain.port.GameInstanceClearPort;
import fr.plop.contexts.game.instance.core.presenter.GameInstanceController;
import fr.plop.contexts.game.instance.inventory.presenter.GameInstanceInventoryController;
import fr.plop.contexts.game.instance.push.PushEvent;
import fr.plop.contexts.game.instance.push.PushPort;
import fr.plop.contexts.game.instance.scenario.domain.model.ScenarioState;
import fr.plop.contexts.game.instance.scenario.presenter.GameInstanceScenarioController;
import fr.plop.subs.i18n.domain.I18n;
import fr.plop.subs.i18n.domain.Language;
import fr.plop.subs.image.Image;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
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
import static org.mockito.Mockito.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class InventoryDropIntegrationTest {

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
        Thread.sleep(200); // Wait for async event processing from previous test
        sessionClear.clearAll();
        templateInitUseCase.deleteAll();
        reset(pushPort);

        // Item IDs
        var itemSimpleId = new GameConfigInventoryItem.Id("ITEM_SIMPLE");
        var itemConsumableId = new GameConfigInventoryItem.Id("ITEM_CONSUMABLE");
        var itemUsableId = new GameConfigInventoryItem.Id("ITEM_USABLE");
        var itemEquippableId = new GameConfigInventoryItem.Id("ITEM_EQUIPPABLE");
        var itemEquippable2Id = new GameConfigInventoryItem.Id("ITEM_EQUIPPABLE_2");
        var itemMergeAId = new GameConfigInventoryItem.Id("ITEM_MERGE_A");
        var itemMergeBId = new GameConfigInventoryItem.Id("ITEM_MERGE_B");
        var itemMergedResultId = new GameConfigInventoryItem.Id("ITEM_MERGED_RESULT");
        var itemQuestId = new GameConfigInventoryItem.Id("ITEM_QUEST");

        // Target IDs
        var target1Id = new ScenarioConfig.Target.Id("TARGET_1");
        var targetConsumeId = new ScenarioConfig.Target.Id("TARGET_CONSUME");
        var targetUseId = new ScenarioConfig.Target.Id("TARGET_USE");
        var targetEquipUseId = new ScenarioConfig.Target.Id("TARGET_EQUIP_USE");
        var targetQuestId = new ScenarioConfig.Target.Id("TARGET_QUEST");

        // Step ID
        var stepId = new ScenarioConfig.Step.Id("STEP_1");

        // Inventory items
        var items = List.of(
                item(itemSimpleId, "Item simple", "Simple item", "items/simple.png", GameConfigInventoryItem.ActionType.NONE, Optional.empty()),
                item(itemConsumableId, "Potion", "Potion", "items/potion.png", GameConfigInventoryItem.ActionType.CONSUMABLE, Optional.empty()),
                item(itemUsableId, "Lunettes", "Glasses", "items/glasses.png", GameConfigInventoryItem.ActionType.USABLE, Optional.empty()),
                item(itemEquippableId, "Epee", "Sword", "items/sword.png", GameConfigInventoryItem.ActionType.EQUIPPABLE, Optional.empty()),
                item(itemEquippable2Id, "Bouclier", "Shield", "items/shield.png", GameConfigInventoryItem.ActionType.EQUIPPABLE, Optional.empty()),
                item(itemMergeAId, "Partie A", "Part A", "items/part_a.png", GameConfigInventoryItem.ActionType.NONE, Optional.empty()),
                item(itemMergeBId, "Partie B", "Part B", "items/part_b.png", GameConfigInventoryItem.ActionType.NONE, Optional.empty()),
                item(itemMergedResultId, "Objet complet", "Complete object", "items/complete.png", GameConfigInventoryItem.ActionType.NONE, Optional.empty()),
                item(itemQuestId, "Objet de quete", "Quest item", "items/quest.png", GameConfigInventoryItem.ActionType.NONE, Optional.of(targetQuestId))
        );

        // Inventory config with merge rules
        var inventoryConfig = new InventoryConfig(new InventoryConfig.Id(), items,
                List.of(new InventoryMergedRule(List.of(itemMergeAId, itemMergeBId), itemMergedResultId)));

        // Targets
        var targets = List.of(
                target(target1Id, "Objectif", "Goal", false),
                target(targetConsumeId, "Consommation", "Consume", true),
                target(targetUseId, "Utilisation", "Use", true),
                target(targetEquipUseId, "Utilisation equipement", "Equip use", true),
                target(targetQuestId, "Objet de quete", "Quest item", true)
        );

        // Possibilities
        var stepActivePossibility = new Possibility(
                new PossibilityTrigger.StepActive(new PossibilityTrigger.Id(), stepId),
                List.of(
                        new Consequence.InventoryAddItem(new Consequence.Id(), itemSimpleId),
                        new Consequence.InventoryAddItem(new Consequence.Id(), itemConsumableId),
                        new Consequence.InventoryAddItem(new Consequence.Id(), itemUsableId),
                        new Consequence.InventoryAddItem(new Consequence.Id(), itemEquippableId),
                        new Consequence.InventoryAddItem(new Consequence.Id(), itemEquippable2Id),
                        new Consequence.InventoryAddItem(new Consequence.Id(), itemMergeAId),
                        new Consequence.InventoryAddItem(new Consequence.Id(), itemMergeBId),
                        new Consequence.InventoryAddItem(new Consequence.Id(), itemQuestId)
                ));

        var consumePossibility = new Possibility(
                new PossibilityTrigger.InventoryItemAction(new PossibilityTrigger.Id(), itemConsumableId),
                List.of(new Consequence.ScenarioTarget(new Consequence.Id(), targetConsumeId, ScenarioState.SUCCESS)));

        var usePossibility = new Possibility(
                new PossibilityTrigger.InventoryItemAction(new PossibilityTrigger.Id(), itemUsableId),
                List.of(new Consequence.ScenarioTarget(new Consequence.Id(), targetUseId, ScenarioState.SUCCESS)));

        var equipUsePossibility = new Possibility(
                new PossibilityTrigger.InventoryItemAction(new PossibilityTrigger.Id(), itemEquippableId),
                List.of(new Consequence.ScenarioTarget(new Consequence.Id(), targetEquipUseId, ScenarioState.SUCCESS)));

        // Step
        var step = new ScenarioConfig.Step(stepId, i18n("Etape 1", "Step 1"), Optional.empty(), 0, targets,
                List.of(stepActivePossibility, consumePossibility, usePossibility, equipUsePossibility));

        // Build template
        template = Template.builder()
                .scenario(new ScenarioConfig(List.of(step)))
                .inventory(inventoryConfig)
                .build();

        Game.Id gameId = templateInitUseCase.findOrCreateGame(new GameProject.Code("INVENTORY_TEST"), new Game.Version("1.0.0"));
        templateInitUseCase.createOrUpdate(gameId, template);
    }

    private static I18n i18n(String fr, String en) {
        return new I18n(Map.of(Language.FR, fr, Language.EN, en));
    }

    private static GameConfigInventoryItem item(GameConfigInventoryItem.Id id, String fr, String en, String imagePath,
                                                GameConfigInventoryItem.ActionType actionType, Optional<ScenarioConfig.Target.Id> optTargetId) {
        return new GameConfigInventoryItem(id, i18n(fr, en), new Image(Image.Type.ASSET, imagePath),
                Optional.empty(), GameConfigInventoryItem.Type.UNIQUE, 0, optTargetId, actionType);
    }

    private static ScenarioConfig.Target target(ScenarioConfig.Target.Id id, String fr, String en, boolean optional) {
        return new ScenarioConfig.Target(id, i18n(fr, en), Optional.empty(), optional, List.of(), Optional.empty());
    }

    // ==================== DROP TESTS ====================

    @Nested
    class DropTests {

        @Test
        public void drop_happyPath_itemIsRemovedFromInventory() throws URISyntaxException, InterruptedException {
            // Setup
            ConnectionController.ResponseDTO connection = createAuth();
            GameInstanceController.ResponseDTO session = createGame(connection.token());
            startGame(session.auth().token(), session.id());
            Thread.sleep(500);
            reset(pushPort);

            // Get inventory and find droppable item
            List<GameInstanceInventoryController.SimpleResponseDTO> inventoryBefore = getInventoryList(session.auth().token(), session.id());
            GameInstanceInventoryController.SimpleResponseDTO simpleItem = findItemByLabel(inventoryBefore, "Item simple");
            assertThat(simpleItem.actions()).contains("DROP");

            // Drop the item
            ResponseEntity<Void> dropResponse = dropItem(session.auth().token(), session.id(), simpleItem.id());
            assertThat(dropResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

            // Verify item was removed
            List<GameInstanceInventoryController.SimpleResponseDTO> inventoryAfter = getInventoryList(session.auth().token(), session.id());
            assertThat(inventoryAfter.stream().noneMatch(item -> item.label().equals("Item simple"))).isTrue();

            // Verify PushPort was called
            verify(pushPort, atLeastOnce()).push(any(PushEvent.Inventory.class));
        }

        @Test
        public void drop_itemNotFound_returns400() throws URISyntaxException, InterruptedException {
            // Setup
            ConnectionController.ResponseDTO connection = createAuth();
            GameInstanceController.ResponseDTO session = createGame(connection.token());
            startGame(session.auth().token(), session.id());
            Thread.sleep(500);

            // Try to drop non-existent item
            ResponseEntity<Void> dropResponse = dropItem(session.auth().token(), session.id(), "non-existent-item-id");
            assertThat(dropResponse.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        }

        @Test
        public void drop_itemNotDroppable_returns400() throws URISyntaxException, InterruptedException {
            // Setup
            ConnectionController.ResponseDTO connection = createAuth();
            GameInstanceController.ResponseDTO session = createGame(connection.token());
            startGame(session.auth().token(), session.id());
            Thread.sleep(500);

            // Get inventory and find quest item (linked to target, not droppable)
            List<GameInstanceInventoryController.SimpleResponseDTO> inventory = getInventoryList(session.auth().token(), session.id());
            GameInstanceInventoryController.SimpleResponseDTO questItem = findItemByLabel(inventory, "Objet de quete");

            // Verify item does NOT have DROP action
            assertThat(questItem.actions()).doesNotContain("DROP");

            // Try to drop non-droppable item
            ResponseEntity<Void> dropResponse = dropItem(session.auth().token(), session.id(), questItem.id());
            assertThat(dropResponse.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        }
    }

    // ==================== CONSUME TESTS ====================

    @Nested
    class ConsumeTests {

        @Test
        public void consume_happyPath_itemIsRemovedAndActionExecuted() throws URISyntaxException, InterruptedException {
            // Setup
            ConnectionController.ResponseDTO connection = createAuth();
            GameInstanceController.ResponseDTO session = createGame(connection.token());
            startGame(session.auth().token(), session.id());
            Thread.sleep(500);
            reset(pushPort);

            // Get inventory and find consumable item
            List<GameInstanceInventoryController.SimpleResponseDTO> inventoryBefore = getInventoryList(session.auth().token(), session.id());
            GameInstanceInventoryController.SimpleResponseDTO consumableItem = findItemByLabel(inventoryBefore, "Potion");
            assertThat(consumableItem.actions()).contains("CONSUME");

            // Consume the item
            ResponseEntity<Void> consumeResponse = consumeItem(session.auth().token(), session.id(), consumableItem.id());
            assertThat(consumeResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

            // Verify item was removed
            List<GameInstanceInventoryController.SimpleResponseDTO> inventoryAfter = getInventoryList(session.auth().token(), session.id());
            assertThat(inventoryAfter.stream().noneMatch(item -> item.label().equals("Potion"))).isTrue();

            // Verify PushPort was called for inventory change
            verify(pushPort, atLeastOnce()).push(any(PushEvent.Inventory.class));
        }

        @Test
        public void consume_itemNotConsumable_returns400() throws URISyntaxException, InterruptedException {
            // Setup
            ConnectionController.ResponseDTO connection = createAuth();
            GameInstanceController.ResponseDTO session = createGame(connection.token());
            startGame(session.auth().token(), session.id());
            Thread.sleep(500);

            // Get inventory and find non-consumable item
            List<GameInstanceInventoryController.SimpleResponseDTO> inventory = getInventoryList(session.auth().token(), session.id());
            GameInstanceInventoryController.SimpleResponseDTO simpleItem = findItemByLabel(inventory, "Item simple");
            assertThat(simpleItem.actions()).doesNotContain("CONSUME");

            // Try to consume non-consumable item
            ResponseEntity<String> consumeResponse = consumeItemWithBody(session.auth().token(), session.id(), simpleItem.id());
            assertThat(consumeResponse.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        }

        @Test
        public void consume_happyPath_actionIsExecuted() throws URISyntaxException, InterruptedException {
            // Setup
            ConnectionController.ResponseDTO connection = createAuth();
            GameInstanceController.ResponseDTO session = createGame(connection.token());
            startGame(session.auth().token(), session.id());
            Thread.sleep(500);

            // Verify target is NOT done before consume
            List<GameInstanceScenarioController.GameGoalResponseDTO> goalsBefore = getGoals(session.auth().token(), session.id());
            assertThat(isTargetDone(goalsBefore, "TARGET_CONSUME")).isFalse();

            // Get consumable item and consume it
            List<GameInstanceInventoryController.SimpleResponseDTO> inventory = getInventoryList(session.auth().token(), session.id());
            GameInstanceInventoryController.SimpleResponseDTO consumableItem = findItemByLabel(inventory, "Potion");
            consumeItem(session.auth().token(), session.id(), consumableItem.id());

            // Wait for async processing
            Thread.sleep(500);

            // Verify target IS done after consume (action was executed)
            List<GameInstanceScenarioController.GameGoalResponseDTO> goalsAfter = getGoals(session.auth().token(), session.id());
            assertThat(isTargetDone(goalsAfter, "TARGET_CONSUME")).isTrue();
        }
    }

    // ==================== USE TESTS ====================

    @Nested
    class UseTests {

        @Test
        public void use_happyPath_inventoryUnchangedAndActionExecuted() throws URISyntaxException, InterruptedException {
            // Setup
            ConnectionController.ResponseDTO connection = createAuth();
            GameInstanceController.ResponseDTO session = createGame(connection.token());
            startGame(session.auth().token(), session.id());
            Thread.sleep(500);

            // Get inventory and find usable item
            List<GameInstanceInventoryController.SimpleResponseDTO> inventoryBefore = getInventoryList(session.auth().token(), session.id());
            int countBefore = inventoryBefore.size();
            GameInstanceInventoryController.SimpleResponseDTO usableItem = findItemByLabel(inventoryBefore, "Lunettes");
            assertThat(usableItem.actions()).contains("USE");

            // Use the item
            ResponseEntity<Void> useResponse = useItem(session.auth().token(), session.id(), usableItem.id());
            assertThat(useResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

            // Verify inventory is unchanged (item still present)
            List<GameInstanceInventoryController.SimpleResponseDTO> inventoryAfter = getInventoryList(session.auth().token(), session.id());
            assertThat(inventoryAfter).hasSize(countBefore);
            assertThat(inventoryAfter.stream().anyMatch(item -> item.label().equals("Lunettes"))).isTrue();
        }

        @Test
        public void use_itemNotUsable_returns400() throws URISyntaxException, InterruptedException {
            // Setup
            ConnectionController.ResponseDTO connection = createAuth();
            GameInstanceController.ResponseDTO session = createGame(connection.token());
            startGame(session.auth().token(), session.id());
            Thread.sleep(500);

            // Get inventory and find non-usable item
            List<GameInstanceInventoryController.SimpleResponseDTO> inventory = getInventoryList(session.auth().token(), session.id());
            GameInstanceInventoryController.SimpleResponseDTO simpleItem = findItemByLabel(inventory, "Item simple");
            assertThat(simpleItem.actions()).doesNotContain("USE");

            // Try to use non-usable item
            ResponseEntity<String> useResponse = useItemWithBody(session.auth().token(), session.id(), simpleItem.id());
            assertThat(useResponse.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        }

        @Test
        public void use_happyPath_actionIsExecuted() throws URISyntaxException, InterruptedException {
            // Setup
            ConnectionController.ResponseDTO connection = createAuth();
            GameInstanceController.ResponseDTO session = createGame(connection.token());
            startGame(session.auth().token(), session.id());
            Thread.sleep(500);

            // Verify target is NOT done before use
            List<GameInstanceScenarioController.GameGoalResponseDTO> goalsBefore = getGoals(session.auth().token(), session.id());
            assertThat(isTargetDone(goalsBefore, "TARGET_USE")).isFalse();

            // Get usable item and use it
            List<GameInstanceInventoryController.SimpleResponseDTO> inventory = getInventoryList(session.auth().token(), session.id());
            GameInstanceInventoryController.SimpleResponseDTO usableItem = findItemByLabel(inventory, "Lunettes");
            useItem(session.auth().token(), session.id(), usableItem.id());

            // Wait for async processing
            Thread.sleep(500);

            // Verify target IS done after use (action was executed)
            List<GameInstanceScenarioController.GameGoalResponseDTO> goalsAfter = getGoals(session.auth().token(), session.id());
            assertThat(isTargetDone(goalsAfter, "TARGET_USE")).isTrue();
        }
    }

    // ==================== EQUIP TESTS ====================

    @Nested
    class EquipTests {

        @Test
        public void equip_happyPath_itemAvailabilityChangesToEquip() throws URISyntaxException, InterruptedException {
            // Setup
            ConnectionController.ResponseDTO connection = createAuth();
            GameInstanceController.ResponseDTO session = createGame(connection.token());
            startGame(session.auth().token(), session.id());
            Thread.sleep(500);

            // Get equippable item and verify initial state
            List<GameInstanceInventoryController.SimpleResponseDTO> inventory = getInventoryList(session.auth().token(), session.id());
            GameInstanceInventoryController.SimpleResponseDTO equippableItem = findItemByLabel(inventory, "Epee");
            assertThat(equippableItem.actions()).contains("EQUIP");

            // Get details and check availability is FREE
            GameInstanceInventoryController.DetailResponseDTO detailsBefore = getItemDetails(session.auth().token(), session.id(), equippableItem.id());
            assertThat(detailsBefore.availability()).isEqualTo("FREE");

            // Equip the item
            ResponseEntity<Void> equipResponse = equipItem(session.auth().token(), session.id(), equippableItem.id());
            assertThat(equipResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

            // Verify availability changed to EQUIP
            GameInstanceInventoryController.DetailResponseDTO detailsAfter = getItemDetails(session.auth().token(), session.id(), equippableItem.id());
            assertThat(detailsAfter.availability()).isEqualTo("EQUIP");
        }

        @Test
        public void equip_itemNotEquippable_returns400() throws URISyntaxException, InterruptedException {
            // Setup
            ConnectionController.ResponseDTO connection = createAuth();
            GameInstanceController.ResponseDTO session = createGame(connection.token());
            startGame(session.auth().token(), session.id());
            Thread.sleep(500);

            // Get non-equippable item
            List<GameInstanceInventoryController.SimpleResponseDTO> inventory = getInventoryList(session.auth().token(), session.id());
            GameInstanceInventoryController.SimpleResponseDTO simpleItem = findItemByLabel(inventory, "Item simple");
            assertThat(simpleItem.actions()).doesNotContain("EQUIP");

            // Try to equip non-equippable item
            ResponseEntity<String> equipResponse = equipItemWithBody(session.auth().token(), session.id(), simpleItem.id());
            assertThat(equipResponse.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        }

        @Test
        public void equip_anotherItemEquipped_previousItemUnequipped() throws URISyntaxException, InterruptedException {
            // Setup
            ConnectionController.ResponseDTO connection = createAuth();
            GameInstanceController.ResponseDTO session = createGame(connection.token());
            startGame(session.auth().token(), session.id());
            Thread.sleep(500);

            // Get both equippable items
            List<GameInstanceInventoryController.SimpleResponseDTO> inventory = getInventoryList(session.auth().token(), session.id());
            GameInstanceInventoryController.SimpleResponseDTO sword = findItemByLabel(inventory, "Epee");
            GameInstanceInventoryController.SimpleResponseDTO shield = findItemByLabel(inventory, "Bouclier");

            // Equip first item (sword)
            equipItem(session.auth().token(), session.id(), sword.id());
            GameInstanceInventoryController.DetailResponseDTO swordAfterFirstEquip = getItemDetails(session.auth().token(), session.id(), sword.id());
            assertThat(swordAfterFirstEquip.availability()).isEqualTo("EQUIP");

            // Equip second item (shield)
            equipItem(session.auth().token(), session.id(), shield.id());

            // Verify sword is now unequipped and shield is equipped
            GameInstanceInventoryController.DetailResponseDTO swordAfterSecondEquip = getItemDetails(session.auth().token(), session.id(), sword.id());
            GameInstanceInventoryController.DetailResponseDTO shieldAfterEquip = getItemDetails(session.auth().token(), session.id(), shield.id());
            assertThat(swordAfterSecondEquip.availability()).isEqualTo("FREE");
            assertThat(shieldAfterEquip.availability()).isEqualTo("EQUIP");
        }
    }

    // ==================== UNEQUIP TESTS ====================

    @Nested
    class UnequipTests {

        @Test
        public void unequip_happyPath_itemAvailabilityChangesToFree() throws URISyntaxException, InterruptedException {
            // Setup
            ConnectionController.ResponseDTO connection = createAuth();
            GameInstanceController.ResponseDTO session = createGame(connection.token());
            startGame(session.auth().token(), session.id());
            Thread.sleep(500);

            // Equip an item first
            List<GameInstanceInventoryController.SimpleResponseDTO> inventory = getInventoryList(session.auth().token(), session.id());
            GameInstanceInventoryController.SimpleResponseDTO sword = findItemByLabel(inventory, "Epee");
            equipItem(session.auth().token(), session.id(), sword.id());

            // Verify it's equipped
            GameInstanceInventoryController.DetailResponseDTO detailsEquipped = getItemDetails(session.auth().token(), session.id(), sword.id());
            assertThat(detailsEquipped.availability()).isEqualTo("EQUIP");

            // Unequip
            ResponseEntity<Void> unequipResponse = unequipItem(session.auth().token(), session.id(), sword.id());
            assertThat(unequipResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

            // Verify availability is FREE
            GameInstanceInventoryController.DetailResponseDTO detailsUnequipped = getItemDetails(session.auth().token(), session.id(), sword.id());
            assertThat(detailsUnequipped.availability()).isEqualTo("FREE");
        }

        @Test
        public void unequip_itemNotEquipped_returns400() throws URISyntaxException, InterruptedException {
            // Setup
            ConnectionController.ResponseDTO connection = createAuth();
            GameInstanceController.ResponseDTO session = createGame(connection.token());
            startGame(session.auth().token(), session.id());
            Thread.sleep(500);

            // Get an equippable item that is NOT equipped
            List<GameInstanceInventoryController.SimpleResponseDTO> inventory = getInventoryList(session.auth().token(), session.id());
            GameInstanceInventoryController.SimpleResponseDTO sword = findItemByLabel(inventory, "Epee");

            // Verify it's not equipped
            GameInstanceInventoryController.DetailResponseDTO details = getItemDetails(session.auth().token(), session.id(), sword.id());
            assertThat(details.availability()).isEqualTo("FREE");

            // Try to unequip
            ResponseEntity<String> unequipResponse = unequipItemWithBody(session.auth().token(), session.id(), sword.id());
            assertThat(unequipResponse.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        }
    }

    // ==================== USE EQUIP TESTS ====================

    @Nested
    class UseEquipTests {

        @Test
        public void useEquip_happyPath_actionExecuted() throws URISyntaxException, InterruptedException {
            // Setup
            ConnectionController.ResponseDTO connection = createAuth();
            GameInstanceController.ResponseDTO session = createGame(connection.token());
            startGame(session.auth().token(), session.id());
            Thread.sleep(500);

            // Equip an item first
            List<GameInstanceInventoryController.SimpleResponseDTO> inventory = getInventoryList(session.auth().token(), session.id());
            GameInstanceInventoryController.SimpleResponseDTO sword = findItemByLabel(inventory, "Epee");
            equipItem(session.auth().token(), session.id(), sword.id());

            // Use the equipped item
            ResponseEntity<Void> useEquipResponse = useEquipItem(session.auth().token(), session.id(), sword.id());
            assertThat(useEquipResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        }

        @Test
        public void useEquip_itemNotEquipped_returns400() throws URISyntaxException, InterruptedException {
            // Setup
            ConnectionController.ResponseDTO connection = createAuth();
            GameInstanceController.ResponseDTO session = createGame(connection.token());
            startGame(session.auth().token(), session.id());
            Thread.sleep(500);

            // Get an equippable item that is NOT equipped
            List<GameInstanceInventoryController.SimpleResponseDTO> inventory = getInventoryList(session.auth().token(), session.id());
            GameInstanceInventoryController.SimpleResponseDTO sword = findItemByLabel(inventory, "Epee");

            // Try to use equipped action without equipping
            ResponseEntity<String> useEquipResponse = useEquipItemWithBody(session.auth().token(), session.id(), sword.id());
            assertThat(useEquipResponse.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        }

        @Test
        public void useEquip_happyPath_actionIsExecuted() throws URISyntaxException, InterruptedException {
            // Setup
            ConnectionController.ResponseDTO connection = createAuth();
            GameInstanceController.ResponseDTO session = createGame(connection.token());
            startGame(session.auth().token(), session.id());
            Thread.sleep(500);

            // Verify target is NOT done before useEquip
            List<GameInstanceScenarioController.GameGoalResponseDTO> goalsBefore = getGoals(session.auth().token(), session.id());
            assertThat(isTargetDone(goalsBefore, "TARGET_EQUIP_USE")).isFalse();

            // Get equippable item, equip it, then use it
            List<GameInstanceInventoryController.SimpleResponseDTO> inventory = getInventoryList(session.auth().token(), session.id());
            GameInstanceInventoryController.SimpleResponseDTO sword = findItemByLabel(inventory, "Epee");
            equipItem(session.auth().token(), session.id(), sword.id());
            useEquipItem(session.auth().token(), session.id(), sword.id());

            // Wait for async processing
            Thread.sleep(500);

            // Verify target IS done after useEquip (action was executed)
            List<GameInstanceScenarioController.GameGoalResponseDTO> goalsAfter = getGoals(session.auth().token(), session.id());
            assertThat(isTargetDone(goalsAfter, "TARGET_EQUIP_USE")).isTrue();
        }
    }

    // ==================== MERGE TESTS ====================

    @Nested
    class MergeTests {

        @Test
        public void merge_happyPath_itemsMergedIntoNewItem() throws URISyntaxException, InterruptedException {
            // Setup
            ConnectionController.ResponseDTO connection = createAuth();
            GameInstanceController.ResponseDTO session = createGame(connection.token());
            startGame(session.auth().token(), session.id());
            Thread.sleep(500);
            reset(pushPort);

            // Get merge items
            List<GameInstanceInventoryController.SimpleResponseDTO> inventoryBefore = getInventoryList(session.auth().token(), session.id());
            GameInstanceInventoryController.SimpleResponseDTO partA = findItemByLabel(inventoryBefore, "Partie A");
            GameInstanceInventoryController.SimpleResponseDTO partB = findItemByLabel(inventoryBefore, "Partie B");
            assertThat(partA.actions()).contains("MERGE");
            assertThat(partB.actions()).contains("MERGE");

            int countBefore = inventoryBefore.size();

            // Merge items
            ResponseEntity<Void> mergeResponse = mergeItems(session.auth().token(), session.id(), partA.id(), partB.id());
            assertThat(mergeResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

            // Verify inventory changed: 2 items removed, 1 new item added
            List<GameInstanceInventoryController.SimpleResponseDTO> inventoryAfter = getInventoryList(session.auth().token(), session.id());
            assertThat(inventoryAfter).hasSize(countBefore - 1); // -2 merged items + 1 result

            // Verify merged items are gone
            assertThat(inventoryAfter.stream().noneMatch(item -> item.label().equals("Partie A"))).isTrue();
            assertThat(inventoryAfter.stream().noneMatch(item -> item.label().equals("Partie B"))).isTrue();

            // Verify result item is present
            assertThat(inventoryAfter.stream().anyMatch(item -> item.label().equals("Objet complet"))).isTrue();

            // Verify PushPort was called
            verify(pushPort, atLeastOnce()).push(any(PushEvent.Inventory.class));
        }

        @Test
        public void merge_itemsNotMergeable_returns400WithErrorMessage() throws URISyntaxException, InterruptedException {
            // Setup
            ConnectionController.ResponseDTO connection = createAuth();
            GameInstanceController.ResponseDTO session = createGame(connection.token());
            startGame(session.auth().token(), session.id());
            Thread.sleep(500);

            // Get non-mergeable items
            List<GameInstanceInventoryController.SimpleResponseDTO> inventory = getInventoryList(session.auth().token(), session.id());
            GameInstanceInventoryController.SimpleResponseDTO simpleItem = findItemByLabel(inventory, "Item simple");
            GameInstanceInventoryController.SimpleResponseDTO potion = findItemByLabel(inventory, "Potion");

            // Try to merge non-mergeable items
            ResponseEntity<String> mergeResponse = mergeItemsWithBody(session.auth().token(), session.id(), simpleItem.id(), potion.id());
            assertThat(mergeResponse.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

            // Verify error message is in body
            assertThat(mergeResponse.getBody()).isNotNull();
        }
    }

    // ==================== HELPER METHODS ====================

    private GameInstanceInventoryController.SimpleResponseDTO findItemByLabel(
            List<GameInstanceInventoryController.SimpleResponseDTO> inventory, String label) {
        return inventory.stream()
                .filter(item -> item.label().equals(label))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Item not found: " + label));
    }

    private ConnectionController.ResponseDTO createAuth() throws URISyntaxException {
        final String baseUrl = "http://localhost:" + randomServerPort + "/connect/";
        URI uri = new URI(baseUrl);

        ConnectionController.RequestDTO request = new ConnectionController.RequestDTO("testDeviceId" + System.currentTimeMillis());

        ResponseEntity<ConnectionController.ResponseDTO> result = this.restTemplate.postForEntity(uri, request, ConnectionController.ResponseDTO.class);
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

    private List<GameInstanceInventoryController.SimpleResponseDTO> getInventoryList(String gameToken, String sessionId) throws URISyntaxException {
        final String baseUrl = "http://localhost:" + randomServerPort + "/instances/" + sessionId + "/inventory/";
        URI uri = new URI(baseUrl);

        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.add("Authorization", gameToken);
        headers.add("Language", "FR");

        ResponseEntity<List<GameInstanceInventoryController.SimpleResponseDTO>> result = this.restTemplate
                .exchange(uri, HttpMethod.GET, new HttpEntity<>(headers),
                        new ParameterizedTypeReference<>() {});
        return result.getBody();
    }

    private GameInstanceInventoryController.DetailResponseDTO getItemDetails(String gameToken, String sessionId, String itemId) throws URISyntaxException {
        final String baseUrl = "http://localhost:" + randomServerPort + "/instances/" + sessionId + "/inventory/" + itemId;
        URI uri = new URI(baseUrl);

        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.add("Authorization", gameToken);
        headers.add("Language", "FR");

        ResponseEntity<GameInstanceInventoryController.DetailResponseDTO> result = this.restTemplate
                .exchange(uri, HttpMethod.GET, new HttpEntity<>(headers), GameInstanceInventoryController.DetailResponseDTO.class);
        return result.getBody();
    }

    private ResponseEntity<Void> dropItem(String gameToken, String sessionId, String itemId) throws URISyntaxException {
        final String baseUrl = "http://localhost:" + randomServerPort + "/instances/" + sessionId + "/inventory/" + itemId;
        URI uri = new URI(baseUrl);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", gameToken);

        return this.restTemplate.exchange(uri, HttpMethod.DELETE, new HttpEntity<>(headers), Void.class);
    }

    private ResponseEntity<Void> consumeItem(String gameToken, String sessionId, String itemId) throws URISyntaxException {
        final String baseUrl = "http://localhost:" + randomServerPort + "/instances/" + sessionId + "/inventory/" + itemId + "/consume";
        URI uri = new URI(baseUrl);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", gameToken);

        return this.restTemplate.exchange(uri, HttpMethod.POST, new HttpEntity<>(headers), Void.class);
    }

    private ResponseEntity<String> consumeItemWithBody(String gameToken, String sessionId, String itemId) throws URISyntaxException {
        final String baseUrl = "http://localhost:" + randomServerPort + "/instances/" + sessionId + "/inventory/" + itemId + "/consume";
        URI uri = new URI(baseUrl);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", gameToken);

        return this.restTemplate.exchange(uri, HttpMethod.POST, new HttpEntity<>(headers), String.class);
    }

    private ResponseEntity<Void> useItem(String gameToken, String sessionId, String itemId) throws URISyntaxException {
        final String baseUrl = "http://localhost:" + randomServerPort + "/instances/" + sessionId + "/inventory/" + itemId + "/use";
        URI uri = new URI(baseUrl);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", gameToken);

        return this.restTemplate.exchange(uri, HttpMethod.POST, new HttpEntity<>(headers), Void.class);
    }

    private ResponseEntity<String> useItemWithBody(String gameToken, String sessionId, String itemId) throws URISyntaxException {
        final String baseUrl = "http://localhost:" + randomServerPort + "/instances/" + sessionId + "/inventory/" + itemId + "/use";
        URI uri = new URI(baseUrl);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", gameToken);

        return this.restTemplate.exchange(uri, HttpMethod.POST, new HttpEntity<>(headers), String.class);
    }

    private ResponseEntity<Void> equipItem(String gameToken, String sessionId, String itemId) throws URISyntaxException {
        final String baseUrl = "http://localhost:" + randomServerPort + "/instances/" + sessionId + "/inventory/" + itemId + "/equip";
        URI uri = new URI(baseUrl);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", gameToken);

        return this.restTemplate.exchange(uri, HttpMethod.POST, new HttpEntity<>(headers), Void.class);
    }

    private ResponseEntity<String> equipItemWithBody(String gameToken, String sessionId, String itemId) throws URISyntaxException {
        final String baseUrl = "http://localhost:" + randomServerPort + "/instances/" + sessionId + "/inventory/" + itemId + "/equip";
        URI uri = new URI(baseUrl);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", gameToken);

        return this.restTemplate.exchange(uri, HttpMethod.POST, new HttpEntity<>(headers), String.class);
    }

    private ResponseEntity<Void> unequipItem(String gameToken, String sessionId, String itemId) throws URISyntaxException {
        final String baseUrl = "http://localhost:" + randomServerPort + "/instances/" + sessionId + "/inventory/" + itemId + "/unequip";
        URI uri = new URI(baseUrl);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", gameToken);

        return this.restTemplate.exchange(uri, HttpMethod.POST, new HttpEntity<>(headers), Void.class);
    }

    private ResponseEntity<String> unequipItemWithBody(String gameToken, String sessionId, String itemId) throws URISyntaxException {
        final String baseUrl = "http://localhost:" + randomServerPort + "/instances/" + sessionId + "/inventory/" + itemId + "/unequip";
        URI uri = new URI(baseUrl);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", gameToken);

        return this.restTemplate.exchange(uri, HttpMethod.POST, new HttpEntity<>(headers), String.class);
    }

    private ResponseEntity<Void> useEquipItem(String gameToken, String sessionId, String itemId) throws URISyntaxException {
        final String baseUrl = "http://localhost:" + randomServerPort + "/instances/" + sessionId + "/inventory/" + itemId + "/equip/use";
        URI uri = new URI(baseUrl);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", gameToken);

        return this.restTemplate.exchange(uri, HttpMethod.POST, new HttpEntity<>(headers), Void.class);
    }

    private ResponseEntity<String> useEquipItemWithBody(String gameToken, String sessionId, String itemId) throws URISyntaxException {
        final String baseUrl = "http://localhost:" + randomServerPort + "/instances/" + sessionId + "/inventory/" + itemId + "/equip/use";
        URI uri = new URI(baseUrl);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", gameToken);

        return this.restTemplate.exchange(uri, HttpMethod.POST, new HttpEntity<>(headers), String.class);
    }

    private ResponseEntity<Void> mergeItems(String gameToken, String sessionId, String itemId1, String itemId2) throws URISyntaxException {
        final String baseUrl = "http://localhost:" + randomServerPort + "/instances/" + sessionId + "/inventory/merge";
        URI uri = new URI(baseUrl);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("Authorization", gameToken);

        List<String> items = List.of(itemId1, itemId2);

        return this.restTemplate.exchange(uri, HttpMethod.POST, new HttpEntity<>(items, headers), Void.class);
    }

    private ResponseEntity<String> mergeItemsWithBody(String gameToken, String sessionId, String itemId1, String itemId2) throws URISyntaxException {
        final String baseUrl = "http://localhost:" + randomServerPort + "/instances/" + sessionId + "/inventory/merge";
        URI uri = new URI(baseUrl);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("Authorization", gameToken);

        List<String> items = List.of(itemId1, itemId2);

        return this.restTemplate.exchange(uri, HttpMethod.POST, new HttpEntity<>(items, headers), String.class);
    }

    private List<GameInstanceScenarioController.GameGoalResponseDTO> getGoals(String gameToken, String sessionId) throws URISyntaxException {
        final String baseUrl = "http://localhost:" + randomServerPort + "/instances/" + sessionId + "/goals";
        URI uri = new URI(baseUrl);

        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.add("Authorization", gameToken);
        headers.add("Language", "FR");

        ResponseEntity<List<GameInstanceScenarioController.GameGoalResponseDTO>> result = this.restTemplate
                .exchange(uri, HttpMethod.GET, new HttpEntity<>(headers),
                        new ParameterizedTypeReference<>() {});
        return result.getBody();
    }

    private boolean isTargetDone(List<GameInstanceScenarioController.GameGoalResponseDTO> goals, String targetId) {
        return goals.stream()
                .flatMap(goal -> goal.targets().stream())
                .filter(target -> target.id().equals(targetId))
                .findFirst()
                .map(GameInstanceScenarioController.GameTargetSimpleResponseDTO::done)
                .orElse(false);
    }
}

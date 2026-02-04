package fr.plop.contexts.game.config.template.domain.usecase.generator.json;

import fr.plop.contexts.game.config.Image.domain.ImageObject;
import fr.plop.contexts.game.config.condition.Condition;
import fr.plop.contexts.game.config.consequence.Consequence;
import fr.plop.contexts.game.config.inventory.domain.model.GameConfigInventoryItem;
import fr.plop.contexts.game.config.map.domain.MapItem;
import fr.plop.contexts.game.config.scenario.domain.model.PossibilityTrigger;
import fr.plop.contexts.game.config.talk.domain.TalkItem;
import fr.plop.contexts.game.config.template.domain.model.Template;
import fr.plop.contexts.game.config.board.domain.model.BoardSpace;
import fr.plop.generic.enumerate.Priority;
import fr.plop.generic.position.Rectangle;
import fr.plop.contexts.game.instance.situation.domain.GameInstanceSituation;
import fr.plop.subs.i18n.domain.Language;
import fr.plop.subs.image.Image;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.offset;

/**
 * Test de validation du skill json-template-generator.
 *
 * Ce test vérifie que la structure JSON documentée dans le skill
 * (.claude/commands/json-template-generator.md) est correctement
 * parsée par TemplateGeneratorJsonUseCase.
 *
 * Quand le skill évolue (ajout de nouvelles sections comme Board, Maps),
 * ce test doit être mis à jour pour couvrir les nouvelles structures.
 */
public class TemplateGeneratorJsonUseCaseSkillValidationTest {

    private final TemplateGeneratorJsonUseCase generator = new TemplateGeneratorJsonUseCase();
    private Template template;

    @BeforeEach
    void setUp() throws IOException {
        String json = Files.readString(Path.of("src/test/resources/template/skill_validation_test.json"));
        template = generator.template(TemplateGeneratorRootParser.apply(json));
    }

    @Test
    @DisplayName("Le template est correctement parsé")
    void templateIsParsed() {
        assertThat(template).isNotNull();
        //assertThat(template.code().value()).isEqualTo("SKILL_VALIDATION_TEST");
    }

    @Nested
    @DisplayName("Talk - Validation du skill")
    class TalkValidation {

        @Test
        @DisplayName("Items: 5 messages de discussion")
        void itemsCount() {
            assertThat(template.talk().items()).hasSize(5);
        }

        @Test
        @DisplayName("Character: PNJ_MARCEL accessible via TalkItem")
        void characterAccessible() {
            // Le character est accessible via les TalkItems
            TalkItem firstItem = template.talk().items().getFirst();

            assertThat(firstItem.character().name()).isEqualTo("PNJ_MARCEL");
            // L'image est accessible via characterReference
            assertThat(firstItem.characterReference().image()).isNotNull();
            assertThat(firstItem.characterReference().image().value()).contains("happy");
        }

        @Test
        @DisplayName("Message simple avec chaînage (next)")
        void messageContinue() {
            // Premier message: "Bonjour l'ami."
            TalkItem item = template.talk().items().get(0);

            assertThat(item.out().resolve(new GameInstanceSituation()).value(Language.FR))
                    .isEqualTo("Bonjour l'ami.");
            assertThat(item.character().name()).isEqualTo("PNJ_MARCEL");
            assertThat(item.characterReference().value()).isEqualTo("happy");
            assertThat(item.isContinue()).isTrue();

            // Vérifie que next pointe vers le deuxième item
            TalkItem nextItem = template.talk().items().get(1);
            assertThat(item.nextId()).isEqualTo(nextItem.id());
        }

        @Test
        @DisplayName("Message avec options (sans condition)")
        void messageWithOptions() {
            // Deuxième message: "Voici ma carte." avec options
            TalkItem item = template.talk().items().get(1);

            assertThat(item.out().resolve(new GameInstanceSituation()).value(Language.FR))
                    .isEqualTo("Voici ma carte.");
            assertThat(item.isOptions()).isTrue();
            assertThat(item.options().options().toList()).hasSize(2);

            // Option 1: avec redirection
            var opt1 = item.options().options().toList().get(0);
            assertThat(opt1.value().value(Language.FR)).isEqualTo("Merci, j'ai des questions.");
            assertThat(opt1.optNextId()).isPresent();

            // Option 2: fin de discussion (next = null)
            var opt2 = item.options().options().toList().get(1);
            assertThat(opt2.value().value(Language.FR)).isEqualTo("Merci, c'est parti !");
            assertThat(opt2.optNextId()).isEmpty();
        }

        @Test
        @DisplayName("Option avec condition INVENTORY_HAS")
        void optionWithCondition() {
            // Troisième message: "Que puis-je faire pour toi ?" avec option conditionnelle
            TalkItem item = template.talk().items().get(2);

            assertThat(item.out().resolve(new GameInstanceSituation()).value(Language.FR))
                    .isEqualTo("Que puis-je faire pour toi ?");
            assertThat(item.options().options().toList()).hasSize(3);

            // Option 3: avec condition INVENTORY_HAS
            var opt3 = item.options().options().toList().get(2);
            assertThat(opt3.value().value(Language.FR)).isEqualTo("J'ai la carte complete !");
            assertThat(opt3.optCondition()).isPresent();
            assertThat(opt3.optCondition().get()).isInstanceOf(Condition.InventoryHasItem.class);

            Condition.InventoryHasItem condition = (Condition.InventoryHasItem) opt3.optCondition().get();
            // La condition référence l'item CARTE_COMPLETE de l'inventaire
            var carteComplete = findInventoryItemByLabel("Carte complete");
            assertThat(condition.itemId()).isEqualTo(carteComplete.id());
        }

        @Test
        @DisplayName("Message avec redirection vers un autre message")
        void messageWithRedirection() {
            // Quatrième message: "Sophie est au café." -> redirige vers message racine 2
            TalkItem item = template.talk().items().get(3);

            assertThat(item.out().resolve(new GameInstanceSituation()).value(Language.FR))
                    .isEqualTo("Sophie est au cafe.");
            assertThat(item.characterReference().value()).isEqualTo("default");
            assertThat(item.isContinue()).isTrue();

            // Redirige vers "Que puis-je faire pour toi ?"
            TalkItem targetItem = template.talk().items().get(2);
            assertThat(item.nextId()).isEqualTo(targetItem.id());
        }

        @Test
        @DisplayName("Message simple sans next (fin de discussion)")
        void messageSimple() {
            // Cinquième message: fin de discussion
            TalkItem item = template.talk().items().get(4);

            assertThat(item.isSimple()).isTrue();
            assertThat(item.out().resolve(new GameInstanceSituation()).value(Language.FR))
                    .isEqualTo("Bravo ! Tu peux maintenant chercher le tresor.");
        }
    }

    @Nested
    @DisplayName("Inventory - Validation du skill")
    class InventoryValidation {

        @Test
        @DisplayName("Items: 5 objets dans l'inventaire")
        void itemsCount() {
            assertThat(template.inventory().items()).hasSize(5);
        }

        @Test
        @DisplayName("Item UNIQUE sans action")
        void itemUniqueSimple() {
            var carte1 = findInventoryItemByLabel("Carte dechiree");

            assertThat(carte1.label().value(Language.FR)).isEqualTo("Carte dechiree");
            assertThat(carte1.optDescription()).isPresent();
            assertThat(carte1.optDescription().get().value(Language.FR)).isEqualTo("Premiere partie de la carte.");
            assertThat(carte1.image().type()).isEqualTo(Image.Type.ASSET);
            assertThat(carte1.image().value()).isEqualTo("assets/demo/inventory/carte_dechiree.png");
            assertThat(carte1.type()).isEqualTo(GameConfigInventoryItem.Type.UNIQUE);
            assertThat(carte1.initValue()).isEqualTo(0);
            assertThat(carte1.actionType()).isEqualTo(GameConfigInventoryItem.ActionType.NONE);
        }

        @Test
        @DisplayName("Item UNIQUE avec action EQUIPPABLE")
        void itemWithEquippableAction() {
            GameConfigInventoryItem pelle = findInventoryItemByLabel("Pelle");

            assertThat(pelle.label().value(Language.FR)).isEqualTo("Pelle");
            assertThat(pelle.type()).isEqualTo(GameConfigInventoryItem.Type.UNIQUE);
            assertThat(pelle.actionType()).isEqualTo(GameConfigInventoryItem.ActionType.EQUIPPABLE);
        }

        @Test
        @DisplayName("Item COLLECTION")
        void itemCollection() {
            var piecesOr = findInventoryItemByLabel("Pieces d'or");

            assertThat(piecesOr.label().value(Language.FR)).isEqualTo("Pieces d'or");
            assertThat(piecesOr.type()).isEqualTo(GameConfigInventoryItem.Type.COLLECTION);
        }

        @Test
        @DisplayName("MergeRules: 2 règles de fusion (permutations)")
        void mergeRules() {
            assertThat(template.inventory().mergedRules()).hasSize(2);

            var carte1 = findInventoryItemByLabel("Carte dechiree");
            var carteSophie = findInventoryItemByLabel("Morceau carte Sophie");
            var carteComplete = findInventoryItemByLabel("Carte complete");

            // Règle 1: CARTE_1 + CARTE_SOPHIE -> CARTE_COMPLETE
            var rule1 = template.inventory().mergedRules().get(0);
            assertThat(rule1.accept()).containsExactly(carte1.id(), carteSophie.id());
            assertThat(rule1.convertTo()).isEqualTo(carteComplete.id());

            // Règle 2: CARTE_SOPHIE + CARTE_1 -> CARTE_COMPLETE (permutation)
            var rule2 = template.inventory().mergedRules().get(1);
            assertThat(rule2.accept()).containsExactly(carteSophie.id(), carte1.id());
            assertThat(rule2.convertTo()).isEqualTo(carteComplete.id());
        }
    }

    @Nested
    @DisplayName("Scenario - Validation du skill")
    class ScenarioValidation {

        @Test
        @DisplayName("Steps: 1 étape avec 1 target")
        void stepsAndTargets() {
            assertThat(template.scenario().steps()).hasSize(1);

            var step = template.scenario().steps().getFirst();
            assertThat(step.label().value(Language.FR)).isEqualTo("Etape principale");
            assertThat(step.targets()).hasSize(1);
            assertThat(step.targets().getFirst().label().value(Language.FR)).isEqualTo("Trouver le tresor");
        }

        @Test
        @DisplayName("Possibility avec trigger AbsoluteTime et conséquence Talk")
        void possibilityTalkOnStart() {
            var step = template.scenario().steps().getFirst();
            var possibility = step.possibilities().get(0);

            // Trigger AbsoluteTime = 0 (au démarrage)
            assertThat(possibility.trigger()).isInstanceOf(PossibilityTrigger.AbsoluteTime.class);
            var trigger = (PossibilityTrigger.AbsoluteTime) possibility.trigger();
            assertThat(trigger.value().value()).isEqualTo(0);

            // Conséquence: afficher un Talk
            assertThat(possibility.consequences()).hasSize(1);
            assertThat(possibility.consequences().getFirst()).isInstanceOf(Consequence.DisplayTalk.class);

            // Vérifie que le Talk référencé est le premier message
            var displayTalk = (Consequence.DisplayTalk) possibility.consequences().getFirst();
            assertThat(displayTalk.talkId()).isEqualTo(template.talk().items().getFirst().id());
        }

        @Test
        @DisplayName("Possibility avec trigger TALKEND et conséquence INVENTORY_ADD")
        void possibilityInventoryAddOnTalkEnd() {
            var step = template.scenario().steps().getFirst();
            var possibility = step.possibilities().get(1);

            // Trigger TalkEnd
            assertThat(possibility.trigger()).isInstanceOf(PossibilityTrigger.Talk.class);
            var trigger = (PossibilityTrigger.Talk) possibility.trigger();

            // Le trigger référence le deuxième TalkItem (celui avec les options)
            var talkItem = template.talk().items().get(1);
            assertThat(trigger.talkId()).isEqualTo(talkItem.id());

            // Conséquence: ajouter un item à l'inventaire
            assertThat(possibility.consequences()).hasSize(1);
            assertThat(possibility.consequences().getFirst()).isInstanceOf(Consequence.InventoryAddItem.class);

            var consequence = (Consequence.InventoryAddItem) possibility.consequences().getFirst();
            var carte1 = findInventoryItemByLabel("Carte dechiree");
            assertThat(consequence.itemId()).isEqualTo(carte1.id());
        }
    }

    @Nested
    @DisplayName("Maps - Validation du skill")
    class MapsValidation {

        @Test
        @DisplayName("Maps: 2 cartes chargées")
        void mapsCount() {
            assertThat(template.map().items()).hasSize(2);
        }

        @Test
        @DisplayName("Carte principale avec priorité HIGH")
        void mapPrincipale() {
            MapItem map = template.map().items().get(0);

            assertThat(map.imageGeneric().label()).isEqualTo("Carte principale Bellecour");
            assertThat(map.priority()).isEqualTo(Priority.HIGH);
            assertThat(map.imageGeneric().imageType()).isEqualTo(Image.Type.ASSET);
            assertThat(map.imageGeneric().imageValue()).isEqualTo("assets/demo/map/bellecour.png");
        }

        @Test
        @DisplayName("Carte principale avec 3 objets")
        void mapObjectsCount() {
            MapItem map = template.map().items().get(0);
            assertThat(map.imageGeneric().objects()).hasSize(3);
        }

        @Test
        @DisplayName("Objet image sans condition (Marcel)")
        void objectImageSimple() {
            MapItem map = template.map().items().get(0);
            ImageObject marcel = map.imageGeneric().objects().get(0);

            assertThat(marcel).isInstanceOf(ImageObject._Image.class);
            assertThat(marcel.top()).isCloseTo(0.15, offset(0.001));
            assertThat(marcel.left()).isCloseTo(0.5, offset(0.001));

            ImageObject._Image marcelImage = (ImageObject._Image) marcel;
            assertThat(marcelImage.value().value()).isEqualTo("assets/demo/map/marcel.png");
            assertThat(marcel.optCondition()).isEmpty();
        }

        @Test
        @DisplayName("Objet image avec condition INVENTORY_HAS (Coffre)")
        void objectImageWithCondition() {
            MapItem map = template.map().items().get(0);
            ImageObject coffre = map.imageGeneric().objects().get(1);

            assertThat(coffre).isInstanceOf(ImageObject._Image.class);
            assertThat(coffre.top()).isCloseTo(0.7, offset(0.001));
            assertThat(coffre.left()).isCloseTo(0.3, offset(0.001));

            // Vérifie la condition
            assertThat(coffre.optCondition()).isPresent();
            assertThat(coffre.optCondition().get()).isInstanceOf(Condition.InventoryHasItem.class);

            Condition.InventoryHasItem condition = (Condition.InventoryHasItem) coffre.optCondition().get();
            var carteComplete = findInventoryItemByLabel("Carte complete");
            assertThat(condition.itemId()).isEqualTo(carteComplete.id());
        }

        @Test
        @DisplayName("Objet point (marqueur rouge)")
        void objectPoint() {
            MapItem map = template.map().items().get(0);
            ImageObject point = map.imageGeneric().objects().get(2);

            assertThat(point).isInstanceOf(ImageObject.Point.class);
            assertThat(point.top()).isCloseTo(0.5, offset(0.001));
            assertThat(point.left()).isCloseTo(0.8, offset(0.001));

            ImageObject.Point pointObj = (ImageObject.Point) point;
            assertThat(pointObj.color()).isEqualTo("red");
        }

        @Test
        @DisplayName("Carte secondaire avec priorité MEDIUM et sans objets")
        void mapSecondaire() {
            MapItem map = template.map().items().get(1);

            assertThat(map.imageGeneric().label()).isEqualTo("Carte secondaire");
            assertThat(map.priority()).isEqualTo(Priority.MEDIUM);
            assertThat(map.imageGeneric().objects()).isEmpty();
        }
    }

    @Nested
    @DisplayName("Board - Validation du skill")
    class BoardValidation {

        @Test
        @DisplayName("Spaces: 3 zones géographiques")
        void spacesCount() {
            assertThat(template.board().spaces()).hasSize(3);
        }

        @Test
        @DisplayName("Espace BELLECOUR avec 1 rectangle et priorité MEDIUM")
        void spaceBellecour() {
            BoardSpace bellecour = findBoardSpaceByLabel("Place Bellecour");

            assertThat(bellecour.label()).isEqualTo("Place Bellecour");
            assertThat(bellecour.priority()).isEqualTo(Priority.MEDIUM);
            assertThat(bellecour.rectangles()).hasSize(1);

            Rectangle rect = bellecour.rectangles().get(0);
            assertThat(rect.bottomLeft().lat().doubleValue()).isCloseTo(45.75685821314044, offset(0.0000001));
            assertThat(rect.bottomLeft().lng().doubleValue()).isCloseTo(4.829412996001784, offset(0.0000001));
            assertThat(rect.topRight().lat().doubleValue()).isCloseTo(45.75834025856722, offset(0.0000001));
            assertThat(rect.topRight().lng().doubleValue()).isCloseTo(4.83419244823547, offset(0.0000001));
        }

        @Test
        @DisplayName("Espace BELLECOUR_STATUE avec priorité HIGH")
        void spaceBellecourStatue() {
            BoardSpace statue = findBoardSpaceByLabel("Statue equestre de Louis XIV");

            assertThat(statue.label()).isEqualTo("Statue equestre de Louis XIV");
            assertThat(statue.priority()).isEqualTo(Priority.HIGH);
            assertThat(statue.rectangles()).hasSize(1);
        }

        @Test
        @DisplayName("Espace BELLECOUR_AIRE_JEUX avec 2 rectangles")
        void spaceBellecourAireJeux() {
            BoardSpace aireJeux = findBoardSpaceByLabel("Aire de Jeux Place Bellecour");

            assertThat(aireJeux.label()).isEqualTo("Aire de Jeux Place Bellecour");
            assertThat(aireJeux.priority()).isEqualTo(Priority.MEDIUM);
            assertThat(aireJeux.rectangles()).hasSize(2);

            // Premier rectangle
            Rectangle rect1 = aireJeux.rectangles().get(0);
            assertThat(rect1.bottomLeft().lat().doubleValue()).isCloseTo(45.75705883259998, offset(0.0000001));
            assertThat(rect1.bottomLeft().lng().doubleValue()).isCloseTo(4.830043297214081, offset(0.0000001));

            // Deuxième rectangle
            Rectangle rect2 = aireJeux.rectangles().get(1);
            assertThat(rect2.bottomLeft().lat().doubleValue()).isCloseTo(45.75684710935075, offset(0.0000001));
            assertThat(rect2.bottomLeft().lng().doubleValue()).isCloseTo(4.831109191036608, offset(0.0000001));
        }

        @Test
        @DisplayName("Recherche par label via BoardConfig.findByLabel")
        void findByLabel() {
            var bellecour = template.board().findByLabel("Place Bellecour");
            assertThat(bellecour).isPresent();
            assertThat(bellecour.get().label()).isEqualTo("Place Bellecour");

            var notFound = template.board().findByLabel("Inexistant");
            assertThat(notFound).isEmpty();
        }
    }

    // Utilitaire pour trouver un espace Board par son label
    private BoardSpace findBoardSpaceByLabel(String label) {
        return template.board().spaces().stream()
                .filter(space -> space.label().equals(label))
                .findFirst()
                .orElseThrow(() -> new AssertionError("BoardSpace not found with label: " + label));
    }

    // Utilitaire pour trouver un item d'inventaire par son label
    private GameConfigInventoryItem findInventoryItemByLabel(String label) {
        return template.inventory().items().stream()
                .filter(item -> item.label().value(Language.FR).equals(label))
                .findFirst()
                .orElseThrow(() -> new AssertionError("InventoryItem not found with label: " + label));
    }
}

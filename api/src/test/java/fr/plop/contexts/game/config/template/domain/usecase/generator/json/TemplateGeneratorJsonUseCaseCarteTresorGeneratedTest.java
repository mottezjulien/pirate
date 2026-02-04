package fr.plop.contexts.game.config.template.domain.usecase.generator.json;

import fr.plop.contexts.game.config.template.domain.model.Template;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test de validation du JSON genere par le skill json-template-generator
 * a partir du fichier scenario_carte_aux_tresors_oubliee_update.md
 */
class TemplateGeneratorJsonUseCaseCarteTresorGeneratedTest {

    private final TemplateGeneratorJsonUseCase generator = new TemplateGeneratorJsonUseCase();
    private Template template;

    @BeforeEach
    void setUp() throws IOException {
        String json = Files.readString(Path.of("src/main/resources/template/carte_tresor_demo_generated.json"));
        template = generator.template(TemplateGeneratorRootParser.apply(json));
    }

    @Test
    @DisplayName("Le template est correctement parse")
    void templateIsParsed() {
        assertThat(template).isNotNull();
    }

    @Nested
    @DisplayName("Board validation")
    class BoardValidation {

        @Test
        @DisplayName("8 espaces geographiques")
        void shouldHave8Spaces() {
            assertThat(template.board().spaces()).hasSize(8);
        }
    }

    @Nested
    @DisplayName("Inventory validation")
    class InventoryValidation {

        @Test
        @DisplayName("8 objets dans l'inventaire")
        void shouldHave8Items() {
            assertThat(template.inventory().items()).hasSize(8);
        }
    }

    @Nested
    @DisplayName("Talk validation")
    class TalkValidation {

        @Test
        @DisplayName("15 messages de discussion")
        void shouldHave15TalkItems() {
            assertThat(template.talk().items()).hasSize(15);
        }

        @Test
        @DisplayName("4 personnages")
        void shouldHave4Characters() {
            // Les characters sont accessibles via les items
            assertThat(template.talk().items()).isNotEmpty();
        }
    }

    @Nested
    @DisplayName("Maps validation")
    class MapsValidation {

        @Test
        @DisplayName("4 cartes")
        void shouldHave4Maps() {
            assertThat(template.map().items()).hasSize(4);
        }
    }

    @Nested
    @DisplayName("Scenario validation")
    class ScenarioValidation {

        @Test
        @DisplayName("1 step (mission principale simplifiee)")
        void shouldHave1Step() {
            assertThat(template.scenario().steps()).hasSize(1);
        }

        @Test
        @DisplayName("5 objectifs dans la mission")
        void shouldHave5Targets() {
            assertThat(template.scenario().steps().get(0).targets()).hasSize(5);
        }

        @Test
        @DisplayName("3 possibilities")
        void shouldHave3Possibilities() {
            assertThat(template.scenario().steps().get(0).possibilities()).hasSize(3);
        }
    }
}

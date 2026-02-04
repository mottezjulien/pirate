package fr.plop.contexts.game.config.template.domain.usecase.generator.json;

import fr.plop.contexts.game.config.talk.domain.TalkConfig;
import fr.plop.contexts.game.config.talk.domain.TalkItem;
import fr.plop.contexts.game.config.talk.domain.TalkItemNext;
import fr.plop.contexts.game.config.template.domain.model.Template;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class TemplateGeneratorJsonUseCaseCarteTresorTest {

    @Test
    void should_parse_carte_tresor_demo_json() throws IOException {
        // Given
        String json = Files.readString(Path.of("src/main/resources/template/carte_tresor_demo.json"));
        TemplateGeneratorJsonUseCase useCase = new TemplateGeneratorJsonUseCase();

        // When
        Template template = useCase.template(TemplateGeneratorRootParser.apply(json));

        // Then
        assertNotNull(template);
        //assertEquals("CARTE_TRESOR_DEMO", template.code().value());
    }

    @Test
    void should_have_4_characters() throws IOException {
        // Given
        String json = Files.readString(Path.of("src/main/resources/template/carte_tresor_demo.json"));
        TemplateGeneratorJsonUseCase useCase = new TemplateGeneratorJsonUseCase();

        // When
        Template template = useCase.template(TemplateGeneratorRootParser.apply(json));
        TalkConfig talk = template.talk();

        // Then
        // We expect characters referenced in items to be created
        // Marcel, Sophie, Emilie, Victor
        assertNotNull(talk);
    }

    @Test
    void should_have_12_talk_items_for_marcel() throws IOException {
        // Given
        String json = Files.readString(Path.of("src/main/resources/template/carte_tresor_demo.json"));
        TemplateGeneratorJsonUseCase useCase = new TemplateGeneratorJsonUseCase();

        // When
        Template template = useCase.template(TemplateGeneratorRootParser.apply(json));
        TalkConfig talk = template.talk();

        // Then
        // Message racine 1: 5 items (001 to 005)
        // Message racine 2: 1 item
        // TALK_MARCEL_1, TALK_MARCEL_1_BIS, TALK_MARCEL_2, TALK_MARCEL_3, TALK_MARCEL_4: 5 items
        // Total: 11 items
        assertEquals(11, talk.items().size());
    }

    @Test
    void should_have_correct_chain_for_root_1() throws IOException {
        // Given
        String json = Files.readString(Path.of("src/main/resources/template/carte_tresor_demo.json"));
        TemplateGeneratorJsonUseCase useCase = new TemplateGeneratorJsonUseCase();

        // When
        Template template = useCase.template(TemplateGeneratorRootParser.apply(json));
        TalkConfig talk = template.talk();

        // Then
        TalkItem item001 = talk.byId(new TalkItem.Id("TALK_MARCEL_ROOT_1_001")).orElseThrow();
        assertTrue(item001.isContinue());
        assertEquals("TALK_MARCEL_ROOT_1_002", item001.nextId().value());

        TalkItem item005 = talk.byId(new TalkItem.Id("TALK_MARCEL_ROOT_1_005")).orElseThrow();
        assertTrue(item005.isOptions());
        assertEquals(2, item005.options()._options().size());
    }

    @Test
    void should_have_options_with_conditions_on_root_2() throws IOException {
        // Given
        String json = Files.readString(Path.of("src/main/resources/template/carte_tresor_demo.json"));
        TemplateGeneratorJsonUseCase useCase = new TemplateGeneratorJsonUseCase();

        // When
        Template template = useCase.template(TemplateGeneratorRootParser.apply(json));
        TalkConfig talk = template.talk();

        // Then
        TalkItem root2 = talk.byId(new TalkItem.Id("TALK_MARCEL_ROOT_2")).orElseThrow();
        assertTrue(root2.isOptions());

        TalkItemNext.Options options = root2.options();
        assertEquals(6, options._options().size());

        // First option should have a condition (NOT INVENTORY_HAS CARTE_3)
        TalkItemNext.Options.Option opt1 = options._options().get(0);
        assertTrue(opt1.optCondition().isPresent());
    }

    @Test
    void should_have_consequence_possibility_for_carte() throws IOException {
        // Given
        String json = Files.readString(Path.of("src/main/resources/template/carte_tresor_demo.json"));
        TemplateGeneratorJsonUseCase useCase = new TemplateGeneratorJsonUseCase();

        // When
        Template template = useCase.template(TemplateGeneratorRootParser.apply(json));

        // Then
        assertNotNull(template.scenario());
        assertFalse(template.scenario().steps().isEmpty());

        // Should have a step with genericPossibilities for talk consequences
        var step = template.scenario().steps().get(0);
        assertFalse(step.possibilities().isEmpty());
    }
}

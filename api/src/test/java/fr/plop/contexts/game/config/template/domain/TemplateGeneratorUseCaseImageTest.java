package fr.plop.contexts.game.config.template.domain;

import fr.plop.contexts.game.config.template.domain.model.Template;
import fr.plop.contexts.game.config.template.domain.usecase.generator.TemplateGeneratorUseCase;
import fr.plop.subs.image.Image;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class TemplateGeneratorUseCaseImageTest {

    private final TemplateGeneratorUseCase generator = new TemplateGeneratorUseCase();

    @Test
    public void oneImage() {
        String scriptContent = """
                TEST_IMAGE1
                --- Image:ASSET:pouet/img.jpg
                """;

        TemplateGeneratorUseCase.Script script = new TemplateGeneratorUseCase.Script(scriptContent);
        Template template = generator.apply(script);

        assertThat(template).isNotNull();
        assertThat(template.code().value()).isEqualTo("TEST_IMAGE1");

        assertThat(template.image().items()).hasSize(1)
            .anySatisfy(imageItem -> {
                assertThat(imageItem.id()).isNotNull();
                assertThat(imageItem.generic().value().type()).isEqualTo(Image.Type.ASSET);
                assertThat(imageItem.generic().value().value()).isEqualTo("pouet/img.jpg");
            });
    }
}
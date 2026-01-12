package fr.plop.contexts.game.config.template.domain.usecase.generator.tree;

import fr.plop.contexts.game.config.template.domain.model.Template;
import fr.plop.subs.image.Image;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class TemplateGeneratorTreeUseCaseImageTest {

    private final TemplateGeneratorTreeUseCase generator = new TemplateGeneratorTreeUseCase();

    @Test
    public void oneImage() {
        String scriptContent = """
                TEST_IMAGE1
                --- Image:ASSET:pouet/img.jpg
                """;

        TemplateGeneratorTreeUseCase.Script script = new TemplateGeneratorTreeUseCase.Script(scriptContent);
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
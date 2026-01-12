package fr.plop.contexts.game.config.template.domain.usecase.generator.json;

import com.fasterxml.jackson.core.JsonProcessingException;
import fr.plop.contexts.game.config.template.domain.model.Template;
import fr.plop.subs.image.Image;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class TemplateGeneratorJsonUseCaseImageTest {

    private final TemplateGeneratorJsonUseCase generator = new TemplateGeneratorJsonUseCase();

    @Test
    public void oneImage() throws JsonProcessingException {
        Template template = generator.apply("""
                {
                  "code": "TEST_IMAGE1",
                  "image": {
                    "items": [
                      {
                        "generic": {
                          "value": { "type": "Asset", "value": "pouet/img.jpg" },
                          "objects": []
                        }
                      }
                    ]
                  }
                }
                """);

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

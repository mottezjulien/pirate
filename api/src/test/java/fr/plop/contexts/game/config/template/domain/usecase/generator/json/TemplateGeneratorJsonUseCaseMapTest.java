package fr.plop.contexts.game.config.template.domain.usecase.generator.json;

import com.fasterxml.jackson.core.JsonProcessingException;
import fr.plop.contexts.game.config.Image.domain.ImageObject;
import fr.plop.contexts.game.config.template.domain.model.Template;
import fr.plop.generic.enumerate.Priority;
import fr.plop.subs.image.Image;
import org.junit.jupiter.api.Test;

import static fr.plop.subs.image.Image.Type;
import static org.assertj.core.api.Assertions.assertThat;

public class TemplateGeneratorJsonUseCaseMapTest {

    private final TemplateGeneratorJsonUseCase generator = new TemplateGeneratorJsonUseCase();

    @Test
    public void oneSimpleMap() throws JsonProcessingException {
        Template template = generator.apply("""
                {
                  "code": "TEST_MAP1",
                  "maps": [
                    {
                      "image": { "type": "Asset", "value": "game/tres/map/port.png" },
                      "objects": []
                    }
                  ]
                }
                """);

        assertThat(template).isNotNull();
        assertThat(template.code().value()).isEqualTo("TEST_MAP1");

        assertThat(template.map().items()).hasSize(1)
                .anySatisfy(mapItem -> {
                    assertThat(mapItem.id()).isNotNull();
                    assertThat(mapItem.imageGeneric().imageType()).isEqualTo(Type.ASSET);
                    assertThat(mapItem.imageGeneric().imageValue()).isEqualTo("game/tres/map/port.png");
                });
    }

    @Test
    public void oneCompletMap() throws JsonProcessingException {
        Template template = generator.apply("""
                {
                  "code": "TEST_MAP2",
                  "maps": [
                    {
                      "priority": "HIGH",
                      "image": { "type": "Web", "value": "toto.fr/plop.jpg" },
                      "objects": [
                        {
                          "position": { "top": 0.4711538461538462, "left": 0.51762815622183 },
                          "priority": "HIGHEST",
                          "point": { "color": "yellow" }
                        },
                        {
                          "position": { "top": 0.9158653112558219, "left": 0.2900640047513522 },
                          "priority": "MEDIUM",
                          "image": { "type": "Asset", "value": "pouet/img.jpg" }
                        }
                      ]
                    }
                  ]
                }
                """);

        assertThat(template).isNotNull();
        assertThat(template.code().value()).isEqualTo("TEST_MAP2");

        assertThat(template.map().items()).hasSize(1)
                .anySatisfy(mapItem -> {
                    assertThat(mapItem.id()).isNotNull();
                    assertThat(mapItem.imageGeneric().imageType()).isEqualTo(Image.Type.WEB);
                    assertThat(mapItem.imageGeneric().imageValue()).isEqualTo("toto.fr/plop.jpg");
                    assertThat(mapItem.priority()).isEqualTo(Priority.HIGH);
                    assertThat(mapItem.imageGeneric().objects())
                            .hasSize(2)
                            .anySatisfy(position -> {
                                assertThat(position.top()).isCloseTo(0.4711538461538462, org.assertj.core.data.Offset.offset(0.001));
                                assertThat(position.left()).isCloseTo(0.51762815622183, org.assertj.core.data.Offset.offset(0.001));
                                assertThat(position).isInstanceOf(ImageObject.Point.class);
                                assertThat(((ImageObject.Point) position).color()).isEqualTo("yellow");
                            })
                            .anySatisfy(position -> {
                                assertThat(position.top()).isCloseTo(0.9158653112558219, org.assertj.core.data.Offset.offset(0.001));
                                assertThat(position.left()).isCloseTo(0.2900640047513522, org.assertj.core.data.Offset.offset(0.001));
                                assertThat(position).isInstanceOf(ImageObject._Image.class);
                                assertThat(((ImageObject._Image) position).value().type()).isEqualTo(Type.ASSET);
                                assertThat(((ImageObject._Image) position).value().value()).isEqualTo("pouet/img.jpg");
                            });
                });
    }
}

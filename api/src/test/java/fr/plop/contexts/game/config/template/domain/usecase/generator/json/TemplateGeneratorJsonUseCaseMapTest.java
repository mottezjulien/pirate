package fr.plop.contexts.game.config.template.domain.usecase.generator.json;

import com.fasterxml.jackson.core.JsonProcessingException;
import fr.plop.contexts.game.config.map.domain.MapObject;
import fr.plop.contexts.game.config.template.domain.model.Template;
import fr.plop.generic.enumerate.Priority;
import org.junit.jupiter.api.Test;

import static fr.plop.subs.image.Image.Type;
import static org.assertj.core.api.Assertions.assertThat;

public class TemplateGeneratorJsonUseCaseMapTest {

    private final TemplateGeneratorJsonUseCase generator = new TemplateGeneratorJsonUseCase();

    @Test
    public void oneSimpleMap() throws JsonProcessingException {
        Template template = generator.template(TemplateGeneratorRootParser.apply("""
                {
                  "code": "TEST_MAP1",
                  "maps": [
                    {
                      "image": { "type": "Asset", "value": "game/tres/map/port.png" },
                      "bounds": {
                        "bottomLeft": { "lat": 45.757, "lng": 4.830 },
                        "topRight": { "lat": 45.758, "lng": 4.834 }
                      },
                      "objects": []
                    }
                  ]
                }
                """));

        assertThat(template).isNotNull();

        assertThat(template.map().items()).hasSize(1)
                .anySatisfy(mapItem -> {
                    assertThat(mapItem.id()).isNotNull();
                    assertThat(mapItem.image().type()).isEqualTo(Type.ASSET);
                    assertThat(mapItem.image().value()).isEqualTo("game/tres/map/port.png");
                    assertThat(mapItem.bounds()).isNotNull();
                    assertThat(mapItem.bounds().bottomLeft().lat().doubleValue()).isCloseTo(45.757, org.assertj.core.data.Offset.offset(0.001));
                });
    }

    @Test
    public void oneCompletMap() throws JsonProcessingException {
        Template template = generator.template(TemplateGeneratorRootParser.apply("""
                {
                  "code": "TEST_MAP2",
                  "maps": [
                    {
                      "priority": "HIGH",
                      "image": { "type": "Web", "value": "toto.fr/plop.jpg" },
                      "bounds": {
                        "bottomLeft": { "lat": 45.757, "lng": 4.830 },
                        "topRight": { "lat": 45.758, "lng": 4.834 }
                      },
                      "objects": [
                        {
                          "label": "Yellow marker",
                          "position": { "lat": 45.7575, "lng": 4.832 },
                          "priority": "HIGHEST",
                          "point": { "color": "yellow" }
                        },
                        {
                          "label": "Image marker",
                          "position": { "lat": 45.7577, "lng": 4.831 },
                          "priority": "MEDIUM",
                          "image": { "type": "Asset", "value": "pouet/img.jpg" }
                        }
                      ]
                    }
                  ]
                }
                """));

        assertThat(template).isNotNull();

        assertThat(template.map().items()).hasSize(1)
                .anySatisfy(mapItem -> {
                    assertThat(mapItem.id()).isNotNull();
                    assertThat(mapItem.image().type()).isEqualTo(fr.plop.subs.image.Image.Type.WEB);
                    assertThat(mapItem.image().value()).isEqualTo("toto.fr/plop.jpg");
                    assertThat(mapItem.priority()).isEqualTo(Priority.HIGH);
                    assertThat(mapItem.objects())
                            .hasSize(2)
                            .anySatisfy(obj -> {
                                assertThat(obj.position().lat().doubleValue()).isCloseTo(45.7575, org.assertj.core.data.Offset.offset(0.001));
                                assertThat(obj.position().lng().doubleValue()).isCloseTo(4.832, org.assertj.core.data.Offset.offset(0.001));
                                assertThat(obj).isInstanceOf(MapObject.Point.class);
                                assertThat(((MapObject.Point) obj).color()).isEqualTo("yellow");
                            })
                            .anySatisfy(obj -> {
                                assertThat(obj.position().lat().doubleValue()).isCloseTo(45.7577, org.assertj.core.data.Offset.offset(0.001));
                                assertThat(obj.position().lng().doubleValue()).isCloseTo(4.831, org.assertj.core.data.Offset.offset(0.001));
                                assertThat(obj).isInstanceOf(MapObject._Image.class);
                                assertThat(((MapObject._Image) obj).image().type()).isEqualTo(Type.ASSET);
                                assertThat(((MapObject._Image) obj).image().value()).isEqualTo("pouet/img.jpg");
                            });
                });
    }
}

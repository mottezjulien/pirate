package fr.plop.contexts.game.config.template.domain;

import fr.plop.contexts.game.config.map.domain.MapItem;
import fr.plop.contexts.game.config.template.domain.model.Template;
import fr.plop.contexts.game.config.template.domain.usecase.generator.TemplateGeneratorUseCase;
import fr.plop.generic.enumerate.Priority;
import fr.plop.subs.image.Image;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class TemplateGeneratorUseCaseMapTest {

    private final TemplateGeneratorUseCase generator = new TemplateGeneratorUseCase();

    @Test
    public void oneSimpleMap() {
        String scriptContent = """
                TEST_MAP1
                --- Map:Asset:game/tres/map/port.png
                """;

        TemplateGeneratorUseCase.Script script = new TemplateGeneratorUseCase.Script(scriptContent);
        Template template = generator.apply(script);

        assertThat(template).isNotNull();
        assertThat(template.code().value()).isEqualTo("TEST_MAP1");

        assertThat(template.map().items()).hasSize(1)
            .anySatisfy(mapItem -> {
                assertThat(mapItem.id()).isNotNull();
                assertThat(mapItem.image().type()).isEqualTo(Image.Type.ASSET);
                assertThat(mapItem.image().value()).isEqualTo("game/tres/map/port.png");
            });
    }

    @Test
    public void oneCompletMap() {
        String scriptContent = """
                TEST_MAP2
                --- Map:Web:toto.fr/plop.jpg
                ------ Priority:HIGH
                ------ Object:Point:0.4711538461538462:0.51762815622183
                --------- Label:Bateau rouge
                --------- Priority:HIGHEST
                --------- Color:yellow
                ------ object:Image:0.9158653112558219:0.2900640047513522
                --------- Label:Le bar
                --------- Priority:MEDIUM
                --------- Image:ASSET:pouet/img.jpg
                """;

        TemplateGeneratorUseCase.Script script = new TemplateGeneratorUseCase.Script(scriptContent);
        Template template = generator.apply(script);

        assertThat(template).isNotNull();
        assertThat(template.code().value()).isEqualTo("TEST_MAP2");

        assertThat(template.map().items()).hasSize(1)
                .anySatisfy(mapItem -> {
                    assertThat(mapItem.id()).isNotNull();
                    assertThat(mapItem.image().type()).isEqualTo(Image.Type.WEB);
                    assertThat(mapItem.image().value()).isEqualTo("toto.fr/plop.jpg");
                    assertThat(mapItem.priority()).isEqualTo(Priority.HIGH);
                    assertThat(mapItem.objects())
                            .hasSize(2)
                            .anySatisfy(position -> {
                                assertThat(position.label()).isEqualTo("Bateau rouge");
                                assertThat(position.priority()).isEqualTo(Priority.HIGHEST);
                                assertThat(position.top()).isEqualTo(0.4711538461538462);
                                assertThat(position.left()).isEqualTo(0.51762815622183);
                                assertThat(position).isInstanceOf(MapItem._Object.Point.class);
                                assertThat(((MapItem._Object.Point) position).color()).isEqualTo("yellow");
                            })
                            .anySatisfy(position -> {
                                assertThat(position.label()).isEqualTo("Le bar");
                                assertThat(position.priority()).isEqualTo(Priority.MEDIUM);
                                assertThat(position.top()).isEqualTo(0.9158653112558219);
                                assertThat(position.left()).isEqualTo(0.2900640047513522);
                                assertThat(position).isInstanceOf(MapItem._Object._Image.class);
                                assertThat(((MapItem._Object._Image) position).value().type()).isEqualTo(Image.Type.ASSET);
                                assertThat(((MapItem._Object._Image) position).value().value()).isEqualTo("pouet/img.jpg");
                            });
                });
    }

    @Test
    public void oneCompletMapWithPositions() {
        String scriptContent = """
                TEST_MAP2
                --- Map:Web:toto.fr/plop.jpg
                ------ Priority:HIGH
                ------ Object:Point:0.4711538461538462:0.51762815622183
                --------- Label:Bateau rouge
                --------- Priority:HIGHEST
                --------- Color:yellow
                ------ Object:Image:0.9158653112558219:0.2900640047513522
                --------- Label:Le bar
                --------- Priority:MEDIUM
                --------- Image:ASSET:pouet/img.jpg
                """;

        TemplateGeneratorUseCase.Script script = new TemplateGeneratorUseCase.Script(scriptContent);
        Template template = generator.apply(script);

        assertThat(template).isNotNull();
        assertThat(template.code().value()).isEqualTo("TEST_MAP2");

        assertThat(template.map().items()).hasSize(1)
                .anySatisfy(mapItem -> {
                    assertThat(mapItem.id()).isNotNull();
                    assertThat(mapItem.image().type()).isEqualTo(Image.Type.WEB);
                    assertThat(mapItem.image().value()).isEqualTo("toto.fr/plop.jpg");
                    assertThat(mapItem.priority()).isEqualTo(Priority.HIGH);
                    assertThat(mapItem.objects())
                            .hasSize(2)
                            .anySatisfy(position -> {
                                assertThat(position.label()).isEqualTo("Bateau rouge");
                                assertThat(position.priority()).isEqualTo(Priority.HIGHEST);
                                assertThat(position.top()).isEqualTo(0.4711538461538462);
                                assertThat(position.left()).isEqualTo(0.51762815622183);
                                assertThat(position).isInstanceOf(MapItem._Object.Point.class);
                                assertThat(((MapItem._Object.Point) position).color()).isEqualTo("yellow");
                            })
                            .anySatisfy(position -> {
                                assertThat(position.label()).isEqualTo("Le bar");
                                assertThat(position.priority()).isEqualTo(Priority.MEDIUM);
                                assertThat(position.top()).isEqualTo(0.9158653112558219);
                                assertThat(position.left()).isEqualTo(0.2900640047513522);
                                assertThat(position).isInstanceOf(MapItem._Object._Image.class);
                                assertThat(((MapItem._Object._Image) position).value().type()).isEqualTo(Image.Type.ASSET);
                                assertThat(((MapItem._Object._Image) position).value().value()).isEqualTo("pouet/img.jpg");
                            });
                });
    }


}
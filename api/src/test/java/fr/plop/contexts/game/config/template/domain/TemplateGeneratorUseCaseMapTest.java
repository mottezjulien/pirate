package fr.plop.contexts.game.config.template.domain;

import fr.plop.contexts.game.config.Image.domain.ImageObject;
import fr.plop.contexts.game.config.condition.Condition;
import fr.plop.contexts.game.config.template.domain.model.Template;
import fr.plop.contexts.game.config.template.domain.usecase.generator.TemplateGeneratorUseCase;
import fr.plop.contexts.game.session.time.GameSessionTimeUnit;
import fr.plop.generic.ImagePoint;
import fr.plop.generic.enumerate.BeforeOrAfter;
import fr.plop.generic.enumerate.Priority;
import fr.plop.subs.image.Image;
import org.junit.jupiter.api.Test;

import static fr.plop.subs.image.Image.Type;
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
                assertThat(mapItem.imageGeneric().imageType()).isEqualTo(Type.ASSET);
                assertThat(mapItem.imageGeneric().imageValue()).isEqualTo("game/tres/map/port.png");
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
                    assertThat(mapItem.imageGeneric().imageType()).isEqualTo(Image.Type.WEB);
                    assertThat(mapItem.imageGeneric().imageValue()).isEqualTo("toto.fr/plop.jpg");
                    assertThat(mapItem.priority()).isEqualTo(Priority.HIGH);
                    assertThat(mapItem.imageObjects())
                            .hasSize(2)
                            .anySatisfy(position -> {
                                assertThat(position.label()).isEqualTo("Bateau rouge");
                                assertThat(position.top()).isEqualTo(0.4711538461538462);
                                assertThat(position.left()).isEqualTo(0.51762815622183);
                                assertThat(position).isInstanceOf(ImageObject.Point.class);
                                assertThat(((ImageObject.Point) position).color()).isEqualTo("yellow");
                            })
                            .anySatisfy(position -> {
                                assertThat(position.label()).isEqualTo("Le bar");
                                assertThat(position.top()).isEqualTo(0.9158653112558219);
                                assertThat(position.left()).isEqualTo(0.2900640047513522);
                                assertThat(position).isInstanceOf(ImageObject._Image.class);
                                assertThat(((ImageObject._Image) position).value().type()).isEqualTo(Type.ASSET);
                                assertThat(((ImageObject._Image) position).value().value()).isEqualTo("pouet/img.jpg");
                            });
                });
    }

    @Test
    public void oneCompletMapWithPositionsAndConditionAndPointer() {
        String scriptContent = """
                TEST_MAP2
                --- Scenario
                ------ Step (ref STEP_AAA):FR:Étape 1:EN:Step 1
                
                --- Board
                ------ Space (ref SPACE_ABC)
                --------- bottomLeft:100:200:topRight:50:70
                ------ Space (ref SPACE_EFG)
                --------- bottomLeft:0.654987:52.369258147:topRight:0.3654789:63.456789
                
                --- Map:Web:toto.fr/plop.jpg
                ------ Condition:AbsoluteTime:10:after
                ------ Priority:HIGH
                ------ Pointer:ASSET:pouet/pointer.jpg
                ------ Object:Point:0.4711538461538462:0.51762815622183
                --------- Label:Bateau rouge
                --------- Color:yellow
                --------- CONDITION:InStep:STEP_AAA
                ------ Object:Image:0.9158653112558219:0.2900640047513522
                --------- Label:Le bar
                --------- Image:ASSET:pouet/img.jpg
                --------- Space:SPACE_EFG
                ------ Position:5.02:698547
                --------- Space:SPACE_EFG
                --------- PRIORITY:MEDIUM
                ------ Position
                --------- Space:SPACE_ABC
                --------- TOP:10
                --------- LEFT:20
                """;

        TemplateGeneratorUseCase.Script script = new TemplateGeneratorUseCase.Script(scriptContent);
        Template template = generator.apply(script);

        assertThat(template).isNotNull();
        assertThat(template.code().value()).isEqualTo("TEST_MAP2");

        assertThat(template.map().items()).hasSize(1)
                .anySatisfy(mapItem -> {
                    assertThat(mapItem.id()).isNotNull();
                    assertThat(mapItem.optCondition()).hasValueSatisfying(condition -> {
                        assertThat(condition).isInstanceOf(Condition.AbsoluteTime.class);
                        assertThat(((Condition.AbsoluteTime)condition).value()).isEqualTo(new GameSessionTimeUnit(10));
                        assertThat(((Condition.AbsoluteTime)condition).beforeOrAfter()).isEqualTo(BeforeOrAfter.AFTER);
                    });
                    assertThat(mapItem.imageGeneric().imageType()).isEqualTo(Image.Type.WEB);
                    assertThat(mapItem.imageGeneric().imageValue()).isEqualTo("toto.fr/plop.jpg");
                    assertThat(mapItem.priority()).isEqualTo(Priority.HIGH);
                    assertThat(mapItem.optPointer()).hasValueSatisfying(pointer -> {
                        assertThat(pointer.type()).isEqualTo(Type.ASSET);
                        assertThat(pointer.value()).isEqualTo("pouet/pointer.jpg");
                    });
                    assertThat(mapItem.imageObjects())
                            .hasSize(2)
                            .anySatisfy(object -> {
                                assertThat(object.label()).isEqualTo("Bateau rouge");
                                assertThat(object.top()).isEqualTo(0.4711538461538462);
                                assertThat(object.left()).isEqualTo(0.51762815622183);
                                assertThat(object).isInstanceOf(ImageObject.Point.class);
                                assertThat(((ImageObject.Point) object).color()).isEqualTo("yellow");
                                assertThat(object.optCondition()).hasValueSatisfying(condition -> {
                                    assertThat(condition).isInstanceOf(Condition.Step.class);
                                    assertThat(((Condition.Step)condition).stepId()).isEqualTo(template.scenario().steps().getFirst().id());
                                });
                            })
                            .anySatisfy(object -> {
                                assertThat(object.label()).isEqualTo("Le bar");
                                assertThat(object.top()).isEqualTo(0.9158653112558219);
                                assertThat(object.left()).isEqualTo(0.2900640047513522);
                                assertThat(object).isInstanceOf(ImageObject._Image.class);
                                assertThat(((ImageObject._Image) object).value().type()).isEqualTo(Type.ASSET);
                                assertThat(((ImageObject._Image) object).value().value()).isEqualTo("pouet/img.jpg");
                                assertThat(object.optCondition()).isEmpty();
                            });

                    assertThat(mapItem.positions())
                            .hasSize(2)
                            .anySatisfy(position -> {
                               assertThat(position.id()).isNotNull();
                               assertThat(position.spaceId()).isEqualTo(template.board().spaces().get(1).id());
                               assertThat(position.priority()).isEqualTo(Priority.MEDIUM);
                               assertThat(position.point()).isEqualTo(new ImagePoint(5.02,698547));
                            })
                            .anySatisfy(position -> {
                                assertThat(position.id()).isNotNull();
                                assertThat(position.spaceId()).isEqualTo(template.board().spaces().getFirst().id());
                                assertThat(position.priority()).isEqualTo(Priority.byDefault());
                                assertThat(position.point()).isEqualTo(new ImagePoint(10,20));
                            });

                });
    }


}
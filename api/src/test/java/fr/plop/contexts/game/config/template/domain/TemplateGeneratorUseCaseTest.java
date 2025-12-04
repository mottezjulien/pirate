package fr.plop.contexts.game.config.template.domain;

import fr.plop.contexts.game.config.Image.domain.ImageObject;
import fr.plop.contexts.game.config.board.domain.model.BoardSpace;
import fr.plop.contexts.game.config.condition.Condition;
import fr.plop.contexts.game.config.consequence.Consequence;
import fr.plop.contexts.game.config.scenario.domain.model.Possibility;
import fr.plop.contexts.game.config.scenario.domain.model.PossibilityRecurrence;
import fr.plop.contexts.game.config.scenario.domain.model.PossibilityTrigger;
import fr.plop.contexts.game.config.scenario.domain.model.ScenarioConfig;
import fr.plop.contexts.game.config.talk.domain.TalkItem;
import fr.plop.contexts.game.config.template.domain.model.Template;
import fr.plop.contexts.game.config.template.domain.usecase.generator.TemplateGeneratorUseCase;
import fr.plop.contexts.game.session.scenario.domain.model.ScenarioSessionState;
import fr.plop.contexts.game.session.time.GameSessionTimeUnit;
import fr.plop.generic.enumerate.Priority;
import fr.plop.generic.position.Point;
import fr.plop.subs.i18n.domain.I18n;
import fr.plop.subs.i18n.domain.Language;
import fr.plop.subs.image.Image;
import org.assertj.core.data.Offset;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class TemplateGeneratorUseCaseTest {

    private final TemplateGeneratorUseCase generator = new TemplateGeneratorUseCase();

    @Test
    public void emptyDefaultTemplate() {
        TemplateGeneratorUseCase.Script script = new TemplateGeneratorUseCase.Script("ABCD");
        Template template = generator.apply(script);
        assertThat(template).isNotNull();
        assertThat(template.id()).isNotNull();
        assertThat(template.code()).isEqualTo(new Template.Code("ABCD"));
        assertThat(template.version()).isEqualTo("0.0.0");
        assertThat(template.label()).isEqualTo("");
        assertThat(template.maxDuration()).isEqualTo(Duration.ofHours(1));
        assertThat(template.scenario()).isNotNull();
        assertThat(template.scenario().id()).isNotNull();
        assertThat(template.scenario().steps()).isEmpty();
        assertThat(template.board()).isNotNull();
        assertThat(template.board().id()).isNotNull();
        assertThat(template.board().spaces()).isEmpty();
        assertThat(template.map()).isNotNull();
        assertThat(template.map().id()).isNotNull();
        assertThat(template.map().items()).isEmpty();
    }

    @Test
    public void versionAndLabel() {
        TemplateGeneratorUseCase.Script script = new TemplateGeneratorUseCase.Script("XYZ:1.8.4:Mon nouveau niveau");
        Template template = generator.apply(script);
        assertThat(template).isNotNull();
        assertThat(template.id()).isNotNull();
        assertThat(template.code()).isEqualTo(new Template.Code("XYZ"));
        assertThat(template.version()).isEqualTo("1.8.4");
        assertThat(template.label()).isEqualTo("Mon nouveau niveau");
        assertThat(template.maxDuration()).isEqualTo(Duration.ofHours(1));
        assertThat(template.scenario()).isNotNull();
        assertThat(template.scenario().id()).isNotNull();
        assertThat(template.scenario().steps()).isEmpty();
        assertThat(template.board()).isNotNull();
        assertThat(template.board().id()).isNotNull();
        assertThat(template.board().spaces()).isEmpty();
        assertThat(template.map()).isNotNull();
        assertThat(template.map().id()).isNotNull();
        assertThat(template.map().items()).isEmpty();
    }


    @Test
    public void versionAndLabelAndDuration() {
        TemplateGeneratorUseCase.Script script = new TemplateGeneratorUseCase.Script("POUET:1.4:Mon nouveau niveau 2:180");
        Template template = generator.apply(script);
        assertThat(template).isNotNull();
        assertThat(template.id()).isNotNull();
        assertThat(template.code()).isEqualTo(new Template.Code("POUET"));
        assertThat(template.version()).isEqualTo("1.4");
        assertThat(template.label()).isEqualTo("Mon nouveau niveau 2");
        assertThat(template.maxDuration()).isEqualTo(Duration.ofMinutes(180));
        assertThat(template.scenario()).isNotNull();
        assertThat(template.scenario().id()).isNotNull();
        assertThat(template.scenario().steps()).isEmpty();
        assertThat(template.board()).isNotNull();
        assertThat(template.board().id()).isNotNull();
        assertThat(template.board().spaces()).isEmpty();
        assertThat(template.map()).isNotNull();
        assertThat(template.map().id()).isNotNull();
        assertThat(template.map().items()).isEmpty();
    }

    @Test
    public void oneStepWithTargets() {
        TemplateGeneratorUseCase.Script script = new TemplateGeneratorUseCase.Script("""
                Coucou:2::30
                --- Step:FR:Chez Moi 123:EN:At Home 456
                ------ Target:FR:Cuisine:EN:Kitchen
                ------ Target:EN:Office:FR:Bureau:OPTIONAL:TRUE
                ------ Target:FR:Chambre:EN:Room
                --------- OPTIONAL:TRUE
                --------- FR:Ceci est ma chambre.
                --------- C'est une belle chambre.
                --------- EN:This is my room.
                --------- It's a beautiful room.
                ------ Target:FR:Autre:EN:Other
                --------- EN:This is another space.
                --------- It's an other space.
                --------- It's an other space.
                --------- FR: Ceci est mon bureau.
                --------- Ceci est ma chambre.
                --------- Ceci est mon bureau.
                """);
        Template template = generator.apply(script);
        assertThat(template).isNotNull();
        assertThat(template.id()).isNotNull();
        assertThat(template.code()).isEqualTo(new Template.Code("Coucou"));
        assertThat(template.version()).isEqualTo("2");
        assertThat(template.label()).isEqualTo("");
        assertThat(template.maxDuration()).isEqualTo(Duration.ofMinutes(30));
        assertThat(template.scenario()).isNotNull();
        assertThat(template.scenario().id()).isNotNull();
        assertThat(template.scenario().steps()).hasSize(1).
                anySatisfy(step -> {
                    assertThat(step.id()).isNotNull();
                    assertThat(step.label())
                            .satisfies(withoutId(i18n("Chez Moi 123", "At Home 456")));
                    assertThat(step.targets())
                            .hasSize(4)
                            .anySatisfy(target -> {
                                assertThat(target.id()).isNotNull();
                                assertThat(target.label()).satisfies(withoutId(i18n("Cuisine", "Kitchen")));
                                assertThat(target.desc()).isEmpty();
                                assertThat(target.optional()).isFalse();
                            })
                            .anySatisfy(target -> {
                                assertThat(target.id()).isNotNull();
                                assertThat(target.label()).satisfies(withoutId(i18n("Bureau", "Office")));
                                assertThat(target.desc()).isEmpty();
                                assertThat(target.optional()).isTrue();
                            })
                            .anySatisfy(target -> {
                                assertThat(target.id()).isNotNull();
                                assertThat(target.label()).satisfies(withoutId(i18n("Chambre", "Room")));
                                assertThat(target.desc()).isPresent().hasValueSatisfying(withoutId(i18n("""
                                        Ceci est ma chambre.
                                        C'est une belle chambre.""", """
                                        This is my room.
                                        It's a beautiful room.""")));
                                assertThat(target.optional()).isTrue();
                            })
                            .anySatisfy(target -> {
                                assertThat(target.id()).isNotNull();
                                assertThat(target.label()).satisfies(withoutId(i18n("Autre", "Other")));
                                assertThat(target.desc()).isPresent().hasValueSatisfying(withoutId(i18n("""
                                        Ceci est mon bureau.
                                        Ceci est ma chambre.
                                        Ceci est mon bureau.""", """
                                        This is another space.
                                        It's an other space.
                                        It's an other space.""")));
                                assertThat(target.optional()).isFalse();
                            });
                });
    }

    @Test
    public void oneStepWithTargetAndPossibilities() {
        TemplateGeneratorUseCase.Script script = new TemplateGeneratorUseCase.Script("""
                With Possibility:3:Hello:45
                --- Board
                ------ Space (REF ABCD):Office:HIGH
                ------ Space (REF XYZ):Kitchen:LOW
                --- Step (REF STEP_0):FR:Pouet:EN:Pouet
                --- Step (REF STEP_1):FR:Chez Moi:EN:At Home
                ------ Target (REF TARGET_A):FR:Cuisine:EN:Kitchen
                ------ Target:EN:Office:FR:Bureau:Optional:true
                ------ Possibility:ALWAYS
                --------- Condition:outsidespace:SpaceId:ABCD
                --------- consequence:Alert
                ------------ EN:Alarm !!!
                ------------ FR: C'est la vie
                ------------ Cuicui
                --------- Trigger:GoInSpace:SpaceId:ABCD
                --------- consequence:GoalTarget:stepId:STEP_1:targetId:TARGET_A:state:FAILURE
                --------- condition:ABSOLUTETIME:Duration:27
                ------ Possibility:times:4:OR
                --------- Trigger:ABSOLUTETIME:42
                --------- CONDITION:InStep:STEP_0
                --------- CONDITION:outSidespace:XYZ
                --------- consequence:Goal:state:SUCCESS:stepId:STEP_1
                --------- consequence:GoalTarget:targetId:TARGET_A:state:active:stepId:STEP_1
                
                """);
        Template template = generator.apply(script);
        assertThat(template).isNotNull();
        assertThat(template.id()).isNotNull();
        assertThat(template.code()).isEqualTo(new Template.Code("With Possibility"));
        assertThat(template.version()).isEqualTo("3");
        assertThat(template.label()).isEqualTo("Hello");
        assertThat(template.maxDuration()).isEqualTo(Duration.ofMinutes(45));
        assertThat(template.scenario()).isNotNull();
        assertThat(template.scenario().id()).isNotNull();
        assertThat(template.scenario().steps()).hasSize(2).
                anySatisfy(step -> {
                    assertThat(step.id()).isNotNull();
                    assertThat(step.label()).satisfies(withoutId(i18n("Chez Moi", "At Home")));
                    assertThat(step.targets())
                            .hasSize(2)
                            .anySatisfy(target -> {
                                assertThat(target.id()).isNotNull();
                                assertThat(target.label()).satisfies(withoutId(i18n("Cuisine", "Kitchen")));
                                assertThat(target.desc()).isEmpty();
                                assertThat(target.optional()).isFalse();
                            })
                            .anySatisfy(target -> {
                                assertThat(target.id()).isNotNull();
                                assertThat(target.label()).satisfies(withoutId(i18n("Bureau", "Office")));
                                assertThat(target.desc()).isEmpty();
                                assertThat(target.optional()).isTrue();
                            });
                    assertThat(step.possibilities())
                            .hasSize(2)
                            .anySatisfy(possibility -> {
                                assertThat(possibility.id()).isNotNull();
                                assertThat(possibility.recurrence()).isInstanceOf(PossibilityRecurrence.Always.class);
                                assertThat(possibility.trigger()).isInstanceOf(PossibilityTrigger.SpaceGoIn.class);
                                PossibilityTrigger.SpaceGoIn trigger = (PossibilityTrigger.SpaceGoIn) possibility.trigger();
                                assertThat(trigger.spaceId()).isEqualTo(template.board().spaces().getFirst().id());
                                assertThat(possibility.optCondition())
                                        .hasValueSatisfying(condition -> {
                                            assertThat(condition).isInstanceOf(Condition.And.class);
                                            assertThat(((Condition.And) condition).conditions()).hasSize(2)
                                                    .anySatisfy(subCondition -> {
                                                        assertThat(subCondition).isInstanceOf(Condition.OutsideSpace.class);
                                                        assertThat(((Condition.OutsideSpace) subCondition).spaceId()).isEqualTo(template.board().spaces().getFirst().id());
                                                    })
                                                    .anySatisfy(subCondition -> {
                                                        assertThat(subCondition).isInstanceOf(Condition.AbsoluteTime.class);
                                                        assertThat(((Condition.AbsoluteTime) subCondition).value()).isEqualTo(GameSessionTimeUnit.ofMinutes(27));
                                                    });
                                        });

                                assertThat(possibility.consequences())
                                        .hasSize(2)
                                        .anySatisfy(consequence -> {
                                            assertThat(consequence).isInstanceOf(Consequence.DisplayMessage.class);
                                            assertThat(((Consequence.DisplayMessage) consequence).value()).satisfies(withoutId(i18n("""
                                                    C'est la vie
                                                    Cuicui""", "Alarm !!!")));
                                        })
                                        .anySatisfy(consequence -> {
                                            assertThat(consequence).isInstanceOf(Consequence.ScenarioTarget.class);
                                            Consequence.ScenarioTarget goalTarget = (Consequence.ScenarioTarget) consequence;
                                            assertThat(goalTarget.stepId()).isEqualTo(template.scenario().steps().get(1).id());
                                            assertThat(goalTarget.targetId()).isEqualTo(template.scenario().steps().get(1).targets().getFirst().id());
                                            assertThat(goalTarget.state()).isEqualTo(ScenarioSessionState.FAILURE);
                                        });

                            })
                            .anySatisfy(possibility -> {
                                assertThat(possibility.id()).isNotNull();
                                assertThat(possibility.recurrence()).isInstanceOf(PossibilityRecurrence.Times.class);
                                assertThat(((PossibilityRecurrence.Times) possibility.recurrence()).value()).isEqualTo(4);
                                assertThat(possibility.trigger()).isInstanceOf(PossibilityTrigger.AbsoluteTime.class);
                                PossibilityTrigger.AbsoluteTime trigger = (PossibilityTrigger.AbsoluteTime) possibility.trigger();
                                assertThat(trigger.value()).isEqualTo(GameSessionTimeUnit.ofMinutes(42));
                                assertThat(possibility.optCondition())
                                        .hasValueSatisfying(condition -> {
                                            assertThat(condition).isInstanceOf(Condition.And.class);
                                            assertThat(((Condition.And) condition).conditions()).hasSize(2)
                                                    .anySatisfy(subCondition -> {
                                                        assertThat(subCondition).isInstanceOf(Condition.Step.class);
                                                        assertThat(((Condition.Step) subCondition).stepId()).isEqualTo(template.scenario().steps().getFirst().id());
                                                    })
                                                    .anySatisfy(subCondition -> {
                                                        assertThat(subCondition).isInstanceOf(Condition.OutsideSpace.class);
                                                        assertThat(((Condition.OutsideSpace) subCondition).spaceId()).isEqualTo(template.board().spaces().get(1).id());
                                                    });
                                        });

                               assertThat(possibility.consequences())
                                        .hasSize(2)
                                        .anySatisfy(consequence -> {
                                            assertThat(consequence).isInstanceOf(Consequence.ScenarioStep.class);
                                            Consequence.ScenarioStep goal = (Consequence.ScenarioStep) consequence;
                                            assertThat(goal.stepId()).isEqualTo(template.scenario().steps().get(1).id());
                                            assertThat(goal.state()).isEqualTo(ScenarioSessionState.SUCCESS);
                                        })
                                        .anySatisfy(consequence -> {
                                            assertThat(consequence).isInstanceOf(Consequence.ScenarioTarget.class);
                                            Consequence.ScenarioTarget goalTarget = (Consequence.ScenarioTarget) consequence;
                                            assertThat(goalTarget.stepId()).isEqualTo(template.scenario().steps().get(1).id());
                                            assertThat(goalTarget.targetId()).isEqualTo(template.scenario().steps().get(1).targets().getFirst().id());
                                            assertThat(goalTarget.state()).isEqualTo(ScenarioSessionState.ACTIVE);
                                        });

                            });
                });
    }

    @Test
    public void stepNoLabel() {
        TemplateGeneratorUseCase.Script script = new TemplateGeneratorUseCase.Script("""
                Two StepNoLabel:4:Hello:50
                --- Step
                """);
        assertThatThrownBy(() -> generator.apply(script))
                .isInstanceOf(NoSuchElementException.class); //TODO
    }


    @Test
    public void twoSteps() {
        TemplateGeneratorUseCase.Script script = new TemplateGeneratorUseCase.Script("""
                Two Step:4:Hello:50
                
                --- Board
                ------ Space (REF monespace):Mon premier espace:LOW
                
                --- Step:FR:Premier:EN:First
                
                ------ Target:FR:Mon objectif:EN:My target
                ------ Possibility
                --------- Trigger:goinSPACE:monespace
                --- Step:FR:Deuxième:EN:Second
                ------ Possibility
                
                --------- RECURRENCE:tiMes:5
                
                --------- Trigger:AbsoluteTime:51
                
                """);
        Template template = generator.apply(script);
        assertThat(template).isNotNull();
        assertThat(template.id()).isNotNull();
        assertThat(template.code()).isEqualTo(new Template.Code("Two Step"));
        assertThat(template.version()).isEqualTo("4");
        assertThat(template.label()).isEqualTo("Hello");
        assertThat(template.maxDuration()).isEqualTo(Duration.ofMinutes(50));
        assertThat(template.scenario()).isNotNull();
        assertThat(template.scenario().id()).isNotNull();
        assertThat(template.scenario().steps()).hasSize(2).
                anySatisfy(step -> {
                    assertThat(step.id()).isNotNull();
                    assertThat(step.label()).satisfies(withoutId(i18n("Premier", "First")));
                    assertThat(step.targets())
                            .hasSize(1)
                            .anySatisfy(target -> {
                                assertThat(target.id()).isNotNull();
                                assertThat(target.label()).satisfies(withoutId(i18n("Mon objectif", "My target")));
                                assertThat(target.desc()).isEmpty();
                                assertThat(target.optional()).isFalse();
                            });
                    assertThat(step.possibilities())
                            .hasSize(1)
                            .anySatisfy(possibility -> {
                                assertThat(possibility.id()).isNotNull();
                                assertThat(possibility.recurrence()).isInstanceOf(PossibilityRecurrence.Always.class);
                                assertThat(possibility.trigger()).isInstanceOf(PossibilityTrigger.SpaceGoIn.class);
                                PossibilityTrigger.SpaceGoIn trigger = (PossibilityTrigger.SpaceGoIn) possibility.trigger();
                                assertThat(trigger.spaceId()).isEqualTo(template.board().spaces().getFirst().id());
                                assertThat(possibility.optCondition()).isEmpty();
                                assertThat(possibility.consequences()).isEmpty();
                            });
                })
                .anySatisfy(step -> {
                    assertThat(step.id()).isNotNull();
                    assertThat(step.label()).satisfies(withoutId(i18n("Deuxième", "Second")));
                    assertThat(step.targets()).isEmpty();
                    assertThat(step.possibilities())
                            .hasSize(1)
                            .anySatisfy(possibility -> {
                                assertThat(possibility.id()).isNotNull();
                                assertThat(possibility.recurrence()).isInstanceOf(PossibilityRecurrence.Times.class);
                                assertThat(((PossibilityRecurrence.Times) possibility.recurrence()).value()).isEqualTo(5);
                                assertThat(possibility.trigger()).isInstanceOf(PossibilityTrigger.AbsoluteTime.class);
                                PossibilityTrigger.AbsoluteTime trigger = (PossibilityTrigger.AbsoluteTime) possibility.trigger();
                                assertThat(trigger.value()).isEqualTo(GameSessionTimeUnit.ofMinutes(51));
                                assertThat(possibility.optCondition()).isEmpty();
                                assertThat(possibility.consequences()).isEmpty();
                            });
                });
    }


    @Test
    public void firstBoard() {
        TemplateGeneratorUseCase.Script script = new TemplateGeneratorUseCase.Script("""
                firstBoard:5.1:Mon premier plateau
                --- Board
                ------ Space:Mon premier espace:LOW
                --------- bottomLeft:5.7:10:topRight:8.097:50.43
                --------- topRight:8.56:9:bottomLeft:10:20
                
                """);
        Template template = generator.apply(script);
        assertThat(template).isNotNull();
        assertThat(template.id()).isNotNull();
        assertThat(template.code()).isEqualTo(new Template.Code("firstBoard"));
        assertThat(template.version()).isEqualTo("5.1");
        assertThat(template.label()).isEqualTo("Mon premier plateau");
        assertThat(template.maxDuration()).isEqualTo(Duration.ofHours(1));
        assertThat(template.board().id()).isNotNull();
        assertThat(template.board().spaces())
                .hasSize(1)
                .anySatisfy(board -> {
                    assertThat(board.id()).isNotNull();
                    assertThat(board.label()).isEqualTo("Mon premier espace");
                    assertThat(board.priority()).isEqualTo(Priority.LOW);
                    assertThat(board.rects())
                            .hasSize(2)
                            .anySatisfy(rect -> {
                                assertThat(rect.bottomLeft()).isEqualTo(new Point(5.7F, 10));
                                assertThat(rect.topRight()).isEqualTo(new Point(8.097F, 50.43F));
                            })
                            .anySatisfy(rect -> {
                                assertThat(rect.bottomLeft()).isEqualTo(new Point(10, 20));
                                assertThat(rect.topRight()).isEqualTo(new Point(8.56F, 9));
                            });
                });
    }


    @Test
    public void secondBoard() {
        TemplateGeneratorUseCase.Script script = new TemplateGeneratorUseCase.Script("""
                secondBoard:5.2:Mon deuxième plateau
                --- Board
                ------ Space:Mon deuxième espace:MEDIUM
                --------- bottomLeft:5.798:10.894:topRight:45.98:50.4338
                --------- 1:2:4:3
                ------ Space:Mon 3eme espace:LOWEST
                --------- 89.745:5.5684:0.8547:8.147
                """);
        Template template = generator.apply(script);
        assertThat(template).isNotNull();
        assertThat(template.id()).isNotNull();
        assertThat(template.code()).isEqualTo(new Template.Code("secondBoard"));
        assertThat(template.version()).isEqualTo("5.2");
        assertThat(template.label()).isEqualTo("Mon deuxième plateau");
        assertThat(template.maxDuration()).isEqualTo(Duration.ofHours(1));
        assertThat(template.board().id()).isNotNull();
        assertThat(template.board().spaces())
                .hasSize(2)
                .anySatisfy(board -> {
                    assertThat(board.id()).isNotNull();
                    assertThat(board.label()).isEqualTo("Mon deuxième espace");
                    assertThat(board.priority()).isEqualTo(Priority.MEDIUM);
                    assertThat(board.rects())
                            .hasSize(2)
                            .anySatisfy(rect -> {
                                assertThat(rect.bottomLeft()).isEqualTo(new Point(5.798F, 10.894F));
                                assertThat(rect.topRight()).isEqualTo(new Point(45.98F, 50.4338F));
                            })
                            .anySatisfy(rect -> {
                                assertThat(rect.bottomLeft()).isEqualTo(new Point(1, 2));
                                assertThat(rect.topRight()).isEqualTo(new Point(4, 3));
                            });
                })
                .anySatisfy(board -> {
                    assertThat(board.id()).isNotNull();
                    assertThat(board.label()).isEqualTo("Mon 3eme espace");
                    assertThat(board.priority()).isEqualTo(Priority.LOWEST);
                    assertThat(board.rects())
                            .hasSize(1)
                            .anySatisfy(rect -> {
                                assertThat(rect.bottomLeft()).isEqualTo(new Point(89.745F, 5.5684F));
                                assertThat(rect.topRight()).isEqualTo(new Point(0.8547F, 8.147F));
                            });
                });
    }


    @Test
    public void firstMap() {
        TemplateGeneratorUseCase.Script script = new TemplateGeneratorUseCase.Script("""
                firstMap:2.0.0:Ma première carte
                
                --- Map:Asset:imgs/first/map.png
                ------ Priority:HIGH
                
                ------ object:point:89.09:10.064
                --------- Priority:HIGHest
                
                ------ ObJect:PoInT:78.865:23.9887
                --------- Priority:LOW
                --------- condition:absolutetime:41
                --------- color:yellow
                """);
        Template template = generator.apply(script);
        assertThat(template).isNotNull();
        assertThat(template.id()).isNotNull();
        assertThat(template.code()).isEqualTo(new Template.Code("firstMap"));
        assertThat(template.version()).isEqualTo("2.0.0");
        assertThat(template.label()).isEqualTo("Ma première carte");
        assertThat(template.maxDuration()).isEqualTo(Duration.ofHours(1));
        assertThat(template.map().id()).isNotNull();
        assertThat(template.map().items())
                .hasSize(1)
                .anySatisfy(item -> {
                    assertThat(item.id()).isNotNull();
                    assertThat(item.imageType()).isEqualTo(Image.Type.ASSET);
                    assertThat(item.imageValue()).isEqualTo("imgs/first/map.png");
                    assertThat(item.priority()).isEqualTo(Priority.HIGH);
                    assertThat(item.imageObjects())
                            .hasSize(2)
                            .anySatisfy(object -> {
                                assertThat(object.id()).isNotNull();

                                assertThat(object).isInstanceOf(ImageObject.Point.class);
                                ImageObject.Point point = (ImageObject.Point) object;
                                assertThat(point.top()).isCloseTo(89.09F, Offset.offset(0.001));
                                assertThat(point.left()).isCloseTo(10.064F, Offset.offset(0.001));
                                assertThat(point.color()).isEqualTo("");
                                assertThat(point.atom().optCondition()).isEmpty();
                            })
                            .anySatisfy(position -> {
                                assertThat(position.id()).isNotNull();

                                assertThat(position).isInstanceOf(ImageObject.Point.class);
                                ImageObject.Point point = (ImageObject.Point) position;
                                assertThat(point.top()).isCloseTo(78.865F, Offset.offset(0.001));
                                assertThat(point.left()).isCloseTo(23.9887F, Offset.offset(0.001));
                                assertThat(point.color()).isEqualTo("yellow");
                                assertThat(point.atom().optCondition()).hasValueSatisfying(condition -> {
                                    assertThat(condition).isInstanceOf(Condition.AbsoluteTime.class);
                                    assertThat(((Condition.AbsoluteTime) condition).value()).isEqualTo(new GameSessionTimeUnit(41));
                                });
                            });
                });
    }

    @Test
    public void prepareFirstMap_ManageLinkBoardScenario() {
        TemplateGeneratorUseCase.Script script = new TemplateGeneratorUseCase.Script("""
                linkBoardScenario:0.0.0:Link Board Scenario
                --- Board
                ------ Space (Ref La lune):La lune:HIGH
                --------- 1:2:3:4
                ------ Space (Ref Mars):Mars:HIGH
                --------- 5:6:7:8
                --- Step:FR:Aucun:EN:None
                ------ Possibility
                --------- Trigger:GoInSpace:SpaceId:La lune
                ------ Possibility
                --------- Trigger:GoOutSpace:SpaceId:Mars
                """);
        Template template = generator.apply(script);
        assertThat(template.id()).isNotNull();
        assertThat(template.code()).isEqualTo(new Template.Code("linkBoardScenario"));
        assertThat(template.version()).isEqualTo("0.0.0");
        assertThat(template.label()).isEqualTo("Link Board Scenario");

        List<BoardSpace> spaces = template.board().spaces();
        assertThat(spaces.getFirst().id()).isNotNull();
        assertThat(spaces.getFirst().label()).isEqualTo("La lune");
        assertThat(spaces.get(1).id()).isNotNull();
        assertThat(spaces.get(1).label()).isEqualTo("Mars");

        List<Possibility> possibilities = template.scenario().steps().getFirst().possibilities();
        assertThat(possibilities.getFirst().trigger())
                .isInstanceOf(PossibilityTrigger.SpaceGoIn.class);
        PossibilityTrigger.SpaceGoIn goInSpace = (PossibilityTrigger.SpaceGoIn) possibilities.getFirst().trigger();
        assertThat(goInSpace.spaceId()).isEqualTo(spaces.getFirst().id());
        assertThat(possibilities.get(1).trigger())
                .isInstanceOf(PossibilityTrigger.SpaceGoOut.class);
        PossibilityTrigger.SpaceGoOut goOutSpace = (PossibilityTrigger.SpaceGoOut) possibilities.get(1).trigger();
        assertThat(goOutSpace.spaceId()).isEqualTo(spaces.get(1).id());

    }


    @Test
    public void prepareFirstMap_TalkOptions() {
        TemplateGeneratorUseCase.Script script = new TemplateGeneratorUseCase.Script("""
                linkBoardScenario:0.0.0:Link Board Scenario
                --- Board
                ------ Space (ref La lune):La lune:HIGH
                --------- 1:2:3:4
                --- Step:FR:Aucun:EN:None
                ------ Possibility
                --------- Trigger:GoInSpace:SpaceId:La lune
                --------- consequence:Talk:OPTIONS_ABCD
                --- Talk
                ------ Character
                --------- Bob
                ------------ Happy:ASSET:bob-happy.jpg
                ------ Options(ref OPTIONS_ABCD)
                --------- Character:Bob:Happy
                --------- Option
                ------------ FR: Le choix A
                ------------ EN: Le choix A en anglais ?
                --------- Label
                ------------ FR: C'est quoi ton choix ?
                ------------ EN: C'est quoi ton choix en anglais ?
                --------- Option
                ------------ EN: Le choix B en anglais
                ------------ FR: Le choix B
                
                """);
        Template template = generator.apply(script);
        assertThat(template.id()).isNotNull();
        assertThat(template.code()).isEqualTo(new Template.Code("linkBoardScenario"));
        assertThat(template.version()).isEqualTo("0.0.0");
        assertThat(template.label()).isEqualTo("Link Board Scenario");

        List<BoardSpace> spaces = template.board().spaces();
        assertThat(spaces.getFirst().id()).isNotNull();
        assertThat(spaces.getFirst().label()).isEqualTo("La lune");

        List<Possibility> possibilities = template.scenario().steps().getFirst().possibilities();
        assertThat(possibilities.getFirst().trigger())
                .isInstanceOf(PossibilityTrigger.SpaceGoIn.class);
        PossibilityTrigger.SpaceGoIn goInSpace = (PossibilityTrigger.SpaceGoIn) possibilities.getFirst().trigger();
        assertThat(goInSpace.spaceId()).isEqualTo(spaces.getFirst().id());

        assertThat(possibilities.getFirst().consequences().getFirst()).isInstanceOf(Consequence.DisplayTalk.class);
        Consequence.DisplayTalk displayTalk = (Consequence.DisplayTalk) possibilities.getFirst().consequences().getFirst();
        TalkItem.Id talkId = displayTalk.talkId();
        assertThat(talkId).isNotNull();

        assertThat(template.talk().items())
                .hasSize(1)
                .anySatisfy(talk -> {
                    assertThat(talk).isInstanceOf(TalkItem.Options.class);
                    TalkItem.Options talkOptions = (TalkItem.Options) talk;
                    assertThat(talkOptions.id()).isEqualTo(talkId);
                    assertThat(talkOptions.value().value(Language.FR)).isEqualTo("C'est quoi ton choix ?");
                    assertThat(talkOptions.value().value(Language.EN)).isEqualTo("C'est quoi ton choix en anglais ?");
                    assertThat(talkOptions.options()).hasSize(2)
                            .anySatisfy(option -> {
                                assertThat(option.id()).isNotNull();
                                assertThat(option.value().value(Language.FR)).isEqualTo("Le choix A");
                                assertThat(option.value().value(Language.EN)).isEqualTo("Le choix A en anglais ?");
                            })
                            .anySatisfy(option -> {
                                assertThat(option.id()).isNotNull();
                                assertThat(option.value().value(Language.FR)).isEqualTo("Le choix B");
                                assertThat(option.value().value(Language.EN)).isEqualTo("Le choix B en anglais");
                            });
                });
    }

    @Test
    public void prepareFirstMap_add_refs_withoutStepId() {

        TemplateGeneratorUseCase.Script script = new TemplateGeneratorUseCase.Script("""
                addRef:0.0.0:Ajout de la notion de Reférénce - optimisation step id
                --- Step:FR:Aucun:EN:None
                ------ Target:FR:Réparer la station:EN:Réparer la station en anglais
                --- Step:FR:Aucun:EN:None
                ------ Target (ref TARGET_ATTERRIR):FR:Atterrir sur la lune:EN:Atterrir sur la lune en anglais
                ------ Possibility
                --------- Trigger:ABSOLUTETIME:51
                --------- Consequence:GoalTarget:targetId:TARGET_ATTERRIR:state:active
                """);
        Template template = generator.apply(script);

        List<ScenarioConfig.Step> steps = template.scenario().steps();
        assertThat(steps.size()).isEqualTo(2);

        ScenarioConfig.Step stepFirst = steps.getFirst();
        assertThat(stepFirst.targets().getFirst().id()).isNotNull();
        assertThat(stepFirst.targets().getFirst().label().value(Language.FR)).isEqualTo("Réparer la station");
        assertThat(stepFirst.targets().getFirst().label().value(Language.EN)).isEqualTo("Réparer la station en anglais");

        ScenarioConfig.Step stepSecond = steps.get(1);
        assertThat(stepSecond.targets().getFirst().id()).isNotNull();
        assertThat(stepSecond.targets().getFirst().label().value(Language.FR)).isEqualTo("Atterrir sur la lune");
        assertThat(stepSecond.targets().getFirst().label().value(Language.EN)).isEqualTo("Atterrir sur la lune en anglais");


        Possibility possibility = stepSecond.possibilities().getFirst();

        assertThat(possibility.trigger())
                .isInstanceOf(PossibilityTrigger.AbsoluteTime.class);
        PossibilityTrigger.AbsoluteTime goInSpace = (PossibilityTrigger.AbsoluteTime) possibility.trigger();
        assertThat(goInSpace.id()).isNotNull();
        assertThat(goInSpace.value()).isEqualTo(new GameSessionTimeUnit(51));

        assertThat(possibility.consequences().getFirst()).isInstanceOf(Consequence.ScenarioTarget.class);
        Consequence.ScenarioTarget scenarioTarget = (Consequence.ScenarioTarget) possibility.consequences().getFirst();
        assertThat(scenarioTarget.id()).isNotNull();
        assertThat(scenarioTarget.stepId()).isEqualTo(stepSecond.id());
        assertThat(scenarioTarget.targetId()).isEqualTo(stepSecond.targets().getFirst().id());
        assertThat(scenarioTarget.state()).isEqualTo(ScenarioSessionState.ACTIVE);
    }

    private Consumer<I18n> withoutId(I18n compareTo) {
        return value -> {
            assertThat(value.description()).isEqualTo(compareTo.description());
            assertThat(value.values()).isEqualTo(compareTo.values());
        };
    }

    private static I18n i18n(String fr, String en) {
        return new I18n(Map.of(Language.FR, fr, Language.EN, en));
    }

}
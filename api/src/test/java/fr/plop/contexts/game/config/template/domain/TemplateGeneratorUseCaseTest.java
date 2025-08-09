package fr.plop.contexts.game.config.template.domain;

import fr.plop.contexts.game.config.board.domain.model.BoardSpace;
import fr.plop.contexts.game.config.scenario.domain.model.PossibilityCondition;
import fr.plop.contexts.game.config.scenario.domain.model.PossibilityConsequence;
import fr.plop.contexts.game.config.scenario.domain.model.PossibilityRecurrence;
import fr.plop.contexts.game.config.scenario.domain.model.PossibilityTrigger;
import fr.plop.contexts.game.config.scenario.domain.model.ScenarioConfig;
import fr.plop.contexts.game.config.template.domain.model.Template;
import fr.plop.contexts.game.session.scenario.domain.model.ScenarioGoal;
import fr.plop.contexts.game.session.time.TimeUnit;
import fr.plop.contexts.i18n.domain.I18n;
import fr.plop.contexts.i18n.domain.Language;
import fr.plop.generic.enumerate.AndOrOr;
import fr.plop.generic.position.Point;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Map;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;

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
                ------ Target Description optionnelle (Opt):EN:Office:FR:Bureau
                ------ Target (Opt):FR:Chambre:EN:Room
                --------- FR:Ceci est ma chambre.
                --------- C'est une belle chambre.
                --------- EN:This is my room.
                --------- It's a beautiful room.
                ------ Target
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
                            .isPresent()
                            .hasValueSatisfying(withoutId(i18n("Chez Moi 123", "At Home 456")));
                    assertThat(step.targets())
                            .hasSize(4)
                            .anySatisfy(target -> {
                                assertThat(target.id()).isNotNull();
                                assertThat(target.label()).isPresent().hasValueSatisfying(withoutId(i18n("Cuisine", "Kitchen")));
                                assertThat(target.desc()).isEmpty();
                                assertThat(target.optional()).isFalse();
                            })
                            .anySatisfy(target -> {
                                assertThat(target.id()).isNotNull();
                                assertThat(target.label()).isPresent().hasValueSatisfying(withoutId(i18n("Bureau", "Office")));
                                assertThat(target.desc()).isEmpty();
                                assertThat(target.optional()).isTrue();
                            })
                            .anySatisfy(target -> {
                                assertThat(target.id()).isNotNull();
                                assertThat(target.label()).isPresent().hasValueSatisfying(withoutId(i18n("Chambre", "Room")));
                                assertThat(target.desc()).isPresent().hasValueSatisfying(withoutId(i18n("""
                                        Ceci est ma chambre.
                                        C'est une belle chambre.
                                        """, """
                                        This is my room.
                                        It's a beautiful room.
                                        """)));
                                assertThat(target.optional()).isTrue(); // Corrigé: (Opt) dans le script
                            })
                            .anySatisfy(target -> {
                                assertThat(target.id()).isNotNull();
                                assertThat(target.label()).isEmpty();
                                assertThat(target.desc()).isPresent().hasValueSatisfying(withoutId(i18n("""
                                        Ceci est mon bureau.
                                        Ceci est ma chambre.
                                        Ceci est mon bureau.
                                        """, """
                                        This is another space.
                                        It's an other space.
                                        It's an other space.
                                        """)));
                                assertThat(target.optional()).isFalse();
                            });
                });
    }

    @Test
    public void oneStepWithTargetAndPossibilities() {
        TemplateGeneratorUseCase.Script script = new TemplateGeneratorUseCase.Script("""
                With Possibility:3:Hello:45
                --- Step:FR:Chez Moi:EN:At Home
                ------ Target:FR:Cuisine:EN:Kitchen
                ------ Target(Opt):EN:Office:FR:Bureau
                ------ Possibility:ALWAYS
                --------- Condition:outsidespace:SpaceId:ABCD
                --------- consequence:Alert
                ------------ EN:Alarm !!!
                ------------ FR: C'est la vie
                ------------ Cuicui
                --------- Trigger:GoInSpace:SpaceId:ABCD
                --------- consequence:GoalTarget:stepId:EFG:targetId:9876:state:FAILURE
                --------- condition:ABSOLUTETIME:Duration:27
                ------ Possibility:times:4:OR
                --------- Trigger:ABSOLUTETIME:42
                --------- CONDITION:InStep:0987
                --------- CONDITION:outSidespace:9823
                --------- consequence:Goal:state:SUCCESS:stepId:KLM
                --------- consequence:GoalTarget:targetId:9876:state:active:stepId:EFG123
                
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
        assertThat(template.scenario().steps()).hasSize(1).
                anySatisfy(step -> {
                    assertThat(step.id()).isNotNull();
                    assertThat(step.label()).isPresent().hasValueSatisfying(withoutId(i18n("Chez Moi", "At Home")));
                    assertThat(step.targets())
                            .hasSize(2)
                            .anySatisfy(target -> {
                                assertThat(target.id()).isNotNull();
                                assertThat(target.label()).isPresent().hasValueSatisfying(withoutId(i18n("Cuisine", "Kitchen")));
                                assertThat(target.desc()).isEmpty();
                                assertThat(target.optional()).isFalse();
                            })
                            .anySatisfy(target -> {
                                assertThat(target.id()).isNotNull();
                                assertThat(target.label()).isPresent().hasValueSatisfying(withoutId(i18n("Bureau", "Office")));
                                assertThat(target.desc()).isEmpty();
                                assertThat(target.optional()).isTrue();
                            });
                    assertThat(step.possibilities())
                            .hasSize(2)
                            .anySatisfy(possibility -> {
                                assertThat(possibility.id()).isNotNull();
                                assertThat(possibility.recurrence()).isInstanceOf(PossibilityRecurrence.Always.class);
                                assertThat(possibility.conditionType()).isEqualTo(AndOrOr.AND);
                                assertThat(possibility.trigger()).isInstanceOf(PossibilityTrigger.GoInSpace.class);
                                PossibilityTrigger.GoInSpace trigger = (PossibilityTrigger.GoInSpace) possibility.trigger();
                                assertThat(trigger.spaceId()).isEqualTo(new BoardSpace.Id("ABCD"));
                                assertThat(possibility.conditions())
                                        .hasSize(2)
                                        .anySatisfy(condition -> {
                                            assertThat(condition).isInstanceOf(PossibilityCondition.OutsideSpace.class);
                                            assertThat(((PossibilityCondition.OutsideSpace) condition).spaceId()).isEqualTo(new BoardSpace.Id("ABCD"));
                                        })
                                        .anySatisfy(condition -> {
                                            assertThat(condition).isInstanceOf(PossibilityCondition.AbsoluteTime.class);
                                            assertThat(((PossibilityCondition.AbsoluteTime) condition).duration()).isEqualTo(Duration.ofMinutes(27));
                                        });
                                assertThat(possibility.consequences())
                                        .hasSize(2)
                                        .anySatisfy(consequence -> {
                                            assertThat(consequence).isInstanceOf(PossibilityConsequence.Alert.class);
                                            assertThat(((PossibilityConsequence.Alert) consequence).message()).satisfies(withoutId(i18n("""
                                                    C'est la vie
                                                    Cuicui""", "Alarm !!!")));
                                        })
                                        .anySatisfy(consequence -> {
                                            assertThat(consequence).isInstanceOf(PossibilityConsequence.GoalTarget.class);
                                            PossibilityConsequence.GoalTarget goalTarget = (PossibilityConsequence.GoalTarget) consequence;
                                            assertThat(goalTarget.stepId()).isEqualTo(new ScenarioConfig.Step.Id("EFG"));
                                            assertThat(goalTarget.targetId()).isEqualTo(new ScenarioConfig.Target.Id("9876"));
                                            assertThat(goalTarget.state()).isEqualTo(ScenarioGoal.State.FAILURE);
                                        });

                            })
                            .anySatisfy(possibility -> {
                                assertThat(possibility.id()).isNotNull();
                                assertThat(possibility.recurrence()).isInstanceOf(PossibilityRecurrence.Times.class);
                                assertThat(((PossibilityRecurrence.Times) possibility.recurrence()).value()).isEqualTo(4);
                                assertThat(possibility.conditionType()).isEqualTo(AndOrOr.OR);
                                assertThat(possibility.trigger()).isInstanceOf(PossibilityTrigger.AbsoluteTime.class);
                                PossibilityTrigger.AbsoluteTime trigger = (PossibilityTrigger.AbsoluteTime) possibility.trigger();
                                assertThat(trigger.value()).isEqualTo(TimeUnit.ofMinutes(42));
                                assertThat(possibility.conditions())
                                        .hasSize(2)
                                        .anySatisfy(condition -> {
                                            assertThat(condition).isInstanceOf(PossibilityCondition.InStep.class);
                                            assertThat(((PossibilityCondition.InStep) condition).stepId()).isEqualTo(new ScenarioConfig.Step.Id("0987"));
                                        })
                                        .anySatisfy(condition -> {
                                            assertThat(condition).isInstanceOf(PossibilityCondition.OutsideSpace.class);
                                            assertThat(((PossibilityCondition.OutsideSpace) condition).spaceId()).isEqualTo(new BoardSpace.Id("9823"));
                                        });
                                assertThat(possibility.consequences())
                                        .hasSize(2)
                                        .anySatisfy(consequence -> {
                                            assertThat(consequence).isInstanceOf(PossibilityConsequence.Goal.class);
                                            PossibilityConsequence.Goal goal = (PossibilityConsequence.Goal) consequence;
                                            assertThat(goal.stepId()).isEqualTo(new ScenarioConfig.Step.Id("KLM"));
                                            assertThat(goal.state()).isEqualTo(ScenarioGoal.State.SUCCESS);
                                        })
                                        .anySatisfy(consequence -> {
                                            assertThat(consequence).isInstanceOf(PossibilityConsequence.GoalTarget.class);
                                            PossibilityConsequence.GoalTarget goalTarget = (PossibilityConsequence.GoalTarget) consequence;
                                            assertThat(goalTarget.stepId()).isEqualTo(new ScenarioConfig.Step.Id("9876"));
                                            assertThat(goalTarget.targetId()).isEqualTo(new ScenarioConfig.Target.Id("active"));
                                            assertThat(goalTarget.state()).isEqualTo(ScenarioGoal.State.ACTIVE);
                                        });

                            });
                });
    }


    @Test
    public void twoSteps() {
        TemplateGeneratorUseCase.Script script = new TemplateGeneratorUseCase.Script("""
                Two Step:4:Hello:50
                --- Step
                ------ Target
                ------ Possibility
                --------- Trigger:goinSPACE:EFG
                --- Step
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
                    assertThat(step.label()).isEmpty();
                    assertThat(step.targets())
                            .hasSize(1)
                            .anySatisfy(target -> {
                                assertThat(target.id()).isNotNull();
                                assertThat(target.label()).isEmpty();
                                assertThat(target.desc()).isEmpty();
                                assertThat(target.optional()).isFalse();
                            });
                    assertThat(step.possibilities())
                            .hasSize(1)
                            .anySatisfy(possibility -> {
                                assertThat(possibility.id()).isNotNull();
                                assertThat(possibility.recurrence()).isInstanceOf(PossibilityRecurrence.Always.class);
                                assertThat(possibility.conditionType()).isEqualTo(AndOrOr.AND);
                                assertThat(possibility.trigger()).isInstanceOf(PossibilityTrigger.GoInSpace.class);
                                PossibilityTrigger.GoInSpace trigger = (PossibilityTrigger.GoInSpace) possibility.trigger();
                                assertThat(trigger.spaceId()).isEqualTo(new BoardSpace.Id("EFG"));
                                assertThat(possibility.conditions()).isEmpty();
                                assertThat(possibility.consequences()).isEmpty();
                            });
                })
                .anySatisfy(step -> {
                    assertThat(step.id()).isNotNull();
                    assertThat(step.label()).isEmpty();
                    assertThat(step.targets()).isEmpty();
                    assertThat(step.possibilities())
                            .hasSize(1)
                            .anySatisfy(possibility -> {
                                assertThat(possibility.id()).isNotNull();
                                assertThat(possibility.recurrence()).isInstanceOf(PossibilityRecurrence.Times.class);
                                assertThat(((PossibilityRecurrence.Times) possibility.recurrence()).value()).isEqualTo(5);
                                assertThat(possibility.conditionType()).isEqualTo(AndOrOr.AND);
                                assertThat(possibility.trigger()).isInstanceOf(PossibilityTrigger.AbsoluteTime.class);
                                PossibilityTrigger.AbsoluteTime trigger = (PossibilityTrigger.AbsoluteTime) possibility.trigger();
                                assertThat(trigger.value()).isEqualTo(TimeUnit.ofMinutes(51));
                                assertThat(possibility.conditions()).isEmpty();
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
                    assertThat(board.priority()).isEqualTo(BoardSpace.Priority.LOW);
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
                firstBoard:5.2:Mon deuxième plateau
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
        assertThat(template.code()).isEqualTo(new Template.Code("firstBoard"));
        assertThat(template.version()).isEqualTo("5.2");
        assertThat(template.label()).isEqualTo("Mon deuxième plateau");
        assertThat(template.maxDuration()).isEqualTo(Duration.ofHours(1));
        assertThat(template.board().id()).isNotNull();
        assertThat(template.board().spaces())
                .hasSize(2)
                .anySatisfy(board -> {
                    assertThat(board.id()).isNotNull();
                    assertThat(board.label()).isEqualTo("Mon deuxième espace");
                    assertThat(board.priority()).isEqualTo(BoardSpace.Priority.MEDIUM);
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
                    assertThat(board.priority()).isEqualTo(BoardSpace.Priority.LOWEST);
                    assertThat(board.rects())
                            .hasSize(1)
                            .anySatisfy(rect -> {
                                assertThat(rect.bottomLeft()).isEqualTo(new Point(89.745F, 5.5684F));
                                assertThat(rect.topRight()).isEqualTo(new Point(0.8547F, 8.147F));
                            });
                });
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

    /*
    - Template
        - Board -> DONE On va dire ok
            - BoardSpace
                Priority -> Done
                Rect -> Done ?
        - ScenarioConfig -> DONE On va dire ok
            - Step
                - Target -> Done
                - Possibility
                    - conditionType -> Done
                    - Recurrency -> Done
                    - Trigger -> AbsoluteTime, GoInSpace, TODO -> GoOutSpace, RelativeTimeAfterOtherTrigger
                    - Condition -> OutsideSpace, InStep, AbsoluteTime, TODO InsideSpace,RelativeTimeAfterOtherTrigger
                    - Consequence -> GoalTarget, Goal, Alert, TODO GameOver
        - MapConfig -> Todo
            - Item
                - Map
                    - PositionS
                    - Definition
                    - Priority
     */

}
package fr.plop.contexts.game.config.template.domain;

import fr.plop.contexts.game.config.board.domain.model.BoardSpace;
import fr.plop.contexts.game.config.consequence.Consequence;
import fr.plop.contexts.game.config.map.domain.MapItem;
import fr.plop.contexts.game.config.scenario.domain.model.Possibility;
import fr.plop.contexts.game.config.scenario.domain.model.PossibilityCondition;
import fr.plop.contexts.game.config.scenario.domain.model.PossibilityRecurrence;
import fr.plop.contexts.game.config.scenario.domain.model.PossibilityTrigger;
import fr.plop.contexts.game.config.scenario.domain.model.ScenarioConfig;
import fr.plop.contexts.game.config.talk.domain.TalkItem;
import fr.plop.contexts.game.config.template.domain.model.Template;
import fr.plop.contexts.game.session.scenario.domain.model.ScenarioGoal;
import fr.plop.contexts.game.session.time.GameSessionTimeUnit;
import fr.plop.subs.i18n.domain.I18n;
import fr.plop.subs.i18n.domain.Language;
import fr.plop.generic.enumerate.AndOrOr;
import fr.plop.generic.position.Point;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;
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
                                assertThat(possibility.trigger()).isInstanceOf(PossibilityTrigger.SpaceGoIn.class);
                                PossibilityTrigger.SpaceGoIn trigger = (PossibilityTrigger.SpaceGoIn) possibility.trigger();
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
                                            assertThat(consequence).isInstanceOf(Consequence.DisplayMessage.class);
                                            assertThat(((Consequence.DisplayMessage) consequence).value()).satisfies(withoutId(i18n("""
                                                    C'est la vie
                                                    Cuicui""", "Alarm !!!")));
                                        })
                                        .anySatisfy(consequence -> {
                                            assertThat(consequence).isInstanceOf(Consequence.ScenarioTarget.class);
                                            Consequence.ScenarioTarget goalTarget = (Consequence.ScenarioTarget) consequence;
                                            assertThat(goalTarget.stepId()).isEqualTo(new ScenarioConfig.Step.Id("EFG"));
                                            assertThat(goalTarget.targetId()).isEqualTo(new ScenarioConfig.Target.Id("9876"));
                                            assertThat(goalTarget.state()).isEqualTo(fr.plop.contexts.game.session.scenario.domain.model.ScenarioGoal.State.FAILURE);
                                        });

                            })
                            .anySatisfy(possibility -> {
                                assertThat(possibility.id()).isNotNull();
                                assertThat(possibility.recurrence()).isInstanceOf(PossibilityRecurrence.Times.class);
                                assertThat(((PossibilityRecurrence.Times) possibility.recurrence()).value()).isEqualTo(4);
                                assertThat(possibility.conditionType()).isEqualTo(AndOrOr.OR);
                                assertThat(possibility.trigger()).isInstanceOf(PossibilityTrigger.AbsoluteTime.class);
                                PossibilityTrigger.AbsoluteTime trigger = (PossibilityTrigger.AbsoluteTime) possibility.trigger();
                                assertThat(trigger.value()).isEqualTo(GameSessionTimeUnit.ofMinutes(42));
                                assertThat(possibility.conditions())
                                        .hasSize(2)
                                        .anySatisfy(condition -> {
                                            assertThat(condition).isInstanceOf(PossibilityCondition.StepIn.class);
                                            assertThat(((PossibilityCondition.StepIn) condition).stepId()).isEqualTo(new ScenarioConfig.Step.Id("0987"));
                                        })
                                        .anySatisfy(condition -> {
                                            assertThat(condition).isInstanceOf(PossibilityCondition.OutsideSpace.class);
                                            assertThat(((PossibilityCondition.OutsideSpace) condition).spaceId()).isEqualTo(new BoardSpace.Id("9823"));
                                        });
                                assertThat(possibility.consequences())
                                        .hasSize(2)
                                        .anySatisfy(consequence -> {
                                            assertThat(consequence).isInstanceOf(Consequence.ScenarioStep.class);
                                            Consequence.ScenarioStep goal = (Consequence.ScenarioStep) consequence;
                                            assertThat(goal.stepId()).isEqualTo(new ScenarioConfig.Step.Id("KLM"));
                                            assertThat(goal.state()).isEqualTo(fr.plop.contexts.game.session.scenario.domain.model.ScenarioGoal.State.SUCCESS);
                                        })
                                        .anySatisfy(consequence -> {
                                            assertThat(consequence).isInstanceOf(Consequence.ScenarioTarget.class);
                                            Consequence.ScenarioTarget goalTarget = (Consequence.ScenarioTarget) consequence;
                                            assertThat(goalTarget.stepId()).isEqualTo(new ScenarioConfig.Step.Id("EFG123"));
                                            assertThat(goalTarget.targetId()).isEqualTo(new ScenarioConfig.Target.Id("9876"));
                                            assertThat(goalTarget.state()).isEqualTo(fr.plop.contexts.game.session.scenario.domain.model.ScenarioGoal.State.ACTIVE);
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
                                assertThat(possibility.trigger()).isInstanceOf(PossibilityTrigger.SpaceGoIn.class);
                                PossibilityTrigger.SpaceGoIn trigger = (PossibilityTrigger.SpaceGoIn) possibility.trigger();
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
                                assertThat(trigger.value()).isEqualTo(GameSessionTimeUnit.ofMinutes(51));
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


    @Test
    public void firstMap() {
        TemplateGeneratorUseCase.Script script = new TemplateGeneratorUseCase.Script("""
                firstMap:2.0.0:Ma première carte
                
                --- Map:Asset:imgs/first/map.png
                ------ Priority:HIGH
                
                ------ position:89.09:10.064
                --------- Priority:HIGHest
                
                ------ POSITION:78.865:23.9887
                --------- Priority:LOW
                --------- Space:Id1
                --------- Space:Id3
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
                    assertThat(item.isImageAssert()).isEqualTo(true);
                    assertThat(item.imagePath()).isEqualTo("imgs/first/map.png");
                    assertThat(item.priority()).isEqualTo(MapItem.Priority.HIGH);
                    assertThat(item.positions())
                            .hasSize(2)
                            .anySatisfy(position -> {
                                assertThat(position.id()).isNotNull();
                                assertThat(position.priority()).isEqualTo(MapItem.Priority.HIGHEST);

                                assertThat(position).isInstanceOf(MapItem.Position.Point.class);
                                MapItem.Position.Point point = (MapItem.Position.Point) position;
                                assertThat(point.x()).isEqualTo(89.09F);
                                assertThat(point.y()).isEqualTo(10.064F);
                            })
                            .anySatisfy(position -> {
                                assertThat(position.id()).isNotNull();
                                assertThat(position.priority()).isEqualTo(MapItem.Priority.LOW);

                                assertThat(position).isInstanceOf(MapItem.Position.Point.class);
                                MapItem.Position.Point point = (MapItem.Position.Point) position;
                                assertThat(point.x()).isEqualTo(78.865F);
                                assertThat(point.y()).isEqualTo(23.9887F);
                            });
                    assertThat(item.isStep(new ScenarioConfig.Step.Id("Id1"))).isTrue();
                    assertThat(item.isStep(new ScenarioConfig.Step.Id("Id2"))).isFalse();
                    assertThat(item.isStep(new ScenarioConfig.Step.Id("Id3"))).isTrue();
                });
    }

    @Test
    public void prepareFirstMap_ManageLinkBoardScenario() {
        TemplateGeneratorUseCase.Script script = new TemplateGeneratorUseCase.Script("""
                linkBoardScenario:0.0.0:Link Board Scenario
                --- Board
                ------ Space:La lune:HIGH
                --------- 1:2:3:4
                ------ Space:Mars:HIGH
                --------- 5:6:7:8
                --- Step
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
                ------ Space:La lune:HIGH
                --------- 1:2:3:4
                --- Step
                ------ Possibility
                --------- Trigger:GoInSpace:SpaceId:La lune
                --------- consequence:TalkOptions:OPTIONS_ABCD
                --- Talk
                ------ Options(ref OPTIONS_ABCD)
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
                    assertThat(talk).isInstanceOf(TalkItem.MultipleOptions.class);
                    TalkItem.MultipleOptions talkOptions = (TalkItem.MultipleOptions) talk;
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
    public void prepareFirstMap_add_refs() {

        TemplateGeneratorUseCase.Script script = new TemplateGeneratorUseCase.Script("""
                addRef:0.0.0:Ajout de la notion de Reférénce
                --- Step(ref STEP_A)
                ------ Target (ref TARGET_ATTERRIR):FR:Atterrir sur la lune:EN:Atterrir sur la lune en anglais
                ------ Possibility
                --------- Trigger:GoInSpace:SpaceId:abcd
                --------- consequence:TalkOptions:OPTIONS_ABCD
                ------ Possibility
                --------- Trigger:SelectTalkOption:CHOIX_A
                --------- Consequence:GoalTarget:stepId:STEP_A:targetId:TARGET_ATTERRIR:state:active
                --------- Consequence:GoalTarget:stepId:STEP_A:targetId:TARGET_ATTERRIR:state:active
                --- Step(ref REF_STEP_B)
                ------ Target:FR:Réparer la station:EN:Réparer la station en anglais
                --- Talk
                ------ Options(ref OPTIONS_ABCD)
                --------- Option (REF CHOIX_A)
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


        assertThat(template.talk().items())
                .hasSize(1)
                .anySatisfy(talk -> {
                    assertThat(talk).isInstanceOf(TalkItem.MultipleOptions.class);
                    TalkItem.MultipleOptions talkOptions = (TalkItem.MultipleOptions) talk;
                    assertThat(talkOptions.id()).isNotNull();
                    assertThat(talkOptions.value().value(Language.FR)).isEqualTo("C'est quoi ton choix ?");
                    assertThat(talkOptions.value().value(Language.EN)).isEqualTo("C'est quoi ton choix en anglais ?");
                    assertThat(talkOptions.options()).hasSize(2);
                    assertThat(talkOptions.options().getFirst().id()).isNotNull();
                    assertThat(talkOptions.options().getFirst().value().value(Language.FR)).isEqualTo("Le choix A");
                    assertThat(talkOptions.options().getFirst().value().value(Language.EN)).isEqualTo("Le choix A en anglais ?");
                    assertThat(talkOptions.options().get(1).id()).isNotNull();
                    assertThat(talkOptions.options().get(1).value().value(Language.FR)).isEqualTo("Le choix B");
                    assertThat(talkOptions.options().get(1).value().value(Language.EN)).isEqualTo("Le choix B en anglais");
                });
        TalkItem.MultipleOptions.Option.Id optionIdChoixA = ((TalkItem.MultipleOptions) template.talk().items().getFirst()).options().getFirst().id();

        List<ScenarioConfig.Step> steps = template.scenario().steps();
        assertThat(steps.size()).isEqualTo(2);

        ScenarioConfig.Step step = steps.getFirst();

        assertThat(step.targets().getFirst().id()).isNotNull();
        assertThat(step.targets().getFirst().label().orElseThrow().value(Language.FR)).isEqualTo("Atterrir sur la lune");
        assertThat(step.targets().getFirst().label().orElseThrow().value(Language.EN)).isEqualTo("Atterrir sur la lune en anglais");

        Possibility possibilityFirst = step.possibilities().getFirst();

        assertThat(possibilityFirst.trigger())
                .isInstanceOf(PossibilityTrigger.SpaceGoIn.class);
        PossibilityTrigger.SpaceGoIn goInSpace = (PossibilityTrigger.SpaceGoIn) possibilityFirst.trigger();
        assertThat(goInSpace.id()).isNotNull();
        assertThat(goInSpace.spaceId()).isEqualTo(new BoardSpace.Id("abcd"));

        assertThat(possibilityFirst.consequences().getFirst()).isInstanceOf(Consequence.DisplayTalk.class);
        Consequence.DisplayTalk displayTalk = (Consequence.DisplayTalk) possibilityFirst.consequences().getFirst();


        Possibility possibilitySecond = step.possibilities().get(1);

        assertThat(possibilitySecond.trigger()).isInstanceOf(PossibilityTrigger.TalkNext.class);
        PossibilityTrigger.TalkNext talkNext = (PossibilityTrigger.TalkNext) possibilitySecond.trigger();
        assertThat(talkNext.id()).isNotNull();
        assertThat(talkNext.talkItemId()).isEqualTo(displayTalk.talkId());
        assertThat(talkNext.optionId()).contains(optionIdChoixA);


        assertThat(possibilitySecond.consequences().getFirst()).isInstanceOf(Consequence.ScenarioTarget.class);
        Consequence.ScenarioTarget scenarioTarget = (Consequence.ScenarioTarget) possibilitySecond.consequences().getFirst();
        assertThat(scenarioTarget.id()).isNotNull();
        assertThat(scenarioTarget.stepId()).isEqualTo(step.id());
        assertThat(scenarioTarget.targetId()).isEqualTo(step.targets().getFirst().id());
        assertThat(scenarioTarget.state()).isEqualTo(ScenarioGoal.State.ACTIVE);

    }

    @Test
    public void prepareFirstMap_add_refs_withoutStepId() {

        TemplateGeneratorUseCase.Script script = new TemplateGeneratorUseCase.Script("""
                addRef:0.0.0:Ajout de la notion de Reférénce - optimisation step id
                --- Step
                ------ Target:FR:Réparer la station:EN:Réparer la station en anglais
                --- Step
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
        assertThat(stepFirst.targets().getFirst().label().orElseThrow().value(Language.FR)).isEqualTo("Réparer la station");
        assertThat(stepFirst.targets().getFirst().label().orElseThrow().value(Language.EN)).isEqualTo("Réparer la station en anglais");

        ScenarioConfig.Step stepSecond = steps.get(1);
        assertThat(stepSecond.targets().getFirst().id()).isNotNull();
        assertThat(stepSecond.targets().getFirst().label().orElseThrow().value(Language.FR)).isEqualTo("Atterrir sur la lune");
        assertThat(stepSecond.targets().getFirst().label().orElseThrow().value(Language.EN)).isEqualTo("Atterrir sur la lune en anglais");


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
        assertThat(scenarioTarget.state()).isEqualTo(ScenarioGoal.State.ACTIVE);
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
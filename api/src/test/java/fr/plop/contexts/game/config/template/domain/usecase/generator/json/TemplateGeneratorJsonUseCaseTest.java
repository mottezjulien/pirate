package fr.plop.contexts.game.config.template.domain.usecase.generator.json;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.plop.contexts.game.config.board.domain.model.BoardSpace;
import fr.plop.contexts.game.config.map.domain.MapObject;
import fr.plop.contexts.game.config.condition.Condition;
import fr.plop.contexts.game.config.consequence.Consequence;
import fr.plop.contexts.game.config.scenario.domain.model.Possibility;
import fr.plop.contexts.game.config.scenario.domain.model.PossibilityRecurrence;
import fr.plop.contexts.game.config.scenario.domain.model.PossibilityTrigger;
import fr.plop.contexts.game.config.scenario.domain.model.ScenarioConfig;
import fr.plop.contexts.game.config.talk.domain.TalkItem;
import fr.plop.contexts.game.config.talk.domain.TalkItemNext;
import fr.plop.contexts.game.config.template.domain.model.Template;
import fr.plop.contexts.game.instance.scenario.domain.model.ScenarioState;
import fr.plop.contexts.game.instance.situation.domain.GameInstanceSituation;
import fr.plop.contexts.game.instance.time.GameInstanceTimeUnit;
import fr.plop.generic.enumerate.Priority;
import fr.plop.subs.i18n.domain.I18n;
import fr.plop.subs.i18n.domain.Language;
import fr.plop.subs.image.Image;
import org.assertj.core.data.Offset;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;

public class TemplateGeneratorJsonUseCaseTest {

    private final TemplateGeneratorJsonUseCase generator = new TemplateGeneratorJsonUseCase();
    private final ObjectMapper objectMapper = new ObjectMapper();

    private TemplateGeneratorRoot parseRoot(String json) throws JsonProcessingException {
        return objectMapper.readValue(json, TemplateGeneratorRoot.class);
    }

    @Test
    public void emptyDefaultTemplate() throws JsonProcessingException {
        TemplateGeneratorRoot root = parseRoot("{ \"code\": \"ABCD\" }");
        Template template = generator.template(root);
        assertThat(template).isNotNull();
        assertThat(template.id()).isNotNull();
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
    public void versionAndLabel() throws JsonProcessingException {
        TemplateGeneratorRoot root = parseRoot("{ \"code\": \"XYZ\",\"version\": \"1.8.4\",\"label\": \"Mon nouveau niveau\" }");
        Template template = generator.template(root);
        assertThat(template).isNotNull();
        assertThat(template.id()).isNotNull();
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
    public void versionAndLabelAndDuration() throws JsonProcessingException {
        TemplateGeneratorRoot root = parseRoot("{ \"code\": \"POUET\",\"version\": \"1.4\",\"label\": \"Mon nouveau niveau 2\", \"duration\": 180 }");
        Template template = generator.template(root);
        assertThat(template).isNotNull();
        assertThat(template.id()).isNotNull();
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
    public void oneStepWithTargets() throws JsonProcessingException {


        TemplateGeneratorRoot root = parseRoot("""
                { "code": "Coucou","version": "2", "duration": 30, "scenario" : {
                    "steps": [
                        {
                            "label": {"FR":"Chez Moi 123", "EN":"At Home 456"},
                            "targets": [
                                {"label": {"FR":"Cuisine", "EN":"Kitchen"}},
                                {"label": {"EN":"Office", "FR":"Bureau"}, "optional": true},
                                {
                                "label": {"FR":"Chambre","EN":"Room"}, "optional": true,
                                "description": {"FR":"Ceci est ma chambre.\\nC'est une belle chambre.", "EN":"This is my room.\\nIt's a beautiful room."}
                                },
                                {
                                "label": {"FR":"Autre","EN":"Other"},
                                "description": {"EN":"This is another space.\\nIt's an other space.\\nIt's an other space.", "FR":"Ceci est mon bureau.\\nCeci est ma chambre.\\nCeci est mon bureau."}
                            }
                            ]
                        }
                    ]
                }
                }""");
        Template template = generator.template(root);

        assertThat(template).isNotNull();
        assertThat(template.id()).isNotNull();
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
                                assertThat(target.optDescription()).isEmpty();
                                assertThat(target.optional()).isFalse();
                            })
                            .anySatisfy(target -> {
                                assertThat(target.id()).isNotNull();
                                assertThat(target.label()).satisfies(withoutId(i18n("Bureau", "Office")));
                                assertThat(target.optDescription()).isEmpty();
                                assertThat(target.optional()).isTrue();
                            })
                            .anySatisfy(target -> {
                                assertThat(target.id()).isNotNull();
                                assertThat(target.label()).satisfies(withoutId(i18n("Chambre", "Room")));
                                assertThat(target.optDescription()).isPresent().hasValueSatisfying(withoutId(i18n("""
                                        Ceci est ma chambre.
                                        C'est une belle chambre.""", """
                                        This is my room.
                                        It's a beautiful room.""")));
                                assertThat(target.optional()).isTrue();
                            })
                            .anySatisfy(target -> {
                                assertThat(target.id()).isNotNull();
                                assertThat(target.label()).satisfies(withoutId(i18n("Autre", "Other")));
                                assertThat(target.optDescription()).isPresent().hasValueSatisfying(withoutId(i18n("""
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
    public void oneStepWithTargetAndPossibilities() throws JsonProcessingException {
        TemplateGeneratorRoot root = parseRoot("""
                {
                  "code": "With Possibility",
                  "version": "3",
                  "label": "Hello",
                  "duration": 45,
                  "board": {
                    "spaces": [
                      { "ref": "ABCD", "label": "Office", "priority": "HIGH" },
                      { "ref": "XYZ", "label": "Kitchen", "priority": "LOW" }
                    ]
                  },
                  "scenario": {
                    "steps": [
                      {
                        "ref": "STEP_0",
                        "label": { "FR": "Pouet", "EN": "Pouet" }
                      },
                      {
                        "ref": "STEP_1",
                        "label": { "FR": "Chez Moi", "EN": "At Home" },
                        "targets": [
                          { "ref": "TARGET_A", "label": { "FR": "Cuisine", "EN": "Kitchen" } },
                          { "label": { "FR": "Bureau", "EN": "Office" }, "optional": true }
                        ],
                        "possibilities": [
                          {
                            "recurrence": { "type": "ALWAYS" },
                            "trigger": { "type": "GoInSpace", "value": "ABCD" },
                            "condition": {
                               "type": "AND",
                               "children": [
                                 { "type": "OutsideSpace", "metadata": { "spaceId": "ABCD" } },
                                 { "type": "AbsoluteTime", "metadata": { "duration": 27 } }
                               ]
                            },
                            "consequences": [
                              { "type": "Alert", "metadata": { "value": { "EN": "Alarm !!!", "FR": "C'est la vie\\nCuicui" } } },
                              { "type": "GoalTarget", "metadata": { "targetId": "TARGET_A", "state": "FAILURE" } }
                            ]
                          },
                          {
                            "recurrence": { "type": "times", "value": 4 },
                            "trigger": { "type": "AbsoluteTime", "value": "42" },
                            "condition": {
                               "type": "AND",
                               "children": [
                                 { "type": "InStep", "metadata": { "stepId": "STEP_0" } },
                                 { "type": "OutsideSpace", "metadata": { "spaceId": "XYZ" } }
                               ]
                            },
                            "consequences": [
                              { "type": "Goal", "metadata": { "stepId": "STEP_1", "state": "SUCCESS" } },
                              { "type": "GoalTarget", "metadata": { "targetId": "TARGET_A", "state": "ACTIVE" } }
                            ]
                          }
                        ]
                      }
                    ]
                  }
                }
                """);
        Template template = generator.template(root);
        assertThat(template).isNotNull();
        assertThat(template.id()).isNotNull();
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
                                assertThat(target.optDescription()).isEmpty();
                                assertThat(target.optional()).isFalse();
                            })
                            .anySatisfy(target -> {
                                assertThat(target.id()).isNotNull();
                                assertThat(target.label()).satisfies(withoutId(i18n("Bureau", "Office")));
                                assertThat(target.optDescription()).isEmpty();
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
                                                        assertThat(((Condition.AbsoluteTime) subCondition).value()).isEqualTo(GameInstanceTimeUnit.ofMinutes(27));
                                                    });
                                        });

                                assertThat(possibility.consequences())
                                        .hasSize(2)
                                        .anySatisfy(consequence -> {
                                            assertThat(consequence).isInstanceOf(Consequence.DisplayAlert.class);
                                            assertThat(((Consequence.DisplayAlert) consequence).value()).satisfies(withoutId(i18n("C'est la vie\nCuicui", "Alarm !!!")));
                                        })
                                        .anySatisfy(consequence -> {
                                            assertThat(consequence).isInstanceOf(Consequence.ScenarioTarget.class);
                                            Consequence.ScenarioTarget goalTarget = (Consequence.ScenarioTarget) consequence;
                                            assertThat(goalTarget.targetId()).isEqualTo(template.scenario().steps().get(1).targets().getFirst().id());
                                            assertThat(goalTarget.state()).isEqualTo(ScenarioState.FAILURE);
                                        });

                            })
                            .anySatisfy(possibility -> {
                                assertThat(possibility.id()).isNotNull();
                                assertThat(possibility.recurrence()).isInstanceOf(PossibilityRecurrence.Times.class);
                                assertThat(((PossibilityRecurrence.Times) possibility.recurrence()).value()).isEqualTo(4);
                                assertThat(possibility.trigger()).isInstanceOf(PossibilityTrigger.AbsoluteTime.class);
                                PossibilityTrigger.AbsoluteTime trigger = (PossibilityTrigger.AbsoluteTime) possibility.trigger();
                                assertThat(trigger.value()).isEqualTo(GameInstanceTimeUnit.ofMinutes(42));
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
                                            assertThat(goal.state()).isEqualTo(ScenarioState.SUCCESS);
                                        })
                                        .anySatisfy(consequence -> {
                                            assertThat(consequence).isInstanceOf(Consequence.ScenarioTarget.class);
                                            Consequence.ScenarioTarget goalTarget = (Consequence.ScenarioTarget) consequence;
                                            assertThat(goalTarget.targetId()).isEqualTo(template.scenario().steps().get(1).targets().getFirst().id());
                                            assertThat(goalTarget.state()).isEqualTo(ScenarioState.ACTIVE);
                                        });

                            });
                });
    }

    @Test
    public void twoSteps() throws JsonProcessingException {
        TemplateGeneratorRoot root = parseRoot("""
                {
                  "code": "Two Step",
                  "version": "4",
                  "label": "Hello",
                  "duration": 50,
                  "board": {
                    "spaces": [
                      { "ref": "monespace", "label": "Mon premier espace", "priority": "LOW" }
                    ]
                  },
                  "scenario": {
                    "steps": [
                      {
                        "label": { "FR": "Premier", "EN": "First" },
                        "targets": [
                          { "label": { "FR": "Mon objectif", "EN": "My target" } }
                        ],
                        "possibilities": [
                          { "trigger": { "type": "goinSPACE", "value": "monespace" } }
                        ]
                      },
                      {
                        "label": { "FR": "Deuxième", "EN": "Second" },
                        "possibilities": [
                          {
                            "recurrence": { "type": "times", "value": 5 },
                            "trigger": { "type": "AbsoluteTime", "value": "51" }
                          }
                        ]
                      }
                    ]
                  }
                }
                """);
        Template template = generator.template(root);
        assertThat(template).isNotNull();
        assertThat(template.id()).isNotNull();
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
                                assertThat(target.optDescription()).isEmpty();
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
                                assertThat(trigger.value()).isEqualTo(GameInstanceTimeUnit.ofMinutes(51));
                                assertThat(possibility.optCondition()).isEmpty();
                                assertThat(possibility.consequences()).isEmpty();
                            });
                });
    }


    @Test
    public void firstBoard() throws JsonProcessingException {
        TemplateGeneratorRoot root = parseRoot("""
                {
                  "code": "firstBoard",
                  "version": "5.1",
                  "label": "Mon premier plateau",
                  "board": {
                    "spaces": [
                      {
                        "label": "Mon premier espace",
                        "priority": "LOW",
                        "rectangles": [
                          { "bottomLeft": { "lat": 5.7, "lng": 10 }, "topRight": { "lat": 8.097, "lng": 50.43 } },
                          { "bottomLeft": { "lat": 10, "lng": 20 }, "topRight": { "lat": 8.56, "lng": 9 } }
                        ]
                      }
                    ]
                  }
                }
                """);
        Template template = generator.template(root);
        assertThat(template).isNotNull();
        assertThat(template.id()).isNotNull();
        assertThat(template.maxDuration()).isEqualTo(Duration.ofHours(1));
        assertThat(template.board().id()).isNotNull();
        assertThat(template.board().spaces())
                .hasSize(1)
                .anySatisfy(board -> {
                    assertThat(board.id()).isNotNull();
                    assertThat(board.label()).isEqualTo("Mon premier espace");
                    assertThat(board.priority()).isEqualTo(Priority.LOW);
                    assertThat(board.rectangles())
                            .hasSize(2)
                            .anySatisfy(rect -> {
                                assertThat(rect.bottomLeft()).isEqualTo(fr.plop.generic.position.Point.from(5.7, 10.0));
                                assertThat(rect.topRight()).isEqualTo(fr.plop.generic.position.Point.from(8.097, 50.43));
                            })
                            .anySatisfy(rect -> {
                                assertThat(rect.bottomLeft()).isEqualTo(fr.plop.generic.position.Point.from(10.0, 20.0));
                                assertThat(rect.topRight()).isEqualTo(fr.plop.generic.position.Point.from(8.56, 9.0));
                            });
                });
    }


    @Test
    public void secondBoard() throws JsonProcessingException {
        TemplateGeneratorRoot root = parseRoot("""
                {
                  "code": "secondBoard",
                  "version": "5.2",
                  "label": "Mon deuxième plateau",
                  "board": {
                    "spaces": [
                      {
                        "label": "Mon deuxième espace",
                        "priority": "MEDIUM",
                        "rectangles": [
                          { "bottomLeft": { "lat": 5.798, "lng": 10.894 }, "topRight": { "lat": 45.98, "lng": 50.4338 } },
                          { "bottomLeft": { "lat": 1, "lng": 2 }, "topRight": { "lat": 4, "lng": 3 } }
                        ]
                      },
                      {
                        "label": "Mon 3eme espace",
                        "priority": "LOWEST",
                        "rectangles": [
                          { "bottomLeft": { "lat": 89.745, "lng": 5.5684 }, "topRight": { "lat": 0.8547, "lng": 8.147 } }
                        ]
                      }
                    ]
                  }
                }
                """);
        Template template = generator.template(root);
        assertThat(template).isNotNull();
        assertThat(template.id()).isNotNull();
        assertThat(template.maxDuration()).isEqualTo(Duration.ofHours(1));
        assertThat(template.board().id()).isNotNull();
        assertThat(template.board().spaces())
                .hasSize(2)
                .anySatisfy(board -> {
                    assertThat(board.id()).isNotNull();
                    assertThat(board.label()).isEqualTo("Mon deuxième espace");
                    assertThat(board.priority()).isEqualTo(Priority.MEDIUM);
                    assertThat(board.rectangles())
                            .hasSize(2)
                            .anySatisfy(rect -> {
                                assertThat(rect.bottomLeft()).isEqualTo(fr.plop.generic.position.Point.from(5.798, 10.894));
                                assertThat(rect.topRight()).isEqualTo(fr.plop.generic.position.Point.from(45.98, 50.4338));
                            })
                            .anySatisfy(rect -> {
                                assertThat(rect.bottomLeft()).isEqualTo(fr.plop.generic.position.Point.from(1, 2));
                                assertThat(rect.topRight()).isEqualTo(fr.plop.generic.position.Point.from(4, 3));
                            });
                })
                .anySatisfy(board -> {
                    assertThat(board.id()).isNotNull();
                    assertThat(board.label()).isEqualTo("Mon 3eme espace");
                    assertThat(board.priority()).isEqualTo(Priority.LOWEST);
                    assertThat(board.rectangles())
                            .hasSize(1)
                            .anySatisfy(rect -> {
                                assertThat(rect.bottomLeft()).isEqualTo(fr.plop.generic.position.Point.from(89.745, 5.5684));
                                assertThat(rect.topRight()).isEqualTo(fr.plop.generic.position.Point.from(0.8547, 8.147));
                            });
                });
    }


    @Test
    public void firstMap() throws JsonProcessingException {
        TemplateGeneratorRoot root = parseRoot("""
                {
                  "code": "firstMap",
                  "version": "2.0.0",
                  "label": "Ma première carte",
                  "maps": [
                    {
                      "priority": "HIGH",
                      "image": { "type": "Asset", "value": "imgs/first/map.png" },
                      "bounds": {
                        "bottomLeft": { "lat": 45.0, "lng": 4.0 },
                        "topRight": { "lat": 46.0, "lng": 5.0 }
                      },
                      "objects": [
                        {
                          "position": { "lat": 45.89, "lng": 4.10 },
                          "point": { "color": "" }
                        },
                        {
                          "position": { "lat": 45.78, "lng": 4.24 },
                          "point": { "color": "yellow" },
                          "condition": { "type": "AbsoluteTime", "metadata": { "duration": 41 } }
                        }
                      ]
                    }
                  ]
                }
                """);
        Template template = generator.template(root);
        assertThat(template).isNotNull();
        assertThat(template.id()).isNotNull();
        assertThat(template.maxDuration()).isEqualTo(Duration.ofHours(1));
        assertThat(template.map().id()).isNotNull();
        assertThat(template.map().items())
                .hasSize(1)
                .anySatisfy(item -> {
                    assertThat(item.id()).isNotNull();
                    assertThat(item.image().type()).isEqualTo(Image.Type.ASSET);
                    assertThat(item.image().value()).isEqualTo("imgs/first/map.png");
                    assertThat(item.priority()).isEqualTo(Priority.HIGH);
                    assertThat(item.bounds()).isNotNull();
                    assertThat(item.objects())
                            .hasSize(2)
                            .anySatisfy(object -> {
                                assertThat(object.id()).isNotNull();

                                assertThat(object).isInstanceOf(MapObject.Point.class);
                                MapObject.Point point = (MapObject.Point) object;
                                assertThat(point.position().lat().doubleValue()).isCloseTo(45.89, Offset.offset(0.001));
                                assertThat(point.position().lng().doubleValue()).isCloseTo(4.10, Offset.offset(0.001));
                                assertThat(point.color()).isEqualTo("");
                                assertThat(point.optCondition()).isEmpty();
                            })
                            .anySatisfy(position -> {
                                assertThat(position.id()).isNotNull();

                                assertThat(position).isInstanceOf(MapObject.Point.class);
                                MapObject.Point point = (MapObject.Point) position;
                                assertThat(point.position().lat().doubleValue()).isCloseTo(45.78, Offset.offset(0.001));
                                assertThat(point.position().lng().doubleValue()).isCloseTo(4.24, Offset.offset(0.001));
                                assertThat(point.color()).isEqualTo("yellow");
                                assertThat(point.optCondition()).hasValueSatisfying(condition -> {
                                    assertThat(condition).isInstanceOf(Condition.AbsoluteTime.class);
                                    assertThat(((Condition.AbsoluteTime) condition).value()).isEqualTo(new GameInstanceTimeUnit(41));
                                });
                            });
                });
    }

    @Test
    public void prepareFirstMap_ManageLinkBoardScenario() throws JsonProcessingException {
        TemplateGeneratorRoot root = parseRoot("""
                {
                  "code": "linkBoardScenario",
                  "version": "0.0.0",
                  "label": "Link Board Scenario",
                  "board": {
                    "spaces": [
                      { "ref": "La lune", "label": "La lune", "priority": "HIGH", "rectangles": [{ "bottomLeft": { "lat": 1, "lng": 2 }, "topRight": { "lat": 3, "lng": 4 } }] },
                      { "ref": "Mars", "label": "Mars", "priority": "HIGH", "rectangles": [{ "bottomLeft": { "lat": 5, "lng": 6 }, "topRight": { "lat": 7, "lng": 8 } }] }
                    ]
                  },
                  "scenario": {
                    "steps": [
                      {
                        "label": { "FR": "Aucun", "EN": "None" },
                        "possibilities": [
                          { "trigger": { "type": "GoInSpace", "value": "La lune" } },
                          { "trigger": { "type": "GoOutSpace", "value": "Mars" } }
                        ]
                      }
                    ]
                  }
                }
                """);
        Template template = generator.template(root);
        assertThat(template.id()).isNotNull();

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
    public void prepareFirstMap_TalkOptions() throws JsonProcessingException {
        TemplateGeneratorRoot root = parseRoot("""
                {
                  "code": "linkBoardScenario",
                  "version": "0.0.0",
                  "label": "Link Board Scenario",
                  "board": {
                    "spaces": [
                      { "ref": "La lune", "label": "La lune", "priority": "HIGH", "rectangles": [{ "bottomLeft": { "lat": 1, "lng": 2 }, "topRight": { "lat": 3, "lng": 4 } }] }
                    ]
                  },
                  "scenario": {
                    "steps": [
                      {
                        "label": { "FR": "Aucun", "EN": "None" },
                        "possibilities": [
                          {
                            "trigger": { "type": "GoInSpace", "value": "La lune" },
                            "consequences": [
                              { "type": "Talk", "metadata": { "talkRef": "OPTIONS_ABCD" } }
                            ]
                          }
                        ]
                      }
                    ]
                  },
                  "talk": {
                    "characters": [
                      {
                        "ref": "Bob",
                        "images": [
                          { "ref": "Happy", "value": { "type": "Asset", "value": "bob-happy.jpg" } }
                        ]
                      }
                    ],
                    "items": [
                      {
                        "ref": "OPTIONS_ABCD",
                        "value": { "FR": "C'est quoi ton choix ?", "EN": "C'est quoi ton choix en anglais ?" },
                        "character": { "character": "Bob", "image": "Happy" },
                        "options": [
                          { "ref": "OPTION_A", "value": { "FR": "Le choix A", "EN": "Le choix A en anglais ?" } },
                          { "ref": "OPTION_B", "value": { "FR": "Le choix B", "EN": "Le choix B en anglais" } }
                        ]
                      }
                    ]
                  }
                }
                """);
        Template template = generator.template(root);
        assertThat(template.id()).isNotNull();

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
                    assertThat(talk.id()).isEqualTo(talkId);
                    I18n resolve = talk.out().resolve(new GameInstanceSituation());
                    assertThat(resolve.value(Language.FR)).isEqualTo("C'est quoi ton choix ?");
                    assertThat(resolve.value(Language.EN)).isEqualTo("C'est quoi ton choix en anglais ?");

                    TalkItemNext.Options talkOptions = ((TalkItemNext.Options) talk.next());

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
    public void prepareFirstMap_add_refs_withoutStepId() throws JsonProcessingException {
        TemplateGeneratorRoot root = parseRoot("""
                {
                  "code": "addRef",
                  "version": "0.0.0",
                  "label": "Ajout de la notion de Reférénce - optimisation step id",
                  "scenario": {
                    "steps": [
                      {
                        "label": { "FR": "Aucun", "EN": "None" },
                        "targets": [
                          { "label": { "FR": "Réparer la station", "EN": "Réparer la station en anglais" } }
                        ]
                      },
                      {
                        "label": { "FR": "Aucun", "EN": "None" },
                        "targets": [
                          { "ref": "TARGET_ATTERRIR", "label": { "FR": "Atterrir sur la lune", "EN": "Atterrir sur la lune en anglais" } }
                        ],
                        "possibilities": [
                          {
                            "trigger": { "type": "AbsoluteTime", "value": "51" },
                            "consequences": [
                              { "type": "GoalTarget", "metadata": { "targetId": "TARGET_ATTERRIR", "state": "ACTIVE" } }
                            ]
                          }
                        ]
                      }
                    ]
                  }
                }
                """);
        Template template = generator.template(root);

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
        assertThat(goInSpace.value()).isEqualTo(new GameInstanceTimeUnit(51));

        assertThat(possibility.consequences().getFirst()).isInstanceOf(Consequence.ScenarioTarget.class);
        Consequence.ScenarioTarget scenarioTarget = (Consequence.ScenarioTarget) possibility.consequences().getFirst();
        assertThat(scenarioTarget.id()).isNotNull();
        assertThat(scenarioTarget.targetId()).isEqualTo(stepSecond.targets().getFirst().id());
        assertThat(scenarioTarget.state()).isEqualTo(ScenarioState.ACTIVE);
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

    @Test
    public void confirmConsequenceAndTrigger() throws JsonProcessingException {
        TemplateGeneratorRoot root = parseRoot("""
                {
                  "code": "confirmTest",
                  "version": "1.0.0",
                  "label": "Test Confirm",
                  "scenario": {
                    "steps": [
                      {
                        "ref": "STEP_1",
                        "label": { "FR": "Etape 1", "EN": "Step 1" },
                        "targets": [
                          { "ref": "TARGET_CHEST", "label": { "FR": "Ouvrir le coffre", "EN": "Open the chest" } }
                        ],
                        "possibilities": [
                          {
                            "trigger": { "type": "StepActive" },
                            "consequences": [
                              {
                                "type": "CONFIRM",
                                "metadata": {
                                  "ref": "CONFIRM_CHEST",
                                  "message": { "FR": "Voulez-vous ouvrir le coffre ?", "EN": "Do you want to open the chest?" }
                                }
                              }
                            ]
                          },
                          {
                            "trigger": {
                              "type": "ConfirmAnswer",
                              "metadata": { "confirmRef": "CONFIRM_CHEST", "answer": "YES" }
                            },
                            "consequences": [
                              { "type": "GoalTarget", "metadata": { "targetId": "TARGET_CHEST", "state": "SUCCESS" } },
                              { "type": "Alert", "metadata": { "value": { "FR": "Le coffre s'ouvre !", "EN": "The chest opens!" } } }
                            ]
                          },
                          {
                            "trigger": {
                              "type": "ConfirmAnswer",
                              "metadata": { "confirmRef": "CONFIRM_CHEST", "answer": "NO" }
                            },
                            "consequences": [
                              { "type": "Alert", "metadata": { "value": { "FR": "Vous laissez le coffre ferme.", "EN": "You leave the chest closed." } } }
                            ]
                          }
                        ]
                      }
                    ]
                  }
                }
                """);
        Template template = generator.template(root);

        assertThat(template).isNotNull();

        List<ScenarioConfig.Step> steps = template.scenario().steps();
        assertThat(steps).hasSize(1);

        ScenarioConfig.Step step = steps.getFirst();
        assertThat(step.possibilities()).hasSize(3);

        // First possibility: DisplayConfirm consequence
        Possibility displayConfirmPossibility = step.possibilities().getFirst();
        assertThat(displayConfirmPossibility.consequences()).hasSize(1);
        assertThat(displayConfirmPossibility.consequences().getFirst()).isInstanceOf(Consequence.DisplayConfirm.class);
        Consequence.DisplayConfirm displayConfirm = (Consequence.DisplayConfirm) displayConfirmPossibility.consequences().getFirst();
        assertThat(displayConfirm.id()).isNotNull();
        assertThat(displayConfirm.message().value(Language.FR)).isEqualTo("Voulez-vous ouvrir le coffre ?");
        assertThat(displayConfirm.message().value(Language.EN)).isEqualTo("Do you want to open the chest?");

        // Second possibility: ConfirmAnswer YES trigger
        Possibility confirmYesPossibility = step.possibilities().get(1);
        assertThat(confirmYesPossibility.trigger()).isInstanceOf(PossibilityTrigger.MessageConfirmAnswer.class);
        PossibilityTrigger.MessageConfirmAnswer confirmYesTrigger = (PossibilityTrigger.MessageConfirmAnswer) confirmYesPossibility.trigger();
        assertThat(confirmYesTrigger.token()).isEqualTo(displayConfirm.token());
        assertThat(confirmYesTrigger.expectedAnswer()).isTrue();
        assertThat(confirmYesPossibility.consequences()).hasSize(2);

        // Third possibility: ConfirmAnswer NO trigger
        Possibility confirmNoPossibility = step.possibilities().get(2);
        assertThat(confirmNoPossibility.trigger()).isInstanceOf(PossibilityTrigger.MessageConfirmAnswer.class);
        PossibilityTrigger.MessageConfirmAnswer confirmNoTrigger = (PossibilityTrigger.MessageConfirmAnswer) confirmNoPossibility.trigger();
        assertThat(confirmNoTrigger.token()).isEqualTo(displayConfirm.token());
        assertThat(confirmNoTrigger.expectedAnswer()).isFalse();
        assertThat(confirmNoPossibility.consequences()).hasSize(1);
    }

}
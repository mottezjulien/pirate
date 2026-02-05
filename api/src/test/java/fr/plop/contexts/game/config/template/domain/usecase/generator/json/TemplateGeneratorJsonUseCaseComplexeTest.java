package fr.plop.contexts.game.config.template.domain.usecase.generator.json;

import com.fasterxml.jackson.core.JsonProcessingException;
import fr.plop.contexts.game.config.consequence.Consequence;
import fr.plop.contexts.game.config.map.domain.MapObject;
import fr.plop.contexts.game.config.scenario.domain.model.PossibilityTrigger;
import fr.plop.contexts.game.config.template.domain.model.Template;
import fr.plop.contexts.game.instance.time.GameInstanceTimeUnit;
import fr.plop.generic.enumerate.Priority;
import fr.plop.subs.i18n.domain.Language;
import org.assertj.core.data.Offset;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class TemplateGeneratorJsonUseCaseComplexeTest {

    private final TemplateGeneratorJsonUseCase generator = new TemplateGeneratorJsonUseCase();

    @Test
    public void testInlineChezWamGeneScriptOld() throws JsonProcessingException {
        Template template = generator.template(TemplateGeneratorRootParser.apply("""
                {
                  "code": "ChezWamGene",
                  "version": "1.0",
                  "duration": 15,
                  "board": {
                    "spaces": [
                      {
                        "label": "Bureau",
                        "priority": "HIGH",
                        "rectangles": [
                          { "bottomLeft": { "lat": 45.77806, "lng": 4.80351 }, "topRight": { "lat": 45.77820, "lng": 4.80367 } }
                        ]
                      }
                    ]
                  },
                  "scenario": {
                    "steps": [
                      {
                        "ref": "CHAPITRE_BUREAU",
                        "label": { "FR": "Le bureau (tutorial)", "EN": "The office (tutorial)" },
                        "targets": [
                          { "ref": "ALLER_BUREAU", "label": { "FR": "Aller dans le bureau", "EN": "Go to the office" } }
                        ],
                        "possibilities": [
                          {
                            "trigger": { "type": "AbsoluteTime", "value": "0" },
                            "consequences": [
                              { "type": "Alert", "metadata": { "value": { "FR": "Bienvenue dans le jeu !", "EN": "Welcome to the game !" } } }
                            ]
                          }
                        ]
                      }
                    ]
                  }
                }
                """));

        assertThat(template).isNotNull();
        //assertThat(template.code().value()).isEqualTo("CHEZWAMGENE");
        //assertThat(template.version()).isEqualTo("1.0");
        assertThat(template.maxDuration().toMinutes()).isEqualTo(15);

        assertThat(template.scenario()).satisfies(scenario -> assertThat(scenario.steps()).hasSize(1)
                .anySatisfy(step -> {
                    assertThat(step.label().value(Language.FR)).isEqualTo("Le bureau (tutorial)");
                    assertThat(step.label().value(Language.EN)).isEqualTo("The office (tutorial)");
                    assertThat(step.targets()).hasSize(1)
                            .anySatisfy(target -> {
                                assertThat(target.label().value(Language.FR)).isEqualTo("Aller dans le bureau");
                                assertThat(target.label().value(Language.EN)).isEqualTo("Go to the office");
                            });
                    assertThat(step.possibilities())
                            .hasSize(1)
                            .anySatisfy(possibility -> {
                                assertThat(possibility.trigger())
                                        .isInstanceOf(PossibilityTrigger.AbsoluteTime.class);
                                PossibilityTrigger.AbsoluteTime absoluteTime = (PossibilityTrigger.AbsoluteTime) possibility.trigger();
                                assertThat(absoluteTime.value()).isEqualTo(new GameInstanceTimeUnit(0));
                                assertThat(possibility.consequences())
                                        .hasSize(1)
                                        .anySatisfy(consequence -> {
                                            assertThat(consequence).isInstanceOf(Consequence.DisplayAlert.class);
                                            Consequence.DisplayAlert displayAlert = (Consequence.DisplayAlert) consequence;
                                            assertThat(displayAlert.value().value(Language.FR)).isEqualTo("Bienvenue dans le jeu !");
                                            assertThat(displayAlert.value().value(Language.EN)).isEqualTo("Welcome to the game !");
                                        });
                            });

                }));
    }

    @Test
    public void testInlineChezWamGeneScript() throws JsonProcessingException {
        Template template = generator.template(TemplateGeneratorRootParser.apply("""
                {
                  "code": "ChezWamGene",
                  "version": "1.0",
                  "duration": 15,
                  "scenario": {
                    "steps": [
                      {
                        "ref": "CHAPITRE_BUREAU",
                        "label": { "FR": "Le bureau (tutorial)", "EN": "The office (tutorial)" },
                        "targets": [
                          { "ref": "ALLER_BUREAU", "label": { "FR": "Aller dans le bureau", "EN": "Go to the office" } }
                        ],
                        "possibilities": [
                          {
                            "trigger": { "type": "AbsoluteTime", "value": "0" },
                            "consequences": [
                              { "type": "Alert", "metadata": { "value": { "FR": "Bienvenue dans le jeu !", "EN": "Welcome to the game !" } } }
                            ]
                          }
                        ]
                      }
                    ]
                  }
                }
                """));

        assertThat(template).isNotNull();
        //assertThat(template.code().value()).isEqualTo("CHEZWAMGENE");
        //assertThat(template.version()).isEqualTo("1.0");
        assertThat(template.maxDuration().toMinutes()).isEqualTo(15);

        assertThat(template.scenario()).satisfies(scenario -> assertThat(scenario.steps()).hasSize(1)
                .anySatisfy(step -> {
                    assertThat(step.label().value(Language.FR)).isEqualTo("Le bureau (tutorial)");
                    assertThat(step.label().value(Language.EN)).isEqualTo("The office (tutorial)");
                    assertThat(step.targets()).hasSize(1)
                            .anySatisfy(target -> {
                                assertThat(target.label().value(Language.FR)).isEqualTo("Aller dans le bureau");
                                assertThat(target.label().value(Language.EN)).isEqualTo("Go to the office");
                            });
                    assertThat(step.possibilities())
                            .hasSize(1)
                            .anySatisfy(possibility -> {
                                assertThat(possibility.trigger())
                                        .isInstanceOf(PossibilityTrigger.AbsoluteTime.class);
                                PossibilityTrigger.AbsoluteTime absoluteTime = (PossibilityTrigger.AbsoluteTime) possibility.trigger();
                                assertThat(absoluteTime.value()).isEqualTo(new GameInstanceTimeUnit(0));
                                assertThat(possibility.consequences())
                                        .hasSize(1)
                                        .anySatisfy(consequence -> {
                                            assertThat(consequence).isInstanceOf(Consequence.DisplayAlert.class);
                                            Consequence.DisplayAlert alert = (Consequence.DisplayAlert) consequence;
                                            assertThat(alert.value().value(Language.FR)).isEqualTo("Bienvenue dans le jeu !");
                                            assertThat(alert.value().value(Language.EN)).isEqualTo("Welcome to the game !");
                                        });
                            });

                }));
    }

    @Test
    public void testStepTargetConditionAndClickMapObject() throws JsonProcessingException {
        Template template = generator.template(TemplateGeneratorRootParser.apply("""
                {
                  "code": "TutorialGame",
                  "version": "1.0",
                  "duration": 30,
                  "board": {
                    "spaces": [
                      {
                        "ref": "Office",
                        "label": "Office",
                        "priority": "HIGH",
                        "rectangles": [
                          { "bottomLeft": { "lat": 45.77806, "lng": 4.80351 }, "topRight": { "lat": 45.77820, "lng": 4.80367 } }
                        ]
                      }
                    ]
                  },
                  "scenario": {
                    "steps": [
                      {
                        "ref": "TUTORIAL_STEP",
                        "label": { "FR": "Étape tutorial", "EN": "Tutorial step" },
                        "targets": [
                          { "ref": "ENTER_OFFICE", "label": { "FR": "Entrer dans le bureau", "EN": "Enter the office" } },
                          { "ref": "SEARCH_DESK", "label": { "FR": "Fouiller le bureau", "EN": "Search the desk" } }
                        ],
                        "possibilities": [
                          {
                            "trigger": { "type": "GoInSpace", "value": "Office" },
                            "consequences": [
                              { "type": "GoalTarget", "metadata": { "targetId": "ENTER_OFFICE", "state": "SUCCESS" } }
                            ]
                          },
                          {
                            "trigger": { "type": "ImageObjectClick", "value": "OBJECT_A" },
                            "condition": { "type": "StepTarget", "metadata": { "targetId": "ENTER_OFFICE" } },
                            "consequences": [
                              { "type": "GoalTarget", "metadata": { "targetId": "SEARCH_DESK", "state": "SUCCESS" } },
                              { "type": "Alert", "metadata": { "value": { "FR": "Vous avez fouillé le bureau avec succès !", "EN": "You searched the desk successfully!" } } }
                            ]
                          }
                        ]
                      }
                    ]
                  },
                  "maps": [
                    {
                      "priority": "LOWEST",
                      "image": { "type": "Asset", "value": "assets/office.png" },
                      "bounds": {
                        "bottomLeft": { "lat": 45.777, "lng": 4.803 },
                        "topRight": { "lat": 45.779, "lng": 4.804 }
                      },
                      "objects": [
                        {
                          "label": "Desk",
                          "position": { "lat": 45.7785, "lng": 4.8036 },
                          "priority": "HIGH",
                          "point": { "color": "" }
                        }
                      ]
                    }
                  ]
                }
                """));

        assertThat(template).isNotNull();
        //assertThat(template.code().value()).isEqualTo("TUTORIALGAME");
        //assertThat(template.version()).isEqualTo("1.0");
        assertThat(template.maxDuration().toMinutes()).isEqualTo(30);

        assertThat(template.scenario()).satisfies(scenario -> assertThat(scenario.steps()).hasSize(1)
                .anySatisfy(step -> {
                    assertThat(step.label().value(Language.FR)).isEqualTo("Étape tutorial");
                    assertThat(step.label().value(Language.EN)).isEqualTo("Tutorial step");

                    assertThat(step.targets()).hasSize(2);

                    assertThat(step.possibilities()).hasSize(2);
                }));

        // Test de la carte
        assertThat(template.map()).satisfies(mapConfig -> {
            assertThat(mapConfig).isNotNull();
            assertThat(mapConfig.items())
                    .hasSize(1)
                    .anySatisfy(item -> {
                        assertThat(item.priority()).isEqualTo(Priority.LOWEST);
                        assertThat(item.objects())
                                .hasSize(1)
                                .anySatisfy(obj -> {
                                    assertThat(obj).isInstanceOf(MapObject.PointMarker.class);
                                    assertThat(obj.position().lat().doubleValue()).isCloseTo(45.7785, Offset.offset(0.001));
                                    assertThat(obj.position().lng().doubleValue()).isCloseTo(4.8036, Offset.offset(0.001));
                                });
                    });
        });
    }
}

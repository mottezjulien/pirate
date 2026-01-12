package fr.plop.contexts.game.config.template.domain.usecase.generator.json;

import com.fasterxml.jackson.core.JsonProcessingException;
import fr.plop.contexts.game.config.condition.Condition;
import fr.plop.contexts.game.config.consequence.Consequence;
import fr.plop.contexts.game.config.scenario.domain.model.PossibilityTrigger;
import fr.plop.contexts.game.config.talk.domain.TalkItem;
import fr.plop.contexts.game.config.template.domain.model.Template;
import fr.plop.contexts.game.session.situation.domain.GameSessionSituation;
import fr.plop.contexts.game.session.time.GameSessionTimeUnit;
import fr.plop.subs.i18n.domain.Language;
import fr.plop.subs.image.Image;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class TemplateGeneratorJsonUseCaseTalkTest {

    private final TemplateGeneratorJsonUseCase generator = new TemplateGeneratorJsonUseCase();

    @Test
    public void oneSimpleTalk() throws JsonProcessingException {
        Template template = generator.apply("""
                {
                  "code": "TEST_DISCUSSION1",
                  "scenario": {
                    "steps": [
                      {
                        "label": { "FR": "Salut", "EN": "Hi" },
                        "possibilities": [
                          {
                            "trigger": { "type": "AbsoluteTime", "value": "0" },
                            "consequences": [
                              { "type": "Talk", "metadata": { "talkId": "TALK000" } }
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
                        "ref": "TALK000",
                        "value": { "FR": "Bonjour", "EN": "Hello" },
                        "character": { "character": "Bob", "image": "Happy" }
                      }
                    ]
                  }
                }
                """);
        
        assertThat(template).isNotNull();
        assertThat(template.code().value()).isEqualTo("TEST_DISCUSSION1");

        assertThat(template.talk().items())
                .hasSize(1)
                .anySatisfy(talkItem -> {
                    assertThat(talkItem.id()).isNotNull();
                    assertThat(talkItem).isInstanceOf(TalkItem.Simple.class);
                    assertThat(talkItem.value().resolve(new GameSessionSituation()).value(Language.FR)).isEqualTo("Bonjour");
                    assertThat(talkItem.value().resolve(new GameSessionSituation()).value(Language.EN)).isEqualTo("Hello");
                    assertThat(talkItem.character().name()).isEqualTo("Bob");
                    assertThat(talkItem.characterReference().image().value()).isEqualTo("bob-happy.jpg");
                    assertThat(talkItem.characterReference().image().type()).isEqualTo(Image.Type.ASSET);
                });

        assertThat(template.scenario()).satisfies(scenario -> assertThat(scenario.steps()).hasSize(1)
        .anySatisfy(step -> assertThat(step.possibilities())
            .hasSize(1)
            .anySatisfy(possibility -> {
                assertThat(possibility.trigger())
                        .isInstanceOf(PossibilityTrigger.AbsoluteTime.class);
                PossibilityTrigger.AbsoluteTime absoluteTime = (PossibilityTrigger.AbsoluteTime) possibility.trigger();
                assertThat(absoluteTime.value()).isEqualTo(new GameSessionTimeUnit(0));
                assertThat(possibility.consequences())
                    .hasSize(1)
                    .anySatisfy(consequence -> {
                        assertThat(consequence).isInstanceOf(Consequence.DisplayTalk.class);
                        Consequence.DisplayTalk displayTalk = (Consequence.DisplayTalk) consequence;
                        assertThat(displayTalk.talkId()).isEqualTo(template.talk().items().getFirst().id());
                    });
            })));
    }

    @Test
    public void oneContinueTalk() throws JsonProcessingException {
        Template template = generator.apply("""
                {
                  "code": "TEST_DISCUSSION2",
                  "scenario": {
                    "steps": [
                      {
                        "label": { "FR": "Salut", "EN": "Hi" },
                        "possibilities": [
                          {
                            "trigger": { "type": "AbsoluteTime", "value": "0" },
                            "consequences": [
                              { "type": "Talk", "metadata": { "talkId": "TALK001" } }
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
                      },
                      {
                        "ref": "Alice",
                        "images": [
                          { "ref": "alice-happy", "value": { "type": "Asset", "value": "alicehappy.jpg" } },
                          { "ref": "alice-sad", "value": { "type": "Web", "value": "www.toto.yo/alicesad.png" } }
                        ]
                      }
                    ],
                    "items": [
                      {
                        "ref": "TALK001",
                        "value": { "FR": "Pouet", "EN": "Pouet en anglais" },
                        "character": { "character": "Alice", "image": "alice-sad" },
                        "next": "TALK002"
                      },
                      {
                        "ref": "TALK002",
                        "value": { "FR": "La suite", "EN": "The next" },
                        "character": { "character": "Alice", "image": "alice-happy" }
                      }
                    ]
                  }
                }
                """);

        assertThat(template).isNotNull();
        assertThat(template.code().value()).isEqualTo("TEST_DISCUSSION2");

        assertThat(template.talk().items()).hasSize(2);

        TalkItem talkItem = template.talk().items().getFirst();
        assertThat(talkItem.id()).isNotNull();
        assertThat(talkItem).isInstanceOf(TalkItem.Continue.class);
        assertThat(talkItem.value().resolve(new GameSessionSituation()).value(Language.FR)).isEqualTo("Pouet");
        assertThat(talkItem.value().resolve(new GameSessionSituation()).value(Language.EN)).isEqualTo("Pouet en anglais");
        assertThat(talkItem.character().name()).isEqualTo("Alice");
        assertThat(talkItem.characterReference().image().value()).isEqualTo("www.toto.yo/alicesad.png");
        assertThat(talkItem.characterReference().image().type()).isEqualTo(Image.Type.WEB);
        TalkItem.Continue continueItem = (TalkItem.Continue) talkItem;
        assertThat(continueItem.nextId()).isEqualTo(template.talk().items().getLast().id());

        talkItem = template.talk().items().getLast();
        assertThat(talkItem.id()).isNotNull();
        assertThat(talkItem).isInstanceOf(TalkItem.Simple.class);
        assertThat(talkItem.value().resolve(new GameSessionSituation()).value(Language.FR)).isEqualTo("La suite");
        assertThat(talkItem.value().resolve(new GameSessionSituation()).value(Language.EN)).isEqualTo("The next");
        assertThat(talkItem.character().name()).isEqualTo("Alice");
        assertThat(talkItem.characterReference().image().value()).isEqualTo("alicehappy.jpg");
        assertThat(talkItem.characterReference().image().type()).isEqualTo(Image.Type.ASSET);

        assertThat(template.scenario()).satisfies(scenario -> assertThat(scenario.steps()).hasSize(1)
                .anySatisfy(step -> assertThat(step.possibilities())
                        .hasSize(1)
                        .anySatisfy(possibility -> {
                            assertThat(possibility.trigger())
                                    .isInstanceOf(PossibilityTrigger.AbsoluteTime.class);
                            PossibilityTrigger.AbsoluteTime absoluteTime = (PossibilityTrigger.AbsoluteTime) possibility.trigger();
                            assertThat(absoluteTime.value()).isEqualTo(new GameSessionTimeUnit(0));
                            assertThat(possibility.consequences())
                                    .hasSize(1)
                                    .anySatisfy(consequence -> {
                                        assertThat(consequence).isInstanceOf(Consequence.DisplayTalk.class);
                                        Consequence.DisplayTalk displayTalk = (Consequence.DisplayTalk) consequence;
                                        assertThat(displayTalk.talkId()).isEqualTo(template.talk().items().getFirst().id());
                                    });
                        })));
    }

    @Test
    public void oneOptionWithCondition() throws JsonProcessingException {
        Template template = generator.apply("""
                {
                  "code": "TEST_OPTION_CONDITION",
                  "scenario": {
                    "steps": [
                      {
                        "ref": "STEP_1",
                        "label": { "FR": "Etape 1", "EN": "Step 1" },
                        "targets": [
                          { "ref": "TARGET_1", "label": { "FR": "Objectif 1", "EN": "Goal 1" } }
                        ],
                        "possibilities": [
                          {
                            "trigger": { "type": "AbsoluteTime", "value": "0" },
                            "consequences": [
                              { "type": "Talk", "metadata": { "talkId": "TALK_OPTION" } }
                            ]
                          }
                        ]
                      }
                    ]
                  },
                  "talk": {
                    "characters": [
                      {
                        "ref": "Marcel",
                        "images": [
                          { "ref": "default", "value": { "type": "Asset", "value": "marcel.png" } }
                        ]
                      }
                    ],
                    "items": [
                      {
                        "ref": "TALK_OPTION",
                        "value": { "FR": "Que voulez-vous faire ?", "EN": "What do you want to do?" },
                        "character": { "character": "Marcel", "image": "default" },
                        "options": [
                          {
                            "ref": "OPT_ALWAYS",
                            "value": { "FR": "Option toujours visible", "EN": "Always visible option" }
                          },
                          {
                            "ref": "OPT_CONDITIONAL",
                            "value": { "FR": "Option conditionnelle", "EN": "Conditional option" },
                            "condition": { "type": "StepTarget", "metadata": { "targetId": "TARGET_1" } }
                          }
                        ]
                      }
                    ]
                  }
                }
                """);

        assertThat(template).isNotNull();
        assertThat(template.code().value()).isEqualTo("TEST_OPTION_CONDITION");

        // Get the target ID from the scenario to compare later
        var scenarioTarget = template.scenario().steps().getFirst().targets().getFirst();

        assertThat(template.talk().items())
                .hasSize(1)
                .first()
                .satisfies(talkItem -> {
                    assertThat(talkItem).isInstanceOf(TalkItem.Options.class);
                    TalkItem.Options optionsItem = (TalkItem.Options) talkItem;

                    assertThat(optionsItem.options().toList())
                            .hasSize(2);

                    // First option: no condition
                    TalkItem.Options.Option opt1 = optionsItem.options().toList().getFirst();
                    assertThat(opt1.value().value(Language.FR)).isEqualTo("Option toujours visible");
                    assertThat(opt1.optCondition()).isEmpty();

                    // Second option: with condition
                    TalkItem.Options.Option opt2 = optionsItem.options().toList().get(1);
                    assertThat(opt2.value().value(Language.FR)).isEqualTo("Option conditionnelle");
                    assertThat(opt2.optCondition()).isPresent();
                    assertThat(opt2.optCondition().get()).isInstanceOf(Condition.Target.class);
                    Condition.Target target = (Condition.Target) opt2.optCondition().get();
                    // The condition's targetId should match the scenario target's ID
                    assertThat(target.targetId()).isEqualTo(scenarioTarget.id());
                });
    }
}

package fr.plop.contexts.game.config.template.domain.usecase.generator.json;

import com.fasterxml.jackson.core.JsonProcessingException;
import fr.plop.contexts.game.config.condition.Condition;
import fr.plop.contexts.game.config.consequence.Consequence;
import fr.plop.contexts.game.config.scenario.domain.model.PossibilityTrigger;
import fr.plop.contexts.game.config.talk.domain.TalkItem;
import fr.plop.contexts.game.config.talk.domain.TalkItemNext;
import fr.plop.contexts.game.config.talk.domain.TalkItemOut;
import fr.plop.contexts.game.config.template.domain.model.Template;
import fr.plop.contexts.game.session.situation.domain.GameSessionSituation;
import fr.plop.contexts.game.session.time.GameSessionTimeUnit;
import fr.plop.subs.i18n.domain.Language;
import fr.plop.contexts.game.session.event.domain.GameEvent;
import fr.plop.generic.enumerate.EqualsOrDifferent;
import fr.plop.subs.image.Image;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class TemplateGeneratorJsonUseCaseTalkTest {

    private final TemplateGeneratorJsonUseCase generator = new TemplateGeneratorJsonUseCase();

    @Test
    public void talkWithConditionalValue() throws JsonProcessingException {
        Template template = generator.apply("""
                {
                  "code": "TEST_CONDITIONAL_VALUE",
                  "scenario": {
                    "steps": [
                      {
                        "ref": "STEP_1",
                        "label": { "FR": "Etape 1", "EN": "Step 1" },
                        "targets": [
                          { "ref": "TARGET_FOUND", "label": { "FR": "Objectif trouvé", "EN": "Goal found" } }
                        ],
                        "possibilities": [
                          {
                            "trigger": { "type": "AbsoluteTime", "value": "0" },
                            "consequences": [
                              { "type": "Talk", "metadata": { "talkRef": "TALK_CONDITIONAL" } }
                            ]
                          }
                        ]
                      }
                    ]
                  },
                  "talk": {
                    "characters": [
                      {
                        "ref": "Guide",
                        "images": [
                          { "ref": "default", "value": { "type": "Asset", "value": "guide.png" } }
                        ]
                      }
                    ],
                    "items": [
                      {
                        "ref": "TALK_CONDITIONAL",
                        "value": {
                          "default": { "FR": "Tu n'as pas encore trouvé l'indice.", "EN": "You haven't found the clue yet." },
                          "branches": [
                            {
                              "order": 1,
                              "condition": { "type": "StepTarget", "metadata": { "targetId": "TARGET_FOUND" } },
                              "value": { "FR": "Bravo, tu as trouvé l'indice !", "EN": "Well done, you found the clue!" }
                            }
                          ]
                        },
                        "character": { "character": "Guide", "image": "default" }
                      }
                    ]
                  }
                }
                """);

        assertThat(template).isNotNull();
        assertThat(template.code().value()).isEqualTo("TEST_CONDITIONAL_VALUE");

        // Get the target ID from the scenario
        var scenarioTarget = template.scenario().steps().getFirst().targets().getFirst();

        assertThat(template.talk().items())
                .hasSize(1)
                .first()
                .satisfies(talkItem -> {
                    // Verify it's a Conditional TalkOut
                    assertThat(talkItem.out()).isInstanceOf(TalkItemOut.Conditional.class);
                    TalkItemOut.Conditional conditional = (TalkItemOut.Conditional) talkItem.out();

                    // Verify default text
                    assertThat(conditional.defaultText().value(Language.FR)).isEqualTo("Tu n'as pas encore trouvé l'indice.");
                    assertThat(conditional.defaultText().value(Language.EN)).isEqualTo("You haven't found the clue yet.");

                    // Verify branches
                    assertThat(conditional.branches()).hasSize(1);
                    TalkItemOut.Conditional.Branch branch = conditional.branches().getFirst();
                    assertThat(branch.order()).isEqualTo(1);
                    assertThat(branch.condition()).isInstanceOf(Condition.Target.class);
                    Condition.Target targetCondition = (Condition.Target) branch.condition();
                    assertThat(targetCondition.targetId()).isEqualTo(scenarioTarget.id());
                    assertThat(branch.text().value(Language.FR)).isEqualTo("Bravo, tu as trouvé l'indice !");

                    // Test resolve() with empty situation (no target completed) -> should return default
                    GameSessionSituation emptySituation = new GameSessionSituation();
                    assertThat(talkItem.out().resolve(emptySituation).value(Language.FR))
                            .isEqualTo("Tu n'as pas encore trouvé l'indice.");

                    // Test resolve() with target completed -> should return branch text
                    GameSessionSituation situationWithTarget = new GameSessionSituation(
                            new GameSessionSituation.Board(),
                            new GameSessionSituation.Scenario(List.of(), List.of(scenarioTarget.id())),
                            new GameSessionSituation.Time()
                    );
                    assertThat(talkItem.out().resolve(situationWithTarget).value(Language.FR))
                            .isEqualTo("Bravo, tu as trouvé l'indice !");
                });
    }

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
                              { "type": "Talk", "metadata": { "talkRef": "TALK000" } }
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
                    assertThat(talkItem.isSimple()).isTrue();
                    assertThat(talkItem.out().resolve(new GameSessionSituation()).value(Language.FR)).isEqualTo("Bonjour");
                    assertThat(talkItem.out().resolve(new GameSessionSituation()).value(Language.EN)).isEqualTo("Hello");
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
                              { "type": "Talk", "metadata": { "talkRef": "TALK001" } }
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
        assertThat(talkItem.isContinue()).isTrue();
        assertThat(talkItem.out().resolve(new GameSessionSituation()).value(Language.FR)).isEqualTo("Pouet");
        assertThat(talkItem.out().resolve(new GameSessionSituation()).value(Language.EN)).isEqualTo("Pouet en anglais");
        assertThat(talkItem.character().name()).isEqualTo("Alice");
        assertThat(talkItem.characterReference().image().value()).isEqualTo("www.toto.yo/alicesad.png");
        assertThat(talkItem.characterReference().image().type()).isEqualTo(Image.Type.WEB);
        assertThat(talkItem.nextId()).isEqualTo(template.talk().items().getLast().id());

        talkItem = template.talk().items().getLast();
        assertThat(talkItem.id()).isNotNull();
        assertThat(talkItem.isSimple()).isTrue();
        assertThat(talkItem.out().resolve(new GameSessionSituation()).value(Language.FR)).isEqualTo("La suite");
        assertThat(talkItem.out().resolve(new GameSessionSituation()).value(Language.EN)).isEqualTo("The next");
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
                              { "type": "Talk", "metadata": { "talkRef": "TALK_OPTION" } }
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
                    assertThat(talkItem.isOptions()).isTrue();

                    assertThat(talkItem.options().options().toList())
                            .hasSize(2);

                    // First option: no condition
                    TalkItemNext.Options.Option opt1 = talkItem.options().options().toList().getFirst();
                    assertThat(opt1.value().value(Language.FR)).isEqualTo("Option toujours visible");
                    assertThat(opt1.optCondition()).isEmpty();

                    // Second option: with condition
                    TalkItemNext.Options.Option opt2 = talkItem.options().options().toList().get(1);
                    assertThat(opt2.value().value(Language.FR)).isEqualTo("Option conditionnelle");
                    assertThat(opt2.optCondition()).isPresent();
                    assertThat(opt2.optCondition().get()).isInstanceOf(Condition.Target.class);
                    Condition.Target target = (Condition.Target) opt2.optCondition().get();
                    // The condition's targetId should match the scenario target's ID
                    assertThat(target.targetId()).isEqualTo(scenarioTarget.id());
                });
    }

    @Test
    public void inputTextTalk() throws JsonProcessingException {
        Template template = generator.apply("""
                {
                  "code": "TEST_INPUT_TEXT",
                  "scenario": {
                    "steps": [
                      {
                        "ref": "STEP_1",
                        "label": { "FR": "Ouvrir le coffre", "EN": "Open the chest" },
                        "targets": [
                          { "ref": "TARGET_COFFRE", "label": { "FR": "Coffre ouvert", "EN": "Chest opened" } }
                        ],
                        "possibilities": [
                          {
                            "trigger": { "type": "TALKINPUTTEXT", "talkId": "TALK_COFFRE_SAISIE", "value": "862", "matchType": "EQUALS" },
                            "consequences": [
                              { "type": "Talk", "metadata": { "talkRef": "TALK_COFFRE_SUCCESS" } },
                              { "type": "GoalTarget", "metadata": { "targetId": "TARGET_COFFRE", "state": "success" } }
                            ]
                          },
                          {
                            "trigger": { "type": "TALKINPUTTEXT", "talkId": "TALK_COFFRE_SAISIE", "value": "862", "matchType": "DIFFERENT" },
                            "consequences": [
                              { "type": "Talk", "metadata": { "talkRef": "TALK_COFFRE_FAIL" } }
                            ]
                          }
                        ]
                      }
                    ]
                  },
                  "talk": {
                    "characters": [
                      {
                        "ref": "Coffre",
                        "images": [
                          { "ref": "default", "value": { "type": "Asset", "value": "coffre.png" } }
                        ]
                      }
                    ],
                    "items": [
                      {
                        "ref": "TALK_COFFRE_SAISIE",
                        "value": { "FR": "Entrez le code du coffre", "EN": "Enter the chest code" },
                        "character": { "character": "Coffre", "image": "default" },
                        "inputText": { "type": "NUMERIC", "size": 3 }
                      },
                      {
                        "ref": "TALK_COFFRE_SUCCESS",
                        "value": { "FR": "Le coffre s'ouvre !", "EN": "The chest opens!" },
                        "character": { "character": "Coffre", "image": "default" }
                      },
                      {
                        "ref": "TALK_COFFRE_FAIL",
                        "value": { "FR": "Le code est incorrect.", "EN": "Wrong code." },
                        "character": { "character": "Coffre", "image": "default" }
                      }
                    ]
                  }
                }
                """);

        assertThat(template).isNotNull();
        assertThat(template.code().value()).isEqualTo("TEST_INPUT_TEXT");

        // Verify talk items
        assertThat(template.talk().items()).hasSize(3);

        // Find the InputText talk item
        TalkItem inputTextItem = template.talk().items().stream()
                .filter(item -> item.next() instanceof TalkItemNext.InputText)
                .findFirst()
                .orElseThrow();

        assertThat(inputTextItem.out().resolve(new GameSessionSituation()).value(Language.FR))
                .isEqualTo("Entrez le code du coffre");

        // Verify InputText properties
        TalkItemNext.InputText inputText = (TalkItemNext.InputText) inputTextItem.next();
        assertThat(inputText.type()).isEqualTo(TalkItemNext.InputText.Type.NUMERIC);
        assertThat(inputText.optSize()).isPresent().contains(3);

        // Verify possibilities with TalkInputText triggers
        var possibilities = template.scenario().steps().getFirst().possibilities();
        assertThat(possibilities).hasSize(2);

        // First possibility: EQUALS trigger
        var possibilityEquals = possibilities.get(0);
        assertThat(possibilityEquals.trigger()).isInstanceOf(PossibilityTrigger.TalkInputText.class);
        PossibilityTrigger.TalkInputText triggerEquals = (PossibilityTrigger.TalkInputText) possibilityEquals.trigger();
        assertThat(triggerEquals.talkId()).isEqualTo(inputTextItem.id());
        assertThat(triggerEquals.value()).isEqualTo("862");
        assertThat(triggerEquals.matchType()).isEqualTo(EqualsOrDifferent.EQUALS);

        // Second possibility: DIFFERENT trigger
        var possibilityDifferent = possibilities.get(1);
        assertThat(possibilityDifferent.trigger()).isInstanceOf(PossibilityTrigger.TalkInputText.class);
        PossibilityTrigger.TalkInputText triggerDifferent = (PossibilityTrigger.TalkInputText) possibilityDifferent.trigger();
        assertThat(triggerDifferent.talkId()).isEqualTo(inputTextItem.id());
        assertThat(triggerDifferent.value()).isEqualTo("862");
        assertThat(triggerDifferent.matchType()).isEqualTo(EqualsOrDifferent.DIFFERENT);

        // Test trigger matching
        GameEvent.TalkInputText eventCorrect = new GameEvent.TalkInputText(inputTextItem.id(), "862");
        GameEvent.TalkInputText eventWrong = new GameEvent.TalkInputText(inputTextItem.id(), "123");

        // EQUALS trigger should match correct value
        assertThat(triggerEquals.accept(eventCorrect, List.of())).isTrue();
        assertThat(triggerEquals.accept(eventWrong, List.of())).isFalse();

        // DIFFERENT trigger should match wrong value
        assertThat(triggerDifferent.accept(eventCorrect, List.of())).isFalse();
        assertThat(triggerDifferent.accept(eventWrong, List.of())).isTrue();
    }
}

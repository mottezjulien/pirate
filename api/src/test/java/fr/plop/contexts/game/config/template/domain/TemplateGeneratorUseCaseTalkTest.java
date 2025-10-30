package fr.plop.contexts.game.config.template.domain;

import fr.plop.contexts.game.config.consequence.Consequence;
import fr.plop.contexts.game.config.scenario.domain.model.PossibilityTrigger;
import fr.plop.contexts.game.config.talk.domain.TalkItem;
import fr.plop.contexts.game.config.template.domain.model.Template;
import fr.plop.contexts.game.config.template.domain.usecase.TemplateGeneratorUseCase;
import fr.plop.contexts.game.session.time.GameSessionTimeUnit;
import fr.plop.subs.i18n.domain.Language;
import fr.plop.subs.image.Image;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class TemplateGeneratorUseCaseTalkTest {

    private final TemplateGeneratorUseCase generator = new TemplateGeneratorUseCase();

    @Test
    public void oneSimpleTalk() {
        String scriptContent = """
TEST_DISCUSSION1

--- Step
------ Possibility
--------- Trigger:ABSOLUTETIME:0
--------- Consequence:Talk:TALK000

--- Talk
------ Character
--------- Bob
------------ Happy:ASSET:bob-happy.jpg
------ Simple(ref TALK000):Bob:Happy
--------- FR:Bonjour
--------- EN:Hello
            """;
        
        TemplateGeneratorUseCase.Script script = new TemplateGeneratorUseCase.Script(scriptContent);
        Template template = generator.apply(script);
        
        assertThat(template).isNotNull();
        assertThat(template.code().value()).isEqualTo("TEST_DISCUSSION1");

        assertThat(template.talk().items())
                .hasSize(1)
                        .anySatisfy(talkItem -> {
                            assertThat(talkItem.id()).isNotNull();
                            assertThat(talkItem).isInstanceOf(TalkItem.Simple.class);
                            assertThat(talkItem.value().value(Language.FR)).isEqualTo("Bonjour");
                            assertThat(talkItem.value().value(Language.EN)).isEqualTo("Hello");
                            assertThat(talkItem.character().name()).isEqualTo("Bob");
                            assertThat(talkItem.character().image().path()).isEqualTo("bob-happy.jpg");
                            assertThat(talkItem.character().image().type()).isEqualTo(Image.Type.ASSET);
                        });


        assertThat(template.scenario()).satisfies(scenario -> {
           assertThat(scenario.steps()).hasSize(1)
           .anySatisfy(step -> {
              assertThat(step.possibilities())
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
                  });

           });
        });
    }

    @Test
    public void oneContinueTalk() {
        String scriptContent = """
TEST_DISCUSSION2

--- Step
------ Possibility
--------- Trigger:ABSOLUTETIME:0
--------- Consequence:Talk:TALK001

--- Talk
------ Character
--------- Bob
------------ Happy:ASSET:bob-happy.jpg
--------- Alice
------------ alice-happy:ASSET:alicehappy.jpg
------------ alice-sad:WEB:www.toto.yo/alicesad.png
------ Continue(ref TALK001):TALK002
--------- Character:Alice:alice-sad
--------- FR:Pouet
--------- EN:Pouet en anglais
------ Simple(ref TALK002)
--------- Character:Alice:alice-happy
--------- FR:La suite
--------- EN:The next
            """;

        TemplateGeneratorUseCase.Script script = new TemplateGeneratorUseCase.Script(scriptContent);
        Template template = generator.apply(script);

        assertThat(template).isNotNull();
        assertThat(template.code().value()).isEqualTo("TEST_DISCUSSION2");

        assertThat(template.talk().items()).hasSize(2);

        TalkItem talkItem = template.talk().items().getFirst();
        assertThat(talkItem.id()).isNotNull();
        assertThat(talkItem).isInstanceOf(TalkItem.Continue.class);
        assertThat(talkItem.value().value(Language.FR)).isEqualTo("Pouet");
        assertThat(talkItem.value().value(Language.EN)).isEqualTo("Pouet en anglais");
        assertThat(talkItem.character().name()).isEqualTo("Alice");
        assertThat(talkItem.character().image().path()).isEqualTo("www.toto.yo/alicesad.png");
        assertThat(talkItem.character().image().type()).isEqualTo(Image.Type.WEB);
        TalkItem.Continue continueItem = (TalkItem.Continue) talkItem;
        assertThat(continueItem.nextId()).isEqualTo(template.talk().items().getLast().id());

        talkItem = template.talk().items().getLast();
        assertThat(talkItem.id()).isNotNull();
        assertThat(talkItem).isInstanceOf(TalkItem.Simple.class);
        assertThat(talkItem.value().value(Language.FR)).isEqualTo("La suite");
        assertThat(talkItem.value().value(Language.EN)).isEqualTo("The next");
        assertThat(talkItem.character().name()).isEqualTo("Alice");
        assertThat(talkItem.character().image().path()).isEqualTo("alicehappy.jpg");
        assertThat(talkItem.character().image().type()).isEqualTo(Image.Type.ASSET);


        assertThat(template.scenario()).satisfies(scenario -> {
            assertThat(scenario.steps()).hasSize(1)
                    .anySatisfy(step -> {
                        assertThat(step.possibilities())
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
                                });

                    });
        });
    }

    @Test
    public void oneOptionsTalk() {

        String scriptContent = """
TEST_DISCUSSION3
--- Step
------ Possibility
--------- Trigger:ABSOLUTETIME:0
--------- Consequence:Talk:TALK001
------ Possibility
--------- Trigger:SelectTalkOption:WAHUP_YES
--------- Consequence:Alert:TALK001
------------ FR:Tu vas bien !!
------------ EN:You're fine !!

--- Talk
------ Character
--------- Bob
------------ Happy:ASSET:bob-happy.jpg
--------- Alice
------------ alice-happy:ASSET:alicehappy.jpg
------------ alice-sad:WEB:www.toto.yo/alicesad.png
------ Options(ref TALK001)
--------- Character:Alice:alice-sad
--------- Label
------------ FR:Ca va ?
------------ EN:Wahup ?
--------- Option (ref WAHUP_YES)
------------ value
--------------- FR:Oui
--------------- EN:Yes
------------ next:TALK002
--------- Option (ref WAHUP_NO)
------------ value
--------------- FR:Non
--------------- EN:No
------ Simple(ref TALK002)
--------- Character:Alice:alice-happy
--------- FR:Content de le savoir
--------- EN:Happy to know
            """;

        TemplateGeneratorUseCase.Script script = new TemplateGeneratorUseCase.Script(scriptContent);
        Template template = generator.apply(script);

        assertThat(template).isNotNull();
        assertThat(template.code().value()).isEqualTo("TEST_DISCUSSION3");

        assertThat(template.talk().items()).hasSize(2);
        TalkItem firstTalkItem = template.talk().items().getFirst();
        TalkItem lastTalkItem = template.talk().items().getLast();

        // First item: Options
        assertThat(firstTalkItem.id()).isNotNull();
        assertThat(firstTalkItem).isInstanceOf(TalkItem.Options.class);
        assertThat(firstTalkItem.value().value(Language.FR)).isEqualTo("Ca va ?");
        assertThat(firstTalkItem.value().value(Language.EN)).isEqualTo("Wahup ?");
        assertThat(firstTalkItem.character().name()).isEqualTo("Alice");
        assertThat(firstTalkItem.character().image().path()).isEqualTo("www.toto.yo/alicesad.png");
        assertThat(firstTalkItem.character().image().type()).isEqualTo(Image.Type.WEB);
        TalkItem.Options optionsItem = (TalkItem.Options) firstTalkItem;

        assertThat(optionsItem.options()).hasSize(2);

        TalkItem.Options.Option optionYes = optionsItem.options().getFirst();
        TalkItem.Options.Option optionNo = optionsItem.options().getLast();

        assertThat(optionYes.id()).isNotNull();
        assertThat(optionYes.value().value(Language.FR)).isEqualTo("Oui");
        assertThat(optionYes.value().value(Language.EN)).isEqualTo("Yes");
        assertThat(optionYes.hasNext()).isTrue();
        assertThat(optionYes.nextId()).isEqualTo(lastTalkItem.id());

        assertThat(optionNo.id()).isNotNull();
        assertThat(optionNo.value().value(Language.FR)).isEqualTo("Non");
        assertThat(optionNo.value().value(Language.EN)).isEqualTo("No");
        assertThat(optionNo.hasNext()).isFalse();

        // Last item: Simple
        assertThat(lastTalkItem.id()).isNotNull();
        assertThat(lastTalkItem).isInstanceOf(TalkItem.Simple.class);
        assertThat(lastTalkItem.value().value(Language.FR)).isEqualTo("Content de le savoir");
        assertThat(lastTalkItem.value().value(Language.EN)).isEqualTo("Happy to know");
        assertThat(lastTalkItem.character().name()).isEqualTo("Alice");
        assertThat(lastTalkItem.character().image().path()).isEqualTo("alicehappy.jpg");
        assertThat(lastTalkItem.character().image().type()).isEqualTo(Image.Type.ASSET);

        // Scenario assertions
        assertThat(template.scenario().steps()).hasSize(1)
        .anySatisfy(step -> assertThat(step.possibilities())
                .hasSize(2)
                .anySatisfy(possibility -> {
                    assertThat(possibility.trigger())
                            .isInstanceOf(PossibilityTrigger.AbsoluteTime.class);
                    PossibilityTrigger.AbsoluteTime trigger = (PossibilityTrigger.AbsoluteTime) possibility.trigger();
                    assertThat(trigger.value()).isEqualTo(new GameSessionTimeUnit(0));
                    assertThat(possibility.consequences())
                            .hasSize(1)
                            .anySatisfy(consequence -> {
                                assertThat(consequence).isInstanceOf(Consequence.DisplayTalk.class);
                                Consequence.DisplayTalk displayTalk = (Consequence.DisplayTalk) consequence;
                                assertThat(displayTalk.talkId()).isEqualTo(firstTalkItem.id());
                            });
                })
                .anySatisfy(possibility -> {
                    assertThat(possibility.trigger())
                            .isInstanceOf(PossibilityTrigger.SelectTalkOption.class);
                    PossibilityTrigger.SelectTalkOption trigger = (PossibilityTrigger.SelectTalkOption) possibility.trigger();
                    assertThat(trigger.talkId()).isEqualTo(firstTalkItem.id());
                    assertThat(trigger.optionId()).isEqualTo(optionYes.id());
                    assertThat(possibility.consequences())
                            .hasSize(1)
                            .anySatisfy(consequence -> {
                                assertThat(consequence).isInstanceOf(Consequence.DisplayMessage.class);
                                Consequence.DisplayMessage displayTalk = (Consequence.DisplayMessage) consequence;
                                assertThat(displayTalk.value().value(Language.FR)).isEqualTo("Tu vas bien !!");
                                assertThat(displayTalk.value().value(Language.EN)).isEqualTo("You're fine !!");
                            });
                }));
    }




/*

    @Test
    public void testInlineChezWamGeneScriptOld() {
        // Version simplifiée pour tester sans dépendance au fichier
        String scriptContent = """
TEST_DISCUSSION

--- Step
------ Possibility
--------- Trigger:ABSOLUTETIME:0
--------- Consequence:Talk:TALK_LETS_GO

--- Talk
------ Options(ref TALK_LETS_GO)
--------- Label
------------ FR:Tu veux jouer ?
------------ EN:Do you want to play?
--------- Option:TALK_LETS_GO_CHOIX_OUI
------------ FR:Oui
------------ EN:Yes
--------- Option (ref TALK_LETS_GO_CHOIX_NON)
------------ FR:Non
------------ EN:No
------ Simple(ref TALK_LETS_GO_CHOIX_OUI)
--------- FR:Excellent, continuons !
--------- EN:Great, let's continue!
------ Simple (ref TALK_LETS_GO_CHOIX_NON)
--------- FR:Excellent, continuons !
--------- EN:Great, let's continue!
            """;

        TemplateGeneratorUseCase.Script script = new TemplateGeneratorUseCase.Script(scriptContent);
        Template template = generator.apply(script);

        assertThat(template).isNotNull();
        assertThat(template.code().value()).isEqualTo("ChezWamGene");
        assertThat(template.version()).isEqualTo("1.0");
        assertThat(template.maxDuration().toMinutes()).isEqualTo(15);

        assertThat(template.scenario()).satisfies(scenario -> {
            assertThat(scenario.steps()).hasSize(1)
                    .anySatisfy(step -> {
                        assertThat(step.label().orElseThrow().value(Language.FR)).isEqualTo("Le bureau (tutorial)");
                        assertThat(step.label().orElseThrow().value(Language.EN)).isEqualTo("The office (tutorial)");
                        assertThat(step.targets()).hasSize(1)
                                .anySatisfy(target -> {
                                    assertThat(target.label().orElseThrow().value(Language.FR)).isEqualTo("Aller dans le bureau");
                                    assertThat(target.label().orElseThrow().value(Language.EN)).isEqualTo("Go to the office");
                                });
                        assertThat(step.possibilities())
                                .hasSize(1)
                                .anySatisfy(possibility -> {
                                    assertThat(possibility.trigger())
                                            .isInstanceOf(PossibilityTrigger.AbsoluteTime.class);
                                    PossibilityTrigger.AbsoluteTime absoluteTime = (PossibilityTrigger.AbsoluteTime) possibility.trigger();
                                    assertThat(absoluteTime.value()).isEqualTo(new GameSessionTimeUnit(0));
                                    assertThat(possibility.consequences())
                                            .hasSize(1)
                                            .anySatisfy(consequence -> {
                                                assertThat(consequence).isInstanceOf(Consequence.DisplayMessage.class);
                                                Consequence.DisplayMessage displayMessage = (Consequence.DisplayMessage) consequence;
                                                assertThat(displayMessage.value().value(Language.FR)).isEqualTo("Bienvenue dans le jeu !");
                                                assertThat(displayMessage.value().value(Language.EN)).isEqualTo("Welcome to the game !");
                                            });
                                });

                    });
        });
    }*/

}
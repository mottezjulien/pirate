package fr.plop.contexts.game.config.template.domain;

import fr.plop.contexts.game.config.consequence.Consequence;
import fr.plop.contexts.game.config.scenario.domain.model.PossibilityTrigger;
import fr.plop.contexts.game.config.talk.domain.TalkItem;
import fr.plop.contexts.game.config.template.domain.model.Template;
import fr.plop.contexts.game.config.template.domain.usecase.generator.TemplateGeneratorUseCase;
import fr.plop.contexts.game.session.time.GameSessionTimeUnit;
import fr.plop.subs.i18n.domain.Language;
import fr.plop.subs.image.Image;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

import fr.plop.contexts.game.session.core.domain.model.SessionGameOver;

import java.util.List;

public class TemplateGeneratorUseCaseTalkTest {

    private final TemplateGeneratorUseCase generator = new TemplateGeneratorUseCase();

    @Test
    public void oneSimpleTalk() {
        String scriptContent = """
TEST_DISCUSSION1

--- Step:FR:Salut:EN:Hi
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
    public void oneContinueTalk() {
        String scriptContent = """
TEST_DISCUSSION2

--- Step:FR:Salut:EN:Hi
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
        assertThat(talkItem.characterReference().image().value()).isEqualTo("www.toto.yo/alicesad.png");
        assertThat(talkItem.characterReference().image().type()).isEqualTo(Image.Type.WEB);
        TalkItem.Continue continueItem = (TalkItem.Continue) talkItem;
        assertThat(continueItem.nextId()).isEqualTo(template.talk().items().getLast().id());

        talkItem = template.talk().items().getLast();
        assertThat(talkItem.id()).isNotNull();
        assertThat(talkItem).isInstanceOf(TalkItem.Simple.class);
        assertThat(talkItem.value().value(Language.FR)).isEqualTo("La suite");
        assertThat(talkItem.value().value(Language.EN)).isEqualTo("The next");
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
    public void oneOptionsTalk() {

        String scriptContent = """
TEST_DISCUSSION3
--- Step:FR:Salut:EN:Hi
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
--------- Option (ref WAHUP_MAYBE)
------------ value
--------------- FR:peut être
--------------- EN:may be
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
        assertThat(firstTalkItem.characterReference().image().value()).isEqualTo("www.toto.yo/alicesad.png");
        assertThat(firstTalkItem.characterReference().image().type()).isEqualTo(Image.Type.WEB);
        TalkItem.Options optionsItem = (TalkItem.Options) firstTalkItem;

        List<TalkItem.Options.Option> options = optionsItem.options().toList();
        assertThat(options).hasSize(3);

        TalkItem.Options.Option optionYes = options.getFirst();
        TalkItem.Options.Option optionNo = options.get(1);
        TalkItem.Options.Option optionMayBe = options.get(2);

        assertThat(optionYes.id()).isNotNull();
        assertThat(optionYes.value().value(Language.FR)).isEqualTo("Oui");
        assertThat(optionYes.value().value(Language.EN)).isEqualTo("Yes");
        assertThat(optionYes.hasNext()).isTrue();
        assertThat(optionYes.nextId()).isEqualTo(lastTalkItem.id());

        assertThat(optionNo.id()).isNotNull();
        assertThat(optionNo.value().value(Language.FR)).isEqualTo("Non");
        assertThat(optionNo.value().value(Language.EN)).isEqualTo("No");
        assertThat(optionNo.hasNext()).isFalse();

        assertThat(optionMayBe.id()).isNotNull();
        assertThat(optionMayBe.value().value(Language.FR)).isEqualTo("peut être");
        assertThat(optionMayBe.value().value(Language.EN)).isEqualTo("may be");
        assertThat(optionMayBe.hasNext()).isFalse();

        // Last item: Simple
        assertThat(lastTalkItem.id()).isNotNull();
        assertThat(lastTalkItem).isInstanceOf(TalkItem.Simple.class);
        assertThat(lastTalkItem.value().value(Language.FR)).isEqualTo("Content de le savoir");
        assertThat(lastTalkItem.value().value(Language.EN)).isEqualTo("Happy to know");
        assertThat(lastTalkItem.character().name()).isEqualTo("Alice");
        assertThat(lastTalkItem.characterReference().image().value()).isEqualTo("alicehappy.jpg");
        assertThat(lastTalkItem.characterReference().image().type()).isEqualTo(Image.Type.ASSET);

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
                            .isInstanceOf(PossibilityTrigger.TalkOptionSelect.class);
                    PossibilityTrigger.TalkOptionSelect trigger = (PossibilityTrigger.TalkOptionSelect) possibility.trigger();
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


    @Test
    public void fullTemplate() {
        String scriptContent = """
            TEST_DISCUSSION4
            --- Step (ref ETAPE_DEBUT):FR:Démarrage:EN:Start
            ------ Possibility
            --------- Trigger:ABSOLUTETIME:0
            --------- Consequence:Talk:TALK_LETS_GO
            
            ------ Possibility
            --------- Trigger:TalkEnd:TALK002
            --------- Consequence:GameOver:SUCCESS_ONE_CONTINUE
            
            ------ Possibility
            --------- Trigger:TalkOptionSelect:TALK_LETS_GO_CHOIX_NON
            --------- Consequence:GameOver:FAILURE_ONE_CONTINUE
            
            --- Talk
            ------ Character
            --------- Bob
            ------------ default:ASSET:/pouet/bobdefault.jpg
            ------ Options(ref TALK_LETS_GO)
            --------- Character:Bob:default
            --------- Label
            ------------ FR:Tu veux jouer ?
            ------------ EN:Do you want to play?
            --------- Option (ref TALK_LETS_GO_CHOIX_OUI)
            ------------ FR:Oui
            ------------ EN:Yes
            ------------ next:TALK002
            --------- Option (ref TALK_LETS_GO_CHOIX_NON)
            ------------ next:TALK003
            ------------ FR:Non
            ------------ EN:No
            --------- Option (ref TALK_LETS_GO_CHOIX_MAYBE):TALK004
            ------------ FR:Peut être
            ------------ EN:May be
            ------------ next:TALK004
            ------ Simple(ref TALK002)
            --------- Character:Bob:default
            --------- FR:Excellent, continuons !
            --------- EN:Great, let's continue!
            ------ Simple(ref TALK003)
            --------- Character:Bob:default
            --------- FR:Dommage, fin de test.
            --------- EN:Too bad, end of test.
            ------ Continue (ref TALK004):TALK005
            --------- Character:Bob:default
            --------- FR:Alors, on hésite ?
            --------- EN:Alors, on hésite ? EN
            ------ Simple(ref TALK005)
            --------- Character:Bob:default
            --------- FR:fin de la discution.
            --------- EN:fin de la discution. EN
            """;

        TemplateGeneratorUseCase.Script script = new TemplateGeneratorUseCase.Script(scriptContent);
        Template template = generator.apply(script);

        assertThat(template).isNotNull();
        assertThat(template.code().value()).isEqualTo("TEST_DISCUSSION4");

        assertThat(template.talk().items()).hasSize(5);
        TalkItem talkItem0 = template.talk().items().getFirst();
        TalkItem talkItem1 = template.talk().items().get(1);
        TalkItem talkItem2 = template.talk().items().get(2);
        TalkItem talkItem3 = template.talk().items().get(3);
        TalkItem talkItem4 = template.talk().items().get(4);

        // First item: Options
        assertThat(talkItem0.id()).isNotNull();
        assertThat(talkItem0).isInstanceOf(TalkItem.Options.class);
        assertThat(talkItem0.value().value(Language.FR)).isEqualTo("Tu veux jouer ?");
        assertThat(talkItem0.value().value(Language.EN)).isEqualTo("Do you want to play?");
        assertThat(talkItem0.character().name()).isEqualTo("Bob");
        assertThat(talkItem0.characterReference().image().value()).isEqualTo("/pouet/bobdefault.jpg");
        assertThat(talkItem0.characterReference().image().type()).isEqualTo(Image.Type.ASSET);
        TalkItem.Options optionsItem = (TalkItem.Options) talkItem0;

        List<TalkItem.Options.Option> options = optionsItem.options().toList();
        assertThat(options).hasSize(3);

        TalkItem.Options.Option optionYes = options.getFirst();
        TalkItem.Options.Option optionNo = options.get(1);
        TalkItem.Options.Option optionMayBe = options.get(2);

        assertThat(optionYes.id()).isNotNull();
        assertThat(optionYes.value().value(Language.FR)).isEqualTo("Oui");
        assertThat(optionYes.value().value(Language.EN)).isEqualTo("Yes");
        assertThat(optionYes.hasNext()).isTrue();
        assertThat(optionYes.nextId()).isEqualTo(talkItem1.id());

        assertThat(optionNo.id()).isNotNull();
        assertThat(optionNo.value().value(Language.FR)).isEqualTo("Non");
        assertThat(optionNo.value().value(Language.EN)).isEqualTo("No");
        assertThat(optionNo.hasNext()).isTrue();
        assertThat(optionNo.nextId()).isEqualTo(talkItem2.id());

        assertThat(optionMayBe.id()).isNotNull();
        assertThat(optionMayBe.value().value(Language.FR)).isEqualTo("Peut être");
        assertThat(optionMayBe.value().value(Language.EN)).isEqualTo("May be");
        assertThat(optionMayBe.hasNext()).isTrue();
        assertThat(optionMayBe.nextId()).isEqualTo(talkItem3.id());

        assertThat(talkItem1.id()).isNotNull();
        assertThat(talkItem1).isInstanceOf(TalkItem.Simple.class);
        assertThat(talkItem1.value().value(Language.FR)).isEqualTo("Excellent, continuons !");
        assertThat(talkItem1.value().value(Language.EN)).isEqualTo("Great, let's continue!");
        assertThat(talkItem1.character().name()).isEqualTo("Bob");
        assertThat(talkItem1.characterReference().image().value()).isEqualTo("/pouet/bobdefault.jpg");
        assertThat(talkItem1.characterReference().image().type()).isEqualTo(Image.Type.ASSET);

        assertThat(talkItem2.id()).isNotNull();
        assertThat(talkItem2).isInstanceOf(TalkItem.Simple.class);
        assertThat(talkItem2.value().value(Language.FR)).isEqualTo("Dommage, fin de test.");
        assertThat(talkItem2.value().value(Language.EN)).isEqualTo("Too bad, end of test.");
        assertThat(talkItem2.character().name()).isEqualTo("Bob");
        assertThat(talkItem2.characterReference().image().value()).isEqualTo("/pouet/bobdefault.jpg");
        assertThat(talkItem2.characterReference().image().type()).isEqualTo(Image.Type.ASSET);

        assertThat(talkItem3.id()).isNotNull();
        assertThat(talkItem3).isInstanceOf(TalkItem.Continue.class);
        assertThat(talkItem3.value().value(Language.FR)).isEqualTo("Alors, on hésite ?");
        assertThat(talkItem3.value().value(Language.EN)).isEqualTo("Alors, on hésite ? EN");
        assertThat(talkItem3.character().name()).isEqualTo("Bob");
        assertThat(talkItem3.characterReference().image().value()).isEqualTo("/pouet/bobdefault.jpg");
        assertThat(talkItem3.characterReference().image().type()).isEqualTo(Image.Type.ASSET);
        TalkItem.Continue continueItem = (TalkItem.Continue) talkItem3;
        assertThat(continueItem.nextId()).isEqualTo(talkItem4.id());

        assertThat(talkItem4.id()).isNotNull();
        assertThat(talkItem4).isInstanceOf(TalkItem.Simple.class);
        assertThat(talkItem4.value().value(Language.FR)).isEqualTo("fin de la discution.");
        assertThat(talkItem4.value().value(Language.EN)).isEqualTo("fin de la discution. EN");
        assertThat(talkItem4.character().name()).isEqualTo("Bob");
        assertThat(talkItem4.characterReference().image().value()).isEqualTo("/pouet/bobdefault.jpg");
        assertThat(talkItem4.characterReference().image().type()).isEqualTo(Image.Type.ASSET);

        // Scenario assertions
        assertThat(template.scenario().steps()).hasSize(1)
                .anySatisfy(step -> assertThat(step.possibilities())
                        .hasSize(3)
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
                                        assertThat(displayTalk.talkId()).isEqualTo(talkItem0.id());
                                    });
                        })
                        .anySatisfy(possibility -> {
                            assertThat(possibility.trigger())
                                    .isInstanceOf(PossibilityTrigger.TalkOptionSelect.class);
                            PossibilityTrigger.TalkOptionSelect trigger = (PossibilityTrigger.TalkOptionSelect) possibility.trigger();
                            assertThat(trigger.talkId()).isEqualTo(talkItem0.id());
                            assertThat(trigger.optionId()).isEqualTo(optionNo.id());
                            assertThat(possibility.consequences())
                                    .hasSize(1)
                                    .anySatisfy(consequence -> {
                                        assertThat(consequence).isInstanceOf(Consequence.SessionEnd.class);
                                        Consequence.SessionEnd sessionEnd = (Consequence.SessionEnd) consequence;
                                        assertThat(sessionEnd.gameOver().type()).isEqualTo(SessionGameOver.Type.FAILURE_ONE_CONTINUE);
                                    });
                        })
                        .anySatisfy(possibility -> {
                            assertThat(possibility.trigger())
                                    .isInstanceOf(PossibilityTrigger.TalkEnd.class);
                            PossibilityTrigger.TalkEnd trigger = (PossibilityTrigger.TalkEnd) possibility.trigger();
                            assertThat(trigger.talkId()).isEqualTo(talkItem1.id());
                            assertThat(possibility.consequences())
                                    .hasSize(1)
                                    .anySatisfy(consequence -> {
                                        assertThat(consequence).isInstanceOf(Consequence.SessionEnd.class);
                                        Consequence.SessionEnd sessionEnd = (Consequence.SessionEnd) consequence;
                                        assertThat(sessionEnd.gameOver().type()).isEqualTo(SessionGameOver.Type.SUCCESS_ONE_CONTINUE);
                                    });
                        }));
    }

}
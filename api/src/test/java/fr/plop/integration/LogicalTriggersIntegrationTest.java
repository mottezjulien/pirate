package fr.plop.integration;

import fr.plop.contexts.connect.domain.ConnectAuthGameSession;
import fr.plop.contexts.connect.domain.ConnectAuthUser;
import fr.plop.contexts.connect.domain.ConnectException;
import fr.plop.contexts.connect.usecase.ConnectAuthGameSessionUseCase;
import fr.plop.contexts.connect.usecase.ConnectAuthUserCreateUseCase;
import fr.plop.contexts.game.config.board.domain.model.BoardConfig;
import fr.plop.contexts.game.config.board.domain.model.BoardSpace;
import fr.plop.contexts.game.config.consequence.Consequence;
import fr.plop.contexts.game.config.scenario.domain.model.Possibility;
import fr.plop.contexts.game.config.scenario.domain.model.PossibilityTrigger;
import fr.plop.contexts.game.config.scenario.domain.model.ScenarioConfig;
import fr.plop.contexts.game.config.talk.domain.TalkCharacter;
import fr.plop.contexts.game.config.talk.domain.TalkConfig;
import fr.plop.contexts.game.config.talk.domain.TalkItem;
import fr.plop.contexts.game.config.talk.domain.TalkItemNext;
import fr.plop.contexts.game.config.talk.domain.TalkItemOut;
import fr.plop.contexts.game.config.template.domain.model.Template;
import fr.plop.contexts.game.config.template.domain.usecase.TemplateInitUseCase;
import fr.plop.contexts.game.session.core.domain.GameException;
import fr.plop.contexts.game.session.core.domain.model.GameSessionContext;
import fr.plop.contexts.game.session.core.domain.port.GameSessionClearPort;
import fr.plop.contexts.game.session.core.domain.usecase.GameSessionStartUseCase;
import fr.plop.contexts.game.session.core.domain.usecase.GameSessionUseCase;
import fr.plop.contexts.game.session.event.domain.GameEvent;
import fr.plop.contexts.game.session.event.domain.GameEventOrchestrator;
import fr.plop.contexts.game.session.scenario.domain.model.ScenarioSessionState;
import fr.plop.contexts.game.session.situation.domain.port.GameSessionSituationGetPort;
import fr.plop.generic.position.Point;
import fr.plop.generic.position.Rectangle;
import fr.plop.subs.i18n.domain.I18n;
import fr.plop.subs.i18n.domain.Language;
import fr.plop.subs.image.Image;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for logical triggers (And, Or, Not) with GameEventOrchestrator.
 *
 * Tests the scenario:
 * - Sophie asks for a painter's name
 * - "Golu" -> SUCCESS (ALMOST_EQUALS)
 * - "Losof" OR "Celle" -> WRONG_ARTIST (OR trigger)
 * - Anything else that is NOT similar to "Golu" -> UNKNOWN (NOT + ALMOST_EQUALS)
 */
@SpringBootTest
public class LogicalTriggersIntegrationTest {

    @Autowired
    private GameEventOrchestrator eventOrchestrator;

    @Autowired
    private GameSessionClearPort sessionClear;

    @Autowired
    private TemplateInitUseCase.OutPort templatePort;

    @Autowired
    private ConnectAuthUserCreateUseCase createAuthUseCase;

    @Autowired
    private GameSessionUseCase createUseCase;

    @Autowired
    private ConnectAuthGameSessionUseCase authGameSessionUseCase;

    @Autowired
    private GameSessionStartUseCase startUseCase;

    @Autowired
    private GameSessionSituationGetPort situationPort;

    private final Template.Id templateId = new Template.Id();
    private final ScenarioConfig.Step.Id stepId = new ScenarioConfig.Step.Id();
    private final ScenarioConfig.Target.Id targetSuccess = new ScenarioConfig.Target.Id();
    private final ScenarioConfig.Target.Id targetWrongArtist = new ScenarioConfig.Target.Id();
    private final ScenarioConfig.Target.Id targetUnknown = new ScenarioConfig.Target.Id();
    private final TalkItem.Id talkInputId = new TalkItem.Id();

    @BeforeEach
    void setUp() {
        sessionClear.clearAll();
        templatePort.deleteAll();
        createTemplate();
    }

    private void createTemplate() {
        // Board (required but not used in this test)
        BoardSpace.Id spaceId = new BoardSpace.Id();
        BoardSpace space = new BoardSpace(spaceId, List.of(new Rectangle(Point.from(0, 0), Point.from(10, 10))));

        // Talk item with InputText
        TalkCharacter sophie = new TalkCharacter(new TalkCharacter.Id(), "Sophie");
        Image sophieImage = new Image(Image.Type.ASSET, "sophie.png");
        TalkCharacter.Reference sophieRef = new TalkCharacter.Reference(new TalkCharacter.Reference.Id(), sophie, "default", sophieImage);
        I18n talkMessage = new I18n(Map.of(Language.FR, "Quel est le nom du peintre ?"));
        TalkItemOut.Fixed talkOut = new TalkItemOut.Fixed(talkMessage);
        TalkItemNext.InputText inputText = new TalkItemNext.InputText(TalkItemNext.InputText.Type.ALPHANUMERIC, Optional.of(20));
        TalkItem talkItem = new TalkItem(talkInputId, talkOut, sophieRef, inputText);
        TalkConfig talkConfig = new TalkConfig(List.of(talkItem));

        // Targets
        ScenarioConfig.Target target1 = new ScenarioConfig.Target(targetSuccess,
                new I18n(Map.of(Language.FR, "Artiste trouve")),
                Optional.empty(), false, List.of(), Optional.empty());
        ScenarioConfig.Target target2 = new ScenarioConfig.Target(targetWrongArtist,
                new I18n(Map.of(Language.FR, "Mauvais artiste")),
                Optional.empty(), false, List.of(), Optional.empty());
        ScenarioConfig.Target target3 = new ScenarioConfig.Target(targetUnknown,
                new I18n(Map.of(Language.FR, "Inconnu")),
                Optional.empty(), false, List.of(), Optional.empty());

        // Possibility 1: "Golu" (ALMOST_EQUALS) -> SUCCESS
        PossibilityTrigger triggerGolu = new PossibilityTrigger.TalkInputText(
                new PossibilityTrigger.Id(), talkInputId, "Golu", PossibilityTrigger.TalkInputText.MatchType.ALMOST_EQUALS);
        Consequence consequenceSuccess = new Consequence.ScenarioTarget(new Consequence.Id(), targetSuccess, ScenarioSessionState.SUCCESS);
        Possibility possibilitySuccess = new Possibility(triggerGolu, List.of(consequenceSuccess));

        // Possibility 2: OR("Losof", "Celle") -> WRONG_ARTIST
        PossibilityTrigger triggerLosof = new PossibilityTrigger.TalkInputText(
                new PossibilityTrigger.Id(), talkInputId, "Losof", PossibilityTrigger.TalkInputText.MatchType.ALMOST_EQUALS);
        PossibilityTrigger triggerCelle = new PossibilityTrigger.TalkInputText(
                new PossibilityTrigger.Id(), talkInputId, "Celle", PossibilityTrigger.TalkInputText.MatchType.ALMOST_EQUALS);
        PossibilityTrigger triggerOr = new PossibilityTrigger.Or(new PossibilityTrigger.Id(), List.of(triggerLosof, triggerCelle));
        Consequence consequenceWrongArtist = new Consequence.ScenarioTarget(new Consequence.Id(), targetWrongArtist, ScenarioSessionState.SUCCESS);
        Possibility possibilityWrongArtist = new Possibility(triggerOr, List.of(consequenceWrongArtist));

        // Possibility 3: NOT(ALMOST_EQUALS "Golu") AND NOT(OR("Losof", "Celle")) -> UNKNOWN
        // Simplified: COMPLETELY_DIFFERENT from "Golu" AND COMPLETELY_DIFFERENT from "Losof" AND COMPLETELY_DIFFERENT from "Celle"
        PossibilityTrigger triggerNotGolu = new PossibilityTrigger.TalkInputText(
                new PossibilityTrigger.Id(), talkInputId, "Golu", PossibilityTrigger.TalkInputText.MatchType.COMPLETELY_DIFFERENT);
        PossibilityTrigger triggerNotLosof = new PossibilityTrigger.TalkInputText(
                new PossibilityTrigger.Id(), talkInputId, "Losof", PossibilityTrigger.TalkInputText.MatchType.COMPLETELY_DIFFERENT);
        PossibilityTrigger triggerNotCelle = new PossibilityTrigger.TalkInputText(
                new PossibilityTrigger.Id(), talkInputId, "Celle", PossibilityTrigger.TalkInputText.MatchType.COMPLETELY_DIFFERENT);
        PossibilityTrigger triggerAnd = new PossibilityTrigger.And(new PossibilityTrigger.Id(), List.of(triggerNotGolu, triggerNotLosof, triggerNotCelle));
        Consequence consequenceUnknown = new Consequence.ScenarioTarget(new Consequence.Id(), targetUnknown, ScenarioSessionState.SUCCESS);
        Possibility possibilityUnknown = new Possibility(triggerAnd, List.of(consequenceUnknown));

        // Step with all possibilities
        ScenarioConfig.Step step = new ScenarioConfig.Step(stepId, new I18n(Map.of(Language.FR, "Mission Sophie")),
                Optional.empty(), 0, List.of(target1, target2, target3),
                List.of(possibilitySuccess, possibilityWrongArtist, possibilityUnknown));

        Template template = Template.builder()
                .id(templateId)
                .scenario(new ScenarioConfig(List.of(step)))
                .board(new BoardConfig(List.of(space)))
                .talk(talkConfig)
                .build();
        templatePort.create(template);
    }

    @Test
    public void inputGolu_triggersSuccess() throws GameException, ConnectException {
        GameSessionContext context = start();

        // User types "Golu"
        eventOrchestrator.fireAndWait(context, new GameEvent.TalkInputText(talkInputId, "Golu"));

        // Should trigger SUCCESS target
        assertThat(situationPort.get(context).scenario().targetIds()).contains(targetSuccess);
        assertThat(situationPort.get(context).scenario().targetIds()).doesNotContain(targetWrongArtist);
        assertThat(situationPort.get(context).scenario().targetIds()).doesNotContain(targetUnknown);
    }

    @Test
    public void inputGoluLowercase_triggersSuccess_almostEquals() throws GameException, ConnectException {
        GameSessionContext context = start();

        // User types "golu" (lowercase) - should match via ALMOST_EQUALS
        eventOrchestrator.fireAndWait(context, new GameEvent.TalkInputText(talkInputId, "golu"));

        assertThat(situationPort.get(context).scenario().targetIds()).contains(targetSuccess);
    }

    @Test
    public void inputLosof_triggersOrTrigger_wrongArtist() throws GameException, ConnectException {
        GameSessionContext context = start();

        // User types "Losof" - should match OR trigger (first child)
        eventOrchestrator.fireAndWait(context, new GameEvent.TalkInputText(talkInputId, "Losof"));

        assertThat(situationPort.get(context).scenario().targetIds()).contains(targetWrongArtist);
        assertThat(situationPort.get(context).scenario().targetIds()).doesNotContain(targetSuccess);
    }

    @Test
    public void inputCelle_triggersOrTrigger_wrongArtist() throws GameException, ConnectException {
        GameSessionContext context = start();

        // User types "Celle" - should match OR trigger (second child)
        eventOrchestrator.fireAndWait(context, new GameEvent.TalkInputText(talkInputId, "Celle"));

        assertThat(situationPort.get(context).scenario().targetIds()).contains(targetWrongArtist);
        assertThat(situationPort.get(context).scenario().targetIds()).doesNotContain(targetSuccess);
    }

    @Test
    public void inputUnknown_triggersAndTrigger_unknown() throws GameException, ConnectException {
        GameSessionContext context = start();

        // User types "ABCD" - completely different from all known values
        // Should match AND trigger (COMPLETELY_DIFFERENT from Golu AND Losof AND Celle)
        eventOrchestrator.fireAndWait(context, new GameEvent.TalkInputText(talkInputId, "ABCD"));

        assertThat(situationPort.get(context).scenario().targetIds()).contains(targetUnknown);
        assertThat(situationPort.get(context).scenario().targetIds()).doesNotContain(targetSuccess);
        assertThat(situationPort.get(context).scenario().targetIds()).doesNotContain(targetWrongArtist);
    }

    @Test
    public void inputSimilarToGolu_doesNotTriggerUnknown() throws GameException, ConnectException {
        GameSessionContext context = start();

        // User types "Gola" - similar to Golu (1 char different)
        // Should trigger SUCCESS (ALMOST_EQUALS Golu), NOT unknown
        eventOrchestrator.fireAndWait(context, new GameEvent.TalkInputText(talkInputId, "Gola"));

        assertThat(situationPort.get(context).scenario().targetIds()).contains(targetSuccess);
        assertThat(situationPort.get(context).scenario().targetIds()).doesNotContain(targetUnknown);
    }

    @Test
    public void testNotTrigger_directUsage() throws GameException, ConnectException {
        // This test validates NOT trigger behavior directly
        // We create a separate template with a NOT trigger

        Template.Id notTemplateId = new Template.Id();
        ScenarioConfig.Step.Id notStepId = new ScenarioConfig.Step.Id();
        ScenarioConfig.Target.Id notTargetId = new ScenarioConfig.Target.Id();
        TalkItem.Id notTalkId = new TalkItem.Id();

        // Board
        BoardSpace.Id spaceId = new BoardSpace.Id();
        BoardSpace space = new BoardSpace(spaceId, List.of(new Rectangle(Point.from(0, 0), Point.from(10, 10))));

        // Talk
        TalkCharacter character = new TalkCharacter(new TalkCharacter.Id(), "Test");
        Image testImage = new Image(Image.Type.ASSET, "test.png");
        TalkCharacter.Reference charRef = new TalkCharacter.Reference(new TalkCharacter.Reference.Id(), character, "default", testImage);
        TalkItemOut.Fixed talkOut = new TalkItemOut.Fixed(new I18n(Map.of(Language.FR, "Test")));
        TalkItemNext.InputText input = new TalkItemNext.InputText(TalkItemNext.InputText.Type.ALPHANUMERIC, Optional.empty());
        TalkItem talk = new TalkItem(notTalkId, talkOut, charRef, input);
        TalkConfig talkConfig = new TalkConfig(List.of(talk));

        // Target
        ScenarioConfig.Target target = new ScenarioConfig.Target(notTargetId,
                new I18n(Map.of(Language.FR, "Not triggered")),
                Optional.empty(), false, List.of(), Optional.empty());

        // NOT trigger: fires when input is NOT equal to "secret"
        PossibilityTrigger innerTrigger = new PossibilityTrigger.TalkInputText(
                new PossibilityTrigger.Id(), notTalkId, "secret", PossibilityTrigger.TalkInputText.MatchType.EQUALS);
        PossibilityTrigger notTrigger = new PossibilityTrigger.Not(new PossibilityTrigger.Id(), innerTrigger);
        Consequence consequence = new Consequence.ScenarioTarget(new Consequence.Id(), notTargetId, ScenarioSessionState.SUCCESS);
        Possibility possibility = new Possibility(notTrigger, List.of(consequence));

        ScenarioConfig.Step step = new ScenarioConfig.Step(notStepId, new I18n(Map.of(Language.FR, "Test NOT")),
                Optional.empty(), 0, List.of(target), List.of(possibility));

        Template template = Template.builder()
                .id(notTemplateId)
                .scenario(new ScenarioConfig(List.of(step)))
                .board(new BoardConfig(List.of(space)))
                .talk(talkConfig)
                .build();
        templatePort.create(template);

        // Start game with NOT template
        final ConnectAuthUser auth = createAuthUseCase.byDeviceId("notTestDevice");
        final GameSessionContext context = createUseCase.create(notTemplateId, auth.userId());
        final ConnectAuthGameSession authGameSession = authGameSessionUseCase.create(auth, context);
        startUseCase.apply(authGameSession);

        // Input "wrong" - NOT "secret" should trigger
        eventOrchestrator.fireAndWait(context, new GameEvent.TalkInputText(notTalkId, "wrong"));
        assertThat(situationPort.get(context).scenario().targetIds()).contains(notTargetId);

        // Clean and restart
        sessionClear.clearAll();
        final ConnectAuthUser auth2 = createAuthUseCase.byDeviceId("notTestDevice2");
        final GameSessionContext context2 = createUseCase.create(notTemplateId, auth2.userId());
        final ConnectAuthGameSession authGameSession2 = authGameSessionUseCase.create(auth2, context2);
        startUseCase.apply(authGameSession2);

        // Input "secret" - NOT "secret" should NOT trigger
        eventOrchestrator.fireAndWait(context2, new GameEvent.TalkInputText(notTalkId, "secret"));
        assertThat(situationPort.get(context2).scenario().targetIds()).doesNotContain(notTargetId);
    }

    private GameSessionContext start() throws GameException, ConnectException {
        final ConnectAuthUser auth = createAuthUseCase.byDeviceId("testDevice" + System.currentTimeMillis());
        final GameSessionContext context = createUseCase.create(templateId, auth.userId());
        final ConnectAuthGameSession authGameSession = authGameSessionUseCase.create(auth, context);
        startUseCase.apply(authGameSession);
        return context;
    }
}

package fr.plop.contexts.game.session.event.domain;

import fr.plop.contexts.game.config.board.domain.model.BoardSpace;
import fr.plop.contexts.game.config.consequence.Consequence;
import fr.plop.contexts.game.config.scenario.domain.model.Possibility;
import fr.plop.contexts.game.config.scenario.domain.model.PossibilityRecurrence;
import fr.plop.contexts.game.config.scenario.domain.model.PossibilityTrigger;
import fr.plop.contexts.game.config.scenario.domain.model.ScenarioConfig;
import fr.plop.contexts.game.session.core.domain.model.GameContext;
import fr.plop.contexts.game.session.core.domain.model.GamePlayer;
import fr.plop.contexts.game.session.core.domain.model.GameSession;
import fr.plop.contexts.game.session.event.adapter.action.GameEventActionPushAdapter;
import fr.plop.contexts.game.session.event.adapter.action.GameEventActionScenarioAdapter;
import fr.plop.contexts.game.session.scenario.domain.model.ScenarioSessionState;
import fr.plop.subs.i18n.domain.I18n;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class GameEventBroadCastInternTest_eventGoIn_consequenceAlertAndGoal {

    private final GameEventBroadCastIntern.Port port = mock(GameEventBroadCastIntern.Port.class);
    private final GameEventActionPushAdapter pushAdapter = mock(GameEventActionPushAdapter.class);
    private final GameEventActionScenarioAdapter scenarioAdapter = mock(GameEventActionScenarioAdapter.class);
    private final GameEventBroadCastIntern broadCast = new GameEventBroadCastIntern(port, pushAdapter, scenarioAdapter);

    private final GameSession.Id sessionId = new GameSession.Id();
    private final GamePlayer.Id playerId = new GamePlayer.Id();
    private final GameContext context = new GameContext(sessionId, playerId);
    private final BoardSpace.Id spaceId = new BoardSpace.Id();


    @Test
    public void emptyPossibility() {
        when(port.findPossibilities(context)).thenReturn(Stream.empty());
        broadCast.fire(goInEvent(), context);
        verify(pushAdapter, never()).message(any(), any(), any());
        verify(scenarioAdapter, never()).updateStateOrCreateGoalStep(any(), any());
    }

    @Test
    public void fireEvent_ifSameSpace() {
        List<Consequence> consequences = consequences();
        when(port.findPossibilities(context))
                .thenReturn(Stream.of(new Possibility(new PossibilityRecurrence.Always(), triggerGoIn(spaceId), consequences)));
        broadCast.fire(goInEvent(), new GameContext(sessionId, playerId));

        verify(pushAdapter).message(sessionId, playerId, ((Consequence.DisplayMessage) consequences.getFirst()).value());
        verify(scenarioAdapter).updateStateOrCreateGoalStep(playerId, ((Consequence.ScenarioStep) consequences.get(1)));
    }

    @Test
    public void nothing_ifDifferentSpace() {
        PossibilityTrigger trigger = triggerGoIn(new BoardSpace.Id("other space"));
        when(port.findPossibilities(context))
                .thenReturn(Stream.of(new Possibility(new PossibilityRecurrence.Always(), trigger, consequences())));
        broadCast.fire(goInEvent(), new GameContext(sessionId, playerId));

        verify(pushAdapter, never()).message(any(), any(), any());
        verify(scenarioAdapter, never()).updateStateOrCreateGoalStep(any(), any());
    }

    private List<Consequence> consequences() {
        Consequence.DisplayMessage message = new Consequence.DisplayMessage(new Consequence.Id(), mock(I18n.class));
        Consequence.ScenarioStep goal = new Consequence.ScenarioStep(new Consequence.Id(), new ScenarioConfig.Step.Id(), ScenarioSessionState.FAILURE);
        return List.of(message, goal);
    }

    private GameEvent.GoIn goInEvent() {
        return new GameEvent.GoIn(spaceId);
    }

    private PossibilityTrigger.SpaceGoIn triggerGoIn(BoardSpace.Id spaceId) {
        return new PossibilityTrigger.SpaceGoIn(new PossibilityTrigger.Id(), spaceId);
    }

}

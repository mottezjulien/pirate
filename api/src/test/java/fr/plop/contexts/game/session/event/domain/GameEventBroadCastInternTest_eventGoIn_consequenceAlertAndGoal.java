package fr.plop.contexts.game.session.event.domain;

import fr.plop.contexts.game.config.board.domain.model.BoardSpace;
import fr.plop.contexts.game.config.consequence.Consequence;
import fr.plop.contexts.game.config.scenario.domain.model.Possibility;
import fr.plop.contexts.game.config.scenario.domain.model.PossibilityRecurrence;
import fr.plop.contexts.game.config.scenario.domain.model.PossibilityTrigger;
import fr.plop.contexts.game.config.scenario.domain.model.ScenarioConfig;
import fr.plop.contexts.game.session.core.domain.model.GamePlayer;
import fr.plop.contexts.game.session.core.domain.model.GameSession;
import fr.plop.subs.i18n.domain.I18n;
import fr.plop.generic.enumerate.AndOrOr;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class GameEventBroadCastInternTest_eventGoIn_consequenceAlertAndGoal {

    private final GameEventBroadCastIntern.OutPort outputPort = mock(GameEventBroadCastIntern.OutPort.class);
    private final GameEventBroadCastIntern broadCast = new GameEventBroadCastIntern(outputPort);

    private final GameSession.Id sessionId = new GameSession.Id();
    private final GamePlayer.Id playerId = new GamePlayer.Id();
    private final BoardSpace.Id spaceId = new BoardSpace.Id();

    @Test
    public void emptyPossibility() {
        when(outputPort.findPossibilities(sessionId, playerId)).thenReturn(Stream.empty());
        broadCast.fire(goInEvent(), new GameEventContext(sessionId, playerId));
        verify(outputPort, never()).doMessage(any(), any(), any());
        verify(outputPort, never()).doGoal(any(), any(), any());
    }

    @Test
    public void fireEvent_ifSameSpace() {
        List<Consequence> consequences = consequences();
        when(outputPort.findPossibilities(sessionId, playerId))
                .thenReturn(Stream.of(new Possibility(new PossibilityRecurrence.Always(), triggerGoIn(spaceId), List.of(), AndOrOr.AND, consequences)));
        broadCast.fire(goInEvent(), new GameEventContext(sessionId, playerId));

        verify(outputPort).doMessage(sessionId, playerId, ((Consequence.DisplayMessage) consequences.getFirst()));
        verify(outputPort).doGoal(sessionId, playerId, ((Consequence.ScenarioStep) consequences.get(1)));
    }

    @Test
    public void nothing_ifDifferentSpace() {
        PossibilityTrigger trigger = triggerGoIn(new BoardSpace.Id("other space"));
        when(outputPort.findPossibilities(sessionId, playerId))
                .thenReturn(Stream.of(new Possibility(new PossibilityRecurrence.Always(), trigger, List.of(), AndOrOr.AND, consequences())));
        broadCast.fire(goInEvent(), new GameEventContext(sessionId, playerId));

        verify(outputPort, never()).doMessage(any(), any(), any());
        verify(outputPort, never()).doGoal(any(), any(), any());
    }

    private List<Consequence> consequences() {
        Consequence.DisplayMessage message = new Consequence.DisplayMessage(new Consequence.Id(), mock(I18n.class));
        Consequence.ScenarioStep goal = new Consequence.ScenarioStep(new Consequence.Id(), new ScenarioConfig.Step.Id(), fr.plop.contexts.game.session.scenario.domain.model.ScenarioGoal.State.FAILURE);
        return List.of(message, goal);
    }

    private GameEvent.GoIn goInEvent() {
        return new GameEvent.GoIn(spaceId);
    }

    private PossibilityTrigger.SpaceGoIn triggerGoIn(BoardSpace.Id spaceId) {
        return new PossibilityTrigger.SpaceGoIn(new PossibilityTrigger.Id(), spaceId);
    }

}

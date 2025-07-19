package fr.plop.contexts.game.session.event.domain;

import fr.plop.contexts.game.config.board.domain.model.BoardSpace;
import fr.plop.contexts.game.config.scenario.domain.model.Possibility;
import fr.plop.contexts.game.config.scenario.domain.model.PossibilityConsequence;
import fr.plop.contexts.game.config.scenario.domain.model.PossibilityRecurrence;
import fr.plop.contexts.game.config.scenario.domain.model.PossibilityTrigger;
import fr.plop.contexts.game.config.scenario.domain.model.ScenarioConfig;
import fr.plop.contexts.game.session.core.domain.model.GamePlayer;
import fr.plop.contexts.game.session.core.domain.model.GameSession;
import fr.plop.contexts.game.session.scenario.domain.model.ScenarioGoal;
import fr.plop.contexts.i18n.domain.I18n;
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
        broadCast.fire(goInEvent());
        verify(outputPort, never()).doAlert(any(), any(), any());
        verify(outputPort, never()).doGoal(any(), any());
    }

    @Test
    public void fireEvent_ifSameSpace() {
        List<PossibilityConsequence> consequences = consequences();
        when(outputPort.findPossibilities(sessionId, playerId))
                .thenReturn(Stream.of(new Possibility(new PossibilityRecurrence.Always(), triggerGoIn(spaceId), List.of(), AndOrOr.AND, consequences)));
        broadCast.fire(goInEvent());

        verify(outputPort).doAlert(sessionId, playerId, ((PossibilityConsequence.Alert) consequences.getFirst()));
        verify(outputPort).doGoal(playerId, ((PossibilityConsequence.Goal) consequences.get(1)));
    }

    @Test
    public void nothing_ifDifferentSpace() {
        PossibilityTrigger trigger = triggerGoIn(new BoardSpace.Id("other space"));
        when(outputPort.findPossibilities(sessionId, playerId))
                .thenReturn(Stream.of(new Possibility(new PossibilityRecurrence.Always(), trigger, List.of(), AndOrOr.AND, consequences())));
        broadCast.fire(goInEvent());

        verify(outputPort, never()).doAlert(any(), any(), any());
        verify(outputPort, never()).doGoal(any(), any());
    }

    private List<PossibilityConsequence> consequences() {
        PossibilityConsequence.Alert alert = new PossibilityConsequence.Alert(new PossibilityConsequence.Id(), mock(I18n.class));
        PossibilityConsequence.Goal goal = new PossibilityConsequence.Goal(new PossibilityConsequence.Id(), new ScenarioConfig.Step.Id(), ScenarioGoal.State.FAILURE);
        return List.of(alert, goal);
    }

    private GameEvent.GoIn goInEvent() {
        return new GameEvent.GoIn(sessionId, playerId, spaceId);
    }

    private PossibilityTrigger.GoInSpace triggerGoIn(BoardSpace.Id spaceId) {
        return new PossibilityTrigger.GoInSpace(new PossibilityTrigger.Id(), spaceId);
    }

}

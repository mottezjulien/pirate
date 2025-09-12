package fr.plop.contexts.game.session.event.domain;

import fr.plop.contexts.game.config.board.domain.model.BoardSpace;
import fr.plop.contexts.game.config.consequence.Consequence;
import fr.plop.contexts.game.config.scenario.domain.model.Possibility;
import fr.plop.contexts.game.config.scenario.domain.model.PossibilityRecurrence;
import fr.plop.contexts.game.config.scenario.domain.model.PossibilityTrigger;
import fr.plop.contexts.game.config.scenario.domain.model.ScenarioConfig;
import fr.plop.contexts.game.session.core.domain.model.GamePlayer;
import fr.plop.contexts.game.session.core.domain.model.GameSession;
import fr.plop.contexts.game.session.scenario.domain.model.ScenarioGoal;
import fr.plop.contexts.game.session.time.TimeUnit;
import fr.plop.generic.enumerate.AndOrOr;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class GameEventBroadCastInternTest_goalActive {

    private final GameEventBroadCastIntern.OutPort outputPort = mock(GameEventBroadCastIntern.OutPort.class);
    private final GameEventBroadCastIntern broadCast = spy(new GameEventBroadCastIntern(outputPort));

    private final GameSession.Id sessionId = new GameSession.Id();
    private final GamePlayer.Id playerId = new GamePlayer.Id();
    private final BoardSpace.Id spaceId = new BoardSpace.Id();

    @Test
    public void goalActive_should_emit_GoalActive_event_with_same_time_and_step() {
        // Given
        TimeUnit time = TimeUnit.ofMinutes(7);
        ScenarioConfig.Step.Id stepId = new ScenarioConfig.Step.Id();
        Consequence.ScenarioStep activeGoal = new Consequence.ScenarioStep(new Consequence.Id(), stepId, ScenarioGoal.State.ACTIVE);

        PossibilityTrigger.SpaceGoIn trigger = new PossibilityTrigger.SpaceGoIn(new PossibilityTrigger.Id(), spaceId);
        Possibility possibility = new Possibility(trigger, activeGoal);

        when(outputPort.findActions(playerId)).thenReturn(List.of());
        when(outputPort.findPossibilities(sessionId, playerId))
                .thenReturn(Stream.of(possibility))  // first fire (GoIn)
                .thenReturn(Stream.empty());         // second fire (GoalActive)

        // When
        GameEvent.GoIn event = new GameEvent.GoIn(sessionId, playerId, time, spaceId);
        broadCast.fire(event);

        // Then: outPort.doGoal called for the ACTIVE step
        verify(outputPort, times(1)).doGoal(sessionId, playerId, activeGoal);
        // And: broadcaster fires a GoalActive event with same time and stepId
        ArgumentCaptor<GameEvent> captor = ArgumentCaptor.forClass(GameEvent.class);
        verify(broadCast, times(2)).fire(captor.capture()); // first is GoIn, second is GoalActive
        GameEvent second = captor.getAllValues().get(1);
        assertThat(second).isInstanceOf(GameEvent.GoalActive.class);
        GameEvent.GoalActive goalActive = (GameEvent.GoalActive) second;
        assertThat(goalActive.sessionId()).isEqualTo(sessionId);
        assertThat(goalActive.playerId()).isEqualTo(playerId);
        assertThat(goalActive.timeUnit()).isEqualTo(time);
        assertThat(goalActive.stepId()).isEqualTo(stepId);

        // And: no unexpected side effects
        verify(outputPort, never()).doAlert(any(), any(), any());
        verify(outputPort, never()).doGoalTarget(any(), any());
        verify(outputPort, never()).doGameOver(any(), any(), any());
    }


    @Test
    public void no_goalActive_if_state_not_ACTIVE() {
        // Given
        TimeUnit time = TimeUnit.ofMinutes(3);
        ScenarioConfig.Step.Id stepId = new ScenarioConfig.Step.Id();
        Consequence.ScenarioStep successGoal = new Consequence.ScenarioStep(new Consequence.Id(), stepId, ScenarioGoal.State.SUCCESS);

        Possibility possibility = new Possibility(
                new PossibilityRecurrence.Always(),
                new PossibilityTrigger.SpaceGoIn(new PossibilityTrigger.Id(), spaceId),
                List.of(), AndOrOr.AND,
                List.of(successGoal)
        );

        when(outputPort.findActions(playerId)).thenReturn(List.of());
        when(outputPort.findPossibilities(sessionId, playerId))
                .thenReturn(Stream.of(possibility));

        // When
        broadCast.fire(new GameEvent.GoIn(sessionId, playerId, time, spaceId));

        // Then
        verify(outputPort, times(1)).doGoal(sessionId, playerId, successGoal);
        verify(broadCast, times(1)).fire(any(GameEvent.class)); // only the initial event
        verifyNoMoreInteractions(broadCast);

        verify(outputPort, never()).doAlert(any(), any(), any());
        verify(outputPort, never()).doGoalTarget(any(), any());
        verify(outputPort, never()).doGameOver(any(), any(), any());
    }

    @Test
    public void chain_multiple_possibilities_and_saveAction_each_time() {
        // Given
        TimeUnit time = TimeUnit.ofMinutes(5);
        ScenarioConfig.Step.Id stepId1 = new ScenarioConfig.Step.Id();
        ScenarioConfig.Step.Id stepId2 = new ScenarioConfig.Step.Id();
        Consequence.ScenarioStep activeGoal1 = new Consequence.ScenarioStep(new Consequence.Id(), stepId1, ScenarioGoal.State.ACTIVE);
        Consequence.ScenarioStep successGoal2 = new Consequence.ScenarioStep(new Consequence.Id(), stepId2, ScenarioGoal.State.SUCCESS);

        Possibility goInTriggersTwo = new Possibility(
                new PossibilityRecurrence.Always(),
                new PossibilityTrigger.SpaceGoIn(new PossibilityTrigger.Id(), spaceId),
                List.of(), AndOrOr.AND,
                List.of(activeGoal1, successGoal2)
        );

        // GoalActive follow-up should not re-trigger same go-in based possibility: return empty on second call
        when(outputPort.findActions(playerId)).thenReturn(List.of());
        when(outputPort.findPossibilities(sessionId, playerId))
                .thenReturn(Stream.of(goInTriggersTwo)) // first fire (GoIn)
                .thenReturn(Stream.empty());            // second fire (GoalActive)

        // When
        broadCast.fire(new GameEvent.GoIn(sessionId, playerId, time, spaceId));

        // Then: both consequences executed
        verify(outputPort).doGoal(sessionId, playerId, activeGoal1);
        verify(outputPort).doGoal(sessionId, playerId, successGoal2);

        // And: GoalActive fired once with same time and stepId1
        ArgumentCaptor<GameEvent> captor = ArgumentCaptor.forClass(GameEvent.class);
        verify(broadCast, times(2)).fire(captor.capture());
        GameEvent second = captor.getAllValues().get(1);
        assertThat(second).isInstanceOf(GameEvent.GoalActive.class);
        GameEvent.GoalActive goalActive = (GameEvent.GoalActive) second;
        assertThat(goalActive.stepId()).isEqualTo(stepId1);
        assertThat(goalActive.timeUnit()).isEqualTo(time);

        // And: saveAction called for the initial possibility only once
        verify(outputPort, times(1)).saveAction(eq(playerId), eq(goInTriggersTwo.id()), eq(time));

        // No unexpected side effects
        verify(outputPort, never()).doAlert(any(), any(), any());
        verify(outputPort, never()).doGoalTarget(any(), any());
        verify(outputPort, never()).doGameOver(any(), any(), any());
    }

}
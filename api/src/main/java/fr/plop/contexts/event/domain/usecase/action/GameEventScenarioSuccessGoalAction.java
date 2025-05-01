package fr.plop.contexts.event.domain.usecase.action;

import fr.plop.contexts.game.domain.model.GamePlayer;
import fr.plop.contexts.scenario.domain.model.PossibilityConsequence;
import fr.plop.contexts.scenario.domain.model.Scenario;
import fr.plop.contexts.scenario.domain.model.ScenarioGoal;

import java.util.Optional;

public class GameEventScenarioSuccessGoalAction implements GameEventAction<PossibilityConsequence.SuccessGoal> {

    public interface OutPort {
        Optional<ScenarioGoal> findActiveGoal(Scenario.Step.Id stepId, GamePlayer.Id playerId);
        void setSuccess(ScenarioGoal goal);
    }

    private final OutPort outPort;

    public GameEventScenarioSuccessGoalAction(OutPort outPort) {
        this.outPort = outPort;
    }

    @Override
    public void apply(PossibilityConsequence.SuccessGoal consequence, GamePlayer.Id playerId) {
        Optional<ScenarioGoal> optGoal = outPort.findActiveGoal(consequence.stepId(), playerId);
        optGoal.ifPresent(outPort::setSuccess);
    }

}

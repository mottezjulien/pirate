package fr.plop.contexts.game.config.consequence.handler;


import fr.plop.contexts.game.config.consequence.Consequence;
import fr.plop.contexts.game.instance.core.domain.model.GameInstanceContext;
import fr.plop.contexts.game.instance.scenario.adapter.GameInstanceScenarioGoalAdapter;
import org.springframework.stereotype.Component;

@Component
public class ConsequenceScenarioGoalHandler implements ConsequenceHandler {

    private final GameInstanceScenarioGoalAdapter scenarioAdapter;

    public ConsequenceScenarioGoalHandler(GameInstanceScenarioGoalAdapter scenarioAdapter) {
        this.scenarioAdapter = scenarioAdapter;
    }


    @Override
    public boolean supports(Consequence consequence) {
        return consequence instanceof Consequence.ScenarioStep || consequence instanceof Consequence.ScenarioTarget;
    }

    @Override
    public void handle(GameInstanceContext context, Consequence consequence) {
        switch (consequence) {
            case Consequence.ScenarioStep scenarioStep
                    -> scenarioAdapter.saveStep(context, scenarioStep.stepId(), scenarioStep.state());
            case  Consequence.ScenarioTarget scenarioTarget
                    -> scenarioAdapter.saveTarget(context.playerId(), scenarioTarget.targetId(), scenarioTarget.state());
            default -> throw new IllegalStateException("Unexpected value: " + consequence);
        }
    }
}

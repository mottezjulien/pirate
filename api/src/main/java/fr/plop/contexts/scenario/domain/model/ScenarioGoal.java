package fr.plop.contexts.scenario.domain.model;

import fr.plop.contexts.game.domain.model.GamePlayer;

public record ScenarioGoal(Scenario.Step.Id stepId, GamePlayer.Id playerId, State state) {

    public enum State {
        ACTIVE, SUCCESS, FAILURE
    }

}

package fr.plop.contexts.game.session.scenario.domain.model;

import fr.plop.contexts.game.config.scenario.domain.model.ScenarioConfig;
import fr.plop.contexts.game.session.core.domain.model.GamePlayer;
import fr.plop.generic.tools.StringTools;

public record ScenarioGoal(Id id, GamePlayer.Id playerId, ScenarioConfig.Step.Id stepId, State state) {

    public ScenarioGoal(GamePlayer.Id playerId, ScenarioConfig.Step.Id stepId, State state) {
        this(new Id(), playerId, stepId, state);
    }

    public record Id(String value) {
        public Id() {
            this(StringTools.generate());
        }
    }

    public enum State {
        ACTIVE, SUCCESS, FAILURE
    }
}

package fr.plop.contexts.game.session.scenario.domain.model;

import fr.plop.contexts.game.config.scenario.domain.model.ScenarioConfig;
import fr.plop.contexts.game.session.core.domain.model.GamePlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public record ScenarioSession(ScenarioConfig config, List<ScenarioGoal> goals) {

    public static ScenarioSession build(ScenarioConfig config) {
        return new ScenarioSession(config, new ArrayList<>());
    }

    public void init(GamePlayer.Id playerId) {
        config.firstSteps()
                .forEach(step -> goals.add(new ScenarioGoal(playerId, step.id(), ScenarioGoal.State.ACTIVE)));
    }

    public Stream<ScenarioGoal> goals(GamePlayer.Id playerId) {
        return goals.stream()
                .filter(goal -> goal.playerId().equals(playerId));
    }
}

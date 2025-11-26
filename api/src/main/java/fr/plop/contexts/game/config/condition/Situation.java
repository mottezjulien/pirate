package fr.plop.contexts.game.config.condition;

import fr.plop.contexts.game.config.board.domain.model.BoardSpace;
import fr.plop.contexts.game.config.scenario.domain.model.ScenarioConfig;
import fr.plop.contexts.game.session.time.GameSessionTimeUnit;

import java.util.List;

public sealed interface Situation permits Situation.Board, Situation.Scenario, Situation.Time {

    record Board(List<BoardSpace.Id> spaceIds) implements Situation {

    }

    record Scenario(List<ScenarioConfig.Step.Id> stepIds, List<ScenarioConfig.Target.Id> targetIds) implements Situation {

    }

    record Time(GameSessionTimeUnit current) implements Situation {

    }


}

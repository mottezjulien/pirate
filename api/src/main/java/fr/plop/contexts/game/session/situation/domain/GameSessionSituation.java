package fr.plop.contexts.game.session.situation.domain;

import fr.plop.contexts.game.config.board.domain.model.BoardSpace;
import fr.plop.contexts.game.config.scenario.domain.model.ScenarioConfig;
import fr.plop.contexts.game.session.time.GameSessionTimeUnit;

import java.util.List;

public record GameSessionSituation(Board board, Scenario scenario, Time time) {

    public record Board(List<BoardSpace.Id> spaceIds) {

    }

    public record Scenario(List<ScenarioConfig.Step.Id> stepIds, List<ScenarioConfig.Target.Id> targetIds) {

    }

    public record Time(GameSessionTimeUnit current) {

    }

}

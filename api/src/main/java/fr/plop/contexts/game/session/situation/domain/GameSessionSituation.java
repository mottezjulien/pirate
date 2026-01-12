package fr.plop.contexts.game.session.situation.domain;

import fr.plop.contexts.game.config.board.domain.model.BoardSpace;
import fr.plop.contexts.game.config.scenario.domain.model.ScenarioConfig;
import fr.plop.contexts.game.session.time.GameSessionTimeUnit;

import java.util.List;

public record GameSessionSituation(Board board, Scenario scenario, Time time) {

    public GameSessionSituation() {
        this(new Board(), new Scenario(), new Time());
    }

    public record Board(List<BoardSpace.Id> spaceIds) {

        public Board() {
            this(List.of());
        }
    }

    public record Scenario(List<ScenarioConfig.Step.Id> stepIds, List<ScenarioConfig.Target.Id> targetIds) {
        public Scenario() {
            this(List.of(), List.of());
        }
    }

    public record Time(GameSessionTimeUnit current) {
        public Time() {
            this(GameSessionTimeUnit.ofMinutes(0));
        }
    }

}

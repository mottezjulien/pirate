package fr.plop.contexts.game.instance.core.domain.model;

import fr.plop.contexts.game.config.board.domain.model.BoardSpace;
import fr.plop.contexts.game.config.scenario.domain.model.ScenarioConfig;
import fr.plop.contexts.user.User;
import fr.plop.generic.tools.StringTools;

import java.util.List;

/*
public record GamePlayer(Id id, List<ScenarioConfig.Step.Id> activeStepIds, List<BoardSpace.Id> spaceIds) {

    public record Id(String value) {
        public Id() {
            this(StringTools.generate());
        }
    }

    public GamePlayer(Id id) {
        this(id, List.of(), List.of());
    }

}*/

public record GamePlayer(Id id, User.Id userId, State state) {

    public boolean is(User.Id userId) {
        return this.userId.equals(userId);
    }

    public record Id(String value) {
        public Id() {
            this(StringTools.generate());
        }
    }

    public enum State {
        INIT, ACTIVE, WIN, LOSE
    }

}

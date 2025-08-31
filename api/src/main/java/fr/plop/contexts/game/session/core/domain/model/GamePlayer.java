package fr.plop.contexts.game.session.core.domain.model;

import fr.plop.contexts.game.config.board.domain.model.BoardSpace;
import fr.plop.contexts.game.config.scenario.domain.model.ScenarioConfig;
import fr.plop.generic.tools.StringTools;

import java.util.List;

public record GamePlayer(Id id, List<ScenarioConfig.Step.Id> stepActiveIds, List<BoardSpace.Id> spaceIds) {

    public record Id(String value) {
        public Id() {
            this(StringTools.generate());
        }
    }

    public enum State {
        INIT, ACTIVE, WIN
    }

    /*public boolean inSpace(BoardSpace.Id spaceId) {
        return spaceIds.contains(spaceId);
    }*/

}

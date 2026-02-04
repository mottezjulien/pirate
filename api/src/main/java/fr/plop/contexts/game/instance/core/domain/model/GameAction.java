package fr.plop.contexts.game.instance.core.domain.model;

import fr.plop.contexts.game.config.scenario.domain.model.Possibility;
import fr.plop.contexts.game.instance.time.GameInstanceTimeUnit;

public record GameAction(GamePlayer.Id playerId, Possibility.Id possibilityId, GameInstanceTimeUnit timeClick) {
    public boolean is(Possibility.Id possibilityId) {
        return possibilityId.equals(this.possibilityId);
    }
}

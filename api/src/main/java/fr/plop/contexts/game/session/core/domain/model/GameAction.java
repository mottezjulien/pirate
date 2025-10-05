package fr.plop.contexts.game.session.core.domain.model;

import fr.plop.contexts.game.config.scenario.domain.model.Possibility;
import fr.plop.contexts.game.session.time.GameSessionTimeUnit;

public record GameAction(GamePlayer.Id playerId, Possibility.Id possibilityId, GameSessionTimeUnit timeClick) {
    public boolean is(Possibility.Id possibilityId) {
        return possibilityId.equals(this.possibilityId);
    }
}

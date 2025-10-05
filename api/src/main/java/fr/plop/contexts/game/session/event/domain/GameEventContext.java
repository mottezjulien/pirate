package fr.plop.contexts.game.session.event.domain;

import fr.plop.contexts.game.session.core.domain.model.GamePlayer;
import fr.plop.contexts.game.session.core.domain.model.GameSession;

public record GameEventContext(GameSession.Id sessionId, GamePlayer.Id playerId/*, TimeUnit timeUnit*/) {

}

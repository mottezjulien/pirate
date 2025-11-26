package fr.plop.contexts.game.session.core.domain.model;

public record GameContext(GameSession.Id sessionId, GamePlayer.Id playerId) {

}

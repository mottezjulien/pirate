package fr.plop.contexts.game.session.core.domain.model;

public record GameSessionContext(GameSession.Id sessionId, GamePlayer.Id playerId) {
    public GameSessionContext() {
        this(new GameSession.Id(), new GamePlayer.Id());
    }

    public boolean isSessionId(GameSession.Id sessionId) {
        return this.sessionId.equals(sessionId);
    }
}

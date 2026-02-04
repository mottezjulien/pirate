package fr.plop.contexts.game.instance.core.domain.model;

public record GameInstanceContext(GameInstance.Id instanceId, GamePlayer.Id playerId) {
    public GameInstanceContext() {
        this(new GameInstance.Id(), new GamePlayer.Id());
    }

    public boolean isInstanceId(GameInstance.Id instanceId) {
        return this.instanceId.equals(instanceId);
    }
}

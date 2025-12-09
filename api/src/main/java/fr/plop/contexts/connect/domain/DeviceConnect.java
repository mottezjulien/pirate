package fr.plop.contexts.connect.domain;


import fr.plop.contexts.game.session.core.domain.model.GamePlayer;

public record DeviceConnect(Id id, ConnectUser user, String deviceId) {

    public record Id(String value) {

    }

    public DeviceConnect withPlayerId(GamePlayer.Id playerId) {
        return new DeviceConnect(id(), user.withPlayerId(playerId), deviceId());
    }

}

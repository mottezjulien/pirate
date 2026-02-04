package fr.plop.contexts.connect.domain;

import fr.plop.contexts.user.User;

public record ConnectUserDevice(Id id, User.Id userId, String deviceId) {

    public record Id(String value) {

    }

}

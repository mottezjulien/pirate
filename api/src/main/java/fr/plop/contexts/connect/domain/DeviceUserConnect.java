package fr.plop.contexts.connect.domain;


import fr.plop.contexts.user.User;

public record DeviceUserConnect(Id id, String deviceId, User.Id userId) {

    public record Id(String value) {

    }

}

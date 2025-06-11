package fr.plop.contexts.connect.domain;


public record DeviceConnect(Id id, ConnectUser user, String deviceId) {

    public record Id(String value) {

    }

}

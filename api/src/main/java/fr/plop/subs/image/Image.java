package fr.plop.subs.image;

public record Image(Type type, String value) {
    public enum Type { ASSET, WEB }

}
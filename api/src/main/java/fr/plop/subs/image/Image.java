package fr.plop.subs.image;

public record Image(Type type, String value) {
    public enum Type { ASSET, WEB }

    public boolean isAsset() {
        return this.type == Type.ASSET;
    }

}
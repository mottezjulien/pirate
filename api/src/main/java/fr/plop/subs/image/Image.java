package fr.plop.subs.image;

public record Image(Type type, String path) {

    public static Image no() {
        return new Image(Image.Type.ASSET, "");
    }

    public enum Type {
        ASSET, WEB
    }

}

package fr.plop.subs.image;

public record ImageResponseDTO(String type, String value) {
    public static ImageResponseDTO fromModel(Image image) {
        return new ImageResponseDTO(image.type().name(), image.value());
    }
}
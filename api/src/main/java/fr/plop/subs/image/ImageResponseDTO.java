package fr.plop.subs.image;

public record ImageResponseDTO(String type, String value, Size size) {
    public record Size(int width, int height) {

    }
}
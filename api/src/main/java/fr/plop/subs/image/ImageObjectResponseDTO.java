package fr.plop.subs.image;

import fr.plop.contexts.game.config.Image.domain.ImageObject;



public record ImageObjectResponseDTO(String id, String label, String type, ImagePositionDTO position, Point point,
                                     ImageResponseDTO image) {

    public static ImageObjectResponseDTO fromModel(ImageObject model) {
        return switch (model) {
            case ImageObject.Point point ->
                    new ImageObjectResponseDTO(model.id().value(), model.label(), "POINT", new ImagePositionDTO(point.top(), point.left()), new Point(point.color()), null);
            case ImageObject._Image image ->
                    new ImageObjectResponseDTO(model.id().value(), model.label(), "IMAGE", new ImagePositionDTO(image.top(), image.left()), null, ImageResponseDTO.fromModel(image.value()));
        };
    }

    public record Point(String color) {

    }

}

package fr.plop.subs.image;

import fr.plop.contexts.game.config.Image.domain.ImageGeneric;

import java.util.List;

public record ImageDetailsResponseDTO(String id, String label, ImageResponseDTO image, List<ImageObjectResponseDTO> objects) {
    public static ImageDetailsResponseDTO fromModel(ImageGeneric imageGeneric) {
        return new ImageDetailsResponseDTO(imageGeneric.id().value(), imageGeneric.label(),
                ImageResponseDTO.fromModel(imageGeneric.value()),
                imageGeneric.objects().stream()
                        .map(ImageObjectResponseDTO::fromModel).toList());
    }

}
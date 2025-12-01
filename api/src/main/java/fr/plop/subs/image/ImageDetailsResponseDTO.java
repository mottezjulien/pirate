package fr.plop.subs.image;

import fr.plop.contexts.game.config.Image.domain.ImageGeneric;
import fr.plop.contexts.game.config.Image.domain.ImageItem;
import fr.plop.contexts.game.config.map.domain.MapItem;

import java.util.List;

public record ImageDetailsResponseDTO(String id, String label, ImageResponseDTO image, List<ImageObjectResponseDTO> objects) {
    public static ImageDetailsResponseDTO fromImageItemModel(ImageItem model) {
        ImageGeneric generic = model.generic();
        return new ImageDetailsResponseDTO(model.id().value(), generic.label(),
                ImageResponseDTO.fromModel(generic.value()),
                generic.objects().stream().map(ImageObjectResponseDTO::fromModel).toList());
    }

    public static ImageDetailsResponseDTO fromMapItemModel(MapItem model) {
        ImageGeneric generic = model.imageGeneric();
        return new ImageDetailsResponseDTO(model.id().value(), generic.label(),
                ImageResponseDTO.fromModel(generic.value()),
                generic.objects().stream().map(ImageObjectResponseDTO::fromModel).toList());
    }
}
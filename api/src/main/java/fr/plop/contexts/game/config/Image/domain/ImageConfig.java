package fr.plop.contexts.game.config.Image.domain;

import fr.plop.generic.tools.StringTools;

import java.util.List;
import java.util.Optional;

public record ImageConfig(Id id, List<ImageItem> items) {

    public record Id(String value) {
        public Id() {
            this(StringTools.generate());
        }
    }

    public ImageConfig() {
        this(new Id(), List.of());
    }

    public ImageConfig(List<ImageItem> items) {
        this(new Id(), items);
    }

    public Optional<ImageItem> byItemId(ImageItem.Id itemId) {
        return items.stream()
                .filter(item -> item.id().equals(itemId))
                .findFirst();
    }

}

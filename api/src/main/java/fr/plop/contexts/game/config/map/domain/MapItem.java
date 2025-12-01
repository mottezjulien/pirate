package fr.plop.contexts.game.config.map.domain;

import fr.plop.contexts.game.config.Image.domain.ImageGeneric;
import fr.plop.contexts.game.config.Image.domain.ImageObject;
import fr.plop.contexts.game.config.condition.Condition;
import fr.plop.generic.enumerate.Priority;
import fr.plop.generic.tools.StringTools;
import fr.plop.subs.image.Image;

import java.util.Optional;
import java.util.stream.Stream;

public record MapItem(Id id, ImageGeneric imageGeneric,
                      Priority priority, Optional<Condition> optCondition) {




    public String label() {
        return imageGeneric.label();
    }

    public record Id(String value) {
        public Id() {
            this(StringTools.generate());
        }
    }

    public MapItem(ImageGeneric imageGeneric) {
        this(imageGeneric, Priority.MEDIUM, Optional.empty());
    }

    public MapItem(ImageGeneric imageGeneric, Condition condition) {
        this(imageGeneric, Priority.MEDIUM, Optional.of(condition));
    }

    public MapItem(ImageGeneric imageGeneric, Priority priority, Optional<Condition> optCondition) {
        this(new Id(), imageGeneric, priority, optCondition);
    }

    public String imageValue() {
        return imageGeneric.imageValue();
    }

    public Image.Type imageType() {
        return imageGeneric.imageType();
    }

    public Stream<ImageObject> imageObjects() {
        return imageGeneric.objects().stream();
    }

}

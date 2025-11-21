package fr.plop.contexts.game.config.Image.domain;

import fr.plop.generic.tools.StringTools;
import fr.plop.subs.image.Image;

public record ImageItem(Id id, Image value) {

    public record Id(String value) {
        public Id() {
            this(StringTools.generate());
        }
    }

    public ImageItem(Image value) {
        this(new Id(), value);
    }

}

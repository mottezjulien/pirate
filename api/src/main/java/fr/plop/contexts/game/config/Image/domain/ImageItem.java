package fr.plop.contexts.game.config.Image.domain;

import fr.plop.contexts.game.instance.situation.domain.GameInstanceSituation;
import fr.plop.generic.tools.StringTools;

public record ImageItem(Id id, ImageGeneric generic) {

    public record Id(String value) {
        public Id() {
            this(StringTools.generate());
        }
    }

    public ImageItem(ImageGeneric generic) {
        this(new Id(), generic);
    }

    public ImageItem select(GameInstanceSituation situation) {
        return new ImageItem(id(),generic.select(situation));
    }

}

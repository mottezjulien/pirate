package fr.plop.contexts.game.config.Image.domain;

import fr.plop.contexts.game.session.situation.domain.GameSessionSituation;
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

    public ImageItem select(GameSessionSituation situation) {
        return new ImageItem(id(),generic.select(situation));
    }

}

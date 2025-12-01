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


/*
    public ImageItem(String label, Image value, List<ImageObject> objects) {
        this(new Id(), new ImageGeneric(label, value, objects));
    }

    public String imageValue() {
        return generic.imageValue();
    }

    public Image.Type imageType() {
        return generic.imageType();
    }*/

}

package fr.plop.contexts.game.config.Image.domain;

import fr.plop.contexts.game.instance.situation.domain.GameInstanceSituation;
import fr.plop.generic.tools.StringTools;
import fr.plop.subs.image.Image;

import java.util.List;

public record ImageGeneric(Id id, String label, Image value, List<ImageObject> objects) {

    public record Id(String value) {
        public Id() {
            this(StringTools.generate());
        }
    }

    public ImageGeneric(String label, Image value, List<ImageObject> objects) {
        this(new Id(), label, value, objects);
    }

    private ImageGeneric withObjects(List<ImageObject> objects) {
        return new ImageGeneric(id(), label(), value(), objects);
    }

    public String imageValue() {
        return value.value();
    }

    public Image.Type imageType() {
        return value.type();
    }


    public ImageGeneric select(GameInstanceSituation situation) {
        return withObjects(objects.stream().filter(object -> object.isSelected(situation)).toList());
    }




}

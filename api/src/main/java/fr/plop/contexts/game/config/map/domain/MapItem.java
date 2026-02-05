package fr.plop.contexts.game.config.map.domain;

import fr.plop.contexts.game.config.condition.Condition;
import fr.plop.contexts.game.instance.situation.domain.GameInstanceSituation;
import fr.plop.generic.enumerate.Priority;
import fr.plop.generic.position.Rectangle;
import fr.plop.generic.tools.StringTools;
import fr.plop.subs.image.Image;

import java.util.List;
import java.util.Optional;

public record MapItem(Id id, String label, Image image, Rectangle bounds,
                      Priority priority, Optional<Condition> optCondition, Optional<Image> optPointer,
                      List<MapObject> objects) {

    public record Id(String value) {
        public Id() {
            this(StringTools.generate());
        }
    }

    public MapItem(String label, Image image, Rectangle bounds) {
        this(label, image, bounds, Optional.empty(), List.of());
    }

    public MapItem(String label, Image image, Rectangle bounds, Condition condition) {
        this(label, image, bounds, Priority.byDefault(), Optional.of(condition), Optional.empty(), List.of());
    }

    public MapItem(String label, Image image, Rectangle bounds, Optional<Image> optPointer, List<MapObject> objects) {
        this(label, image, bounds, Priority.byDefault(), Optional.empty(), optPointer, objects);
    }

    public MapItem(String label, Image image, Rectangle bounds, Priority priority, Optional<Condition> optCondition, Optional<Image> optPointer, List<MapObject> objects) {
        this(new Id(), label, image, bounds, priority, optCondition, optPointer, objects);
    }

    public MapItem select(GameInstanceSituation situation) {
        List<MapObject> filtered = objects.stream()
                .filter(obj -> obj.isSelected(situation))
                .toList();
        return new MapItem(id, label, image, bounds, priority, optCondition, optPointer, filtered);
    }

}

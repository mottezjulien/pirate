package fr.plop.contexts.game.config.map.domain;

import fr.plop.contexts.game.config.condition.Condition;
import fr.plop.generic.ImagePoint;
import fr.plop.generic.enumerate.Priority;
import fr.plop.generic.tools.StringTools;
import fr.plop.subs.image.Image;

import java.util.List;
import java.util.Optional;

public record MapItem(Id id, String label, Image image,
                      Priority priority, List<_Object> objects, Optional<Condition> optCondition) {

    public MapItem(String label, Image image, Priority priority, List<_Object> objects, Optional<Condition> optCondition) {
        this(new Id(), label, image, priority, objects, optCondition);
    }

    public boolean isImageAsset() {
        return image.isAsset();
    }

    public String imagePath() {
        return image.value();
    }

    public record Id(String value) {
        public Id() {
            this(StringTools.generate());
        }
    }

    public sealed interface _Object permits _Object.Point, _Object._Image {

        record Atom(Id id, String label, ImagePoint center, Priority priority, Optional<Condition> optCondition) {
            public Atom(String label, ImagePoint center, Priority priority, Optional<Condition> optCondition) {
                this(new Id(), label, center, priority, optCondition);
            }
        }

        record Id(String value) {
            public Id() {
                this(StringTools.generate());
            }
        }

        Atom atom();

        default Id id() {
            return atom().id();
        }

        default Priority priority() {
            return atom().priority();
        }

        default String label() {
            return atom().label();
        }

        default double top() {
            return atom().center().top();
        }

        default double left() {
            return atom().center().left();
        }

        record Point(Atom atom, String color) implements _Object {

        }

        record _Image(Atom atom, Image value) implements _Object {

        }

    }

}

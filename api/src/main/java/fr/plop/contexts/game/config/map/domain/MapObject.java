package fr.plop.contexts.game.config.map.domain;

import fr.plop.contexts.game.config.condition.Condition;
import fr.plop.contexts.game.instance.situation.domain.GameInstanceSituation;
import fr.plop.generic.position.Point;
import fr.plop.generic.tools.StringTools;
import fr.plop.subs.image.Image;

import java.util.Optional;

public sealed interface MapObject permits MapObject.PointMarker, MapObject.ImageMarker {

    record Atom(Id id, String label, Point position, Optional<Condition> optCondition) {
        public Atom(String label, Point position, Optional<Condition> optCondition) {
            this(new Id(), label, position, optCondition);
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

    default String label() {
        return atom().label();
    }

    default Point position() {
        return atom().position();
    }

    default Optional<Condition> optCondition() {
        return atom().optCondition();
    }

    default boolean isSelected(GameInstanceSituation situation) {
        return atom().optCondition().map(condition -> condition.accept(situation).toBoolean())
                .orElse(true);
    }

    record PointMarker(Atom atom, String color) implements MapObject {
    }

    record ImageMarker(Atom atom, Image image) implements MapObject {
    }

}

package fr.plop.contexts.game.config.Image.domain;

import fr.plop.contexts.game.config.condition.Condition;
import fr.plop.contexts.game.session.situation.domain.GameSessionSituation;
import fr.plop.generic.ImagePoint;
import fr.plop.generic.tools.StringTools;
import fr.plop.subs.image.Image;

import java.util.Optional;

public sealed interface ImageObject permits ImageObject.Point, ImageObject._Image {

    record Atom(Id id, String label, ImagePoint center, Optional<Condition> optCondition) {
        public Atom(String label, ImagePoint center, Optional<Condition> optCondition) {
            this(new Id(), label, center, optCondition);
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

    default double top() {
        return atom().center().top();
    }

    default double left() {
        return atom().center().left();
    }

    default boolean isSelected(GameSessionSituation situation) {
        return atom().optCondition().map(condition -> condition.accept(situation).toBoolean())
                .orElse(true);
    }

    record Point(Atom atom, String color) implements ImageObject {

    }

    record _Image(Atom atom, Image value) implements ImageObject {

    }

}

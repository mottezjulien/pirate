package fr.plop.contexts.game.config.board.domain.model;

import fr.plop.generic.enumerate.Priority;
import fr.plop.generic.position.Point;
import fr.plop.generic.position.Rect;
import fr.plop.generic.tools.StringTools;

import java.util.List;

public record BoardSpace(Id id, String label, Priority priority, List<Rect> rects) {

    public record Id(String value) {
        public Id() {
            this(StringTools.generate());
        }
    }

    public BoardSpace(String label, Priority priority, List<Rect> rects) {
        this(new Id(), label, priority, rects);
    }

    public BoardSpace(List<Rect> rects) {
        this(new Id(), "", Priority.HIGHEST, rects);
    }

    public boolean in(Point point) {
        return rects.stream().anyMatch(rect -> rect.in(point));
    }

    public boolean hasLabel(String label) {
        return this.label.equalsIgnoreCase(label);
    }


}

package fr.plop.contexts.game.config.board.domain.model;

import fr.plop.generic.enumerate.Priority;
import fr.plop.generic.position.Point;
import fr.plop.generic.position.Rectangle;
import fr.plop.generic.tools.StringTools;

import java.util.List;

public record BoardSpace(Id id, String label, Priority priority, List<Rectangle> rectangles) {

    public record Id(String value) {
        public Id() {
            this(StringTools.generate());
        }
    }


    public BoardSpace(Id id, List<Rectangle> rectangles) {
        this(id, "", Priority.byDefault(), rectangles);
    }

    public BoardSpace(String label, Priority priority, List<Rectangle> rectangles) {
        this(new Id(), label, priority, rectangles);
    }

    public BoardSpace(List<Rectangle> rectangles) {
        this(new Id(), "", Priority.HIGHEST, rectangles);
    }

    public boolean in(Point point) {
        return rectangles.stream().anyMatch(rectangle -> rectangle.in(point));
    }

    public boolean hasLabel(String label) {
        return this.label.equalsIgnoreCase(label);
    }


}

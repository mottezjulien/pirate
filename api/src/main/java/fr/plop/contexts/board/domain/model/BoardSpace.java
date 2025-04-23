package fr.plop.contexts.board.domain.model;

import fr.plop.generic.tools.StringTools;

import java.util.List;

public record BoardSpace(Id id, String label, Priority priority, List<Rect> rects) {

    public enum Priority {
        HIGHEST, HIGH, MEDIUM, LOW, LOWEST
    }

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

    public boolean is(Point point) {
        return rects.stream().anyMatch(rect -> rect.is(point));
    }

    public record Rect(Point bottomLeft, Point topRight) {
        public boolean is(Point point) {
            return point.in(bottomLeft, topRight);
        }
    }

    public record Point(float lat, float lng) {
        public boolean in(Point bottomLeft, Point topRight) {
            return bottomLeft.lat() <= lat && lat <= topRight.lat()
                    && bottomLeft.lng() <= lng && lng <= topRight.lng();
        }

    }

}

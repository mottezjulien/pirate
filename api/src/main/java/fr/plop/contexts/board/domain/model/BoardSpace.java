package fr.plop.contexts.board.domain.model;

import fr.plop.generic.tools.StringTools;

import java.util.List;

public record BoardSpace(Id id, String label, int priority, List<Rect> rects) {

    public record Id(String value) {

        public Id() {
            this(StringTools.generate());
        }

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

    public BoardSpace(String label, int priority, List<Rect> rects) {
        this(new Id(), label, priority, rects);
    }

    public boolean is(Point point) {
        return rects.stream()
                .anyMatch(rect -> rect.is(point));
    }

}

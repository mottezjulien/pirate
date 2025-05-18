package fr.plop.generic.position;

public record Rect(Point bottomLeft, Point topRight) {
    public boolean in(Point point) {
        return point.in(bottomLeft, topRight);
    }
}

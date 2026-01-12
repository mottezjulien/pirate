package fr.plop.generic.position;

public record Rectangle(Point bottomLeft, Point topRight) {
    public static Rectangle ofPoints(Point bottomLeft, Point topRight) {
        return new Rectangle(bottomLeft, topRight);
    }

    public static Rectangle lyonBellecour() {
        return Rectangle.ofPoints(
                Point.from(45.75704827627986, 4.830007307181414),
                Point.from(45.75803698945227, 4.834071434198197));
    }

    public boolean in(Point point) {
        return point.in(bottomLeft, topRight);
    }
}

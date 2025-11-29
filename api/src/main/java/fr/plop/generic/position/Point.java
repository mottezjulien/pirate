package fr.plop.generic.position;

public record Point(double lat, double lng) {
    public boolean in(Point bottomLeft, Point topRight) {
        return bottomLeft.lat() <= lat && lat <= topRight.lat()
                && bottomLeft.lng() <= lng && lng <= topRight.lng();
    }

}

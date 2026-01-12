package fr.plop.generic.position;

import java.math.BigDecimal;

public record Point(BigDecimal lat, BigDecimal lng) {
    public static Point from(BigDecimal lat, BigDecimal lng) {
        return new Point(lat, lng);
    }

    public static Point from(double lat, double lng)  {
        return new Point(BigDecimal.valueOf(lat), BigDecimal.valueOf(lng));
    }

    public boolean in(Point bottomLeft, Point topRight) {
        return bottomLeft.lat().compareTo(lat) <= 0  && lat.compareTo(topRight.lat()) <= 0
                && bottomLeft.lng().compareTo(lng) <= 0 && lng.compareTo(topRight.lng()) <= 0;
    }

}

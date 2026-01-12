package fr.plop.generic.position;

public record Location(Address address, Rectangle rect) {

    public static Location lyonBellecour() {
        return new Location(Address.lyonBellecour(), Rectangle.lyonBellecour());
    }
}

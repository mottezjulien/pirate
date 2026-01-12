package fr.plop.generic.position;

public record Address(String street, String postalCode, String city, String country) {

    private static final String SEPARATOR_FIRST = ", ";
    private static final String SEPARATOR_SECOND = " ";

    public static Address fromString(String str) {
        String[] parts = str.split(SEPARATOR_FIRST);
        if (parts.length == 3) {
            String[] partPostatCodeCity = parts[1].split(SEPARATOR_SECOND);
            if (partPostatCodeCity.length == 2) {
                return new Address(parts[0], partPostatCodeCity[0], partPostatCodeCity[1], parts[2]);
            }
        }
        throw new IllegalArgumentException("Invalid address string: " + str);
    }

    @Override
    public String toString() {
        return street + SEPARATOR_FIRST + postalCode + SEPARATOR_SECOND + city + SEPARATOR_FIRST + country;
    }

    public static Address lyonBellecour() {
        return Address.fromString("Place Bellecour, 69002 Lyon, France");
    }
}

package fr.plop.generic.position;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class AddressTest {

    @Test
    public void testFromStringToString() {
        Address address = new Address("3 rue des fleurs", "75008", "Paris", "France");
        assertThat(address).isEqualTo(Address.fromString(address.toString()));
    }

}

package fr.plop.generic.tools;

import java.util.UUID;

public class StringTools {

    public static String generate() {
        return UUID.randomUUID().toString();
    }

}

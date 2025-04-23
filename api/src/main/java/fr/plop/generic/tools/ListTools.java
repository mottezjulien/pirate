package fr.plop.generic.tools;

import java.util.List;

public class ListTools {

    public static <T> List<T> removed(List<T> origin, List<T> diff) {
        return origin.stream()
                .filter(preview -> !diff.contains(preview))
                .toList();
    }

    public static <T> List<T> added(List<T> origin, List<T> diff) {
        return removed(diff, origin);
    }

}

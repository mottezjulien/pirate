package fr.plop.generic.tools;

import java.util.List;
import java.util.stream.Stream;

public class ListTools {

    public static <T> List<T> removed(List<T> origin, List<T> diff) {
        return origin.stream()
                .filter(preview -> !diff.contains(preview))
                .toList();
    }

    public static <T> List<T> added(List<T> origin, List<T> diff) {
        return removed(diff, origin);
    }

    public static <T> List<T> concat(List<T> one, List<T> other) {
        return Stream.concat(one.stream(), other.stream()).toList();
    }
}

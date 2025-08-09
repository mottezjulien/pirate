package fr.plop.contexts.game.config.template.domain;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class TreeGenerator {

    private static final String SEPARATOR_CHILDREN = "---";
    public static final String LINE_BREAK = "\n";
    public static final String SEPARATOR_INLINE = ":";

    public List<Tree> generate(String str) {
        String[] lines = str.split(LINE_BREAK);
        if(lines.length == 0 || lines[0].isEmpty()) {
            return List.of();
        }
        List<Tree> roots = new ArrayList<>();
        List<String> children = new ArrayList<>();
        Optional<String> currentRoot = Optional.empty();
        for (String s : lines) {
            String line = s.trim();
            if (!line.isBlank()) {
                if (line.startsWith(SEPARATOR_CHILDREN)) {
                    children.add(line.substring(SEPARATOR_CHILDREN.length()));
                } else {
                    if (currentRoot.isPresent()) {
                        roots.add(generateOneLine(currentRoot.orElseThrow(), children));
                        children.clear();
                    }
                    currentRoot = Optional.of(line);
                }
            }
        }
        if(currentRoot.isPresent()) {
            roots.add(generateOneLine(currentRoot.orElseThrow(), children));
        }
        return roots;
    }

    private Tree generateOneLine(String line, List<String> children) {
        List<String> split = new ArrayList<>(Arrays.asList(line.split(SEPARATOR_INLINE)));
        String header = split.getFirst().trim();
        split.removeFirst();
        String strChildren = String.join(LINE_BREAK, children);
        return new Tree(header, split.stream().map(String::trim).toList(), generate(strChildren));
    }

}

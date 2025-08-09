package fr.plop.contexts.game.config.template.domain;

import java.util.List;
import java.util.Objects;

public record Tree(String header, List<String> params, List<Tree> children) {

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Tree tree)) return false;
        return Objects.equals(header, tree.header) && Objects.equals(params, tree.params) && Objects.equals(children, tree.children);
    }

    @Override
    public int hashCode() {
        int result = Objects.hashCode(header);
        result = 31 * result + Objects.hashCode(params);
        result = 31 * result + Objects.hashCode(children);
        return result;
    }
}

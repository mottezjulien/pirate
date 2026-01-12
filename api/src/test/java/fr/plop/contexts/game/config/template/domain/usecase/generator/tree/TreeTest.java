package fr.plop.contexts.game.config.template.domain.usecase.generator.tree;

import fr.plop.contexts.game.config.template.domain.model.Tree;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class TreeTest {

    @Test
    public void findByChild() {
        Tree child0 = new Tree("PARAM2", List.of("VALUE2"));
        Tree child1 = new Tree("PARAM4", List.of("VALUE4"));
        Tree tree = new Tree("HEADER", List.of("PARAM1", "VALUE1", "PARAM3", "VALUE3"), List.of(child0, child1));
        assertThat(tree.findByKeyOrThrow("PARAM2")).isEqualTo("VALUE2");
    }

    @Test
    public void findByKey() {
        Tree child0 = new Tree("PARAM2", List.of("VALUE2"));
        Tree child1 = new Tree("PARAM4", List.of("VALUE4"));
        Tree tree = new Tree("HEADER", List.of("PARAM1", "VALUE1", "PARAM3", "VALUE3"), List.of(child0, child1));
        assertThat(tree.findByKeyOrThrow("PARAM4")).isEqualTo("VALUE4");
    }

    @Test
    public void notFound() {
        Tree child0 = new Tree("PARAM2", List.of("VALUE2"));
        Tree child1 = new Tree("PARAM4", List.of("VALUE4"));
        Tree tree = new Tree("HEADER", List.of("PARAM1", "VALUE1", "PARAM3", "VALUE3"), List.of(child0, child1));
        assertThatThrownBy(() -> tree.findByKeyOrThrow("PARAMXXX"))
                .isInstanceOf(RuntimeException.class);
    }


}

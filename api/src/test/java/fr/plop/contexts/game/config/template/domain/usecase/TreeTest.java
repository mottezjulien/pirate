package fr.plop.contexts.game.config.template.domain.usecase;

import fr.plop.contexts.game.config.template.domain.model.Tree;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class TreeTest {
    @Test
    public void uniqueHappyPath() {
        Tree tree = new Tree("HEADER", List.of("PARAM1"), List.of());
        assertThat(tree.hasUniqueParam()).isTrue();
    }

    @Test
    public void uniqueEmpty() {
        Tree tree = new Tree("HEADER", List.of(), List.of());
        assertThat(tree.hasUniqueParam()).isFalse();
    }

    @Test
    public void uniqueMoreThanOne() {
        Tree tree = new Tree("HEADER", List.of("PARAM1", "PARAM2"), List.of());
        assertThat(tree.hasUniqueParam()).isFalse();
    }

    @Test
    public void keyValueParamEmpty() {
        Tree tree = new Tree("HEADER", List.of(), List.of());
        assertThat(tree.hasParamKey("KEY1")).isFalse();
    }

    @Test
    public void keyValueParamUnique() {
        Tree tree = new Tree("HEADER", List.of("KEY1"), List.of());
        assertThat(tree.hasParamKey("KEY1")).isFalse();
    }

    @Test
    public void keyValueParamOneKeyValue() {
        Tree tree = new Tree("HEADER", List.of("KEY1", "VALUE1"), List.of());
        assertThat(tree.hasParamKey("KEY1")).isTrue();
        assertThat(tree.paramValue("KEY1")).isEqualTo("VALUE1");
    }

    @Test
    public void keyValueParamWrongSyntax() {
        Tree tree = new Tree("HEADER", List.of("KEY1", "VALUE1", "KEY2"), List.of());
        assertThat(tree.hasParamKey("KEY1")).isFalse();
    }

    @Test
    public void keyValueParamMultipleKeys() {
        Tree tree = new Tree("HEADER", List.of("KEY1", "VALUE1", "KEY2", "VALUE2", "KEY3", "VALUE3"), List.of());
        assertThat(tree.hasParamKey("KEY1")).isTrue();
        assertThat(tree.paramValue("KEY1")).isEqualTo("VALUE1");
        assertThat(tree.hasParamKey("KEY2")).isTrue();
        assertThat(tree.paramValue("KEY2")).isEqualTo("VALUE2");
        assertThat(tree.hasParamKey("KEY3")).isTrue();
        assertThat(tree.paramValue("KEY3")).isEqualTo("VALUE3");
        assertThat(tree.hasParamKey("KEY4")).isFalse();
    }

    @Test
    public void keyValueParamWrongSyntaxSameKeys() {
        Tree tree = new Tree("HEADER", List.of("KEY1", "VALUE1", "KEY1", "VALUE2"), List.of());
        assertThat(tree.hasParamKey("KEY1")).isFalse();
    }

}
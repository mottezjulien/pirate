package fr.plop.contexts.game.config.template.domain;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class TreeGeneratorTest {

    @Test
    public void empty() {
        String str = "";
        TreeGenerator treeGenerator = new TreeGenerator();
        List<Tree> roots = treeGenerator.generate(str);
        assertThat(roots).isEmpty();
    }

    @Test
    public void one() {
        String str = "abc:def:ghi";
        TreeGenerator treeGenerator = new TreeGenerator();
        List<Tree> roots = treeGenerator.generate(str);
        assertThat(roots).containsOnly(new Tree("abc", List.of("def", "ghi"), List.of()));
    }

    @Test
    public void twoRoots() {
        String str = """
                abc:def:ghi
                xyz:uv
                """;
        TreeGenerator treeGenerator = new TreeGenerator();
        List<Tree> roots = treeGenerator.generate(str);
        assertThat(roots).containsExactly(
                new Tree("abc", List.of("def", "ghi"), List.of()),
                new Tree("xyz", List.of("uv"), List.of())
        );
    }

    @Test
    public void twoRootsWith1Child() {
        String str = """
                abc:def:ghi
                --- test1:test2
                xyz:uv
                """;
        TreeGenerator treeGenerator = new TreeGenerator();
        List<Tree> roots = treeGenerator.generate(str);
        assertThat(roots).containsExactlyInAnyOrder(
                new Tree("abc", List.of("def", "ghi"), List.of(new Tree("test1", List.of("test2"), List.of()))),
                new Tree("xyz", List.of("uv"), List.of())
        );
    }

    @Test
    public void oneRootsWithChildOfChild() {
        String str = """
                abc:def:   ghi
                ---test1 
                ------  test3 : test4
                """;
        TreeGenerator treeGenerator = new TreeGenerator();
        List<Tree> roots = treeGenerator.generate(str);
        assertThat(roots).containsExactlyInAnyOrder(
                new Tree("abc", List.of("def", "ghi"), List.of(new Tree("test1", List.of(), List.of(new Tree("test3", List.of("test4"), List.of())))))
        );
    }

    @Test
    public void multiples() {
        String str = """
                Bonjour : Pouet:   Pouet2
               --- Hello
               ------ Test:Other
                ------Test2:Other2
               --- Alors:ça:va 
               --- Alors2:oui ça va !!
                """;

        TreeGenerator treeGenerator = new TreeGenerator();
        List<Tree> roots = treeGenerator.generate(str);
        assertThat(roots).containsExactlyInAnyOrder(new Tree("Bonjour",
                List.of("Pouet", "Pouet2"),
                List.of(
                        new Tree("Hello",
                                List.of(),
                                List.of(
                                        new Tree("Test",
                                                List.of("Other"),
                                                List.of()),
                                        new Tree("Test2",
                                                List.of("Other2"),
                                                List.of()))),
                        new Tree("Alors", List.of("ça", "va"), List.of()),
                        new Tree("Alors2", List.of("oui ça va !!"), List.of())
                )));

    }




}

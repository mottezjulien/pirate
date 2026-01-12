package fr.plop.contexts.game.config.template.domain.usecase.generator.tree;

import fr.plop.contexts.game.config.board.domain.model.BoardConfig;
import fr.plop.contexts.game.config.board.domain.model.BoardSpace;
import fr.plop.contexts.game.config.template.domain.model.Tree;
import fr.plop.contexts.game.config.template.domain.usecase.generator.GlobalCache;
import fr.plop.generic.enumerate.Priority;
import fr.plop.generic.position.Point;
import fr.plop.generic.position.Rectangle;

import java.util.ArrayList;
import java.util.List;

public class TemplateGeneratorTreeBoardUseCase {

    private static final String BOARD_KEY = "BOARD";
    private static final String SPACE_KEY = "SPACE";
    private static final String BOTTOM_LEFT = "bottomLeft";
    private static final String TOP_RIGHT = "topRight";

    private final GlobalCache globalCache;

    public TemplateGeneratorTreeBoardUseCase(GlobalCache globalCache) {
        this.globalCache = globalCache;
    }

    public BoardConfig apply(Tree root) {
        return new BoardConfig(generateSpaces(root.children()));
    }

    private List<BoardSpace> generateSpaces(List<Tree> trees) {
        List<BoardSpace> spaces = new ArrayList<>();
        for (Tree tree : trees) {
            if (BOARD_KEY.equalsIgnoreCase(tree.header())) {
                for (Tree spaceTree : tree.children()) {
                    if (SPACE_KEY.equalsIgnoreCase(spaceTree.header())) {
                        spaces.add(generateSpace(spaceTree));
                    }
                }
            }
        }
        return spaces;
    }

    private BoardSpace generateSpace(Tree tree) {

        String label = tree.findByKeyOrParamIndexOrValue("LABEL", 0, "");
        Priority priority = Priority.valueOf(tree.findByKeyOrParamIndexOrValue("PRIORITY", 1, Priority.byDefault().name()));

        List<Rectangle> rectangles = new ArrayList<>();
        for (Tree child : tree.children()) {
            rectangles.add(parseRectFromTree(child));
        }

        BoardSpace.Id id = new BoardSpace.Id();
        if (tree.reference() != null) {
            id = globalCache.reference(tree.reference(), BoardSpace.Id.class, id);
        }
        return new BoardSpace(id, label, priority, rectangles);
    }


    private Rectangle parseRectFromTree(Tree tree) {
        List<String> params = tree.params();
        if (tree.isHeader(BOTTOM_LEFT)) {
            Point bottomLeft = Point.from(Float.parseFloat(params.get(0)), Float.parseFloat(params.get(1)));
            Point topRight = Point.from(Float.parseFloat(params.get(3)), Float.parseFloat(params.get(4)));
            return new Rectangle(bottomLeft, topRight);
        }
        if (tree.isHeader(TOP_RIGHT)) {
            Point bottomLeft = Point.from(Float.parseFloat(params.get(3)), Float.parseFloat(params.get(4)));
            Point topRight = Point.from(Float.parseFloat(params.get(0)), Float.parseFloat(params.get(1)));
            return new Rectangle(bottomLeft, topRight);
        }
        if (params.size() == 3) {
            Point bottomLeft = Point.from(Float.parseFloat(tree.headerKeepCase()), Float.parseFloat(params.get(0)));
            Point topRight = Point.from(Float.parseFloat(params.get(1)), Float.parseFloat(params.get(2)));
            return new Rectangle(bottomLeft, topRight);
        }
        throw new RuntimeException("Impossible de parser le rectangle");
    }

}

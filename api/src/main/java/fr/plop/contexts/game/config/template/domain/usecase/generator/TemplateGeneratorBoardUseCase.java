package fr.plop.contexts.game.config.template.domain.usecase.generator;

import fr.plop.contexts.game.config.board.domain.model.BoardConfig;
import fr.plop.contexts.game.config.board.domain.model.BoardSpace;
import fr.plop.contexts.game.config.template.domain.model.Tree;
import fr.plop.generic.enumerate.Priority;
import fr.plop.generic.position.Point;
import fr.plop.generic.position.Rect;

import java.util.ArrayList;
import java.util.List;

public class TemplateGeneratorBoardUseCase {

    private static final String BOARD_KEY = "BOARD";
    private static final String SPACE_KEY = "SPACE";
    private static final String BOTTOM_LEFT = "bottomLeft";
    private static final String TOP_RIGHT = "topRight";

    private final TemplateGeneratorGlobalCache globalCache;

    public TemplateGeneratorBoardUseCase(TemplateGeneratorGlobalCache globalCache) {
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

        List<Rect> rects = new ArrayList<>();
        for (Tree child : tree.children()) {
            rects.add(parseRectFromTree(child));
        }

        BoardSpace.Id id = new BoardSpace.Id();
        if (tree.reference() != null) {
            id = globalCache.reference(tree.reference(), BoardSpace.Id.class, id);
        }
        return new BoardSpace(id, label, priority, rects);
    }


    private Rect parseRectFromTree(Tree tree) {
        List<String> params = tree.params();
        if (tree.isHeader(BOTTOM_LEFT)) {
            Point bottomLeft = new Point(Float.parseFloat(params.get(0)), Float.parseFloat(params.get(1)));
            Point topRight = new Point(Float.parseFloat(params.get(3)), Float.parseFloat(params.get(4)));
            return new Rect(bottomLeft, topRight);
        }
        if (tree.isHeader(TOP_RIGHT)) {
            Point bottomLeft = new Point(Float.parseFloat(params.get(3)), Float.parseFloat(params.get(4)));
            Point topRight = new Point(Float.parseFloat(params.get(0)), Float.parseFloat(params.get(1)));
            return new Rect(bottomLeft, topRight);
        }
        if (params.size() == 3) {
            Point bottomLeft = new Point(Float.parseFloat(tree.headerKeepCase()), Float.parseFloat(params.get(0)));
            Point topRight = new Point(Float.parseFloat(params.get(1)), Float.parseFloat(params.get(2)));
            return new Rect(bottomLeft, topRight);
        }
        throw new RuntimeException("Impossible de parser le rectangle");

/*
        if(tree.paramSize() == 3) {
            List<String> headerAndParams = ListTools.concat(List.of(tree.header()), tree.params());
            if(headerAndParams.stream().allMatch(this::isNumeric)){
                Point bottomLeft = new Point(Float.parseFloat(tree.header()), Float.parseFloat(tree.param(0)));
                Point topRight = new Point(Float.parseFloat(tree.param(1)), Float.parseFloat(tree.param(2)));
                return new Rect(bottomLeft, topRight);
            }
        }

        String header = tree.header();
        List<String> params = tree.params();

        // Format long: header="bottomLeft", params=[5.7, 10, topRight, 8.097, 50.43]
        Point bottomLeft = null;
        Point topRight = null;

        if (BOTTOM_LEFT.equalsIgnoreCase(header) && params.size() >= 2) {
            try {
                float x = Float.parseFloat(params.get(0));
                float y = Float.parseFloat(params.get(1));
                bottomLeft = new Point(x, y);

                // Chercher topRight dans la suite des params
                for (int i = 2; i < params.size(); i++) {
                    if (TOP_RIGHT.equalsIgnoreCase(params.get(i)) && i + 2 < params.size()) {
                        float x2 = Float.parseFloat(params.get(i + 1));
                        float y2 = Float.parseFloat(params.get(i + 2));
                        topRight = new Point(x2, y2);
                        break;
                    }
                }
            } catch (NumberFormatException e) {
                // Continue
            }
        } else if (TOP_RIGHT.equalsIgnoreCase(header) && params.size() >= 2) {
            try {
                float x = Float.parseFloat(params.get(0));
                float y = Float.parseFloat(params.get(1));
                topRight = new Point(x, y);

                // Chercher bottomLeft dans la suite des params
                for (int i = 2; i < params.size(); i++) {
                    if (BOTTOM_LEFT.equalsIgnoreCase(params.get(i)) && i + 2 < params.size()) {
                        float x2 = Float.parseFloat(params.get(i + 1));
                        float y2 = Float.parseFloat(params.get(i + 2));
                        bottomLeft = new Point(x2, y2);
                        break;
                    }
                }
            } catch (NumberFormatException e) {
                // Continue
            }
        }

        if (bottomLeft != null && topRight != null) {
            return new Rect(bottomLeft, topRight);
        }

        return null;*/
    }

    /*private Priority parsePriority(String priorityStr) {
        try {
            return Priority.valueOf(priorityStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            return Priority.byDefault();
        }
    }


    private boolean isNumeric(String value) {
        try {
            Float.parseFloat(value);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }*/

}

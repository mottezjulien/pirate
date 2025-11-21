package fr.plop.contexts.game.config.template.domain.usecase.generator;

import fr.plop.contexts.game.config.board.domain.model.BoardConfig;
import fr.plop.contexts.game.config.board.domain.model.BoardSpace;
import fr.plop.contexts.game.config.template.domain.TemplateException;
import fr.plop.contexts.game.config.template.domain.model.Tree;
import fr.plop.generic.position.Point;
import fr.plop.generic.position.Rect;
import fr.plop.generic.tools.ListTools;

import java.util.ArrayList;
import java.util.List;

public class TemplateGeneratorBoardUseCase {

    private static final String BOARD_KEY = "BOARD";
    private static final String SPACE_KEY = "SPACE";
    private static final String BOTTOM_LEFT = "bottomLeft";
    private static final String TOP_RIGHT = "topRight";

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

    private BoardSpace generateSpace(Tree spaceTree) {
        List<String> params = spaceTree.params();
        if (params.size() < 2) {
            throw new TemplateException("Invalid space format in tree: " + spaceTree);
        }

        String label = params.get(0);
        BoardSpace.Priority priority = parsePriority(params.get(1));

        List<Rect> rects = parseRectsFromTrees(spaceTree.children());

        return new BoardSpace(label, priority, rects);
    }

    private List<Rect> parseRectsFromTrees(List<Tree> trees) {
        List<Rect> rects = new ArrayList<>();

        for (Tree tree : trees) {
            // Les rectangles sont des arbres avec header et params
            Rect rect = parseRectFromTree(tree);
            if (rect != null) {
                rects.add(rect);
            }
        }

        return rects;
    }

    private Rect parseRectFromTree(Tree tree) {

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

        return null;
    }

    private BoardSpace.Priority parsePriority(String priorityStr) {
        return switch (priorityStr.toUpperCase()) {
            case "HIGHEST" -> BoardSpace.Priority.HIGHEST;
            case "HIGH" -> BoardSpace.Priority.HIGH;
            case "MEDIUM" -> BoardSpace.Priority.MEDIUM;
            case "LOW" -> BoardSpace.Priority.LOW;
            case "LOWEST" -> BoardSpace.Priority.LOWEST;
            default -> BoardSpace.Priority.MEDIUM;
        };
    }


    private boolean isNumeric(String value) {
        try {
            Float.parseFloat(value);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

}

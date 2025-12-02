package fr.plop.contexts.game.config.template.domain.usecase.generator;

import fr.plop.contexts.game.config.Image.domain.ImageGeneric;
import fr.plop.contexts.game.config.Image.domain.ImageObject;
import fr.plop.contexts.game.config.board.domain.model.BoardSpace;
import fr.plop.contexts.game.config.condition.Condition;
import fr.plop.contexts.game.config.map.domain.MapConfig;
import fr.plop.contexts.game.config.map.domain.MapItem;
import fr.plop.contexts.game.config.template.domain.TemplateException;
import fr.plop.contexts.game.config.template.domain.model.Tree;
import fr.plop.generic.ImagePoint;
import fr.plop.generic.enumerate.Priority;
import fr.plop.subs.image.Image;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TemplateGeneratorMapUseCase {

    public static final String KEY_PRIORITY = "PRIORITY";
    public static final String KEY_IMAGE = "IMAGE";
    public static final String KEY_POINT = "POINT";
    public static final String KEY_LABEL = "LABEL";

    private final TemplateGeneratorConditionUseCase conditionGenerator;

    private final TemplateGeneratorGlobalCache globalCache;

    public TemplateGeneratorMapUseCase(TemplateGeneratorGlobalCache globalCache) {
        this.conditionGenerator = new TemplateGeneratorConditionUseCase(globalCache);
        this.globalCache = globalCache;
    }

    public MapConfig apply(Tree root) {
        List<MapItem> mapItems = new ArrayList<>();
        for (Tree tree : root.children()) {
            if ("MAP".equalsIgnoreCase(tree.header())) {
                MapItem item = parseMapItemFromTree(tree);
                mapItems.add(item);
            }
        }
        return new MapConfig(mapItems);
    }

    private MapItem parseMapItemFromTree(Tree tree) {
        if (tree.paramSize() < 2) {
            throw new TemplateException("Invalid map format in tree: " + tree);
        }
        String label = tree.childByKeyOneParam(KEY_LABEL).orElse("");
        Image image = new Image(Image.Type.valueOf(tree.param(0).toUpperCase()), tree.param(1));
        Priority priority = tree.childByKeyOneParam(KEY_PRIORITY).map(Priority::valueOf).orElse(Priority.LOWEST);

        List<ImageObject> objects = tree.childrenByKey("OBJECT").map(this::parseObject).toList();
        ImageGeneric imageGeneric = new ImageGeneric(label, image, objects);

        List<Condition> conditions = tree.childrenByKey("CONDITION").map(conditionGenerator::apply).toList();
        Optional<Condition> optCondition = Condition.buildAndFromList(conditions);

        Optional<Image> optPointer = Optional.empty();
        Optional<Tree> pointerChild = tree.childByKey("POINTER");
        if(pointerChild.isPresent()) {
            Image pointer = new Image(Image.Type.valueOf(tree.param(0).toUpperCase()), tree.param(1));
            optPointer = Optional.of(pointer);
        }
        List<MapItem.Position> positions = tree.childrenByKey("POSITION").map(this::parsePosition).toList();
        return new MapItem(imageGeneric, priority, optCondition, optPointer, positions);
    }

    private ImageObject parseObject(Tree tree) {
        String type = tree.param(0).toUpperCase();
        String top = tree.param(1);
        String lat = tree.param(2);
        ImagePoint center = new ImagePoint(Double.parseDouble(top), Double.parseDouble(lat));

        String label = tree.childByKeyOneParam(KEY_LABEL).orElse("");
        List<Condition> conditions = tree.childrenByKey("CONDITION").map(conditionGenerator::apply).toList();
        Optional<Condition> optCondition = Condition.buildAndFromList(conditions);
        ImageObject.Atom atom = new ImageObject.Atom(label, center, optCondition);

        return switch (type) {
            case KEY_POINT -> new ImageObject.Point(atom, tree.childByKeyOneParam("COLOR").orElse(""));
            case KEY_IMAGE -> {
                Tree imagePosition = tree.childByKey(KEY_IMAGE).orElseThrow();
                Image image = new Image(Image.Type.valueOf(imagePosition.param(0).toUpperCase()), imagePosition.param(1));
                yield new ImageObject._Image(atom, image);
            }
            default -> throw new IllegalStateException("Unexpected value: " + type);
        };
    }

    private MapItem.Position parsePosition(Tree tree) {
        /*String type = tree.param(0).toUpperCase();
        String top = tree.param(1);
        String lat = tree.param(2);
        ImagePoint center = new ImagePoint(Double.parseDouble(top), Double.parseDouble(lat));

        String label = tree.childByKeyOneParam(KEY_LABEL).orElse("");
        List<Condition> conditions = tree.childrenByKey("CONDITION").map(conditionGenerator::apply).toList();
        Optional<Condition> optCondition = Condition.buildAndFromList(conditions);
        ImageObject.Atom atom = new ImageObject.Atom(label, center, optCondition);

        return switch (type) {
            case KEY_POINT -> new ImageObject.Point(atom, tree.childByKeyOneParam("COLOR").orElse(""));
            case KEY_IMAGE -> {
                Tree imagePosition = tree.childByKey(KEY_IMAGE).orElseThrow();
                Image image = new Image(Image.Type.valueOf(imagePosition.param(0).toUpperCase()), imagePosition.param(1));
                yield new ImageObject._Image(atom, image);
            }
            default -> throw new IllegalStateException("Unexpected value: " + type);
        };*/

        //BoardSpace.Id spaceId, ImagePoint point, Priority priority

        String refSpaceId = tree.paramValue("SPACE").toUpperCase();
        BoardSpace.Id spaceId = globalCache.getReference(refSpaceId, BoardSpace.Id.class).orElseThrow();

        String top = tree.paramValue("TOP");
        String lat = tree.paramValue("LEFT");
        ImagePoint point = new ImagePoint(Double.parseDouble(top), Double.parseDouble(lat));

        Priority priority = Priority.LOWEST;
        if(tree.hasParamKey("PRIORITY")) {
            priority = Priority.valueOf(tree.paramValue("PRIORITY").toUpperCase());
        }

        return new MapItem.Position(spaceId, point, priority);
    }


}

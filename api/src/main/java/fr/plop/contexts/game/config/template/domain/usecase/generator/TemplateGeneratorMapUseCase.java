package fr.plop.contexts.game.config.template.domain.usecase.generator;

import fr.plop.contexts.game.config.Image.domain.ImageGeneric;
import fr.plop.contexts.game.config.Image.domain.ImageObject;
import fr.plop.contexts.game.config.board.domain.model.BoardSpace;
import fr.plop.contexts.game.config.condition.Condition;
import fr.plop.contexts.game.config.map.domain.MapConfig;
import fr.plop.contexts.game.config.map.domain.MapItem;
import fr.plop.contexts.game.config.template.domain.model.Tree;
import fr.plop.generic.ImagePoint;
import fr.plop.generic.enumerate.Priority;
import fr.plop.subs.image.Image;

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
        return new MapConfig(root.children().stream()
                .filter(child -> child.header().equals("MAP"))
                .map(this::parseMapItemFromTree)
                .toList());
    }

    private MapItem parseMapItemFromTree(Tree tree) {
        String label = tree.findByKeyOrValue(KEY_LABEL, "");
        Priority priority = tree.findByKey(KEY_PRIORITY).map(Priority::valueOf).orElse(Priority.byDefault());

        List<ImageObject> objects = tree.childrenByKey("OBJECT").map(this::parseObject).toList();
        ImageGeneric imageGeneric = new ImageGeneric(label, toImage(tree), objects);

        List<Condition> conditions = tree.childrenByKey("CONDITION").map(conditionGenerator::apply).toList();
        Optional<Condition> optCondition = Condition.buildAndFromList(conditions);

        Optional<Image> optPointer = tree.findChildKey("POINTER")
                .map(TemplateGeneratorMapUseCase::toImage);

        List<MapItem.Position> positions = tree.childrenByKey("POSITION").map(this::parsePosition).toList();
        return new MapItem(imageGeneric, priority, optCondition, optPointer, positions);
    }



    private ImageObject parseObject(Tree tree) {
        String type = tree.findByKeyOrParamIndexOrThrow("TYPE", 0).toUpperCase();
        String top =  tree.findByKeyOrParamIndexOrThrow("TOP", 1);
        String left =  tree.findByKeyOrParamIndexOrThrow("LEFT", 2);
        ImagePoint center = new ImagePoint(Double.parseDouble(top), Double.parseDouble(left));

        String label = tree.findByKeyOrValue(KEY_LABEL, "");
        List<Condition> conditions = tree.childrenByKey("CONDITION").map(conditionGenerator::apply).toList();
        Optional<Condition> optCondition = Condition.buildAndFromList(conditions);
        ImageObject.Atom atom = new ImageObject.Atom(label, center, optCondition);
        return switch (type) {
            case KEY_POINT -> new ImageObject.Point(atom, tree.findByKeyOrValue("COLOR", ""));
            case KEY_IMAGE -> {
                Tree imagePosition = tree.findChildKey(KEY_IMAGE).orElseThrow();
                Image image = toImage(imagePosition);
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

        String refSpaceId = tree.findByKeyOrThrow("SPACE");
        BoardSpace.Id spaceId = globalCache.getReference(refSpaceId, BoardSpace.Id.class).orElseThrow();

        String top = tree.findByKeyOrParamIndexOrThrow("TOP",0);
        String lat = tree.findByKeyOrParamIndexOrThrow("LEFT",1);
        ImagePoint point = new ImagePoint(Double.parseDouble(top), Double.parseDouble(lat));

        Priority priority = tree.findByKey(KEY_PRIORITY).map(Priority::valueOf).orElse(Priority.byDefault());
        return new MapItem.Position(spaceId, point, priority);
    }

    private static Image toImage(Tree child) {
        String type =  child.findByKeyOrParamIndexOrThrow("TYPE", 0).toUpperCase();
        String value =  child.findByKeyOrParamIndexOrThrow("VALUE", 1);
        return new Image(Image.Type.valueOf(type), value);
    }

}

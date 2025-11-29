package fr.plop.contexts.game.config.template.domain.usecase.generator;

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

    public TemplateGeneratorMapUseCase(TemplateGeneratorGlobalCache context) {
        this.conditionGenerator = new TemplateGeneratorConditionUseCase(context);
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

        List<MapItem._Object> objects = tree.childrenByKey("OBJECT").map(this::parseObject).toList();

        List<Condition> conditions = tree.childrenByKey("CONDITION").map(conditionGenerator::apply).toList();
        Optional<Condition> optCondition = Condition.buildAndFromList(conditions);
        return new MapItem(label, image, priority, objects, optCondition);
    }

    private MapItem._Object parseObject(Tree tree) {
        String type = tree.param(0).toUpperCase();
        String top = tree.param(1);
        String lat = tree.param(2);
        ImagePoint center = new ImagePoint(Double.parseDouble(top), Double.parseDouble(lat));
        Priority priority = tree.childByKeyOneParam(KEY_PRIORITY)
                .map(String::toUpperCase)
                .map(Priority::valueOf).orElse(Priority.LOWEST);

        String label = tree.childByKeyOneParam(KEY_LABEL).orElse("");
        List<Condition> conditions = tree.childrenByKey("CONDITION").map(conditionGenerator::apply).toList();
        Optional<Condition> optCondition = Condition.buildAndFromList(conditions);
        MapItem._Object.Atom atom = new MapItem._Object.Atom(label, center, priority, optCondition);

        return switch (type) {
            case KEY_POINT -> new MapItem._Object.Point(atom, tree.childByKeyOneParam("COLOR").orElse(""));
            case KEY_IMAGE -> {
                Tree imagePosition = tree.childByKey(KEY_IMAGE).orElseThrow();
                Image image = new Image(Image.Type.valueOf(imagePosition.param(0).toUpperCase()), imagePosition.param(1));
                yield new MapItem._Object._Image(atom, image);
            }
            default -> throw new IllegalStateException("Unexpected value: " + type);
        };
    }

}

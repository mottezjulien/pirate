package fr.plop.contexts.game.config.template.domain.usecase.generator;

import fr.plop.contexts.game.config.Image.domain.ImageConfig;
import fr.plop.contexts.game.config.Image.domain.ImageGeneric;
import fr.plop.contexts.game.config.Image.domain.ImageItem;
import fr.plop.contexts.game.config.template.domain.model.Tree;
import fr.plop.subs.image.Image;

import java.util.ArrayList;
import java.util.List;

public class TemplateGeneratorImageUseCase {
    public ImageConfig apply(Tree root) {
        List<ImageItem> items = new ArrayList<>();
        for (Tree child : root.children()) {
            if ("IMAGE".equalsIgnoreCase(child.header())) {
                items.add(generateItem(child));
            }
        }
        return new ImageConfig(items);
    }

    private ImageItem generateItem(Tree tree) {
        //Image:ASSET:pouet/img.jpg
        Image.Type type = Image.Type.valueOf(tree.param(0).toUpperCase());
        Image value = new Image(type, tree.param(1));
        ImageGeneric generic = new ImageGeneric("", value, List.of());
        return new ImageItem(generic);
    }
}

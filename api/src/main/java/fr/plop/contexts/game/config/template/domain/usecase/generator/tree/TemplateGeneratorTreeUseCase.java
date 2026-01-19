package fr.plop.contexts.game.config.template.domain.usecase.generator.tree;

import fr.plop.contexts.game.config.Image.domain.ImageConfig;
import fr.plop.contexts.game.config.board.domain.model.BoardConfig;
import fr.plop.contexts.game.config.inventory.domain.model.InventoryConfig;
import fr.plop.contexts.game.config.map.domain.MapConfig;
import fr.plop.contexts.game.config.scenario.domain.model.ScenarioConfig;
import fr.plop.contexts.game.config.talk.domain.TalkConfig;
import fr.plop.contexts.game.config.template.domain.TemplateException;
import fr.plop.contexts.game.config.template.domain.model.Template;
import fr.plop.contexts.game.config.template.domain.model.Tree;
import fr.plop.contexts.game.config.template.domain.usecase.generator.GlobalCache;

import java.time.Duration;
import java.util.List;

public class TemplateGeneratorTreeUseCase {

    public static final String DEFAULT_VERSION = "0.0.0";

    public static class Script {
        private final String value;

        public Script(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    private final TreeGenerator treeGenerator = new TreeGenerator();
    private final GlobalCache globalCache = new GlobalCache();
    private final TemplateGeneratorTreeScenarioUseCase scenarioGenerator = new TemplateGeneratorTreeScenarioUseCase(globalCache);
    private final TemplateGeneratorTreeBoardUseCase boardGenerator = new TemplateGeneratorTreeBoardUseCase(globalCache);
    private final TemplateGeneratorTreeTalkUseCase talkGenerator = new TemplateGeneratorTreeTalkUseCase(globalCache);
    private final TemplateGeneratorTreeImageUseCase imageGenerator = new TemplateGeneratorTreeImageUseCase();
    private final TemplateGeneratorTreeMapUseCase mapGenerator = new TemplateGeneratorTreeMapUseCase(globalCache);

    public Template apply(Script script) {
        // Parse avec TreeGenerator pour avoir une structure d'arbre
        List<Tree> trees = treeGenerator.generate(script.getValue());

        if (trees.isEmpty()) {
            throw new TemplateException("Script is empty or invalid");
        }

        Tree rootTree = trees.getFirst();

        BoardConfig board = boardGenerator.apply(rootTree);

        TalkConfig talkConfig = talkGenerator.apply(rootTree);



        ImageConfig image = imageGenerator.apply(rootTree);

        ScenarioConfig scenario = scenarioGenerator.apply(rootTree, talkConfig);

        MapConfig map = mapGenerator.apply(rootTree);

        InventoryConfig inventory = new InventoryConfig(); //TODO


        String version = rootTree.findByKeyOrParamIndexOrValue("VERSION", 0, DEFAULT_VERSION);
        String label = rootTree.findByKeyOrParamIndexOrValue("LABEL", 1, "");
        String durationStr = rootTree.findByKeyOrParamIndexOrValue("DURATION", 2, "60");
        Duration duration = Duration.ofMinutes(Integer.parseInt(durationStr));

        Template.Atom templateAtom = new Template.Atom(new Template.Id(), new Template.Code(rootTree.headerKeepCase()));

        Template.Descriptor descriptor = Template.Descriptor.empty();

        return new Template(templateAtom, label, version, descriptor, duration, scenario, board, map, talkConfig, image, inventory);
    }

}

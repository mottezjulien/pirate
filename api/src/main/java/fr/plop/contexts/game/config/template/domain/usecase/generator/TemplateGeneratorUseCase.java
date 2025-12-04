package fr.plop.contexts.game.config.template.domain.usecase.generator;

import fr.plop.contexts.game.config.Image.domain.ImageConfig;
import fr.plop.contexts.game.config.board.domain.model.BoardConfig;
import fr.plop.contexts.game.config.map.domain.MapConfig;
import fr.plop.contexts.game.config.scenario.domain.model.ScenarioConfig;
import fr.plop.contexts.game.config.talk.domain.TalkConfig;
import fr.plop.contexts.game.config.template.domain.TemplateException;
import fr.plop.contexts.game.config.template.domain.model.Template;
import fr.plop.contexts.game.config.template.domain.model.Tree;
import fr.plop.contexts.game.config.template.domain.usecase.TreeGenerator;

import java.time.Duration;
import java.util.List;

public class TemplateGeneratorUseCase {

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
    private final TemplateGeneratorGlobalCache globalCache = new TemplateGeneratorGlobalCache();
    private final TemplateGeneratorScenarioUseCase scenarioGenerator = new TemplateGeneratorScenarioUseCase(globalCache);
    private final TemplateGeneratorBoardUseCase boardGenerator = new TemplateGeneratorBoardUseCase(globalCache);
    private final TemplateGeneratorTalkUseCase talkGenerator = new TemplateGeneratorTalkUseCase(globalCache);
    private final TemplateGeneratorImageUseCase imageGenerator = new TemplateGeneratorImageUseCase();
    private final TemplateGeneratorMapUseCase mapGenerator = new TemplateGeneratorMapUseCase(globalCache);

    public Template apply(Script script) {
        // Parse avec TreeGenerator pour avoir une structure d'arbre
        List<Tree> trees = treeGenerator.generate(script.getValue());

        if (trees.isEmpty()) {
            throw new TemplateException("Script is empty or invalid");
        }

        Tree rootTree = trees.getFirst();

        BoardConfig board = boardGenerator.apply(rootTree);

        TalkConfig talkConfig = talkGenerator.apply(rootTree);

        ScenarioConfig scenario = scenarioGenerator.apply(rootTree, talkConfig);

        ImageConfig image = imageGenerator.apply(rootTree);

        MapConfig map = mapGenerator.apply(rootTree);

        // 4. Vérifier qu'il n'y a pas de références non résolues
        if (globalCache.hasUnresolvedReferences()) {
            throw new TemplateException("Unresolved references: " + globalCache.getUnresolvedReferences());
        }

        String version = rootTree.findByKeyOrParamIndexOrValue("VERSION", 0, DEFAULT_VERSION);
        String label = rootTree.findByKeyOrParamIndexOrValue("LABEL", 1, "");
        String durationStr = rootTree.findByKeyOrParamIndexOrValue("DURATION", 2, "60");
        Duration duration = Duration.ofMinutes(Integer.parseInt(durationStr));

        Template.Atom templateAtom = new Template.Atom(new Template.Id(), new Template.Code(rootTree.headerKeepCase()));
        return new Template(templateAtom, label, version, duration, scenario, board, map, talkConfig, image);
    }

}

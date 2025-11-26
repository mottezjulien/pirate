package fr.plop.contexts.game.config.template.domain.usecase.generator;

import fr.plop.contexts.game.config.Image.domain.ImageConfig;
import fr.plop.contexts.game.config.board.domain.model.BoardConfig;
import fr.plop.contexts.game.config.board.domain.model.BoardSpace;
import fr.plop.contexts.game.config.condition.Condition;
import fr.plop.contexts.game.config.consequence.Consequence;
import fr.plop.contexts.game.config.map.domain.MapConfig;
import fr.plop.contexts.game.config.map.domain.MapItem;
import fr.plop.contexts.game.config.scenario.domain.model.Possibility;
import fr.plop.contexts.game.config.scenario.domain.model.PossibilityRecurrence;
import fr.plop.contexts.game.config.scenario.domain.model.PossibilityTrigger;
import fr.plop.contexts.game.config.scenario.domain.model.ScenarioConfig;
import fr.plop.contexts.game.config.talk.domain.TalkConfig;
import fr.plop.contexts.game.config.talk.domain.TalkItem;
import fr.plop.contexts.game.config.template.domain.TemplateException;
import fr.plop.contexts.game.config.template.domain.model.Template;
import fr.plop.contexts.game.config.template.domain.model.Tree;
import fr.plop.contexts.game.config.template.domain.usecase.TreeGenerator;
import fr.plop.contexts.game.session.core.domain.model.SessionGameOver;
import fr.plop.contexts.game.session.scenario.domain.model.ScenarioSessionState;
import fr.plop.contexts.game.session.time.GameSessionTimeUnit;
import fr.plop.generic.enumerate.BeforeOrAfter;
import fr.plop.subs.i18n.domain.I18n;
import fr.plop.subs.i18n.domain.Language;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

public class TemplateGeneratorUseCase {

    private static final String SEPARATOR = ":";
    public static final String DEFAULT_VERSION = "0.0.0";
    public static final String POSSIBILITY_CONDITION_KEY = "CONDITION";
    public static final String POSSIBILITY_CONSEQUENCE_KEY = "CONSEQUENCE";
    public static final String POSSIBILITY_TRIGGER_KEY = "TRIGGER";
    public static final String POSSIBILITY_RECURRENCE_KEY = "RECURRENCE";

    // Mots-clés des éléments principaux
    public static final String STEP_KEY = "STEP";
    public static final String TARGET_KEY = "TARGET";
    public static final String POSSIBILITY_KEY = "POSSIBILITY";

    private static final String PARAM_KEY_TALK_OPTION = "option";

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
    private final TemplateGeneratorCache context = new TemplateGeneratorCache();

    private final TemplateGeneratorBoardUseCase boardGenerator = new TemplateGeneratorBoardUseCase();
    private final TemplateGeneratorTalkUseCase talkGenerator = new TemplateGeneratorTalkUseCase(context);
    private final TemplateGeneratorImageUseCase imageGenerator = new TemplateGeneratorImageUseCase();
    private final TemplateGeneratorI18nUseCase i18nGenerator = new TemplateGeneratorI18nUseCase();

    public Template apply(Script script) {
        // Parse avec TreeGenerator pour avoir une structure d'arbre
        List<Tree> trees = treeGenerator.generate(script.getValue());

        if (trees.isEmpty()) {
            throw new TemplateException("Script is empty or invalid");
        }

        Tree rootTree = trees.getFirst();

        BoardConfig board = boardGenerator.apply(rootTree);

        TalkConfig talk = talkGenerator.apply(rootTree);

        ImageConfig image = imageGenerator.apply(rootTree);

        // 3. Parse les Steps en utilisant la map pour résoudre les SpaceId et le context pour les références
        ScenarioConfig scenario = new ScenarioConfig(parseSteps(rootTree, board, context));

        MapConfig map = new MapConfig(parseMapItemsFromTrees(rootTree.children()));

        // 4. Vérifier qu'il n'y a pas de références non résolues
        if (context.hasUnresolvedReferences()) {
            throw new TemplateException("Unresolved references: " + context.getUnresolvedReferences());
        }

        String version = rootTree.hasParams() ? rootTree.param(0) : DEFAULT_VERSION;
        String label = rootTree.paramSize() > 1 ? rootTree.param(1) : "";
        Duration duration = Duration.ofHours(1);
        if (rootTree.paramSize() > 2 && !rootTree.param(2).isEmpty()) {
            duration = Duration.ofMinutes(Long.parseLong(rootTree.param(2)));
        }
        Template.Atom templateAtom = new Template.Atom(new Template.Id(), new Template.Code(rootTree.headerKeepCase()));
        return new Template(templateAtom, label, version, duration, scenario, board, map, talk, image);
    }

    // ============ NOUVELLES METHODES BASEES SUR TREEGENERATOR ============

    private BoardSpace.Id findSpaceId(BoardConfig board, String value) {
        Optional<BoardSpace> optSpace = board.findByLabel(value);
        return optSpace.map(BoardSpace::id)
                .orElseGet(() -> new BoardSpace.Id(value));
    }

    private List<MapItem> parseMapItemsFromTrees(List<Tree> trees) {
        List<MapItem> mapItems = new ArrayList<>();
        for (Tree tree : trees) {
            if ("MAP".equalsIgnoreCase(tree.header())) {
                MapItem item = parseMapItemFromTree(tree);
                mapItems.add(item);
            }
        }
        return mapItems;
    }

    private MapItem parseMapItemFromTree(Tree mapTree) {
        List<String> params = mapTree.params();
        if (params.size() < 2) {
            throw new TemplateException("Invalid map format in tree: " + mapTree);
        }

        // Format: "Map:Asset:imgs/first/map.png"
        //String type = params.get(0); // "Asset"
        String imagePath = params.get(1); // "imgs/first/map.png"

        // Parse priority and positions from children
        MapItem.Priority priority = MapItem.Priority.LOW; // default
        List<MapItem.Position> positions = new ArrayList<>();
        Set<ScenarioConfig.Step.Id> stepIds = new HashSet<>();

        for (Tree child : mapTree.children()) {
            if ("PRIORITY".equalsIgnoreCase(child.header()) && !child.params().isEmpty()) {
                priority = MapItem.Priority.valueOf(child.params().getFirst().toUpperCase());
            } else if ("POSITION".equalsIgnoreCase(child.header()) && child.params().size() >= 2) {
                // Format: "position:89.09:10.064"
                float x = Float.parseFloat(child.params().get(0));
                float y = Float.parseFloat(child.params().get(1));

                // Parse priority and step IDs from position children
                MapItem.Priority positionPriority = MapItem.Priority.LOW; // default
                for (Tree posChild : child.children()) {
                    if ("PRIORITY".equalsIgnoreCase(posChild.header()) && !posChild.params().isEmpty()) {
                        positionPriority = MapItem.Priority.valueOf(posChild.params().getFirst().toUpperCase());
                    } else if ("SPACE".equalsIgnoreCase(posChild.header()) && !posChild.params().isEmpty()) {
                        stepIds.add(new ScenarioConfig.Step.Id(posChild.params().getFirst()));
                    }
                }

                MapItem.Position.Atom atom = new MapItem.Position.Atom(new MapItem.Position.Id(), "", positionPriority, List.of());
                MapItem.Position position = new MapItem.Position.Point(atom, x, y);
                positions.add(position);
            }
        }

        // Convertir Set<ScenarioConfig.Step.Id> en List<ScenarioConfig.Step.Id>
        List<ScenarioConfig.Step.Id> stepIdsList = new ArrayList<>(stepIds);

        return new MapItem(
                new MapItem.Id(),
                new I18n(Map.of()), // value I18n vide
                new MapItem.Image(MapItem.Image.Type.ASSET, imagePath, new MapItem.Image.Size(0, 0)),
                priority,
                positions,
                stepIdsList
        );
    }

    // ============ ANCIENNES METHODES (à conserver pour le moment) ============

    private List<ScenarioConfig.Step> parseSteps(Tree root, BoardConfig board, TemplateGeneratorCache context) {
        List<ScenarioConfig.Step> steps = new ArrayList<>();
        root.children().forEach(child -> {
            if (child.header().startsWith(STEP_KEY)) {
                ScenarioConfig.Step step = parseStep(child, board, context);
                steps.add(step);
            }
        });
        return steps;
    }

    private ScenarioConfig.Step parseStep(Tree root, BoardConfig board, TemplateGeneratorCache context) {
        // Extraire la référence du header si elle existe : "Step(ref REF_STEP_A)"
        String referenceName = root.reference();

        // Format: "---" + " Step:FR:Chez Moi:EN:At Home" ou "---" + " Step"
        I18n stepLabel = parseI18nFromLine(root).orElseThrow();

        // Parse les targets et possibilities qui suivent
        List<ScenarioConfig.Target> targets = new ArrayList<>();
        List<Possibility> possibilities = new ArrayList<>();
        List<Tree> possibilityTrees = new ArrayList<>();

        root.children().forEach(child -> {
            switch (child.header()) {
                case TARGET_KEY:
                    ScenarioConfig.Target target = parseTarget(child, context);
                    targets.add(target);
                    break;
                case POSSIBILITY_KEY:
                    possibilityTrees.add(child);
                    break;
                default:
                    // Vérifions si c'est un Target qui commence par TARGET mais avec du texte après
                    if (child.header().startsWith(TARGET_KEY)) {
                        ScenarioConfig.Target extendedTarget = parseTarget(child, context);
                        targets.add(extendedTarget);
                    }
                    break;
            }
        });

        // Créer le step d'abord (sans les possibilités)
        ScenarioConfig.Step step = new ScenarioConfig.Step(new ScenarioConfig.Step.Id(), stepLabel, targets, new ArrayList<>());

        // Enregistrer la référence immédiatement si elle existe
        if (referenceName != null) {
            context.registerReference(referenceName, step);
        }

        // Enregistrer le step actuel comme "CURRENT_STEP" AVANT de parser les possibilités
        context.registerReference("CURRENT_STEP", step);

        // Parse toutes les possibilités maintenant que CURRENT_STEP est disponible
        for (Tree possibilityTree : possibilityTrees) {
            Possibility possibility = parsePossibility(possibilityTree, board, context);
            possibilities.add(possibility);
        }

        // Mettre à jour le step avec les possibilités
        ScenarioConfig.Step finalStep = new ScenarioConfig.Step(step.id(), stepLabel, targets, possibilities);

        // Mettre à jour les références avec le step final
        if (referenceName != null) {
            context.registerReference(referenceName, finalStep);
        }
        context.registerReference("CURRENT_STEP", finalStep);

        return finalStep;
    }

    private ScenarioConfig.Target parseTarget(Tree tree, TemplateGeneratorCache context) {
        // Extraire la référence du header si elle existe : "Target (ref TARGET_ATTERRIR):FR:..."
        String referenceName = tree.reference();

        // 1. Détecter si c'est optionnel
        boolean optional = tree.originalHeader().toUpperCase().contains("(OPT)");

        // 2. Parser le value I18n depuis les paramètres OU depuis le header
        Optional<I18n> targetLabel = parseI18nFromLine(tree);
        if (targetLabel.isEmpty()) {
            // Si pas de params, extraire le value du header (enlever "Target " du début)
            String headerText = tree.header();
            if (headerText.toUpperCase().startsWith(TARGET_KEY + " ")) {
                String labelText = headerText.substring((TARGET_KEY + " ").length()).trim();
                // Enlever "(Opt)" si présent
                labelText = labelText.replace("(Opt)", "").trim();
                labelText = labelText.replace("(OPT)", "").trim();
                // Enlever la référence si présente
                if (referenceName != null) {
                    labelText = labelText.replace("(ref " + referenceName + ")", "").trim();
                }
                if (!labelText.isEmpty()) {
                    // Pour l'instant, pas de traduction donc même texte en FR et EN
                    Map<Language, String> values = new HashMap<>();
                    values.put(Language.FR, labelText);
                    values.put(Language.EN, labelText);
                    targetLabel = Optional.of(new I18n(values));
                }
            }
        }

        // 3. Parser la description depuis les children (lignes de description)
        Optional<I18n> description = i18nGenerator.apply(tree.children());

        ScenarioConfig.Target target = new ScenarioConfig.Target(
                new ScenarioConfig.Target.Id(),
                targetLabel.orElseThrow(),
                description,
                optional
        );

        // Enregistrer la référence si elle existe
        if (referenceName != null) {
            context.registerReference(referenceName, target);
        }
        return target;
    }

    private Optional<I18n> parseI18nFromLine(Tree tree) {
        // Format: "FR:Chez Moi:EN:At Home" ou "EN:Office:FR:Bureau"
        if (tree.paramSize() >= 4) {
            Map<Language, String> i18nMap = new HashMap<>();
            for (int i = 0; i < tree.paramSize() - 1; i += 2) {
                i18nMap.put(Language.valueOf(tree.param(i).trim()), tree.param(i + 1).trim());
            }
            if (!i18nMap.isEmpty()) {
                return Optional.of(new I18n(i18nMap));
            }
        }
        return Optional.empty();
    }

    private Possibility parsePossibility(Tree tree, BoardConfig board, TemplateGeneratorCache context) {
        // Format: "------" + " Possibility:ALWAYS" ou "------" + " Possibility:times:4:OR" ou "------" + " Possibility"
        AtomicReference<PossibilityRecurrence> recurrence = new AtomicReference<>(new PossibilityRecurrence.Always(new PossibilityRecurrence.Id())); // Par défaut



        if (!tree.params().isEmpty()) {
            String typeStr = tree.params().getFirst().trim().toUpperCase();
            switch (typeStr) {
                case "ALWAYS":
                    recurrence.set(new PossibilityRecurrence.Always(new PossibilityRecurrence.Id()));
                    break;
                case "TIMES":
                    int times = Integer.parseInt(tree.params().get(1));
                    recurrence.set(new PossibilityRecurrence.Times(new PossibilityRecurrence.Id(), times));
                    break;
                default:
                    throw new TemplateException("Invalid format: " + tree.header() + SEPARATOR + String.join(SEPARATOR, tree.params()));
            }
        }

        // Parse les conditions, conséquences et trigger qui suivent
        //AtomicReference<Condition> condition = new AtomicReference<>(null);
        List<Condition> conditions = new ArrayList<>();
        List<Consequence> consequences = new ArrayList<>();
        AtomicReference<PossibilityTrigger> trigger = new AtomicReference<>();

        // Parse en deux passes : d'abord les conséquences pour enregistrer les références, puis les triggers
        // Première passe : conséquences et conditions
        tree.children().forEach(child -> {
            String actionStr = child.header().trim();
            switch (actionStr) {
                case POSSIBILITY_CONDITION_KEY:
                    //condition.set(parseCondition(child, board));
                    conditions.add(parseCondition(child, board));
                    break;
                case POSSIBILITY_CONSEQUENCE_KEY:
                    consequences.add(parseConsequence(child, context));
                    break;
                case POSSIBILITY_RECURRENCE_KEY:
                    recurrence.set(parseRecurrence(child));
                    break;
                case POSSIBILITY_TRIGGER_KEY:
                    // Ignorer pour l'instant, sera traité dans la deuxième passe
                    break;
                default:
                    throw new TemplateException("Invalid format: " + child.header() + SEPARATOR + String.join(SEPARATOR, child.params()));
            }
        });

        // Deuxième passe : triggers (après que les références des conséquences soient enregistrées)
        tree.children().forEach(child -> {
            String actionStr = child.header().trim();
            if (POSSIBILITY_TRIGGER_KEY.equals(actionStr)) {
                trigger.set(parseTrigger(child, board, context));
            }
        });

        if (trigger.get() == null) {
            throw new TemplateException("Invalid format: " + tree.header() + SEPARATOR + String.join(SEPARATOR, tree.params()));
        }
        if (!conditions.isEmpty()) {
            if (conditions.size() > 1) {
                Condition.And and = new Condition.And(new Condition.Id(), conditions);
                return new Possibility(recurrence.get(), trigger.get(), and, consequences);
            }
            return new Possibility(recurrence.get(), trigger.get(), conditions.getFirst(), consequences);
        }
        return new Possibility(recurrence.get(), trigger.get(), consequences);
    }

    private Condition parseCondition(Tree tree, BoardConfig board) {

        String type = tree.params().getFirst().toLowerCase();
        switch (type) {
            case "insidespace" -> {
                // Format: "CONDITION:InsideSpace:spaceId:LABEL" ou "CONDITION:InsideSpace:LABEL" (legacy)
                Tree subTree = tree.sub();
                String spaceLabel;
                if (subTree.hasUniqueParam()) {
                    spaceLabel = subTree.uniqueParam();
                } else if (subTree.hasParamKey("spaceId")) {
                    spaceLabel = subTree.paramValue("spaceId");
                } else {
                    throw new TemplateException("InsideSpace missing required parameter: spaceId");
                }

                BoardSpace.Id spaceId = findSpaceId(board, spaceLabel);
                return new Condition.InsideSpace(
                        new Condition.Id(),
                        spaceId
                );
            }
            case "outsidespace" -> {
                if (tree.params().size() == 2) {
                    BoardSpace.Id spaceId = findSpaceId(board, tree.params().get(1));
                    return new Condition.OutsideSpace(
                            new Condition.Id(),
                            spaceId
                    );
                }
                BoardSpace.Id spaceId = findSpaceId(board, tree.params().get(2));
                return new Condition.OutsideSpace(new Condition.Id(), spaceId);
            }
            case "absolutetime" -> {
                // Format: "CONDITION:ABSOLUTETIME:Duration:27" -> params=[ABSOLUTETIME, Duration, 27] 
                // ou "condition:ABSOLUTETIME:27" -> params=[ABSOLUTETIME, 27]
                Tree subTree = tree.sub();
                String durationStr;
                if (subTree.hasUniqueParam()) {
                    durationStr = subTree.uniqueParam();
                } else if (subTree.hasParamKey("Duration")) {
                    durationStr = subTree.paramValue("Duration");
                } else {
                    throw new TemplateException("AbsoluteTime condition missing required parameter: Duration");
                }
                return new Condition.AbsoluteTime(new Condition.Id(), GameSessionTimeUnit.ofMinutes(Integer.parseInt(durationStr)), BeforeOrAfter.BEFORE);
            }
            case "instep" -> {
                // Format: "CONDITION:InStep:stepId:0987" ou "CONDITION:InStep:0987" (legacy)
                Tree subTree = tree.sub();
                String stepIdStr;
                if (subTree.hasUniqueParam()) {
                    stepIdStr = subTree.uniqueParam();
                } else if (subTree.hasParamKey("stepId")) {
                    stepIdStr = subTree.paramValue("stepId");
                } else {
                    throw new TemplateException("InStep missing required parameter: stepId");
                }

                return new Condition.Step(
                        new Condition.Id(),
                        new ScenarioConfig.Step.Id(stepIdStr)
                );
            }

            case "steptarget" -> {
                // Format: "CONDITION:StepTarget:targetId:TARGET_REF" ou "CONDITION:StepTarget:TARGET_REF" (legacy)
                Tree subTree = tree.sub();
                String targetIdStr;
                if (subTree.hasUniqueParam()) {
                    targetIdStr = subTree.uniqueParam();
                } else if (subTree.hasParamKey("targetId")) {
                    targetIdStr = subTree.paramValue("targetId");
                } else {
                    throw new TemplateException("StepTarget missing required parameter: targetId");
                }

                return new Condition.Target(
                        new Condition.Id(),
                        new ScenarioConfig.Target.Id(targetIdStr)
                );
            }
        }

        throw new TemplateException("Invalid condition format: " + String.join(SEPARATOR, tree.params()));
    }

    private PossibilityRecurrence parseRecurrence(Tree tree) {
        // Format: "---------" + " Recurrency:tiMes:5" ou "---------" + " Recurrence:ALWAYS"

        String type = tree.params().getFirst().toLowerCase();
        if ("always".equals(type)) {
            return new PossibilityRecurrence.Always(new PossibilityRecurrence.Id());
        } else if ("times".equals(type)) {
            try {
                int times = Integer.parseInt(tree.params().get(1));
                return new PossibilityRecurrence.Times(new PossibilityRecurrence.Id(), times);
            } catch (NumberFormatException e) {
                throw new TemplateException("Invalid recurrence format: " + String.join(SEPARATOR, tree.params()));
            }
        }

        throw new TemplateException("Invalid recurrence format: " + String.join(SEPARATOR, tree.params()));
    }

    private Consequence parseConsequence(Tree tree, TemplateGeneratorCache context) {
        // Format: "---------" + " consequence:Alert" ou "---------" + " consequence:GoalTarget:stepId:EFG:targetId:9876:state:FAILURE"

        if (tree.params().isEmpty()) {
            throw new TemplateException("Invalid consequence format: no type specified");
        }

        String type = tree.params().getFirst().toUpperCase();
        switch (type) {
            case "ALERT" -> {
                // Parse le value I18n qui suit depuis les enfants (sans \n final pour les Alert)
                Optional<I18n> messageOpt = i18nGenerator.apply(tree.children());
                I18n message = messageOpt.orElse(new I18n(Map.of())); // I18n vide si pas de value
                return new Consequence.DisplayMessage(new Consequence.Id(), message);
            }
            case "GOALTARGET" -> {
                return parseGoalTargetConsequence(tree, context);
            }
            case "GOAL" -> {
                // Format: "Goal:state:SUCCESS:stepId:KLM" ou autre ordre
                Map<String, String> paramMap = parseKeyValueParams(tree.params());

                String stateParam = paramMap.get("state");
                String stepIdParam = paramMap.get("stepid");

                if (stateParam == null || stepIdParam == null) {
                    throw new TemplateException("Goal missing required parameters: state, stepId");
                }

                ScenarioSessionState state = parseState(stateParam);
                ScenarioConfig.Step.Id stepId = new ScenarioConfig.Step.Id(stepIdParam);
                return new Consequence.ScenarioStep(new Consequence.Id(), stepId, state);
            }
            case "ADDOBJECT" -> {
                // Format: "AddObjet:objetId:SWORD123"
                if (tree.params().size() < 3) {
                    throw new TemplateException("AddObjet missing required parameters: objetId");
                }

                // Support legacy format directement ou nouveau format key:value
                String objetId;
                // Nouveau: "AddObjet:objetId:SWORD123"
                Map<String, String> paramMap = parseKeyValueParams(tree.params());
                objetId = paramMap.get("objetid");
                if (objetId == null) {
                    throw new TemplateException("AddObjet missing required parameter: objetId");
                }

                return new Consequence.ObjetAdd(new Consequence.Id(), objetId);
            }
            case "UPDATEDMETADATA" -> {
                // Format: "UpdatedMetadata:metadataId:META123:value:25.5" ou "UpdatedMetadata:metadataId:META123:toMinutes:25.5"
                Map<String, String> paramMap = parseKeyValueParams(tree.params());

                String metadataId = paramMap.get("metadataid");
                String valueStr = paramMap.get("value");
                if (valueStr == null) {
                    valueStr = paramMap.get("tominutes"); // Support legacy format
                }

                if (metadataId == null || valueStr == null) {
                    throw new TemplateException("UpdatedMetadata missing required parameters: metadataId, value");
                }

                float value = Float.parseFloat(valueStr);
                return new Consequence.UpdatedMetadata(new Consequence.Id(), metadataId, value);
            }
            case "TALKOPTIONS" -> {
                return parseTalkOptionsConsequence(tree, context);
            }
            case "TALK" -> {
                return parseTalkConsequence(tree, context);
            }
            case "GAMEOVER" -> {
                return parseGameOverConsequence(tree);
            }
        }
        throw new TemplateException("Invalid consequence format: " + type);
    }
/*
    private I18n parseAlertMessage(String[] allLines, int startIndex) {
        Map<Language, String> messages = new HashMap<>();
        StringBuilder currentMessage = new StringBuilder();
        Language currentLang = null;

        for (int i = startIndex; i < allLines.length; i++) {
            String line = allLines[i].trim();

            if (line.startsWith(SPACE_3 + " EN:")) {
                // Sauvegarder le value précédent
                if (currentLang != null && !currentMessage.isEmpty()) {
                    messages.put(currentLang, currentMessage.toString().trim());
                }
                currentLang = Language.EN;
                currentMessage = new StringBuilder(line.substring((SPACE_3 + " EN:").length()).trim());
            } else if (line.startsWith(SPACE_3 + " FR:")) {
                // Sauvegarder le value précédent
                if (currentLang != null && !currentMessage.isEmpty()) {
                    messages.put(currentLang, currentMessage.toString().trim());
                }
                currentLang = Language.FR;
                currentMessage = new StringBuilder(line.substring((SPACE_3 + " FR:").length()).trim());
            } else if (line.startsWith(SPACE_3 + " ") && currentLang != null) {
                // Suite du value
                String content = line.substring((SPACE_3 + " ").length()).trim();
                if (!currentMessage.isEmpty()) {
                    currentMessage.append("\n");
                }
                currentMessage.append(content);
            } else if (line.startsWith(SPACE_2)) {
                // Sauvegarder le dernier value et arrêter
                if (currentLang != null && !currentMessage.isEmpty()) {
                    messages.put(currentLang, currentMessage.toString().trim());
                }
                break;
            }
        }

        // Sauvegarder le dernier value si on arrive à la fin
        if (currentLang != null && !currentMessage.isEmpty()) {
            messages.put(currentLang, currentMessage.toString().trim());
        }

        return new I18n(messages);
    }*/

    private PossibilityTrigger parseTrigger(Tree tree, BoardConfig board, TemplateGeneratorCache context) {

        String type = tree.params().getFirst().toLowerCase();
        switch (type) {
            case "goinspace" -> {
                // Format: "Trigger:GoInSpace:SpaceId:ABCD" ou "Trigger:GoInSpace:EFG" (legacy)
                Tree subTree = tree.sub();
                String spaceValue;
                if (subTree.hasUniqueParam()) {
                    spaceValue = subTree.uniqueParam();
                } else if (subTree.hasParamKey("SpaceId")) {
                    spaceValue = subTree.paramValue("SpaceId");
                } else {
                    throw new TemplateException("GoInSpace missing required parameter: SpaceId");
                }

                BoardSpace.Id spaceId = findSpaceId(board, spaceValue);
                return new PossibilityTrigger.SpaceGoIn(
                        new PossibilityTrigger.Id(),
                        spaceId
                );
            }
            case "gooutspace" -> {
                // Format: "Trigger:GoOutSpace:SpaceId:ABCD" ou "Trigger:GoOutSpace:EFG" (legacy)
                Tree subTree = tree.sub();
                String spaceValue;
                if (subTree.hasUniqueParam()) {
                    spaceValue = subTree.uniqueParam();
                } else if (subTree.hasParamKey("SpaceId")) {
                    spaceValue = subTree.paramValue("SpaceId");
                } else {
                    throw new TemplateException("GoOutSpace missing required parameter: SpaceId");
                }

                BoardSpace.Id spaceId = findSpaceId(board, spaceValue);
                return new PossibilityTrigger.SpaceGoOut(
                        new PossibilityTrigger.Id(),
                        spaceId
                );
            }
            case "absolutetime" -> {
                // Format: "Trigger:AbsoluteTime:value:42" ou "Trigger:AbsoluteTime:42" (legacy)
                Tree subTree = tree.sub();
                String valueStr;
                if (subTree.hasUniqueParam()) {
                    valueStr = subTree.uniqueParam();
                } else if (subTree.hasParamKey("value")) {
                    valueStr = subTree.paramValue("value");
                } else {
                    throw new TemplateException("AbsoluteTime missing required parameter: value");
                }

                return new PossibilityTrigger.AbsoluteTime(
                        new PossibilityTrigger.Id(),
                        GameSessionTimeUnit.ofMinutes(Integer.parseInt(valueStr))
                );
            }
            case "talkoptionselect", "selecttalkoption" -> {
                return createTalkOptionSelect(tree, context);
            }
            case "talkend" -> {
                // Format: "Trigger:TalkEnd:TALK002" ou "Trigger:TalkEnd:talkId:TALK002" (format clé-valeur)
                Tree subTree = tree.sub();
                String talkIdStr;
                if (subTree.hasUniqueParam()) {
                    talkIdStr = subTree.uniqueParam();
                } else if (subTree.hasParamKey("talkId")) {
                    talkIdStr = subTree.paramValue("talkId");
                } else {
                    throw new TemplateException("TalkEnd missing required parameter: talkId");
                }

                // Créer une référence atomique pour le TalkItem.Id
                AtomicReference<TalkItem.Id> resolvedTalkItemId = new AtomicReference<>();

                // Demander la résolution de la référence
                context.requestReference(talkIdStr, talkItemObj -> {
                    if (talkItemObj instanceof TalkItem talkItem) {
                        resolvedTalkItemId.set(talkItem.id());
                    }
                });

                // Si la référence n'est pas encore résolue, créer un ID temporaire
                if (resolvedTalkItemId.get() == null) {
                    resolvedTalkItemId.set(new TalkItem.Id(talkIdStr));
                }

                return new PossibilityTrigger.TalkEnd(
                        new PossibilityTrigger.Id(),
                        resolvedTalkItemId.get()
                );
            }
            case "clickmapobject" -> {
                // Format: "Trigger:ClickMapObject:objectReference:REF_OBJECT" ou "Trigger:ClickMapObject:REF_OBJECT" (legacy)
                Tree subTree = tree.sub();
                String objectReference;
                if (subTree.hasUniqueParam()) {
                    objectReference = subTree.uniqueParam();
                } else if (subTree.hasParamKey("objectReference")) {
                    objectReference = subTree.paramValue("objectReference");
                } else {
                    throw new TemplateException("ClickMapObject missing required parameter: objectReference");
                }

                return new PossibilityTrigger.ClickMapObject(
                        new PossibilityTrigger.Id(),
                        objectReference
                );
            }
        }
        return null;
    }

    private ScenarioSessionState parseState(String state) {
        return switch (state.toLowerCase()) {
            case "success" -> ScenarioSessionState.SUCCESS;
            case "failure" -> ScenarioSessionState.FAILURE;
            default -> ScenarioSessionState.ACTIVE;
        };
    }

    /**
     * Parse des paramètres par paires clé:valeur depuis une liste de strings.
     *
     * @param params Liste des paramètres à partir du type (exclut le premier paramètre qui est le type)
     * @return Map des paramètres clé -> valeur
     */
    private Map<String, String> parseKeyValueParams(List<String> params) {
        Map<String, String> paramMap = new HashMap<>();

        if (params.size() % 2 == 1) { // Doit être impair car le premier est le type + paires clé:valeur
            // Format par paires : type:key1:value1:key2:value2
            for (int i = 1; i < params.size(); i += 2) {
                if (i + 1 < params.size()) {
                    String key = params.get(i);
                    String value = params.get(i + 1);
                    paramMap.put(key.toLowerCase(), value);
                }
            }
        } else {
            // Format legacy ou spéciaux à gérer manuellement
            throw new TemplateException("Invalid parameter format - expected key:value pairs (total count should be odd: type + key:value pairs)");
        }

        return paramMap;
    }

    /**
     * Parse une conséquence GoalTarget en gérant les références.
     * Format: "GoalTarget:stepId:EFG123:targetId:TARGET_ATTERRIR:state:active" ou ordre différent
     * Format simplifié: "GoalTarget:targetId:TARGET_ATTERRIR:state:active" (stepId déduit automatiquement)
     */
    private Consequence parseGoalTargetConsequence(Tree tree, TemplateGeneratorCache context) {
        // Paramètres attendus: ["goaltarget", puis des paires clé:valeur]
        // Minimum: targetId + state (stepId optionnel)
        if (tree.params().size() < 5) {
            throw new TemplateException("Invalid GoalTarget format: missing parameters (at least targetId and state required)");
        }

        // Parser les paramètres par paires clé:valeur
        Map<String, String> paramMap = parseKeyValueParams(tree.params());

        String stepIdParam = paramMap.get("stepid");
        String targetIdParam = paramMap.get("targetid");
        String stateParam = paramMap.get("state");

        if (targetIdParam == null || stateParam == null) {
            throw new TemplateException("GoalTarget missing required parameters: targetId, state (stepId is optional and will be deduced if not provided)");
        }

        ScenarioSessionState state = parseState(stateParam);

        // Créer les placeholders pour les IDs résolus
        final AtomicReference<ScenarioConfig.Step.Id> resolvedStepId = new AtomicReference<>();
        final AtomicReference<ScenarioConfig.Target.Id> resolvedTargetId = new AtomicReference<>();

        // Résoudre le stepId
        if (stepIdParam != null) {
            // stepId explicitement fourni
            if (stepIdParam.equals("CURRENT_STEP")) {
                // Cas particulier du test : utiliser le step actuel
                Optional<ScenarioConfig.Step> currentStep = context.getReference("CURRENT_STEP", ScenarioConfig.Step.class);
                if (currentStep.isPresent()) {
                    resolvedStepId.set(currentStep.get().id());
                } else {
                    // Si pas disponible immédiatement, utiliser la logique asynchrone
                    context.requestReference("CURRENT_STEP", stepObj -> {
                        if (stepObj instanceof ScenarioConfig.Step step) {
                            resolvedStepId.set(step.id());
                        }
                    });
                }
            } else {
                // Essayer d'abord de résoudre de manière synchrone comme référence
                Optional<ScenarioConfig.Step> referencedStep = context.getReference(stepIdParam, ScenarioConfig.Step.class);
                if (referencedStep.isPresent()) {
                    resolvedStepId.set(referencedStep.get().id());
                } else {
                    // Si pas trouvé, essayer la résolution asynchrone sans marquer comme "non résolue"
                    context.tryRequestReference(stepIdParam, stepObj -> {
                        if (stepObj instanceof ScenarioConfig.Step step) {
                            resolvedStepId.set(step.id());
                        }
                    });
                }
            }
            // Si toujours pas résolu, utiliser comme ID direct
            if (resolvedStepId.get() == null) {
                resolvedStepId.set(new ScenarioConfig.Step.Id(stepIdParam));
            }
        }
        // Si stepId n'est pas fourni, on le déduira après avoir résolu le targetId

        // Résoudre le targetId (peut être une référence ou un ID direct)
        Optional<ScenarioConfig.Target> referencedTarget = context.getReference(targetIdParam, ScenarioConfig.Target.class);
        if (referencedTarget.isPresent()) {
            resolvedTargetId.set(referencedTarget.get().id());
        } else {
            // Si pas trouvé, essayer la résolution asynchrone sans marquer comme "non résolue"
            context.tryRequestReference(targetIdParam, targetObj -> {
                if (targetObj instanceof ScenarioConfig.Target target) {
                    resolvedTargetId.set(target.id());
                }
            });
        }
        // Si toujours pas résolu, utiliser comme ID direct
        if (resolvedTargetId.get() == null) {
            resolvedTargetId.set(new ScenarioConfig.Target.Id(targetIdParam));
        }

        // Si stepId n'est pas fourni, le déduire à partir du target
        if (stepIdParam == null && resolvedStepId.get() == null) {
            // Trouver le step qui contient ce target
            Optional<ScenarioConfig.Step.Id> deducedStepId = findStepContainingTarget(targetIdParam, context);
            if (deducedStepId.isPresent()) {
                resolvedStepId.set(deducedStepId.get());
            } else {
                throw new TemplateException("Cannot deduce stepId for target '" + targetIdParam + "': no step found containing this target");
            }
        }

        return new Consequence.ScenarioTarget(new Consequence.Id(), resolvedStepId.get(), resolvedTargetId.get(), state);
    }

    private Consequence parseTalkOptionsConsequence(Tree tree, TemplateGeneratorCache context) {
        // Cas 1: TalkOptions avec référence (ex: "TalkOptions:OPTIONS_ABCD")
        if (tree.params().size() >= 2) {
            String reference = tree.params().get(1); // "OPTIONS_ABCD"
            return parseTalkOptionsWithReference(reference, context);
        }

        throw new TemplateException("TalkOptions consequence without reference");
    }

    private Consequence parseTalkOptionsWithReference(String reference, TemplateGeneratorCache context) {
        // Créer une référence atomique pour stocker l'ID résolu
        AtomicReference<TalkItem.Id> resolvedTalkId = new AtomicReference<>();

        // Demander la résolution de la référence
        context.requestReference(reference, referencedObject -> {
            if (referencedObject instanceof TalkItem.Options multipleOptions) {
                resolvedTalkId.set(multipleOptions.id());
            } else {
                throw new TemplateException("Reference '" + reference + "' does not point to a MultipleOptions object");
            }
        });

        // Si la référence n'est pas encore résolue, créer un ID temporaire
        if (resolvedTalkId.get() == null) {
            // Cela sera résolu plus tard par le context
            resolvedTalkId.set(new TalkItem.Id(reference));
        }

        return new Consequence.DisplayTalk(new Consequence.Id(), resolvedTalkId.get());
    }


    private Consequence parseTalkConsequence(Tree tree, TemplateGeneratorCache context) {
        // Format: "Consequence:Talk:TALK000"
        // tree.params() contient ["Talk", "TALK000"]

        if (tree.params().size() < 2) {
            throw new TemplateException("Talk consequence must specify a talk ID or reference");
        }

        String talkReference = tree.params().get(1); // "TALK000"

        // Créer une référence atomique pour stocker l'ID résolu
        AtomicReference<TalkItem.Id> resolvedTalkId = new AtomicReference<>();

        // Demander la résolution de la référence
        context.requestReference(talkReference, referencedObject -> {
            if (referencedObject instanceof TalkItem talkItem) {
                resolvedTalkId.set(talkItem.id());
            } else {
                throw new TemplateException("Reference '" + talkReference + "' does not point to a TalkItem object");
            }
        });

        // Si la référence n'est pas encore résolue, créer un ID temporaire
        if (resolvedTalkId.get() == null) {
            // Cela sera résolu plus tard par le context
            resolvedTalkId.set(new TalkItem.Id(talkReference));
        }

        return new Consequence.DisplayTalk(new Consequence.Id(), resolvedTalkId.get());
    }

    private Consequence parseGameOverConsequence(Tree tree) {
        // Format: "Consequence:GameOver:FAILURE_ONE_CONTINUE"
        // tree.params() contient ["GameOver", "FAILURE_ONE_CONTINUE"]

        if (tree.params().size() < 2) {
            throw new TemplateException("GameOver consequence must specify a game over type");
        }

        String gameOverTypeStr = tree.params().get(1); // "FAILURE_ONE_CONTINUE"

        try {
            SessionGameOver.Type gameOverType = SessionGameOver.Type.valueOf(gameOverTypeStr);
            SessionGameOver gameOver = new SessionGameOver(gameOverType, Optional.empty());
            return new Consequence.SessionEnd(new Consequence.Id(), gameOver);
        } catch (IllegalArgumentException e) {
            throw new TemplateException("Invalid GameOver type: '" + gameOverTypeStr + "'. Valid types are: " +
                    java.util.Arrays.toString(SessionGameOver.Type.values()));
        }
    }


    /**
     * Trouve le TalkItem qui contient une option donnée.
     * Cette méthode sera appelée plus tard quand les références seront résolues.
     */
    private TalkItem.Id findTalkItemContainingOption(TalkItem.Options.Option.Id optionId, TemplateGeneratorCache context) {
        // Cette méthode sera implémentée si nécessaire, mais l'approche optimale 
        // est d'enregistrer cette relation lors du parsing
        return context.getOptionToTalkItemMapping(optionId);
    }

    /**
     * Trouve le step qui contient un target donné (par référence ou par ID).
     *
     * @param targetParam Le nom de la référence ou l'ID du target à chercher
     * @param context     Le contexte de parsing contenant les références
     * @return L'ID du step qui contient ce target, ou Optional.empty() si non trouvé
     */
    private Optional<ScenarioConfig.Step.Id> findStepContainingTarget(String targetParam, TemplateGeneratorCache context) {
        // D'abord essayer de résoudre le target comme une référence
        Optional<ScenarioConfig.Target> referencedTarget = context.getReference(targetParam, ScenarioConfig.Target.class);

        if (referencedTarget.isPresent()) {
            // Si on a trouvé le target par référence, chercher dans quel step il se trouve
            ScenarioConfig.Target target = referencedTarget.get();

            // Parcourir tous les steps enregistrés pour trouver celui qui contient ce target
            return context.getAllReferences(ScenarioConfig.Step.class)
                    .stream()
                    .filter(step -> step.targets().stream()
                            .anyMatch(stepTarget -> stepTarget.id().equals(target.id())))
                    .map(ScenarioConfig.Step::id)
                    .findFirst();
        }

        // Si pas trouvé par référence, chercher par ID dans tous les steps
        ScenarioConfig.Target.Id targetId = new ScenarioConfig.Target.Id(targetParam);

        return context.getAllReferences(ScenarioConfig.Step.class)
                .stream()
                .filter(step -> step.targets().stream()
                        .anyMatch(stepTarget -> stepTarget.id().equals(targetId)))
                .map(ScenarioConfig.Step::id)
                .findFirst();
    }


    /// Refacto


    private PossibilityTrigger createTalkOptionSelect(Tree tree, TemplateGeneratorCache context) {
        // Format: "Trigger:SelectTalkOption:option:REF_CHOIX_A" ou "Trigger:SelectTalkOption:REF_CHOIX_A" (legacy)

        Tree subTree = tree.sub();

        String optionReference;
        if (subTree.hasUniqueParam()) {
            optionReference = subTree.uniqueParam();
        } else if (subTree.hasParamKey(PARAM_KEY_TALK_OPTION)) {
            optionReference = subTree.paramValue(PARAM_KEY_TALK_OPTION);
        } else {
            throw new TemplateException("TalkOptionSelect missing required parameter: option");
        }

        // Créer des références atomiques pour l'option et son TalkItem parent
        AtomicReference<TalkItem.Options.Option.Id> resolvedOptionId = new AtomicReference<>();
        AtomicReference<TalkItem.Id> resolvedTalkItemId = new AtomicReference<>();

        // Demander la résolution de la référence
        context.requestReference(optionReference, optionObj -> {
            if (optionObj instanceof TalkItem.Options.Option option) {
                resolvedOptionId.set(option.id());
                // Trouver le TalkItem parent qui contient cette option
                TalkItem.Id parentTalkItemId = findTalkItemContainingOption(option.id(), context);
                if (parentTalkItemId != null) {
                    resolvedTalkItemId.set(parentTalkItemId);
                }
            }
        });

        // Si la référence n'est pas encore résolue, créer des IDs temporaires
        if (resolvedOptionId.get() == null) {
            resolvedOptionId.set(new TalkItem.Options.Option.Id(optionReference));
        }
        if (resolvedTalkItemId.get() == null) {
            resolvedTalkItemId.set(new TalkItem.Id(optionReference + "_parent"));
        }

        return new PossibilityTrigger.TalkOptionSelect(
                new PossibilityTrigger.Id(),
                resolvedTalkItemId.get(),
                resolvedOptionId.get()
        );
    }


}

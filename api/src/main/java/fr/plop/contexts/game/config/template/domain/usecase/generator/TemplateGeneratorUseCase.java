package fr.plop.contexts.game.config.template.domain.usecase.generator;

import fr.plop.contexts.game.config.board.domain.model.BoardConfig;
import fr.plop.contexts.game.config.board.domain.model.BoardSpace;
import fr.plop.contexts.game.config.consequence.Consequence;
import fr.plop.contexts.game.config.map.domain.MapConfig;
import fr.plop.contexts.game.config.map.domain.MapItem;
import fr.plop.contexts.game.config.scenario.domain.model.*;
import fr.plop.contexts.game.config.talk.domain.TalkConfig;
import fr.plop.contexts.game.config.talk.domain.TalkItem;
import fr.plop.contexts.game.config.template.domain.TemplateException;
import fr.plop.contexts.game.config.template.domain.model.Template;
import fr.plop.contexts.game.config.template.domain.model.Tree;
import fr.plop.contexts.game.config.template.domain.usecase.ParsingContext;
import fr.plop.contexts.game.config.template.domain.usecase.TreeGenerator;
import fr.plop.contexts.game.session.core.domain.model.SessionGameOver;
import fr.plop.contexts.game.session.scenario.domain.model.ScenarioGoal;
import fr.plop.contexts.game.session.time.GameSessionTimeUnit;
import fr.plop.generic.enumerate.AndOrOr;
import fr.plop.generic.enumerate.BeforeOrAfter;
import fr.plop.generic.position.Point;
import fr.plop.generic.position.Rect;
import fr.plop.subs.i18n.domain.I18n;
import fr.plop.subs.i18n.domain.Language;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

public class TemplateGeneratorUseCase {

    private static final String SPACE_0 = "---";
    private static final String SPACE_1 = "------";
    private static final String SPACE_2 = "---------";
    private static final String SPACE_3 = "------------";
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
    public static final String BOARD_KEY = "BOARD";
    public static final String SPACE_KEY = "SPACE";


    private static final String PARAM_KEY_TALK_OPTION = "option";

    // Langues
    public static final String BOTTOM_LEFT = "bottomLeft";
    public static final String TOP_RIGHT = "topRight";



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
    private final ParsingContext context = new ParsingContext();
    private final TemplateGeneratorTalkUseCase talkGenerator = new TemplateGeneratorTalkUseCase(context);

    private final TemplateGeneratorI18nUseCase i18nGenerator = new TemplateGeneratorI18nUseCase();

    public Template apply(Script script) {
        // Parse avec TreeGenerator pour avoir une structure d'arbre
        List<Tree> trees = treeGenerator.generate(script.getValue());

        if (trees.isEmpty()) {
            throw new TemplateException("Script is empty or invalid");
        }

        Tree rootTree = trees.getFirst();
        List<String> params = rootTree.params();

        String version = !params.isEmpty() ? params.get(0) : DEFAULT_VERSION;
        String label = params.size() > 1 ? params.get(1) : "";




        // 1. Parse d'abord les BoardSpaces pour créer la map value -> BoardSpace.Id
        List<BoardSpace> boardSpaces = parseBoardSpacesFromTrees(rootTree.children());
        Map<String, BoardSpace.Id> labelToSpaceIdMap = createLabelToSpaceIdMap(boardSpaces);

        // 2. Parse les TalkItems pour définir les références avant les Steps
        List<TalkItem> declaredTalkItems = parseTalkItemsFromTrees(rootTree.children(), context);
        // Inclure aussi les TalkItem créés inline (ex: dans des Consequence)
        List<TalkItem> referencedTalkItems = context.getAllReferences(TalkItem.class);
        
        // Build final list: declared items + referenced items not already in declared
        Map<String, Boolean> seenIds = new HashMap<>();
        List<TalkItem> finalTalkItems = new ArrayList<>();
        
        // Add declared items in order
        for (TalkItem ti : declaredTalkItems) {
            finalTalkItems.add(ti);
            seenIds.put(ti.id().value(), true);
        }
        
        // Add referenced items that aren't already in declared items
        for (TalkItem ti : referencedTalkItems) {
            if (!seenIds.containsKey(ti.id().value())) {
                finalTalkItems.add(ti);
                seenIds.put(ti.id().value(), true);
            }
        }
        
        TalkConfig talk = new TalkConfig(finalTalkItems);

        // 3. Parse les Steps en utilisant la map pour résoudre les SpaceId et le context pour les références
        ScenarioConfig scenario = new ScenarioConfig(parseSteps(rootTree, labelToSpaceIdMap, context));
        BoardConfig board = new BoardConfig(boardSpaces);
        MapConfig map = new MapConfig(parseMapItemsFromTrees(rootTree.children()));

        // 4. Vérifier qu'il n'y a pas de références non résolues
        if (context.hasUnresolvedReferences()) {
            throw new TemplateException("Unresolved references: " + context.getUnresolvedReferences());
        }

        return new Template(templateAtom(rootTree), label, version, maxDurationFromParams(params), scenario, board, map, talk);
    }

    private static Template.Atom templateAtom(Tree rootTree) {
        return new Template.Atom(new Template.Id(), new Template.Code(rootTree.headerKeepCase()));
    }

    private static Duration maxDurationFromParams(List<String> params) {
        if (params.size() > 2 && !params.get(2).isEmpty()) {
            return Duration.ofMinutes(Long.parseLong(params.get(2)));
        }
        return Duration.ofHours(1); // Valeur par défaut
    }

    // ============ NOUVELLES METHODES BASEES SUR TREEGENERATOR ============

    private Map<String, BoardSpace.Id> createLabelToSpaceIdMap(List<BoardSpace> boardSpaces) {
        Map<String, BoardSpace.Id> map = new HashMap<>();
        for (BoardSpace space : boardSpaces) {
            map.put(space.label(), space.id());
        }
        return map;
    }

    private BoardSpace.Id resolveSpaceId(String spaceLabel, Map<String, BoardSpace.Id> labelToSpaceIdMap) {
        BoardSpace.Id resolvedId = labelToSpaceIdMap.get(spaceLabel);
        if (resolvedId != null) {
            // Le value correspond à un BoardSpace existant, on utilise son ID
            return resolvedId;
        } else {
            // Le value ne correspond à aucun BoardSpace, on crée un nouvel ID
            // (comportement de fallback pour rétrocompatibilité)
            return new BoardSpace.Id(spaceLabel);
        }
    }

    private List<BoardSpace> parseBoardSpacesFromTrees(List<Tree> trees) {
        List<BoardSpace> spaces = new ArrayList<>();

        for (Tree tree : trees) {
            if (BOARD_KEY.equalsIgnoreCase(tree.header())) {
                // Un Board peut contenir plusieurs Spaces
                for (Tree spaceTree : tree.children()) {
                    if (SPACE_KEY.equalsIgnoreCase(spaceTree.header())) {
                        BoardSpace space = parseBoardSpaceFromTree(spaceTree);
                        spaces.add(space);
                    }
                }
            }
        }

        return spaces;
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

    private List<TalkItem> parseTalkItemsFromTrees(List<Tree> trees, ParsingContext context) {
        for (Tree tree : trees) {
            if ("TALK".equalsIgnoreCase(tree.header())) {
                return talkGenerator.apply(tree);
            }
        }
        return new ArrayList<>();
    }

    private MapItem parseMapItemFromTree(Tree mapTree) {
        List<String> params = mapTree.params();
        if (params.size() < 2) {
            throw new TemplateException("Invalid map format in tree: " + mapTree);
        }

        // Format: "Map:Asset:imgs/first/map.png"
        String type = params.get(0); // "Asset"
        String imagePath = params.get(1); // "imgs/first/map.png"

        // Parse priority and positions from children
        MapItem.Priority priority = MapItem.Priority.LOW; // default
        List<MapItem.Position> positions = new ArrayList<>();
        Set<ScenarioConfig.Step.Id> stepIds = new HashSet<>();

        for (Tree child : mapTree.children()) {
            if ("PRIORITY".equalsIgnoreCase(child.header()) && !child.params().isEmpty()) {
                priority = parseMapPriority(child.params().get(0));
            } else if ("POSITION".equalsIgnoreCase(child.header()) && child.params().size() >= 2) {
                // Format: "position:89.09:10.064"
                float x = Float.parseFloat(child.params().get(0));
                float y = Float.parseFloat(child.params().get(1));

                // Parse priority and step IDs from position children
                MapItem.Priority positionPriority = MapItem.Priority.LOW; // default
                for (Tree posChild : child.children()) {
                    if ("PRIORITY".equalsIgnoreCase(posChild.header()) && !posChild.params().isEmpty()) {
                        positionPriority = parseMapPriority(posChild.params().get(0));
                    } else if ("SPACE".equalsIgnoreCase(posChild.header()) && !posChild.params().isEmpty()) {
                        stepIds.add(new ScenarioConfig.Step.Id(posChild.params().get(0)));
                    }
                }

                MapItem.Position.Atom atom = new MapItem.Position.Atom(
                        new MapItem.Position.Id(),
                        "", // value vide pour l'instant
                        positionPriority,
                        List.of() // spaceIds vides pour l'instant
                );

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

    private MapItem.Priority parseMapPriority(String priorityStr) {
        return switch (priorityStr.toUpperCase()) {
            case "HIGHEST" -> MapItem.Priority.HIGHEST;
            case "HIGH" -> MapItem.Priority.HIGH;
            case "MEDIUM" -> MapItem.Priority.MEDIUM;
            case "LOW" -> MapItem.Priority.LOW;
            case "LOWEST" -> MapItem.Priority.LOWEST;
            default -> MapItem.Priority.LOW;
        };
    }

    private BoardSpace parseBoardSpaceFromTree(Tree spaceTree) {
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

    private Rect parseRectFromTreeParams(List<String> params) {
        // Note: Avec TreeGenerator, le format est différent. 
        // Le header contient bottomLeft/topRight, et les params contiennent les coordonnées
        return null; // Sera géré dans parseRectFromTree
    }

    private Rect parseRectFromTree(Tree tree) {
        String header = tree.header();
        List<String> params = tree.params();

        // Format court: header numérique avec 4 params numériques [1, 2, 4, 3]
        if (params.size() == 3 && isAllNumeric(new String[]{header, params.get(0), params.get(1), params.get(2)})) {
            try {
                Point bottomLeft = new Point(Float.parseFloat(header), Float.parseFloat(params.get(0)));
                Point topRight = new Point(Float.parseFloat(params.get(1)), Float.parseFloat(params.get(2)));
                return new Rect(bottomLeft, topRight);
            } catch (NumberFormatException e) {
                // Continue to long format
            }
        }

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


    // ============ ANCIENNES METHODES (à conserver pour le moment) ============

    private List<ScenarioConfig.Step> parseSteps(Tree root, Map<String, BoardSpace.Id> labelToSpaceIdMap, ParsingContext context) {
        List<ScenarioConfig.Step> steps = new ArrayList<>();
        root.children().forEach(child -> {
            if (child.header().startsWith(STEP_KEY)) {
                ScenarioConfig.Step step = parseStep(child, labelToSpaceIdMap, context);
                steps.add(step);
            }
        });
        return steps;
    }

    private List<BoardSpace> parseBoardSpaces(String[] lines) {
        List<BoardSpace> spaces = new ArrayList<>();

        for (int i = 1; i < lines.length; i++) {
            String line = lines[i].trim();

            if (line.startsWith(SPACE_0 + " " + BOARD_KEY)) {
                // On a trouvé un board, on parse ses spaces
                for (int j = i + 1; j < lines.length; j++) {
                    String spaceLine = lines[j].trim();

                    if (spaceLine.startsWith(SPACE_1 + " " + SPACE_KEY)) {
                        BoardSpace space = parseBoardSpace(spaceLine, lines, j);
                        spaces.add(space);
                    } else if (spaceLine.startsWith(SPACE_0 + " ")) {
                        // On a atteint un autre élément de niveau 0, on sort
                        break;
                    }
                }
            }
        }

        return spaces;
    }

    private ScenarioConfig.Step parseStep(Tree root, Map<String, BoardSpace.Id> labelToSpaceIdMap, ParsingContext context) {
        // Extraire la référence du header si elle existe : "Step(ref REF_STEP_A)"
        String referenceName = root.reference();

        // Format: "---" + " Step:FR:Chez Moi:EN:At Home" ou "---" + " Step"
        Optional<I18n> stepLabel = Optional.empty();

        if (!root.params().isEmpty()) {
            String stepInfo = String.join(SEPARATOR, root.params());
            stepLabel = parseI18nFromLine(stepInfo);
        }

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
        ScenarioConfig.Step step = new ScenarioConfig.Step(
                new ScenarioConfig.Step.Id(),
                stepLabel,
                targets,
                new ArrayList<>() // possibilités vides pour l'instant
        );

        // Enregistrer la référence immédiatement si elle existe
        if (referenceName != null) {
            context.registerReference(referenceName, step);
        }

        // Enregistrer le step actuel comme "CURRENT_STEP" AVANT de parser les possibilités
        context.registerReference("CURRENT_STEP", step);

        // Parse toutes les possibilités maintenant que CURRENT_STEP est disponible
        for (Tree possibilityTree : possibilityTrees) {
            Possibility possibility = parsePossibility(possibilityTree, labelToSpaceIdMap, context);
            possibilities.add(possibility);
        }

        // Mettre à jour le step avec les possibilités
        ScenarioConfig.Step finalStep = new ScenarioConfig.Step(
                step.id(),
                stepLabel,
                targets,
                possibilities
        );

        // Mettre à jour les références avec le step final
        if (referenceName != null) {
            context.registerReference(referenceName, finalStep);
        }
        context.registerReference("CURRENT_STEP", finalStep);

        return finalStep;
    }

    private ScenarioConfig.Target parseTarget(Tree tree, ParsingContext context) {
        // Extraire la référence du header si elle existe : "Target (ref TARGET_ATTERRIR):FR:..."
        String referenceName = tree.reference();

        // 1. Détecter si c'est optionnel
        boolean optional = tree.originalHeader().toUpperCase().contains("(OPT)");

        // 2. Parser le value I18n depuis les paramètres OU depuis le header
        Optional<I18n> targetLabel = Optional.empty();
        if (!tree.params().isEmpty()) {
            String paramsStr = String.join(SEPARATOR, tree.params());
            targetLabel = parseI18nFromLine(paramsStr);
        } else {
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
                targetLabel,
                description,
                optional
        );

        // Enregistrer la référence si elle existe
        if (referenceName != null) {
            context.registerReference(referenceName, target);
        }

        return target;
    }




    private Optional<I18n> parseI18nFromLine(String line) {
        // Format: "FR:Chez Moi:EN:At Home" ou "EN:Office:FR:Bureau"
        String[] parts = line.split(SEPARATOR);

        if (parts.length >= 4) {
            Map<Language, String> i18nMap = new HashMap<>();

            for (int i = 0; i < parts.length - 1; i += 2) {
                String langCode = parts[i].trim();
                String text = parts[i + 1].trim();

                if ("FR".equals(langCode)) {
                    i18nMap.put(Language.FR, text);
                } else if ("EN".equals(langCode)) {
                    i18nMap.put(Language.EN, text);
                }
            }

            if (!i18nMap.isEmpty()) {
                return Optional.of(new I18n(i18nMap));
            }
        }

        return Optional.empty();
    }

    private Possibility parsePossibility(Tree tree, Map<String, BoardSpace.Id> labelToSpaceIdMap, ParsingContext context) {
        // Format: "------" + " Possibility:ALWAYS" ou "------" + " Possibility:times:4:OR" ou "------" + " Possibility"
        AtomicReference<PossibilityRecurrence> recurrence = new AtomicReference<>(new PossibilityRecurrence.Always(new PossibilityRecurrence.Id())); // Par défaut

        String conditionTypeStr = "AND";

        if (!tree.params().isEmpty()) {
            String typeStr = tree.params().getFirst().trim().toUpperCase();
            switch (typeStr) {
                case "ALWAYS":
                    recurrence.set(new PossibilityRecurrence.Always(new PossibilityRecurrence.Id()));
                    if (tree.params().size() > 1) {
                        conditionTypeStr = tree.params().get(1);
                    }
                    break;
                case "TIMES":
                    int times = Integer.parseInt(tree.params().get(1));
                    recurrence.set(new PossibilityRecurrence.Times(new PossibilityRecurrence.Id(), times));
                    if (tree.params().size() > 2) {
                        conditionTypeStr = tree.params().get(2);
                    }
                    break;
                default:
                    throw new TemplateException("Invalid format: " + tree.header() + SEPARATOR + String.join(SEPARATOR, tree.params()));
            }
        }
        AndOrOr conditionType = AndOrOr.valueOf(conditionTypeStr);

        // Parse les conditions, conséquences et trigger qui suivent
        List<PossibilityCondition> conditions = new ArrayList<>();
        List<Consequence> consequences = new ArrayList<>();
        AtomicReference<PossibilityTrigger> trigger = new AtomicReference<>();

        // Parse en deux passes : d'abord les conséquences pour enregistrer les références, puis les triggers
        // Première passe : conséquences et conditions
        tree.children().forEach(child -> {
            String actionStr = child.header().trim();
            switch (actionStr) {
                case POSSIBILITY_CONDITION_KEY:
                    conditions.add(parseCondition(child, labelToSpaceIdMap));
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
                trigger.set(parseTrigger(child, labelToSpaceIdMap, context));
            }
        });

        if (trigger.get() == null) {
            throw new TemplateException("Invalid format: " + tree.header() + SEPARATOR + String.join(SEPARATOR, tree.params()));
        }

        return new Possibility(recurrence.get(), trigger.get(), conditions, conditionType, consequences);
    }

    private PossibilityCondition parseCondition(Tree tree, Map<String, BoardSpace.Id> labelToSpaceIdMap) {

        String type = tree.params().getFirst().toLowerCase();
        switch (type) {
            case "outsidespace" -> {
                if (tree.params().size() == 2) {
                    String spaceLabel = tree.params().get(1);
                    BoardSpace.Id spaceId = resolveSpaceId(spaceLabel, labelToSpaceIdMap);
                    return new PossibilityCondition.OutsideSpace(
                            new PossibilityCondition.Id(),
                            spaceId
                    );
                }
                String spaceLabel = tree.params().get(2);
                BoardSpace.Id spaceId = resolveSpaceId(spaceLabel, labelToSpaceIdMap);
                return new PossibilityCondition.OutsideSpace(
                        new PossibilityCondition.Id(),
                        spaceId
                );

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
                
                return new PossibilityCondition.AbsoluteTime(
                        new PossibilityCondition.Id(),
                        Duration.ofMinutes(Long.parseLong(durationStr)),
                        BeforeOrAfter.BEFORE //TODO
                );
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

                return new PossibilityCondition.StepIn(
                        new PossibilityCondition.Id(),
                        new ScenarioConfig.Step.Id(stepIdStr)
                );
            }
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

                BoardSpace.Id spaceId = resolveSpaceId(spaceLabel, labelToSpaceIdMap);
                return new PossibilityCondition.InsideSpace(
                        new PossibilityCondition.Id(),
                        spaceId
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

                return new PossibilityCondition.StepTarget(
                        new PossibilityCondition.Id(),
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

    private Consequence parseConsequence(Tree tree, ParsingContext context) {
        // Format: "---------" + " consequence:Alert" ou "---------" + " consequence:GoalTarget:stepId:EFG:targetId:9876:state:FAILURE"

        if (tree.params().isEmpty()) {
            throw new TemplateException("Invalid consequence format: no type specified");
        }

        String type = tree.params().get(0).toLowerCase();
        switch (type) {
            case "alert" -> {
                // Parse le value I18n qui suit depuis les enfants (sans \n final pour les Alert)
                Optional<I18n> messageOpt = i18nGenerator.apply(tree.children());
                I18n message = messageOpt.orElse(new I18n(Map.of())); // I18n vide si pas de value
                return new Consequence.DisplayMessage(new Consequence.Id(), message);
            }
            case "goaltarget" -> {
                return parseGoalTargetConsequence(tree, context);
            }
            case "goal" -> {
                // Format: "Goal:state:SUCCESS:stepId:KLM" ou autre ordre
                Map<String, String> paramMap = parseKeyValueParams(tree.params());

                String stateParam = paramMap.get("state");
                String stepIdParam = paramMap.get("stepid");

                if (stateParam == null || stepIdParam == null) {
                    throw new TemplateException("Goal missing required parameters: state, stepId");
                }

                ScenarioGoal.State state = parseState(stateParam);
                ScenarioConfig.Step.Id stepId = new ScenarioConfig.Step.Id(stepIdParam);
                return new Consequence.ScenarioStep(new Consequence.Id(), stepId, state);
            }
            case "addobjet" -> {
                // Format: "AddObjet:objetId:SWORD123"
                if (tree.params().size() < 3) {
                    throw new TemplateException("AddObjet missing required parameters: objetId");
                }

                // Support legacy format directement ou nouveau format key:value
                String objetId;
                if (tree.params().size() == 2) {
                    // Legacy: "AddObjet:SWORD123"
                    objetId = tree.params().get(1);
                } else {
                    // Nouveau: "AddObjet:objetId:SWORD123"
                    Map<String, String> paramMap = parseKeyValueParams(tree.params());
                    objetId = paramMap.get("objetid");
                    if (objetId == null) {
                        throw new TemplateException("AddObjet missing required parameter: objetId");
                    }
                }

                return new Consequence.ObjetAdd(new Consequence.Id(), objetId);
            }
            case "updatedmetadata" -> {
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
            case "talkoptions" -> {
                return parseTalkOptionsConsequence(tree, context);
            }
            case "talk" -> {
                return parseTalkConsequence(tree, context);
            }
            case "gameover" -> {
                return parseGameOverConsequence(tree, context);
            }
        }

        throw new TemplateException("Invalid consequence format: " + type);
    }

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
    }

    private PossibilityTrigger parseTrigger(Tree tree, Map<String, BoardSpace.Id> labelToSpaceIdMap, ParsingContext context) {

        String type = tree.params().getFirst().toLowerCase();
        switch (type) {
            case "goinspace" -> {
                // Format: "Trigger:GoInSpace:SpaceId:ABCD" ou "Trigger:GoInSpace:EFG" (legacy)
                Tree subTree = tree.sub();
                String spaceLabel;
                if (subTree.hasUniqueParam()) {
                    spaceLabel = subTree.uniqueParam();
                } else if (subTree.hasParamKey("SpaceId")) {
                    spaceLabel = subTree.paramValue("SpaceId");
                } else {
                    throw new TemplateException("GoInSpace missing required parameter: SpaceId");
                }

                BoardSpace.Id spaceId = resolveSpaceId(spaceLabel, labelToSpaceIdMap);
                return new PossibilityTrigger.SpaceGoIn(
                        new PossibilityTrigger.Id(),
                        spaceId
                );
            }
            case "gooutspace" -> {
                // Format: "Trigger:GoOutSpace:SpaceId:ABCD" ou "Trigger:GoOutSpace:EFG" (legacy)
                Tree subTree = tree.sub();
                String spaceLabel;
                if (subTree.hasUniqueParam()) {
                    spaceLabel = subTree.uniqueParam();
                } else if (subTree.hasParamKey("SpaceId")) {
                    spaceLabel = subTree.paramValue("SpaceId");
                } else {
                    throw new TemplateException("GoOutSpace missing required parameter: SpaceId");
                }

                BoardSpace.Id spaceId = resolveSpaceId(spaceLabel, labelToSpaceIdMap);
                return new PossibilityTrigger.SpaceGoOut(
                        new PossibilityTrigger.Id(),
                        spaceId
                );
            }
            case "absolutetime" -> {
                // Format: "Trigger:AbsoluteTime:value:42" ou "Trigger:AbsoluteTime:42" (legacy)
                Tree subTree = tree.sub();
                String valueStr;
                if(subTree.hasUniqueParam()) {
                    valueStr = subTree.uniqueParam();
                } else if(subTree.hasParamKey("value")) {
                    valueStr = subTree.paramValue("value");
                } else {
                    throw new TemplateException("AbsoluteTime missing required parameter: value");
                }

                return new PossibilityTrigger.AbsoluteTime(
                        new PossibilityTrigger.Id(),
                        GameSessionTimeUnit.ofMinutes(Integer.parseInt(valueStr))
                );
            }
            case "talkoptionselect", "selecttalkoption"  -> {
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

    private ScenarioGoal.State parseState(String state) {
        return switch (state.toLowerCase()) {
            case "success" -> ScenarioGoal.State.SUCCESS;
            case "failure" -> ScenarioGoal.State.FAILURE;
            default -> ScenarioGoal.State.ACTIVE;
        };
    }

    private BoardSpace parseBoardSpace(String spaceLine, String[] allLines, int currentIndex) {
        // Format: "------" + " Space:Mon premier espace:LOW"
        String spaceInfo = spaceLine.substring((SPACE_1 + " " + SPACE_KEY + SEPARATOR).length());
        String[] parts = spaceInfo.split(SEPARATOR);

        if (parts.length < 2) {
            throw new TemplateException("Invalid space format: " + spaceLine);
        }

        String label = parts[0];
        BoardSpace.Priority priority = parsePriority(parts[1]);

        // Parse les rectangles qui suivent
        List<Rect> rects = parseRects(allLines, currentIndex);

        return new BoardSpace(label, priority, rects);
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

    private List<Rect> parseRects(String[] allLines, int currentIndex) {
        List<Rect> rects = new ArrayList<>();

        for (int i = currentIndex + 1; i < allLines.length; i++) {
            String line = allLines[i].trim();

            if (line.startsWith(SPACE_2 + " ")) {
                // Différents formats de rectangles supportés
                Rect rect = parseRect(line);
                if (rect != null) {
                    rects.add(rect);
                }
            } else if (line.startsWith(SPACE_1 + " ") || line.startsWith(SPACE_0 + " ")) {
                // On a atteint un autre élément, on sort
                break;
            }
        }

        return rects;
    }

    private Rect parseRect(String rectLine) {
        String rectInfo = rectLine.substring(SPACE_2.length() + 1);
        String[] segments = rectInfo.split(SEPARATOR);

        // Format court: "1:2:4:3" ou "89.745:5.5684:0.8547:8.147"
        if (segments.length == 4 && isAllNumeric(segments)) {
            try {
                float x1 = Float.parseFloat(segments[0]);
                float y1 = Float.parseFloat(segments[1]);
                float x2 = Float.parseFloat(segments[2]);
                float y2 = Float.parseFloat(segments[3]);

                Point bottomLeft = new Point(x1, y1);
                Point topRight = new Point(x2, y2);
                return new Rect(bottomLeft, topRight);
            } catch (NumberFormatException e) {
                // Fallback vers le format long
            }
        }

        // Format long: "bottomLeft:5.7:10:topRight:8.097:50.43"
        Point bottomLeft = null;
        Point topRight = null;

        for (int i = 0; i < segments.length; i++) {
            if (BOTTOM_LEFT.equals(segments[i]) && i + 2 < segments.length) {
                try {
                    float x = Float.parseFloat(segments[i + 1]);
                    float y = Float.parseFloat(segments[i + 2]);
                    bottomLeft = new Point(x, y);
                    i += 2; // Avancer de 2 positions
                } catch (NumberFormatException e) {
                    // Ignorer cette partie
                }
            } else if (TOP_RIGHT.equals(segments[i]) && i + 2 < segments.length) {
                try {
                    float x = Float.parseFloat(segments[i + 1]);
                    float y = Float.parseFloat(segments[i + 2]);
                    topRight = new Point(x, y);
                    i += 2; // Avancer de 2 positions
                } catch (NumberFormatException e) {
                    // Ignorer cette partie
                }
            }
        }

        if (bottomLeft != null && topRight != null) {
            return new Rect(bottomLeft, topRight);
        }

        return null;
    }

    private boolean isAllNumeric(String[] segments) {
        for (String segment : segments) {
            try {
                Float.parseFloat(segment);
            } catch (NumberFormatException e) {
                return false;
            }
        }
        return true;
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
    private Consequence parseGoalTargetConsequence(Tree tree, ParsingContext context) {
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

        ScenarioGoal.State state = parseState(stateParam);

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

    private Consequence parseTalkOptionsConsequence(Tree tree, ParsingContext context) {
        // Cas 1: TalkOptions avec référence (ex: "TalkOptions:OPTIONS_ABCD")
        if (tree.params().size() >= 2) {
            String reference = tree.params().get(1); // "OPTIONS_ABCD"
            return parseTalkOptionsWithReference(reference, context);
        }

        throw new TemplateException("TalkOptions consequence without reference");
    }
    
    private Consequence parseTalkOptionsWithReference(String reference, ParsingContext context) {
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
    


    private Consequence parseTalkConsequence(Tree tree, ParsingContext context) {
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

    private Consequence parseGameOverConsequence(Tree tree, ParsingContext context) {
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
    private TalkItem.Id findTalkItemContainingOption(TalkItem.Options.Option.Id optionId, ParsingContext context) {
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
    private Optional<ScenarioConfig.Step.Id> findStepContainingTarget(String targetParam, ParsingContext context) {
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



    ///Refacto




    private PossibilityTrigger createTalkOptionSelect(Tree tree, ParsingContext context) {
        // Format: "Trigger:SelectTalkOption:option:REF_CHOIX_A" ou "Trigger:SelectTalkOption:REF_CHOIX_A" (legacy)

        Tree subTree = tree.sub();

        String optionReference;
        if(subTree.hasUniqueParam()) {
            optionReference = subTree.uniqueParam();
        } else if(subTree.hasParamKey(PARAM_KEY_TALK_OPTION)) {
            optionReference = subTree.paramValue(PARAM_KEY_TALK_OPTION);
        } else {
            throw new TemplateException("TalkOptionSelect missing required parameter: option");
        }

        /*String optionReference;
        if (tree.params().size() >= 3 && PARAM_KEY_TALK_OPTION.equalsIgnoreCase(tree.params().get(1))) {
            // Nouveau format key:value: SelectTalkOption:option:REF_CHOIX_A
            optionReference = tree.params().get(2);
        } else if (tree.params().size() == 2) {
            // Legacy format: SelectTalkOption:REF_CHOIX_A
            optionReference = tree.params().get(1);
        } else {
            throw new TemplateException("TalkOptionSelect missing required parameter: option");
        }*/

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

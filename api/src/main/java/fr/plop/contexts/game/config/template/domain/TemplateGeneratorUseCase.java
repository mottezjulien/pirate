package fr.plop.contexts.game.config.template.domain;

import fr.plop.contexts.game.config.board.domain.model.BoardConfig;
import fr.plop.contexts.game.config.board.domain.model.BoardSpace;
import fr.plop.contexts.game.config.map.domain.MapConfig;
import fr.plop.contexts.game.config.scenario.domain.model.Possibility;
import fr.plop.contexts.game.config.scenario.domain.model.PossibilityCondition;
import fr.plop.contexts.game.config.scenario.domain.model.PossibilityConsequence;
import fr.plop.contexts.game.config.scenario.domain.model.PossibilityRecurrence;
import fr.plop.contexts.game.config.scenario.domain.model.PossibilityTrigger;
import fr.plop.contexts.game.config.scenario.domain.model.ScenarioConfig;
import fr.plop.contexts.game.config.template.domain.model.Template;
import fr.plop.contexts.game.session.scenario.domain.model.ScenarioGoal;
import fr.plop.contexts.game.session.time.TimeUnit;
import fr.plop.contexts.i18n.domain.I18n;
import fr.plop.contexts.i18n.domain.Language;
import fr.plop.generic.enumerate.AndOrOr;
import fr.plop.generic.enumerate.BeforeOrAfter;
import fr.plop.generic.position.Point;
import fr.plop.generic.position.Rect;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
    public static final String OPTIONAL_KEY = "(OPT)";
    public static final String BOARD_KEY = "BOARD";
    public static final String SPACE_KEY = "SPACE";

    // Langues
    public static final String FR_KEY = "FR";
    public static final String EN_KEY = "EN";
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

        ScenarioConfig scenario = new ScenarioConfig("", parseSteps(rootTree));
        BoardConfig board = new BoardConfig(parseBoardSpacesFromTrees(rootTree.children()));
        MapConfig map = new MapConfig(List.of());



        return new Template(templateAtom(rootTree), label, version, maxDurationFromParams(params), scenario, board, map);
    }

    private static Template.Atom templateAtom(Tree rootTree) {
        return new Template.Atom(new Template.Id(), new Template.Code(rootTree.header()));
    }

    private static Duration maxDurationFromParams(List<String> params) {
        if (params.size() > 2 && !params.get(2).isEmpty()) {
            return Duration.ofMinutes(Long.parseLong(params.get(2)));
        }
        return Duration.ofHours(1); // Valeur par défaut
    }

    // ============ NOUVELLES METHODES BASEES SUR TREEGENERATOR ============
    
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
        String header = tree.header().toUpperCase();
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

    private List<ScenarioConfig.Step> parseSteps(Tree root) {
        List<ScenarioConfig.Step> steps = new ArrayList<>();
        root.children().forEach(child -> {
            if (child.header().toUpperCase().startsWith(STEP_KEY)) {
                ScenarioConfig.Step step = parseStep(child);  // Fix: passer child au lieu de root
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

    private ScenarioConfig.Step parseStep(Tree root) {
        // Format: "---" + " Step:FR:Chez Moi:EN:At Home" ou "---" + " Step"
        Optional<I18n> stepLabel = Optional.empty();

        if (!root.params().isEmpty()) {
            String stepInfo = String.join(":", root.params());
            stepLabel = parseI18nFromLine(stepInfo);
        }

        // Parse les targets et possibilities qui suivent
        List<ScenarioConfig.Target> targets = new ArrayList<>();
        List<Possibility> possibilities = new ArrayList<>();

        root.children().forEach(child -> {
            switch (child.header().toUpperCase()) {
                case TARGET_KEY:
                    ScenarioConfig.Target target = parseTarget(child);
                    targets.add(target);
                    break;
                case POSSIBILITY_KEY:
                    Possibility possibility = parsePossibility(child);
                    possibilities.add(possibility);
                    break;
                default:
                    // Vérifions si c'est un Target qui commence par TARGET mais avec du texte après
                    if (child.header().toUpperCase().startsWith(TARGET_KEY)) {
                        ScenarioConfig.Target extendedTarget = parseTarget(child);
                        targets.add(extendedTarget);
                    }
                    break;
            }
        });


        return new ScenarioConfig.Step(
                new ScenarioConfig.Step.Id(),
                stepLabel,
                targets,
                possibilities
        );
    }

    private ScenarioConfig.Target parseTarget(Tree tree) {
        // 1. Détecter si c'est optionnel
        boolean optional = tree.header().toUpperCase().contains("(OPT)");
        
        // 2. Parser le label I18n depuis les paramètres OU depuis le header
        Optional<I18n> targetLabel = Optional.empty();
        if (!tree.params().isEmpty()) {
            String paramsStr = String.join(":", tree.params());
            targetLabel = parseI18nFromLine(paramsStr);
        } else {
            // Si pas de params, extraire le label du header (enlever "Target " du début)
            String headerText = tree.header().toUpperCase();
            if (headerText.toUpperCase().startsWith(TARGET_KEY + " ")) {
                String labelText = headerText.substring((TARGET_KEY + " ").length()).trim();
                // Enlever "(Opt)" si présent
                labelText = labelText.replace("(Opt)", "").trim();
                labelText = labelText.replace("(OPT)", "").trim();
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
        Optional<I18n> description = parseDescriptionFromChildren(tree.children());

        return new ScenarioConfig.Target(
                new ScenarioConfig.Target.Id(),
                targetLabel,
                description,
                optional
        );
    }
    
    private Optional<I18n> parseDescriptionFromChildren(List<Tree> children) {
        List<String> frenchLines = new ArrayList<>();
        List<String> englishLines = new ArrayList<>();
        String currentLang = null;
        
        for (Tree child : children) {
            String originalHeader = child.header();
            String headerUpper = originalHeader.toUpperCase();
            List<String> params = child.params();
            
            // TreeGenerator sépare différemment : header="FR", params=["Ceci est ma chambre."]
            if (FR_KEY.equals(headerUpper) && !params.isEmpty()) {
                currentLang = FR_KEY;
                frenchLines.add(params.get(0));
            } else if (EN_KEY.equals(headerUpper) && !params.isEmpty()) {
                currentLang = EN_KEY;
                englishLines.add(params.get(0));
            } else if (currentLang != null && params.isEmpty()) {
                // Ligne de continuation sans préfixe de langue (header contient le texte)
                switch (currentLang) {
                    case FR_KEY -> frenchLines.add(originalHeader);
                    case EN_KEY -> englishLines.add(originalHeader);
                }
            }
        }
        
        if (!frenchLines.isEmpty() || !englishLines.isEmpty()) {
            Map<Language, String> values = new HashMap<>();
            if (!frenchLines.isEmpty()) {
                values.put(Language.FR, String.join("\n", frenchLines) + "\n");
            }
            if (!englishLines.isEmpty()) {
                values.put(Language.EN, String.join("\n", englishLines) + "\n");
            }
            return Optional.of(new I18n(values));
        }
        
        return Optional.empty();
    }
    
    private Optional<I18n> parseI18nFromChildrenWithoutNewline(List<Tree> children) {
        List<String> frenchLines = new ArrayList<>();
        List<String> englishLines = new ArrayList<>();
        String currentLang = null;
        
        for (Tree child : children) {
            String originalHeader = child.header();
            String headerUpper = originalHeader.toUpperCase();
            List<String> params = child.params();
            
            // TreeGenerator sépare différemment : header="FR", params=["Ceci est ma chambre."]
            if (FR_KEY.equals(headerUpper) && !params.isEmpty()) {
                currentLang = FR_KEY;
                frenchLines.add(params.get(0));
            } else if (EN_KEY.equals(headerUpper) && !params.isEmpty()) {
                currentLang = EN_KEY;
                englishLines.add(params.get(0));
            } else if (currentLang != null && params.isEmpty()) {
                // Ligne de continuation sans préfixe de langue (header contient le texte)
                switch (currentLang) {
                    case FR_KEY -> frenchLines.add(originalHeader);
                    case EN_KEY -> englishLines.add(originalHeader);
                }
            }
        }
        
        if (!frenchLines.isEmpty() || !englishLines.isEmpty()) {
            Map<Language, String> values = new HashMap<>();
            if (!frenchLines.isEmpty()) {
                values.put(Language.FR, String.join("\n", frenchLines)); // Pas de \n final
            }
            if (!englishLines.isEmpty()) {
                values.put(Language.EN, String.join("\n", englishLines)); // Pas de \n final
            }
            return Optional.of(new I18n(values));
        }
        
        return Optional.empty();
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

    private Possibility parsePossibility(Tree tree) {
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
                    throw new TemplateException("Invalid format: " + tree.header().toUpperCase() + ":" + String.join(":", tree.params()));
            }
        }
        AndOrOr conditionType = AndOrOr.valueOf(conditionTypeStr);

        // Parse les conditions, conséquences et trigger qui suivent
        List<PossibilityCondition> conditions = new ArrayList<>();
        List<PossibilityConsequence> consequences = new ArrayList<>();
        AtomicReference<PossibilityTrigger> trigger = new AtomicReference<>();

        tree.children().forEach(child -> {
            String actionStr = child.header().toUpperCase().trim();
            switch (actionStr) {
                case POSSIBILITY_CONDITION_KEY:
                    conditions.add(parseCondition(child));
                    break;
                case POSSIBILITY_CONSEQUENCE_KEY:
                    consequences.add(parseConsequence(child));
                    break;
                case POSSIBILITY_TRIGGER_KEY:
                    trigger.set(parseTrigger(child));
                    break;
                case POSSIBILITY_RECURRENCE_KEY:
                    recurrence.set(parseRecurrence(child));
                    break;
                default:
                    throw new TemplateException("Invalid format: " + child.header() + ":" + String.join(":", child.params()));
            }
        });

        if (trigger.get() == null) {
            throw new TemplateException("Invalid format: " + tree.header() + ":" + String.join(":", tree.params()));
        }

        return new Possibility(recurrence.get(), trigger.get(), conditions, conditionType, consequences);
    }

    private PossibilityCondition parseCondition(Tree tree) {

        String type = tree.params().getFirst().toLowerCase();
        switch (type) {
            case "outsidespace" -> {
                if(tree.params().size() == 2) {
                    return new PossibilityCondition.OutsideSpace(
                            new PossibilityCondition.Id(),
                            new BoardSpace.Id(tree.params().get(1))
                    );
                }
                return new PossibilityCondition.OutsideSpace(
                        new PossibilityCondition.Id(),
                        new BoardSpace.Id(tree.params().get(2))
                );

                /*if (split.length >= 4 && "SpaceId".equals(split[2])) {
                    // Format: "Condition:outsidespace:SpaceId:ABCD"

                } else if (split.length >= 3) {
                    // Format: "CONDITION:outSidespace:9823"
                    return new PossibilityCondition.OutsideSpace(
                            new PossibilityCondition.Id(),
                            new BoardSpace.Id(split[2])
                    );
                }*/
            }
            case "absolutetime" -> {
                // Format: "CONDITION:ABSOLUTETIME:Duration:27" -> params=[ABSOLUTETIME, Duration, 27] 
                // ou "condition:ABSOLUTETIME:27" -> params=[ABSOLUTETIME, 27]
                if (tree.params().size() >= 3 && "Duration".equalsIgnoreCase(tree.params().get(1))) {
                    return new PossibilityCondition.AbsoluteTime(
                            new PossibilityCondition.Id(),
                            Duration.ofMinutes(Long.parseLong(tree.params().get(2))),
                            BeforeOrAfter.BEFORE //TODO
                    );
                } else {
                    return new PossibilityCondition.AbsoluteTime(
                            new PossibilityCondition.Id(),
                            Duration.ofMinutes(Long.parseLong(tree.params().get(1))),
                            BeforeOrAfter.BEFORE //TODO
                    );
                }
            }
            case "instep" -> {
                // Format: "CONDITION:InStep:0987" -> params=[InStep, 0987]
                return new PossibilityCondition.InStep(
                        new PossibilityCondition.Id(),
                        new ScenarioConfig.Step.Id(tree.params().get(1))
                );
            }
            case "insidespace" -> {
                if(tree.params().size() == 2) {
                    return new PossibilityCondition.InsideSpace(
                            new PossibilityCondition.Id(),
                            new BoardSpace.Id(tree.params().get(1))
                    );
                }
                return new PossibilityCondition.InsideSpace(
                        new PossibilityCondition.Id(),
                        new BoardSpace.Id(tree.params().get(2))
                );
                /*if (split.length >= 3 && "SpaceId".equalsIgnoreCase(split[2])) {

                } else {

                }*/
            }
        }

        throw new TemplateException("Invalid condition format: " + String.join(":", tree.params()));
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
                throw new TemplateException("Invalid recurrence format: " + String.join(":", tree.params()));
            }
        }

        throw new TemplateException("Invalid recurrence format: " + String.join(":", tree.params()));
    }

    private PossibilityConsequence parseConsequence(Tree tree) {
        // Format: "---------" + " consequence:Alert" ou "---------" + " consequence:GoalTarget:stepId:EFG:targetId:9876:state:FAILURE"
        
        if (tree.params().isEmpty()) {
            throw new TemplateException("Invalid consequence format: no type specified");
        }

        String type = tree.params().get(0).toLowerCase();
        switch (type) {
            case "alert" -> {
                // Parse le message I18n qui suit depuis les enfants (sans \n final pour les Alert)
                Optional<I18n> messageOpt = parseI18nFromChildrenWithoutNewline(tree.children());
                I18n message = messageOpt.orElse(new I18n(Map.of())); // I18n vide si pas de message
                return new PossibilityConsequence.Alert(new PossibilityConsequence.Id(), message);
            }
            case "goaltarget" -> {
                ScenarioConfig.Step.Id stepId = new ScenarioConfig.Step.Id(tree.params().get(2));
                ScenarioConfig.Target.Id targetId = new ScenarioConfig.Target.Id(tree.params().get(4));
                ScenarioGoal.State state = parseState(tree.params().get(6));
                return new PossibilityConsequence.GoalTarget(new PossibilityConsequence.Id(), stepId, targetId, state);

                /*if (split.length >= 7) {
                    // Format: "GoalTarget:stepId:EFG:targetId:9876:state:FAILURE"
                    ScenarioConfig.Step.Id stepId = new ScenarioConfig.Step.Id(split[3]);
                    ScenarioConfig.Target.Id targetId = new ScenarioConfig.Target.Id(split[5]);
                    ScenarioGoal.State state = parseState(split[7]);
                    return new PossibilityConsequence.GoalTarget(new PossibilityConsequence.Id(), stepId, targetId, state);
                } else {
                    // Format alternatif: "GoalTarget:targetId:9876:state:active:stepId:EFG"
                    ScenarioConfig.Step.Id stepId = null;
                    ScenarioConfig.Target.Id targetId = null;
                    ScenarioGoal.State state = null;

                    for (int i = 2; i < split.length - 1; i += 2) {
                        String key = split[i];
                        String value = split[i + 1];

                        switch (key.toLowerCase()) {
                            case "stepid" -> stepId = new ScenarioConfig.Step.Id(value);
                            case "targetid" -> targetId = new ScenarioConfig.Target.Id(value);
                            case "state" -> state = parseState(value);
                        }
                    }

                    if (stepId != null && targetId != null && state != null) {
                        return new PossibilityConsequence.GoalTarget(new PossibilityConsequence.Id(), stepId, targetId, state);
                    }
                }*/
            }
            case "goal" -> {
                /*if (split.length >= 5) {

                }*/
                // Format: "Goal:state:SUCCESS:stepId:KLM"
                ScenarioGoal.State state = parseState(tree.params().get(2));
                ScenarioConfig.Step.Id stepId = new ScenarioConfig.Step.Id(tree.params().get(4));
                return new PossibilityConsequence.Goal(new PossibilityConsequence.Id(), stepId, state);
            }
            case "addobjet" -> {
                // Format: "AddObjet:SWORD123"
                String objetId = tree.params().get(2);
                return new PossibilityConsequence.AddObjet(new PossibilityConsequence.Id(), objetId);
            }
            case "updatedmetadata" -> {
                // Format: "UpdatedMetadata:metadataId:toMinutes:25.5"
                String metadataId = tree.params().get(2);
                float value = Float.parseFloat(tree.params().get(4));
                return new PossibilityConsequence.UpdatedMetadata(new PossibilityConsequence.Id(), metadataId, value);
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
                // Sauvegarder le message précédent
                if (currentLang != null && !currentMessage.isEmpty()) {
                    messages.put(currentLang, currentMessage.toString().trim());
                }
                currentLang = Language.EN;
                currentMessage = new StringBuilder(line.substring((SPACE_3 + " EN:").length()).trim());
            } else if (line.startsWith(SPACE_3 + " FR:")) {
                // Sauvegarder le message précédent
                if (currentLang != null && !currentMessage.isEmpty()) {
                    messages.put(currentLang, currentMessage.toString().trim());
                }
                currentLang = Language.FR;
                currentMessage = new StringBuilder(line.substring((SPACE_3 + " FR:").length()).trim());
            } else if (line.startsWith(SPACE_3 + " ") && currentLang != null) {
                // Suite du message
                String content = line.substring((SPACE_3 + " ").length()).trim();
                if (!currentMessage.isEmpty()) {
                    currentMessage.append("\n");
                }
                currentMessage.append(content);
            } else if (line.startsWith(SPACE_2)) {
                // Sauvegarder le dernier message et arrêter
                if (currentLang != null && !currentMessage.isEmpty()) {
                    messages.put(currentLang, currentMessage.toString().trim());
                }
                break;
            }
        }

        // Sauvegarder le dernier message si on arrive à la fin
        if (currentLang != null && !currentMessage.isEmpty()) {
            messages.put(currentLang, currentMessage.toString().trim());
        }

        return new I18n(messages);
    }

    private PossibilityTrigger parseTrigger(Tree tree) {

        String type = tree.params().getFirst().toLowerCase();
        switch (type) {
            case "goinspace" -> {
                // Format: "Trigger:GoInSpace:SpaceId:ABCD" -> params=[GoInSpace, SpaceId, ABCD] 
                // ou "Trigger:goinSPACE:EFG" -> params=[goinSPACE, EFG]
                if (tree.params().size() >= 3 && "SpaceId".equalsIgnoreCase(tree.params().get(1))) {
                    return new PossibilityTrigger.GoInSpace(
                            new PossibilityTrigger.Id(),
                            new BoardSpace.Id(tree.params().get(2))  // Utiliser index 2 pour "ABCD"
                    );
                } else {
                    return new PossibilityTrigger.GoInSpace(
                            new PossibilityTrigger.Id(),
                            new BoardSpace.Id(tree.params().get(1))  // Utiliser index 1 pour "EFG"
                    );
                }
            }
            case "absolutetime" -> {
                return new PossibilityTrigger.AbsoluteTime(
                        new PossibilityTrigger.Id(),
                        TimeUnit.ofMinutes(Integer.parseInt(tree.params().get(1)))
                );
            }
        }

        return null;
    }

    private ScenarioGoal.State parseState(String state) {
        return switch (state.toLowerCase()) {
            case "success" -> ScenarioGoal.State.SUCCESS;
            case "failure" -> ScenarioGoal.State.FAILURE;
            case "active" -> ScenarioGoal.State.ACTIVE;
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
            default -> BoardSpace.Priority.LOW;
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
        String[] segments = rectInfo.split(":");

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
}

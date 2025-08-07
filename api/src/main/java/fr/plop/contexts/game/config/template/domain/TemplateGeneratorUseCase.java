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
import fr.plop.contexts.game.session.time.TimeClick;
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

public class TemplateGeneratorUseCase {

    private static final String SPACE_0 = "---";
    private static final String SPACE_1 = "------";
    private static final String SPACE_2 = "---------";
    private static final String SPACE_3 = "------------";
    private static final String SEPARATOR = ":";
    public static final String DEFAULT_VERSION = "0.0.0";
    public static final String POSSIBILITY_CONDITION_KEY = "condition";
    public static final String POSSIBILITY_CONSEQUENCE_KEY = "consequence";
    public static final String POSSIBILITY_TRIGGER_KEY = "trigger";
    public static final String POSSIBILITY_RECURRENCE_KEY = "recurrence";

    // Mots-clés des éléments principaux
    public static final String STEP_KEY = "Step";
    public static final String TARGET_KEY = "Target";
    public static final String POSSIBILITY_KEY = "Possibility";
    public static final String OPTIONAL_KEY = "(Opt)";
    public static final String BOARD_KEY = "Board";
    public static final String SPACE_KEY = "Space";

    // Langues
    public static final String FR_KEY = "FR";
    public static final String EN_KEY = "EN";


    public static class Script {
        private final String value;

        public Script(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    public Template apply(Script script) {
        String[] lines = script.getValue().split("\n");
        String firstLine = lines[0].trim();
        String[] parts = firstLine.split(SEPARATOR);

        String code = parts[0];
        String version = parts.length > 1 && !parts[1].isEmpty() ? parts[1] : DEFAULT_VERSION;
        String label = parts.length > 2 ? parts[2] : "";


        // Parsing des steps du scenario et des boards
        List<ScenarioConfig.Step> steps = parseSteps(lines);
        List<BoardSpace> boardSpaces = parseBoardSpaces(lines);

        // Création des composants avec des IDs générés
        ScenarioConfig scenario = new ScenarioConfig("", steps);
        BoardConfig board = new BoardConfig(boardSpaces);
        MapConfig map = new MapConfig(List.of());

        // Création de l'atom avec des IDs générés
        Template.Id templateId = new Template.Id();
        Template.Code templateCode = new Template.Code(code);
        Template.Atom atom = new Template.Atom(templateId, templateCode);

        return new Template(atom, label, version, maxDuration(parts), scenario, board, map);
    }

    private static Duration maxDuration(String[] parts) {
        if (parts.length > 3 && !parts[3].isEmpty()) {
            return Duration.ofMinutes(Long.parseLong(parts[3]));
        }
        return Duration.ofHours(1); // Valeur par défaut
    }

    private List<ScenarioConfig.Step> parseSteps(String[] lines) {
        List<ScenarioConfig.Step> steps = new ArrayList<>();

        for (int i = 1; i < lines.length; i++) {
            String line = lines[i].trim();

            if (line.startsWith(SPACE_0 + " " + STEP_KEY)) {
                ScenarioConfig.Step step = parseStep(line, lines, i);
                steps.add(step);
            }
        }

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

    private ScenarioConfig.Step parseStep(String stepLine, String[] allLines, int currentIndex) {
        // Format: "---" + " Step:FR:Chez Moi:EN:At Home" ou "---" + " Step"
        Optional<I18n> stepLabel = Optional.empty();

        if (stepLine.contains(SEPARATOR)) {
            String stepInfo = stepLine.substring((SPACE_0 + " " + STEP_KEY + SEPARATOR).length());
            stepLabel = parseI18nFromLine(stepInfo);
        }

        // Parse les targets et possibilities qui suivent
        List<ScenarioConfig.Target> targets = new ArrayList<>();
        List<Possibility> possibilities = new ArrayList<>();

        for (int i = currentIndex + 1; i < allLines.length; i++) {
            String line = allLines[i].trim();

            if (line.startsWith(SPACE_1 + " " + TARGET_KEY)) {
                ScenarioConfig.Target target = parseTarget(line, allLines, i);
                targets.add(target);
            } else if (line.startsWith(SPACE_1 + " " + POSSIBILITY_KEY)) {
                Possibility possibility = parsePossibility(line, allLines, i);
                if (possibility != null) {
                    possibilities.add(possibility);
                }
            } else if (line.startsWith(SPACE_0 + " ")) {
                // Nouveau step, on arrête
                break;
            }
        }

        return new ScenarioConfig.Step(
                new ScenarioConfig.Step.Id(),
                stepLabel,
                targets,
                possibilities
        );
    }

    private ScenarioConfig.Target parseTarget(String targetLine, String[] allLines, int currentIndex) {
        boolean optional = targetLine.contains(OPTIONAL_KEY);

        Optional<I18n> targetLabel = Optional.empty();

        // Trouver où commence le pattern i18n (après le premier ":")
        int firstColonIndex = targetLine.indexOf(SEPARATOR);
        if (firstColonIndex != -1) {
            String targetInfo = targetLine.substring(firstColonIndex + 1);
            targetLabel = parseI18nFromLine(targetInfo);
        }
        Optional<I18n> description = parseDescription(allLines, currentIndex + 1);

        return new ScenarioConfig.Target(
                new ScenarioConfig.Target.Id(),
                targetLabel,
                description,
                optional
        );
    }

    private Optional<I18n> parseDescription(String[] lines, int startIndex) {
        List<String> frenchLines = new ArrayList<>();
        List<String> englishLines = new ArrayList<>();
        String currentLang = null;

        for (int i = startIndex; i < lines.length; i++) {
            String line = lines[i].trim();

            if (line.startsWith(SPACE_2 + " " + FR_KEY + SEPARATOR)) {
                currentLang = FR_KEY;
                frenchLines.add(line.substring((SPACE_2 + " " + FR_KEY + SEPARATOR).length()).trim());
            } else if (line.startsWith(SPACE_2 + " " + EN_KEY + SEPARATOR)) {
                currentLang = EN_KEY;
                englishLines.add(line.substring((SPACE_2 + " " + EN_KEY + SEPARATOR).length()).trim());
            } else if (line.startsWith(SPACE_2 + " ") && currentLang != null) {
                String content = line.substring((SPACE_2 + " ").length()).trim();
                if (FR_KEY.equals(currentLang)) {
                    frenchLines.add(content);
                } else if (EN_KEY.equals(currentLang)) {
                    englishLines.add(content);
                }
            } else if (line.startsWith(SPACE_1 + " ") || line.startsWith(SPACE_0 + " ")) {
                // Nouveau target ou step, on arrête
                break;
            }
        }

        if (frenchLines.isEmpty() && englishLines.isEmpty()) {
            return Optional.empty();
        }

        Map<Language, String> i18nMap = new HashMap<>();
        if (!frenchLines.isEmpty()) {
            i18nMap.put(Language.FR, String.join("\n", frenchLines) + "\n");
        }
        if (!englishLines.isEmpty()) {
            i18nMap.put(Language.EN, String.join("\n", englishLines) + "\n");
        }

        return Optional.of(new I18n(i18nMap));
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

    private Possibility parsePossibility(String possibilityLine, String[] allLines, int currentIndex) {
        // Format: "------" + " Possibility:ALWAYS" ou "------" + " Possibility:times:4:OR" ou "------" + " Possibility"
        PossibilityRecurrence recurrence = new PossibilityRecurrence.Always(new PossibilityRecurrence.Id()); // Par défaut

        String conditionTypeStr = "AND";

        if (possibilityLine.contains(SEPARATOR)) {
            String[] split = possibilityLine.split(SEPARATOR);
            String typeStr = split[1].trim().toLowerCase();
            switch (typeStr) {
                case "always":
                    recurrence = new PossibilityRecurrence.Always(new PossibilityRecurrence.Id());
                    if (split.length == 3) {
                        conditionTypeStr = split[2];
                    }
                    break;
                case "times":
                    int times = Integer.parseInt(split[2]);
                    recurrence = new PossibilityRecurrence.Times(new PossibilityRecurrence.Id(), times);
                    if (split.length == 4) {
                        conditionTypeStr = split[3];
                    }
                    break;
                default:
                    throw new TemplateException("Invalid format: " + possibilityLine);
            }
        }
        AndOrOr conditionType = AndOrOr.valueOf(conditionTypeStr);

        // Parse les conditions, conséquences et trigger qui suivent
        List<PossibilityCondition> conditions = new ArrayList<>();
        List<PossibilityConsequence> consequences = new ArrayList<>();
        PossibilityTrigger trigger = null;

        for (int i = currentIndex + 1; i < allLines.length; i++) {
            String line = allLines[i].trim();

            if (line.startsWith(SPACE_1 + " ") || line.startsWith(SPACE_0 + " ")) {
                // Nouvelle possibility, target ou step, on arrête
                break;
            }

            if (line.startsWith(SPACE_2 + " ")) {
                String lineInfo = line.substring((SPACE_2 + " ").length()).trim();
                String[] split = lineInfo.split(":");
                if (split.length < 2)
                    throw new TemplateException("Invalid format: " + line);

                String actionStr = split[0].toLowerCase().trim();
                switch (actionStr) {
                    case POSSIBILITY_CONDITION_KEY:
                        conditions.add(parseCondition(split));
                        break;
                    case POSSIBILITY_CONSEQUENCE_KEY:
                        consequences.add(parseConsequence(split, allLines, i));
                        break;
                    case POSSIBILITY_TRIGGER_KEY:
                        trigger = parseTrigger(split);
                        break;
                    case POSSIBILITY_RECURRENCE_KEY:
                        recurrence = parseRecurrence(split);
                        break;
                    default:
                        throw new TemplateException("Invalid format: " + line);
                }
            }
        }

        if (trigger == null) {
            return null; // Un trigger est obligatoire
        }

        return new Possibility(recurrence, trigger, conditions, conditionType, consequences);
    }

    private PossibilityCondition parseCondition(String[] split) {

        String type = split[1].toLowerCase();
        switch (type) {
            case "outsidespace" -> {
                if (split.length >= 4 && "SpaceId".equals(split[2])) {
                    // Format: "Condition:outsidespace:SpaceId:ABCD"
                    return new PossibilityCondition.OutsideSpace(
                            new PossibilityCondition.Id(),
                            new BoardSpace.Id(split[3])  // Utiliser split[3] pour "ABCD"
                    );
                } else if (split.length >= 3) {
                    // Format: "CONDITION:outSidespace:9823"
                    return new PossibilityCondition.OutsideSpace(
                            new PossibilityCondition.Id(),
                            new BoardSpace.Id(split[2])
                    );
                }
            }
            case "absolutetime" -> {
                if (split.length >= 3 && "Duration".equals(split[2])) {
                    return new PossibilityCondition.AbsoluteTime(
                            new PossibilityCondition.Id(),
                            Duration.ofMinutes(Long.parseLong(split[3])),
                            BeforeOrAfter.BEFORE //TODO
                    );
                }
            }
            case "instep" -> {
                return new PossibilityCondition.InStep(
                        new PossibilityCondition.Id(),
                        new ScenarioConfig.Step.Id(split[2])
                );
            }
            case "insidespace" -> {
                if (split.length >= 3 && "SpaceId".equalsIgnoreCase(split[2])) {
                    return new PossibilityCondition.InsideSpace(
                            new PossibilityCondition.Id(),
                            new BoardSpace.Id(split[3])
                    );
                } else {
                    return new PossibilityCondition.InsideSpace(
                            new PossibilityCondition.Id(),
                            new BoardSpace.Id(split[2])
                    );
                }
            }
            /*case "relativetimeafterothertrigger" -> {
                return new PossibilityCondition.RelativeTimeAfterOtherTrigger(
                        new PossibilityCondition.Id(),
                        new PossibilityCondition.Id(split[2]),
                        Duration.ofMinutes(Long.parseLong(split[4]))
                );
            }*/
        }

        throw new TemplateException("Invalid condition format: " + String.join(":", split));
    }

    private PossibilityRecurrence parseRecurrence(String[] split) {
        // Format: "---------" + " Recurrency:tiMes:5" ou "---------" + " Recurrence:ALWAYS"

        String type = split[1].toLowerCase();
        if ("always".equals(type)) {
            return new PossibilityRecurrence.Always(new PossibilityRecurrence.Id());
        } else if ("times".equals(type)) {
            try {
                int times = Integer.parseInt(split[2]);
                return new PossibilityRecurrence.Times(new PossibilityRecurrence.Id(), times);
            } catch (NumberFormatException e) {
                throw new TemplateException("Invalid recurrence format: " + String.join(":", split));
            }
        }

        throw new TemplateException("Invalid recurrence format: " + String.join(":", split));
    }

    /*private PossibilityCondition parseCondition(String conditionLine) {
        // Extraire l'info après "condition:" (case insensitive)
        String conditionInfo;
        if (conditionLine.toLowerCase().contains("condition:")) {
            int index = conditionLine.toLowerCase().indexOf("condition:");
            conditionInfo = conditionLine.substring(index + "condition:".length());
        } else {
            return null;
        }
        
        String[] parts = conditionInfo.split(SEPARATOR);
        if (parts.length < 1) return null;
        
        String type = parts[0].toLowerCase();
        switch (type) {
            case "outsidespace" -> {
                if (parts.length >= 3 && "SpaceId".equals(parts[1])) {
                    return new PossibilityCondition.OutsideSpace(
                        new PossibilityCondition.Id(parts[2]), 
                        new BoardSpace.Id(parts[2])
                    );
                } else if (parts.length >= 2) {
                    // Format: "CONDITION:outSidespace:9823"
                    return new PossibilityCondition.OutsideSpace(
                        new PossibilityCondition.Id(parts[1]), 
                        new BoardSpace.Id(parts[1])
                    );
                }
            }
            case "absolutetime" -> {
                if (parts.length >= 3 && "Duration".equals(parts[1])) {
                    return new PossibilityCondition.AbsoluteTime(
                        new PossibilityCondition.Id(), 
                        Duration.ofMinutes(Long.parseLong(parts[2]))
                    );
                }
            }
            case "instep" -> {
                if (parts.length >= 2) {
                    return new PossibilityCondition.InStep(
                        new PossibilityCondition.Id(), 
                        new ScenarioConfig.Step.Id(parts[1])
                    );
                }
            }
            case "insidespace" -> {
                if (parts.length >= 3 && "SpaceId".equalsIgnoreCase(parts[1])) {
                    return new PossibilityCondition.InsideSpace(
                        new PossibilityCondition.Id(parts[2]), 
                        new BoardSpace.Id(parts[2])
                    );
                } else if (parts.length >= 2) {
                    return new PossibilityCondition.InsideSpace(
                        new PossibilityCondition.Id(parts[1]), 
                        new BoardSpace.Id(parts[1])
                    );
                }
            }
            case "relativetimeafterothertrigger" -> {
                if (parts.length >= 4 && "triggerid".equalsIgnoreCase(parts[1]) && "duration".equalsIgnoreCase(parts[3])) {
                    // Format: "RelativeTimeAfterOtherTrigger:triggerId:ABC123:duration:30"
                    return new PossibilityCondition.RelativeTimeAfterOtherTrigger(
                        new PossibilityCondition.Id(), 
                        new PossibilityCondition.Id(parts[2]),
                        Duration.ofMinutes(Long.parseLong(parts[4]))
                    );
                }
            }
        }
        
        return null;
    }*/

    private PossibilityConsequence parseConsequence(String[] split, String[] allLines, int currentIndex) {
        // Format: "---------" + " consequence:Alert" ou "---------" + " consequence:GoalTarget:stepId:EFG:targetId:9876:state:FAILURE"

        String type = split[1].toLowerCase();
        switch (type) {
            case "alert" -> {
                // Parse le message I18n qui suit
                I18n message = parseAlertMessage(allLines, currentIndex + 1);
                return new PossibilityConsequence.Alert(new PossibilityConsequence.Id(), message);
            }
            case "goaltarget" -> {
                if (split.length >= 7) {
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
                }
            }
            case "goal" -> {
                if (split.length >= 5) {
                    // Format: "Goal:state:SUCCESS:stepId:KLM"
                    ScenarioGoal.State state = parseState(split[3]);
                    ScenarioConfig.Step.Id stepId = new ScenarioConfig.Step.Id(split[5]);
                    return new PossibilityConsequence.Goal(new PossibilityConsequence.Id(), stepId, state);
                }
            }
            case "addobjet" -> {
                // Format: "AddObjet:SWORD123"
                String objetId = split[2];
                return new PossibilityConsequence.AddObjet(new PossibilityConsequence.Id(), objetId);
            }
            case "updatedmetadata" -> {
                // Format: "UpdatedMetadata:metadataId:value:25.5"
                String metadataId = split[2];
                float value = Float.parseFloat(split[4]);
                return new PossibilityConsequence.UpdatedMetadata(new PossibilityConsequence.Id(), metadataId, value);
            }
        }

        throw new TemplateException("Invalid consequence format: " + String.join(":", split));
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

    private PossibilityTrigger parseTrigger(String[] split) {

        String type = split[1].toLowerCase();
        switch (type) {
            case "goinspace" -> {
                // Format: "Trigger:GoInSpace:SpaceId:ABCD" ou "Trigger:goinSPACE:EFG"
                if (split.length >= 4 && "SpaceId".equalsIgnoreCase(split[2])) {
                    return new PossibilityTrigger.GoInSpace(
                            new PossibilityTrigger.Id(),
                            new BoardSpace.Id(split[3])  // Utiliser split[3] pour "ABCD"
                    );
                } else if (split.length >= 3) {
                    // Format simplifié: "Trigger:goinSPACE:EFG"
                    return new PossibilityTrigger.GoInSpace(
                            new PossibilityTrigger.Id(),
                            new BoardSpace.Id(split[2])  // Utiliser split[2] pour "EFG"
                    );
                }
            }
            case "absolutetime" -> {
                return new PossibilityTrigger.AbsoluteTime(
                        new PossibilityTrigger.Id(),
                        TimeClick.ofMinutes(Integer.parseInt(split[2]))
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
            if ("bottomLeft".equals(segments[i]) && i + 2 < segments.length) {
                try {
                    float x = Float.parseFloat(segments[i + 1]);
                    float y = Float.parseFloat(segments[i + 2]);
                    bottomLeft = new Point(x, y);
                    i += 2; // Avancer de 2 positions
                } catch (NumberFormatException e) {
                    // Ignorer cette partie
                }
            } else if ("topRight".equals(segments[i]) && i + 2 < segments.length) {
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

package fr.plop.contexts.game.config.template.domain.usecase.generator;

import fr.plop.contexts.game.config.Image.domain.ImageConfig;
import fr.plop.contexts.game.config.board.domain.model.BoardConfig;
import fr.plop.contexts.game.config.board.domain.model.BoardSpace;
import fr.plop.contexts.game.config.condition.Condition;
import fr.plop.contexts.game.config.consequence.Consequence;
import fr.plop.contexts.game.config.map.domain.MapConfig;
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
    private final TemplateGeneratorGlobalCache globalCache = new TemplateGeneratorGlobalCache();
    private final TemplateGeneratorBoardUseCase boardGenerator = new TemplateGeneratorBoardUseCase(globalCache);
    private final TemplateGeneratorTalkUseCase talkGenerator = new TemplateGeneratorTalkUseCase(globalCache);
    private final TemplateGeneratorImageUseCase imageGenerator = new TemplateGeneratorImageUseCase();
    private final TemplateGeneratorConditionUseCase conditionGenerator = new TemplateGeneratorConditionUseCase(globalCache);
    private final TemplateGeneratorI18nUseCase i18nGenerator = new TemplateGeneratorI18nUseCase();
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

        ImageConfig image = imageGenerator.apply(rootTree);

        MapConfig map = mapGenerator.apply(rootTree);

        // 3. Parse les Steps en utilisant la map pour résoudre les SpaceId et le context pour les références
        ScenarioConfig scenario = new ScenarioConfig(parseSteps(rootTree, globalCache, talkConfig));



        // 4. Vérifier qu'il n'y a pas de références non résolues
        if (globalCache.hasUnresolvedReferences()) {
            throw new TemplateException("Unresolved references: " + globalCache.getUnresolvedReferences());
        }

        String version = rootTree.hasParams() ? rootTree.param(0) : DEFAULT_VERSION;
        String label = rootTree.paramSize() > 1 ? rootTree.param(1) : "";
        Duration duration = Duration.ofHours(1);
        if (rootTree.paramSize() > 2 && !rootTree.param(2).isEmpty()) {
            duration = Duration.ofMinutes(Long.parseLong(rootTree.param(2)));
        }
        Template.Atom templateAtom = new Template.Atom(new Template.Id(), new Template.Code(rootTree.headerKeepCase()));
        return new Template(templateAtom, label, version, duration, scenario, board, map, talkConfig, image);
    }

    // ============ NOUVELLES METHODES BASEES SUR TREEGENERATOR ============

    // ============ ANCIENNES METHODES (à conserver pour le moment) ============

    private List<ScenarioConfig.Step> parseSteps(Tree root, TemplateGeneratorGlobalCache globalCache, TalkConfig talkConfig) {
        List<ScenarioConfig.Step> steps = new ArrayList<>();
        root.children().forEach(child -> {
            if (child.header().startsWith(STEP_KEY)) {
                ScenarioConfig.Step step = parseStep(child, globalCache, talkConfig);
                steps.add(step);
            }
        });
        return steps;
    }

    private ScenarioConfig.Step parseStep(Tree root, TemplateGeneratorGlobalCache globalCache, TalkConfig talkConfig) {
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
                    ScenarioConfig.Target target = parseTarget(child, globalCache);
                    targets.add(target);
                    break;
                case POSSIBILITY_KEY:
                    possibilityTrees.add(child);
                    break;
                default:
                    // Vérifions si c'est un Target qui commence par TARGET mais avec du texte après
                    if (child.header().startsWith(TARGET_KEY)) {
                        ScenarioConfig.Target extendedTarget = parseTarget(child, globalCache);
                        targets.add(extendedTarget);
                    }
                    break;
            }
        });

        // Créer le step d'abord (sans les possibilités)
        ScenarioConfig.Step step = new ScenarioConfig.Step(new ScenarioConfig.Step.Id(), stepLabel, 0, targets, new ArrayList<>()); //TODO order

        // Enregistrer la référence immédiatement si elle existe
        if (referenceName != null) {
            globalCache.registerReference(referenceName, step);
        }

        // Enregistrer le step actuel comme "CURRENT_STEP" AVANT de parser les possibilités
        globalCache.registerReference("CURRENT_STEP", step);

        // Parse toutes les possibilités maintenant que CURRENT_STEP est disponible
        for (Tree possibilityTree : possibilityTrees) {
            Possibility possibility = parsePossibility(possibilityTree, globalCache, talkConfig);
            possibilities.add(possibility);
        }

        // Mettre à jour le step avec les possibilités
        ScenarioConfig.Step finalStep = new ScenarioConfig.Step(step.id(), stepLabel,0, targets, possibilities); //TODO order

        // Mettre à jour les références avec le step final
        if (referenceName != null) {
            globalCache.registerReference(referenceName, finalStep);
        }
        globalCache.registerReference("CURRENT_STEP", finalStep);

        return finalStep;
    }

    private ScenarioConfig.Target parseTarget(Tree tree, TemplateGeneratorGlobalCache globalCache) {
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
            globalCache.registerReference(referenceName, target);
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

    private Possibility parsePossibility(Tree tree, TemplateGeneratorGlobalCache globalCache, TalkConfig talkConfig) {
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
        AtomicReference<PossibilityTrigger> trigger = new AtomicReference<>();
        List<Condition> conditions = new ArrayList<>();
        List<Consequence> consequences = new ArrayList<>();

        // Parse en deux passes : d'abord les conséquences pour enregistrer les références, puis les triggers
        // Première passe : conséquences et conditions
        tree.children().forEach(child -> {
            String actionStr = child.header().trim();
            switch (actionStr) {
                case POSSIBILITY_CONDITION_KEY:
                    conditions.add(conditionGenerator.apply(child));
                    break;
                case POSSIBILITY_CONSEQUENCE_KEY:
                    consequences.add(parseConsequence(child, globalCache));
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
                trigger.set(parseTrigger(child, globalCache, talkConfig));
            }
        });

        if (trigger.get() == null) {
            throw new TemplateException("Invalid format: " + tree.header() + SEPARATOR + String.join(SEPARATOR, tree.params()));
        }
        return new Possibility(new Possibility.Id(), recurrence.get(), trigger.get(),
                Condition.buildAndFromList(conditions), consequences);
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

    private Consequence parseConsequence(Tree tree, TemplateGeneratorGlobalCache globalCache) {
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
                return parseGoalTargetConsequence(tree, globalCache);
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
                return parseTalkOptionsConsequence(tree, globalCache);
            }
            case "TALK" -> {
                return parseTalkConsequence(tree, globalCache);
            }
            case "GAMEOVER" -> {
                return parseGameOverConsequence(tree);
            }
        }
        throw new TemplateException("Invalid consequence format: " + type);
    }
    
    private PossibilityTrigger parseTrigger(Tree tree, TemplateGeneratorGlobalCache globalCache, TalkConfig talkConfig) {

        String type = tree.params().getFirst().toLowerCase();
        switch (type) {
            case "goinspace" -> {
                // Format: "Trigger:GoInSpace:SpaceId:ABCD" ou "Trigger:GoInSpace:EFG" (legacy)
                Tree subTree = tree.sub();
                String spaceRef;
                if (subTree.hasUniqueParam()) {
                    spaceRef = subTree.uniqueParam();
                } else if (subTree.hasParamKey("SpaceId")) {
                    spaceRef = subTree.paramValue("SpaceId");
                } else {
                    throw new TemplateException("GoInSpace missing required parameter: SpaceId");
                }

                BoardSpace.Id spaceId = globalCache.getReference(spaceRef, BoardSpace.Id.class).orElseThrow();
                return new PossibilityTrigger.SpaceGoIn(
                        new PossibilityTrigger.Id(),
                        spaceId
                );
            }
            case "gooutspace" -> {
                // Format: "Trigger:GoOutSpace:SpaceId:ABCD" ou "Trigger:GoOutSpace:EFG" (legacy)
                Tree subTree = tree.sub();
                String spaceRef;
                if (subTree.hasUniqueParam()) {
                    spaceRef = subTree.uniqueParam();
                } else if (subTree.hasParamKey("SpaceId")) {
                    spaceRef = subTree.paramValue("SpaceId");
                } else {
                    throw new TemplateException("GoOutSpace missing required parameter: SpaceId");
                }

                BoardSpace.Id spaceId = globalCache.getReference(spaceRef, BoardSpace.Id.class).orElseThrow();
                return new PossibilityTrigger.SpaceGoOut(new PossibilityTrigger.Id(), spaceId);
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
                return createTalkOptionSelect(tree, globalCache, talkConfig);
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
                globalCache.requestReference(talkIdStr, talkItemObj -> {
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
    private Consequence parseGoalTargetConsequence(Tree tree, TemplateGeneratorGlobalCache globalCache) {
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
                Optional<ScenarioConfig.Step> currentStep = globalCache.getReference("CURRENT_STEP", ScenarioConfig.Step.class);
                if (currentStep.isPresent()) {
                    resolvedStepId.set(currentStep.get().id());
                } else {
                    // Si pas disponible immédiatement, utiliser la logique asynchrone
                    globalCache.requestReference("CURRENT_STEP", stepObj -> {
                        if (stepObj instanceof ScenarioConfig.Step step) {
                            resolvedStepId.set(step.id());
                        }
                    });
                }
            } else {
                // Essayer d'abord de résoudre de manière synchrone comme référence
                Optional<ScenarioConfig.Step> referencedStep = globalCache.getReference(stepIdParam, ScenarioConfig.Step.class);
                if (referencedStep.isPresent()) {
                    resolvedStepId.set(referencedStep.get().id());
                } else {
                    // Si pas trouvé, essayer la résolution asynchrone sans marquer comme "non résolue"
                    globalCache.tryRequestReference(stepIdParam, stepObj -> {
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
        Optional<ScenarioConfig.Target> referencedTarget = globalCache.getReference(targetIdParam, ScenarioConfig.Target.class);
        if (referencedTarget.isPresent()) {
            resolvedTargetId.set(referencedTarget.get().id());
        } else {
            // Si pas trouvé, essayer la résolution asynchrone sans marquer comme "non résolue"
            globalCache.tryRequestReference(targetIdParam, targetObj -> {
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
            Optional<ScenarioConfig.Step.Id> deducedStepId = findStepContainingTarget(targetIdParam, globalCache);
            if (deducedStepId.isPresent()) {
                resolvedStepId.set(deducedStepId.get());
            } else {
                throw new TemplateException("Cannot deduce stepId for target '" + targetIdParam + "': no step found containing this target");
            }
        }

        return new Consequence.ScenarioTarget(new Consequence.Id(), resolvedStepId.get(), resolvedTargetId.get(), state);
    }

    private Consequence parseTalkOptionsConsequence(Tree tree, TemplateGeneratorGlobalCache globalCache) {
        // Cas 1: TalkOptions avec référence (ex: "TalkOptions:OPTIONS_ABCD")
        if (tree.params().size() >= 2) {
            String reference = tree.params().get(1); // "OPTIONS_ABCD"
            return parseTalkOptionsWithReference(reference, globalCache);
        }

        throw new TemplateException("TalkOptions consequence without reference");
    }

    private Consequence parseTalkOptionsWithReference(String reference, TemplateGeneratorGlobalCache globalCache) {
        // Créer une référence atomique pour stocker l'ID résolu
        AtomicReference<TalkItem.Id> resolvedTalkId = new AtomicReference<>();

        // Demander la résolution de la référence
        globalCache.requestReference(reference, referencedObject -> {
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


    private Consequence parseTalkConsequence(Tree tree, TemplateGeneratorGlobalCache globalCache) {
        // Format: "Consequence:Talk:TALK000"
        // tree.params() contient ["Talk", "TALK000"]

        if (tree.params().size() < 2) {
            throw new TemplateException("Talk consequence must specify a talk ID or reference");
        }

        String talkReference = tree.params().get(1); // "TALK000"

        // Créer une référence atomique pour stocker l'ID résolu
        AtomicReference<TalkItem.Id> resolvedTalkId = new AtomicReference<>();

        // Demander la résolution de la référence
        globalCache.requestReference(talkReference, referencedObject -> {
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
     * Trouve le step qui contient un target donné (par référence ou par ID).
     *
     * @param targetParam Le nom de la référence ou l'ID du target à chercher
     * @return L'ID du step qui contient ce target, ou Optional.empty() si non trouvé
     */
    private Optional<ScenarioConfig.Step.Id> findStepContainingTarget(String targetParam, TemplateGeneratorGlobalCache globalCache) {
        // D'abord essayer de résoudre le target comme une référence
        Optional<ScenarioConfig.Target> referencedTarget = globalCache.getReference(targetParam, ScenarioConfig.Target.class);

        if (referencedTarget.isPresent()) {
            // Si on a trouvé le target par référence, chercher dans quel step il se trouve
            ScenarioConfig.Target target = referencedTarget.get();

            // Parcourir tous les steps enregistrés pour trouver celui qui contient ce target
            return globalCache.getAllReferences(ScenarioConfig.Step.class)
                    .stream()
                    .filter(step -> step.targets().stream()
                            .anyMatch(stepTarget -> stepTarget.id().equals(target.id())))
                    .map(ScenarioConfig.Step::id)
                    .findFirst();
        }

        // Si pas trouvé par référence, chercher par ID dans tous les steps
        ScenarioConfig.Target.Id targetId = new ScenarioConfig.Target.Id(targetParam);

        return globalCache.getAllReferences(ScenarioConfig.Step.class)
                .stream()
                .filter(step -> step.targets().stream()
                        .anyMatch(stepTarget -> stepTarget.id().equals(targetId)))
                .map(ScenarioConfig.Step::id)
                .findFirst();
    }


    /// Refacto


    private PossibilityTrigger createTalkOptionSelect(Tree tree, TemplateGeneratorGlobalCache globalCache, TalkConfig talkConfig) {
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

        TalkItem.Options.Option.Id optId = globalCache.getReference(optionReference, TalkItem.Options.Option.Id.class)
                    .orElseThrow(); //TODO
        TalkItem.Id talkId = talkConfig.findByIdByOptionId(optId)
                .orElseThrow(); //TODO

        return new PossibilityTrigger.TalkOptionSelect(new PossibilityTrigger.Id(), talkId, optId);
    }

}

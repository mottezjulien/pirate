package fr.plop.contexts.game.config.template.domain.usecase.generator.tree;

import fr.plop.contexts.game.config.Image.domain.ImageObject;
import fr.plop.contexts.game.config.board.domain.model.BoardSpace;
import fr.plop.contexts.game.config.condition.Condition;
import fr.plop.contexts.game.config.consequence.Consequence;
import fr.plop.contexts.game.config.scenario.domain.model.Possibility;
import fr.plop.contexts.game.config.scenario.domain.model.PossibilityRecurrence;
import fr.plop.contexts.game.config.scenario.domain.model.PossibilityTrigger;
import fr.plop.contexts.game.config.scenario.domain.model.ScenarioConfig;
import fr.plop.contexts.game.config.talk.domain.TalkConfig;
import fr.plop.contexts.game.config.talk.domain.TalkItem;
import fr.plop.contexts.game.config.talk.domain.TalkItemNext;
import fr.plop.contexts.game.config.template.domain.TemplateException;
import fr.plop.contexts.game.config.template.domain.model.Tree;
import fr.plop.contexts.game.config.template.domain.usecase.generator.GlobalCache;
import fr.plop.contexts.game.session.core.domain.model.SessionGameOver;
import fr.plop.contexts.game.session.scenario.domain.model.ScenarioSessionState;
import fr.plop.contexts.game.session.time.GameSessionTimeUnit;
import fr.plop.subs.i18n.domain.I18n;
import fr.plop.subs.i18n.domain.Language;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

public class TemplateGeneratorTreeScenarioUseCase {

    private static final String SEPARATOR = ":";
    public static final String SCENARIO_KEY = "SCENARIO";
    private static final String STEP_KEY = "STEP";
    private static final String TARGET_KEY = "TARGET";
    private static final String POSSIBILITY_KEY = "POSSIBILITY";
    private static final String POSSIBILITY_CONDITION_KEY = "CONDITION";
    private static final String POSSIBILITY_CONSEQUENCE_KEY = "CONSEQUENCE";
    private static final String POSSIBILITY_TRIGGER_KEY = "TRIGGER";
    private static final String POSSIBILITY_RECURRENCE_KEY = "RECURRENCE";
    private static final String PARAM_KEY_TALK_OPTION = "option";

    private final TemplateGeneratorTreeI18nUseCase i18nGenerator = new TemplateGeneratorTreeI18nUseCase();
    private final GlobalCache globalCache;
    private final TemplateGeneratorTreeConditionUseCase conditionGenerator;

    public TemplateGeneratorTreeScenarioUseCase(GlobalCache globalCache) {
        this.globalCache = globalCache;
        this.conditionGenerator = new TemplateGeneratorTreeConditionUseCase(globalCache);
    }

    public ScenarioConfig apply(Tree rootTree, TalkConfig talkConfig) {
        Stream<Tree> withoutScenarioLevel = rootTree.children().stream()
                .filter(child -> child.header().startsWith(STEP_KEY));
        Stream<Tree> withScenarioLevel = rootTree.children().stream()
                .filter(child -> child.isHeader(SCENARIO_KEY))
                .flatMap(child -> child.children().stream())
                .filter(child -> child.header().startsWith(STEP_KEY));
        List<ScenarioConfig.Step> list = Stream.concat(withScenarioLevel, withoutScenarioLevel)
                .map(child -> parseStep(child, talkConfig))
                .toList();
        return new ScenarioConfig(list);
    }


    private ScenarioConfig.Step parseStep(Tree root, TalkConfig talkConfig) {
        final ScenarioConfig.Step.Id stepId = initStepId(root);

        I18n stepLabel = parseI18nFromLine(root).orElseThrow();

        //Step before possibility (keep foreach)
        List<ScenarioConfig.Target> targets = new ArrayList<>();
        for(Tree child: root.children()){
            if(child.header().equals(TARGET_KEY)) {
                targets.add((parseTarget(child)));
            }
        }

        List<Possibility> possibilities = root.children().stream()
                .filter(child -> child.header().equals(POSSIBILITY_KEY))
                .map(child -> parsePossibility(child, stepId, talkConfig))
                .toList();

        return new ScenarioConfig.Step(stepId, stepLabel, Optional.empty(), 0, targets, possibilities);
    }

    private ScenarioConfig.Step.Id initStepId(Tree root) {
        if (root.reference() != null) {
            return globalCache.reference(root.reference(), ScenarioConfig.Step.Id.class, new ScenarioConfig.Step.Id());
        }
        return new ScenarioConfig.Step.Id();
    }


    private ScenarioConfig.Target parseTarget(Tree tree) {
        ScenarioConfig.Target.Id targetId = new ScenarioConfig.Target.Id();
        if (tree.reference() != null) {
            targetId = globalCache.reference(tree.reference(), ScenarioConfig.Target.Id.class, targetId);
        }
        boolean optional = tree.findByKey("OPTIONAL").map(Boolean::parseBoolean).orElse(false);

        I18n label = tree.findChildKey("LABEL")
                .map(childLabel -> i18nGenerator.apply(childLabel.children()).orElseThrow())
                .orElseGet(() -> parseI18nFromLine(tree).orElseThrow());
        Optional<I18n> optDescription = tree.findChildKey("DESCRIPTION")
                .map(childLabel -> i18nGenerator.apply(childLabel.children()).orElseThrow());
        List<I18n> hints = tree.childrenByKey("HINT")
                .map(childLabel -> i18nGenerator.apply(childLabel.children()).orElseThrow())
                .toList();
        Optional<I18n> optAnswer = tree.findChildKey("ANSWER")
                .map(childLabel -> i18nGenerator.apply(childLabel.children()).orElseThrow());
        return new ScenarioConfig.Target(targetId, label, optDescription, optional, hints, optAnswer);
    }

    private Optional<I18n> parseI18nFromLine(Tree tree) {
        Map<Language, String> i18nMap = new HashMap<>();
        tree.keys().forEach(key -> Language.safeValueOf(key.trim())
                .ifPresent(language -> i18nMap.put(language, tree.findByKeyOrThrow(key).trim())));
        if (!i18nMap.isEmpty()) {
            return Optional.of(new I18n(i18nMap));
        }
        return Optional.empty();
    }

    private Possibility parsePossibility(Tree tree, ScenarioConfig.Step.Id currentStepId, TalkConfig talkConfig) {
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
                    consequences.add(parseConsequence(child));
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
                trigger.set(parseTrigger(child, currentStepId, talkConfig));
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

    private Consequence parseConsequence(Tree tree) {
        Tree sub = tree.sub();
        switch (sub.header()) {
            case "ALERT" -> {
                // Parse le value I18n qui suit depuis les enfants (sans \n final pour les Alert)
                Optional<I18n> messageOpt = i18nGenerator.apply(tree.children());
                I18n message = messageOpt.orElse(new I18n(Map.of())); // I18n vide si pas de value
                return new Consequence.DisplayAlert(new Consequence.Id(), message);
            }
            case "GOALTARGET" -> {
                return parseGoalTargetConsequence(sub);
            }
            case "GOAL" -> {
                // Format: "Goal:state:SUCCESS:stepId:KLM" ou autre ordre
                String stateParam = sub.findByKeyOrThrow("STATE");
                String stepIdParam = sub.findByKeyOrThrow("STEPID");

                ScenarioSessionState state = parseState(stateParam);

                ScenarioConfig.Step.Id stepId = globalCache.reference(stepIdParam, ScenarioConfig.Step.Id.class, new ScenarioConfig.Step.Id());

                return new Consequence.ScenarioStep(new Consequence.Id(), stepId, state);
            }
            case "ADDOBJECT" -> {
                String objetId = sub.findByKeyOrThrow("objetid");
                return new Consequence.ObjetAdd(new Consequence.Id(), objetId);
            }
            case "UPDATEDMETADATA" -> {
                String metadataId = sub.findByKeyOrThrow("metadataid");
                String valueStr = sub.findByKeyOrThrow("value");
                float value = Float.parseFloat(valueStr);
                return new Consequence.UpdatedMetadata(new Consequence.Id(), metadataId, value);
            }
            case "TALK" -> {
                String talkReference = sub.findByKeyWithUnique("talkId");
                TalkItem.Id talkId = globalCache.reference(talkReference, TalkItem.Id.class, new TalkItem.Id());
                return new Consequence.DisplayTalk(new Consequence.Id(), talkId);
            }
            case "GAMEOVER" -> {
                return parseGameOverConsequence(tree);
            }
        }
        throw new TemplateException("Invalid consequence format: " + sub.header());
    }

    private PossibilityTrigger parseTrigger(Tree tree, ScenarioConfig.Step.Id currentStepId, TalkConfig talkConfig) {

        Tree subTree = tree.sub();
        switch (subTree.header()) {
            case "GOINSPACE" -> {
                String spaceRef = subTree.findByKeyWithUnique("SpaceId");
                BoardSpace.Id spaceId = globalCache.reference(spaceRef, BoardSpace.Id.class, new BoardSpace.Id());
                return new PossibilityTrigger.SpaceGoIn(new PossibilityTrigger.Id(), spaceId);
            }
            case "GOOUTSPACE" -> {
                String spaceRef = subTree.findByKeyWithUnique("SpaceId");
                BoardSpace.Id spaceId = globalCache.reference(spaceRef, BoardSpace.Id.class, new BoardSpace.Id());
                return new PossibilityTrigger.SpaceGoOut(new PossibilityTrigger.Id(), spaceId);
            }
            case "STEPACTIVE" -> {
                return new PossibilityTrigger.StepActive(new PossibilityTrigger.Id(), currentStepId);
            }
            case "ABSOLUTETIME" -> {
                String valueStr = subTree.findByKeyWithUnique("value");
                return new PossibilityTrigger.AbsoluteTime(
                        new PossibilityTrigger.Id(),
                        GameSessionTimeUnit.ofMinutes(Integer.parseInt(valueStr)));
            }
            case "TALKOPTIONSELECT" -> {
                String optionReference = subTree.findByKeyWithUnique(PARAM_KEY_TALK_OPTION);
                TalkItemNext.Options.Option.Id optId = globalCache.reference(optionReference, TalkItemNext.Options.Option.Id.class, new TalkItemNext.Options.Option.Id());
                TalkItem.Id talkId = talkConfig.findByIdByOptionId(optId).orElseThrow(); //TODO
                return new PossibilityTrigger.TalkOptionSelect(new PossibilityTrigger.Id(), talkId, optId);
            }
            case "TALKEND" -> {
                String talkReference = subTree.findByKeyWithUnique("talkId");
                TalkItem.Id talkId = globalCache.reference(talkReference, TalkItem.Id.class, new TalkItem.Id());
                return new PossibilityTrigger.TalkEnd(new PossibilityTrigger.Id(), talkId);
            }
            case "IMAGEOBJECTCLICK" -> {
                String objectReference = subTree.findByKeyWithUnique("objectId");
                ImageObject.Id objectId = globalCache.reference(objectReference, ImageObject.Id.class, new ImageObject.Id());
                return new PossibilityTrigger.ImageObjectClick(new PossibilityTrigger.Id(), objectId);
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

    private Consequence parseGoalTargetConsequence(Tree sub) {
        String targetIdParam = sub.findByKeyOrThrow("targetid");
        String stateParam = sub.findByKeyOrThrow("state");
        ScenarioConfig.Target.Id targetId = globalCache.reference(targetIdParam, ScenarioConfig.Target.Id.class, new ScenarioConfig.Target.Id());
        return new Consequence.ScenarioTarget(new Consequence.Id(), targetId, parseState(stateParam));
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


}

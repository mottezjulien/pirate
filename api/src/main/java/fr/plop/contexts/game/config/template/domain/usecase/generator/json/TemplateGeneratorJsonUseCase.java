package fr.plop.contexts.game.config.template.domain.usecase.generator.json;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.plop.contexts.game.config.Image.domain.ImageConfig;
import fr.plop.contexts.game.config.Image.domain.ImageObject;
import fr.plop.contexts.game.config.board.domain.model.BoardConfig;
import fr.plop.contexts.game.config.board.domain.model.BoardSpace;
import fr.plop.contexts.game.config.condition.Condition;
import fr.plop.contexts.game.config.consequence.Consequence;
import fr.plop.contexts.game.config.Image.domain.ImageGeneric;
import fr.plop.contexts.game.config.Image.domain.ImageItem;
import fr.plop.contexts.game.config.map.domain.MapConfig;
import fr.plop.contexts.game.config.map.domain.MapItem;
import fr.plop.contexts.game.config.scenario.domain.model.Possibility;
import fr.plop.contexts.game.config.scenario.domain.model.PossibilityRecurrence;
import fr.plop.contexts.game.config.scenario.domain.model.PossibilityTrigger;
import fr.plop.contexts.game.config.scenario.domain.model.ScenarioConfig;
import fr.plop.contexts.game.config.talk.domain.TalkCharacter;
import fr.plop.contexts.game.config.talk.domain.TalkConfig;
import fr.plop.contexts.game.config.talk.domain.TalkItem;
import fr.plop.contexts.game.config.talk.domain.TalkItemNext;
import fr.plop.contexts.game.config.talk.domain.TalkItemOut;
import fr.plop.contexts.game.config.template.domain.model.Template;
import fr.plop.contexts.game.config.template.domain.usecase.generator.GlobalCache;
import fr.plop.contexts.game.session.core.domain.model.SessionGameOver;
import fr.plop.contexts.game.session.scenario.domain.model.ScenarioSessionState;
import fr.plop.contexts.game.session.time.GameSessionTimeUnit;
import fr.plop.generic.enumerate.BeforeOrAfter;

import fr.plop.generic.ImagePoint;
import fr.plop.generic.enumerate.Priority;
import fr.plop.generic.position.Address;
import fr.plop.generic.position.Location;
import fr.plop.generic.position.Rectangle;
import fr.plop.subs.i18n.domain.I18n;
import fr.plop.subs.i18n.domain.Language;
import fr.plop.subs.image.Image;

import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

public class TemplateGeneratorJsonUseCase {

    private final GlobalCache globalCache = new GlobalCache();
    private TalkConfig talkConfig;

    public Template apply(String json) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        TemplateGeneratorRoot root = mapper.readValue(json, TemplateGeneratorRoot.class);

        final Template.Atom templateAtom = new Template.Atom(new Template.Id(), new Template.Code(root.code().toUpperCase()));
        final Duration duration = root.duration() == 0 ? Duration.ofMinutes(60) : Duration.ofMinutes(root.duration());
        String version = root.version() == null ? "0.0.0" : root.version();
        String label = root.label() == null ? "" : root.label();

        Template.Descriptor descriptor = descriptor(root);

        BoardConfig board = mapBoard(root.board());
        TalkConfig talk = mapTalk(root.talk());
        this.talkConfig = talk;
        ScenarioConfig scenario = mapScenario(root.scenario());
        MapConfig map = mapMaps(root.maps());
        ImageConfig imageConfig = mapImage(root.image());



        return new Template(templateAtom, label, version, descriptor, duration, scenario,
                board, map, talk, imageConfig);
    }

    private static Template.Descriptor descriptor(TemplateGeneratorRoot root) {
        Template.Descriptor.Level level = root.level() == 0 ? Template.Descriptor.Level._default() : Template.Descriptor.Level.from(root.level());
        String desc = root.description() == null ? "" : root.description();
        TemplateGeneratorRoot.Departure jsonDeparture = root.departure();
        Location departure = jsonDeparture == null ? Location.lyonBellecour()
                : new Location(Address.fromString(jsonDeparture.address()), jsonDeparture.rectangle().toModel());
        return new Template.Descriptor(level, desc, departure);
    }

    private BoardConfig mapBoard(TemplateGeneratorRoot.Board boardRoot) {
        if (boardRoot == null || boardRoot.spaces() == null) {
            return new BoardConfig();
        }
        List<BoardSpace> spaces = boardRoot.spaces().stream()
                .map(this::mapBoardSpace)
                .toList();
        return new BoardConfig(spaces);
    }

    private BoardSpace mapBoardSpace(TemplateGeneratorRoot.Board.Spaces spaceRoot) {
        BoardSpace.Id id = spaceRoot.ref() != null
                ? globalCache.reference(spaceRoot.ref(), BoardSpace.Id.class, new BoardSpace.Id())
                : new BoardSpace.Id();

        Priority priority = spaceRoot.priority() != null ? Priority.valueOf(spaceRoot.priority().toUpperCase()) : Priority.byDefault();
        String label = spaceRoot.label() != null ? spaceRoot.label() : (spaceRoot.ref() != null ? spaceRoot.ref() : "");

        List<Rectangle> rects = spaceRoot.rectangles() == null ? List.of() : spaceRoot.rectangles().stream()
                .map(TemplateGeneratorRoot.Rectangle::toModel)
                .toList();

        return new BoardSpace(id, label, priority, rects);
    }

    private ScenarioConfig mapScenario(TemplateGeneratorRoot.Scenario scenarioRoot) {
        if (scenarioRoot == null || scenarioRoot.steps() == null) {
            return new ScenarioConfig();
        }
        List<ScenarioConfig.Step> steps = scenarioRoot.steps().stream()
                .map(this::mapStep)
                .collect(Collectors.toList());
        return new ScenarioConfig(steps);
    }

    private ScenarioConfig.Step mapStep(TemplateGeneratorRoot.Scenario.Step stepRoot) {
        ScenarioConfig.Step.Id id = stepRoot.ref() != null
                ? globalCache.reference(stepRoot.ref(), ScenarioConfig.Step.Id.class, new ScenarioConfig.Step.Id())
                : new ScenarioConfig.Step.Id();

        I18n label = toI18n(stepRoot.label());

        List<ScenarioConfig.Target> targets = stepRoot.targets() == null ? List.of() : stepRoot.targets().stream()
                .map(this::mapTarget)
                .toList();

        List<Possibility> possibilities = stepRoot.possibilities() == null ? List.of() : stepRoot.possibilities().stream()
                .map(p -> mapPossibility(p, id))
                .toList();

        return new ScenarioConfig.Step(id, label, Optional.empty(), 0, targets, possibilities);
    }

    private ScenarioConfig.Target mapTarget(TemplateGeneratorRoot.Scenario.Step.Target targetRoot) {
        ScenarioConfig.Target.Id id = targetRoot.ref() != null
                ? globalCache.reference(targetRoot.ref(), ScenarioConfig.Target.Id.class, new ScenarioConfig.Target.Id())
                : new ScenarioConfig.Target.Id();

        I18n label = toI18n(targetRoot.label());
        Optional<I18n> optDescription = Optional.of(toI18n(targetRoot.description()))
                .filter(i18n -> !i18n.values().isEmpty());

        return new ScenarioConfig.Target(id, label, optDescription, targetRoot.optional(), List.of(), Optional.empty());
    }

    private Possibility mapPossibility(TemplateGeneratorRoot.Scenario.Step.Possibility possibilityRoot, ScenarioConfig.Step.Id currentStepId) {
        PossibilityRecurrence recurrence = mapRecurrence(possibilityRoot.recurrence());
        PossibilityTrigger trigger = mapTrigger(possibilityRoot.trigger(), currentStepId);
        Condition condition = mapCondition(possibilityRoot.condition());
        List<Consequence> consequences = possibilityRoot.consequences() == null ? List.of() : possibilityRoot.consequences().stream()
                .map(this::mapConsequence)
                .toList();

        return new Possibility(new Possibility.Id(), recurrence, trigger, Optional.ofNullable(condition), consequences);
    }

    private PossibilityRecurrence mapRecurrence(TemplateGeneratorRoot.Scenario.Step.Possibility.Recurrence recurrenceRoot) {
        if (recurrenceRoot == null || recurrenceRoot.type() == null) {
            return new PossibilityRecurrence.Always(new PossibilityRecurrence.Id());
        }
        return switch (recurrenceRoot.type().toUpperCase()) {
            case "TIMES" -> new PossibilityRecurrence.Times(new PossibilityRecurrence.Id(), recurrenceRoot.value());
            default -> new PossibilityRecurrence.Always(new PossibilityRecurrence.Id());
        };
    }

    private PossibilityTrigger mapTrigger(TemplateGeneratorRoot.Scenario.Step.Possibility.Trigger triggerRoot, ScenarioConfig.Step.Id currentStepId) {
        if (triggerRoot == null) return null;
        return switch (triggerRoot.type().toUpperCase()) {
            case "GOINSPACE" -> {
                BoardSpace.Id spaceId = globalCache.reference(triggerRoot.value(), BoardSpace.Id.class, new BoardSpace.Id());
                yield new PossibilityTrigger.SpaceGoIn(new PossibilityTrigger.Id(), spaceId);
            }
            case "GOOUTSPACE" -> {
                BoardSpace.Id spaceId = globalCache.reference(triggerRoot.value(), BoardSpace.Id.class, new BoardSpace.Id());
                yield new PossibilityTrigger.SpaceGoOut(new PossibilityTrigger.Id(), spaceId);
            }
            case "ABSOLUTETIME" ->
                    new PossibilityTrigger.AbsoluteTime(new PossibilityTrigger.Id(), GameSessionTimeUnit.ofMinutes(Integer.parseInt(triggerRoot.value())));
            case "STEPACTIVE" -> new PossibilityTrigger.StepActive(new PossibilityTrigger.Id(), currentStepId);
            case "TALKINPUTTEXT" -> {
                TalkItem.Id talkId = globalCache.reference(triggerRoot.talkId(), TalkItem.Id.class, new TalkItem.Id());
                String matchTypeStr = triggerRoot.matchType() != null ? triggerRoot.matchType().toUpperCase() : "EQUALS";
                PossibilityTrigger.TalkInputText.MatchType matchType = PossibilityTrigger.TalkInputText.MatchType.valueOf(matchTypeStr);
                yield new PossibilityTrigger.TalkInputText(new PossibilityTrigger.Id(), talkId, triggerRoot.value(), matchType);
            }
            case "TALKOPTIONSELECT" -> {
                String optionRef = triggerRoot.value();
                TalkItemNext.Options.Option.Id optionId = globalCache.reference(optionRef, TalkItemNext.Options.Option.Id.class, new TalkItemNext.Options.Option.Id());
                TalkItem.Id talkId = talkConfig.findByIdByOptionId(optionId).orElseThrow(() ->
                        new IllegalArgumentException("Option not found in any TalkItem: " + optionRef));
                yield new PossibilityTrigger.TalkOptionSelect(new PossibilityTrigger.Id(), talkId, optionId);
            }
            case "TALKEND" -> {
                String talkRef = triggerRoot.value();
                TalkItem.Id talkId = globalCache.reference(talkRef, TalkItem.Id.class, new TalkItem.Id());
                yield new PossibilityTrigger.TalkEnd(new PossibilityTrigger.Id(), talkId);
            }
            case "CONFIRMANSWER" -> {
                String confirmRef = (String) triggerRoot.metadata().get("confirmRef");
                String answerStr = (String) triggerRoot.metadata().get("answer");
                boolean expectedAnswer = "YES".equalsIgnoreCase(answerStr) || "TRUE".equalsIgnoreCase(answerStr);
                Consequence.Id confirmId = globalCache.reference(confirmRef, Consequence.Id.class, new Consequence.Id());
                yield new PossibilityTrigger.ConfirmAnswer(new PossibilityTrigger.Id(), confirmId, expectedAnswer);
            }
            case "AND" -> {
                List<PossibilityTrigger> childTriggers = triggerRoot.children().stream()
                        .map(child -> mapTrigger(child, currentStepId))
                        .toList();
                yield new PossibilityTrigger.And(new PossibilityTrigger.Id(), childTriggers);
            }
            case "OR" -> {
                List<PossibilityTrigger> childTriggers = triggerRoot.children().stream()
                        .map(child -> mapTrigger(child, currentStepId))
                        .toList();
                yield new PossibilityTrigger.Or(new PossibilityTrigger.Id(), childTriggers);
            }
            case "NOT" -> {
                PossibilityTrigger childTrigger = mapTrigger(triggerRoot.children().getFirst(), currentStepId);
                yield new PossibilityTrigger.Not(new PossibilityTrigger.Id(), childTrigger);
            }
            default -> null;
        };
    }

    private Condition mapCondition(TemplateGeneratorRoot.Condition conditionRoot) {
        if (conditionRoot == null) return null;
        return switch (conditionRoot.type().toUpperCase()) {
            case "AND" ->
                    new Condition.And(new Condition.Id(), conditionRoot.children().stream().map(this::mapCondition).toList());
            case "OR" ->
                    new Condition.Or(new Condition.Id(), conditionRoot.children().stream().map(this::mapCondition).toList());
            case "INSIDESPACE" -> {
                String spaceRef = (String) conditionRoot.metadata().get("spaceId");
                BoardSpace.Id spaceId = globalCache.reference(spaceRef, BoardSpace.Id.class, new BoardSpace.Id());
                yield new Condition.InsideSpace(new Condition.Id(), spaceId);
            }
            case "OUTSIDESPACE" -> {
                String spaceRef = (String) conditionRoot.metadata().get("spaceId");
                BoardSpace.Id spaceId = globalCache.reference(spaceRef, BoardSpace.Id.class, new BoardSpace.Id());
                yield new Condition.OutsideSpace(new Condition.Id(), spaceId);
            }
            case "ABSOLUTETIME" -> {
                int duration = ((Number) conditionRoot.metadata().get("duration")).intValue();
                BeforeOrAfter beforeOrAfter = BeforeOrAfter.BEFORE;
                if (conditionRoot.metadata().containsKey("beforeOrAfter")) {
                    beforeOrAfter = BeforeOrAfter.valueOf(((String) conditionRoot.metadata().get("beforeOrAfter")).toUpperCase());
                }
                yield new Condition.AbsoluteTime(new Condition.Id(), GameSessionTimeUnit.ofMinutes(duration), beforeOrAfter);
            }
            case "INSTEP" -> {
                String stepRef = (String) conditionRoot.metadata().get("stepId");
                ScenarioConfig.Step.Id stepId = globalCache.reference(stepRef, ScenarioConfig.Step.Id.class, new ScenarioConfig.Step.Id());
                yield new Condition.Step(new Condition.Id(), stepId);
            }
            case "STEPTARGET" -> {
                String targetRef = (String) conditionRoot.metadata().get("targetId");
                ScenarioConfig.Target.Id targetId = globalCache.reference(targetRef, ScenarioConfig.Target.Id.class, new ScenarioConfig.Target.Id());
                yield new Condition.Target(new Condition.Id(), targetId);
            }
            default -> null;
        };
    }

    private Consequence mapConsequence(TemplateGeneratorRoot.Consequence consequenceRoot) {
        if (consequenceRoot == null) return null;
        return switch (consequenceRoot.type().toUpperCase()) {
            case "ALERT" -> {
                I18n message = toI18n((Map<String, String>) consequenceRoot.metadata().get("value"));
                yield new Consequence.DisplayAlert(new Consequence.Id(), message);
            }
            case "GOALTARGET" -> {
                String targetRef = (String) consequenceRoot.metadata().get("targetId");
                ScenarioConfig.Target.Id targetId = globalCache.reference(targetRef, ScenarioConfig.Target.Id.class, new ScenarioConfig.Target.Id());
                ScenarioSessionState state = parseState((String) consequenceRoot.metadata().get("state"));
                yield new Consequence.ScenarioTarget(new Consequence.Id(), targetId, state);
            }
            case "GOAL" -> {
                String stepRef = (String) consequenceRoot.metadata().get("stepId");
                ScenarioConfig.Step.Id stepId = globalCache.reference(stepRef, ScenarioConfig.Step.Id.class, new ScenarioConfig.Step.Id());
                ScenarioSessionState state = parseState((String) consequenceRoot.metadata().get("state"));
                yield new Consequence.ScenarioStep(new Consequence.Id(), stepId, state);
            }
            case "TALK" -> {
                String talkRef = (String) consequenceRoot.metadata().get("talkRef");
                TalkItem.Id talkId = globalCache.reference(talkRef, TalkItem.Id.class, new TalkItem.Id());
                yield new Consequence.DisplayTalk(new Consequence.Id(), talkId);
            }
            case "GAMEOVER" -> {
                String result = (String) consequenceRoot.metadata().get("result");
                SessionGameOver.Type type = SessionGameOver.Type.valueOf(result);
                yield new Consequence.SessionEnd(new Consequence.Id(), new SessionGameOver(type));
            }
            case "CONFIRM" -> {
                String confirmRef = (String) consequenceRoot.metadata().get("ref");
                I18n message = toI18n((Map<String, String>) consequenceRoot.metadata().get("message"));
                Consequence.Id confirmId = globalCache.reference(confirmRef, Consequence.Id.class, new Consequence.Id());
                yield new Consequence.DisplayConfirm(confirmId, message);
            }
            default -> null;
        };
    }

    private ScenarioSessionState parseState(String state) {
        if (state == null) return ScenarioSessionState.ACTIVE;
        return switch (state.toLowerCase()) {
            case "success" -> ScenarioSessionState.SUCCESS;
            case "failure" -> ScenarioSessionState.FAILURE;
            default -> ScenarioSessionState.ACTIVE;
        };
    }

    private I18n toI18n(Map<String, String> values) {
        if (values == null || values.isEmpty()) {
            return new I18n();
        }
        Map<Language, String> translated = new HashMap<>();
        values.forEach((k, v) -> translated.put(Language.valueOf(k.toUpperCase()), v));
        return new I18n(translated);
    }

    private MapConfig mapMaps(List<TemplateGeneratorRoot.TemplateGeneratorMap> mapsRoot) {
        if (mapsRoot == null || mapsRoot.isEmpty()) {
            return new MapConfig();
        }
        List<MapItem> items = mapsRoot.stream()
                .map(this::mapMap)
                .toList();
        return new MapConfig(items);
    }

    private MapItem mapMap(TemplateGeneratorRoot.TemplateGeneratorMap mapRoot) {
        Image image = new Image(Image.Type.valueOf(mapRoot.image().type().toUpperCase()), mapRoot.image().value());
        Priority priority = mapRoot.priority() != null ? Priority.valueOf(mapRoot.priority().toUpperCase()) : Priority.byDefault();
        Optional<Condition> optCondition = Optional.empty();
        Optional<Image> optPointer = Optional.empty();

        List<ImageObject> imageObjects = mapRoot.objects() == null ? List.of() : mapRoot.objects().stream()
                .map(this::mapMapObject)
                .toList();

        ImageGeneric imageGeneric = new ImageGeneric(mapRoot.label() != null ? mapRoot.label() : "", image, imageObjects);

        return new MapItem(imageGeneric, priority, optCondition, optPointer, List.of());
    }

    private ImageObject mapMapObject(TemplateGeneratorRoot.TemplateGeneratorMap.MapObject objectRoot) {
        ImagePoint center = new ImagePoint((float) objectRoot.position().top(), (float) objectRoot.position().left());
        Condition condition = mapCondition(objectRoot.condition());
        Optional<Condition> optCondition = Optional.ofNullable(condition);

        ImageObject.Atom atom = new ImageObject.Atom("", center, optCondition);
        
        // Check if it's a point or an image object
        if (objectRoot.point() != null) {
            String color = objectRoot.point().color() != null ? objectRoot.point().color() : "";
            return new ImageObject.Point(atom, color);
        } else if (objectRoot.image() != null) {
            Image image = new Image(Image.Type.valueOf(objectRoot.image().type().toUpperCase()), objectRoot.image().value());
            return new ImageObject._Image(atom, image);
        }
        
        // Default to Point
        return new ImageObject.Point(atom, "");
    }

    private TalkConfig mapTalk(TemplateGeneratorRoot.Talk talkRoot) {
        if (talkRoot == null) {
            return new TalkConfig();
        }
        
        // Build a map of character images: characterName -> imageRef -> Image
        Map<String, Map<String, TemplateGeneratorRoot.Talk.Character.CharacterImage>> characterImages = getStringMapMap(talkRoot);

        Map<String, TalkCharacter> characters = new HashMap<>();
        if (talkRoot.characters() != null) {
            for (TemplateGeneratorRoot.Talk.Character charRoot : talkRoot.characters()) {
                if (charRoot.ref() != null) {
                    TalkCharacter character = new TalkCharacter(charRoot.ref());
                    characters.put(charRoot.ref(), character);
                }
            }
        }
        
        List<TalkItem> items = new ArrayList<>();
        if (talkRoot.items() != null) {
            for (TemplateGeneratorRoot.Talk.Item itemRoot : talkRoot.items()) {
                TalkItem item = mapTalkItem(itemRoot, characters, characterImages);
                items.add(item);
            }
        }
        
        return new TalkConfig(items);
    }

    private static Map<String, Map<String, TemplateGeneratorRoot.Talk.Character.CharacterImage>> getStringMapMap(TemplateGeneratorRoot.Talk talkRoot) {
        Map<String, Map<String, TemplateGeneratorRoot.Talk.Character.CharacterImage>> characterImages = new HashMap<>();
        if (talkRoot.characters() != null) {
            for (TemplateGeneratorRoot.Talk.Character charRoot : talkRoot.characters()) {
                if (charRoot.ref() != null) {
                    Map<String, TemplateGeneratorRoot.Talk.Character.CharacterImage> images = new HashMap<>();
                    if (charRoot.images() != null) {
                        for (TemplateGeneratorRoot.Talk.Character.CharacterImage charImage : charRoot.images()) {
                            images.put(charImage.ref(), charImage);
                        }
                    }
                    characterImages.put(charRoot.ref(), images);
                }
            }
        }
        return characterImages;
    }

    private TalkItem mapTalkItem(TemplateGeneratorRoot.Talk.Item itemRoot, Map<String, TalkCharacter> characters, Map<String, Map<String, TemplateGeneratorRoot.Talk.Character.CharacterImage>> characterImages) {
        TalkItem.Id id = itemRoot.ref() != null
                ? globalCache.reference(itemRoot.ref(), TalkItem.Id.class, new TalkItem.Id())
                : new TalkItem.Id();

        TalkItemOut talkItemOut = mapTalkOut(itemRoot.value());
        
        String characterName = itemRoot.character().character();
        String imageRef = itemRoot.character().image();
        
        TalkCharacter character = characters.getOrDefault(characterName, new TalkCharacter(characterName));
        
        // Find the actual image for this character
        String imagePath = imageRef;
        String imageType = "ASSET";
        if (characterImages.containsKey(characterName) && characterImages.get(characterName).containsKey(imageRef)) {
            TemplateGeneratorRoot.Talk.Character.CharacterImage charImage = characterImages.get(characterName).get(imageRef);
            imagePath = charImage.value().value();
            imageType = charImage.value().type();
        }
        
        Image image = new Image(Image.Type.valueOf(imageType.toUpperCase()), imagePath);
        TalkCharacter.Reference characterRef = new TalkCharacter.Reference(character, imageRef, image);
        
        // Priority: inputText > options > next > simple
        if (itemRoot.inputText() != null) {
            TalkItemNext.InputText.Type type = TalkItemNext.InputText.Type.valueOf(itemRoot.inputText().type().toUpperCase());
            Optional<Integer> optSize = Optional.ofNullable(itemRoot.inputText().size());
            TalkItemNext.InputText inputText = new TalkItemNext.InputText(type, optSize);
            return new TalkItem(id, talkItemOut, characterRef, inputText);
        } else if (itemRoot.options() != null && !itemRoot.options().isEmpty()) {
            List<TalkItemNext.Options.Option> options = new ArrayList<>();
            for (int i = 0; i < itemRoot.options().size(); i++) {
                TemplateGeneratorRoot.Talk.Item.Option optionRoot = itemRoot.options().get(i);
                TalkItemNext.Options.Option.Id optionId = optionRoot.ref() != null
                        ? globalCache.reference(optionRoot.ref(), TalkItemNext.Options.Option.Id.class, new TalkItemNext.Options.Option.Id())
                        : new TalkItemNext.Options.Option.Id();

                I18n optionValue = toI18n(optionRoot.value());
                Optional<TalkItem.Id> optNextId = Optional.empty();
                if (optionRoot.next() != null) {
                    optNextId = Optional.of(globalCache.reference(optionRoot.next(), TalkItem.Id.class, new TalkItem.Id()));
                }

                Condition condition = mapCondition(optionRoot.condition());
                Optional<Condition> optCondition = Optional.ofNullable(condition);
                TalkItemNext.Options.Option option = new TalkItemNext.Options.Option(optionId, i, optionValue, optNextId, optCondition);
                options.add(option);
            }
            return TalkItem.options(id, talkItemOut, characterRef, options);
        } else {
            Optional<TalkItem.Id> optNextId = Optional.empty();
            if (itemRoot.next() != null) {
                optNextId = Optional.of(globalCache.reference(itemRoot.next(), TalkItem.Id.class, new TalkItem.Id()));
            }
            return optNextId.map(value -> TalkItem.continueItem(id, talkItemOut, characterRef, value))
                    .orElseGet(() -> TalkItem.simple(id, talkItemOut, characterRef));
        }
    }

    @SuppressWarnings("unchecked")
    private TalkItemOut mapTalkOut(Object valueRoot) {
        if (valueRoot instanceof Map<?, ?> map) {
            // Check if it's a conditional value (has "default" key)
            if (map.containsKey("default")) {
                Map<String, String> defaultMap = (Map<String, String>) map.get("default");
                I18n defaultText = toI18n(defaultMap);

                List<TalkItemOut.Conditional.Branch> branches = new ArrayList<>();
                if (map.containsKey("branches")) {
                    List<Map<String, Object>> branchList = (List<Map<String, Object>>) map.get("branches");
                    for (Map<String, Object> branchMap : branchList) {
                        int order = branchMap.containsKey("order") ? ((Number) branchMap.get("order")).intValue() : 0;
                        Map<String, Object> conditionMap = (Map<String, Object>) branchMap.get("condition");
                        Condition condition = mapConditionFromMap(conditionMap);
                        Map<String, String> branchValueMap = (Map<String, String>) branchMap.get("value");
                        I18n branchText = toI18n(branchValueMap);
                        branches.add(new TalkItemOut.Conditional.Branch(order, condition, branchText));
                    }
                }

                return TalkItemOut.conditional(defaultText, branches);
            } else {
                // Simple fixed value: { "FR": "...", "EN": "..." }
                Map<String, String> simpleMap = (Map<String, String>) map;
                return TalkItemOut.fixed(toI18n(simpleMap));
            }
        }
        return TalkItemOut.fixed(new I18n());
    }

    private Condition mapConditionFromMap(Map<String, Object> conditionMap) {
        if (conditionMap == null) return null;
        String type = (String) conditionMap.get("type");
        Map<String, Object> metadata = (Map<String, Object>) conditionMap.get("metadata");
        List<Map<String, Object>> children = (List<Map<String, Object>>) conditionMap.get("children");

        TemplateGeneratorRoot.Condition conditionRoot = new TemplateGeneratorRoot.Condition(
                type,
                metadata,
                children != null ? children.stream()
                        .map(c -> new TemplateGeneratorRoot.Condition(
                                (String) c.get("type"),
                                (Map<String, Object>) c.get("metadata"),
                                null
                        ))
                        .toList() : null
        );
        return mapCondition(conditionRoot);
    }

    private ImageConfig mapImage(TemplateGeneratorRoot.TemplateGeneratorImage imageRoot) {
        if (imageRoot == null || imageRoot.items() == null) {
            return new ImageConfig();
        }
        
        List<ImageItem> items = imageRoot.items().stream()
                .map(this::mapImageItem)
                .toList();
        return new ImageConfig(items);
    }

    private ImageItem mapImageItem(TemplateGeneratorRoot.TemplateGeneratorImage.ImageItem itemRoot) {
        Image image = new Image(Image.Type.valueOf(itemRoot.generic().value().type().toUpperCase()), 
                                itemRoot.generic().value().value());
        List<ImageObject> objects = itemRoot.generic().objects() == null ? List.of() : itemRoot.generic().objects().stream()
                .map(o -> mapImageObject(o))
                .toList();
        
        ImageGeneric generic = new ImageGeneric("", image, objects);
        return new ImageItem(generic);
    }

    private ImageObject mapImageObject(TemplateGeneratorRoot.TemplateGeneratorImage.ImageItem.TemplateGeneratorImageGeneric.ImageObject objectRoot) {
        // For now, we'll only support Point objects
        if ("point".equalsIgnoreCase(objectRoot.type())) {
            ImagePoint center = new ImagePoint(0, 0);
            ImageObject.Atom atom = new ImageObject.Atom("", center, Optional.empty());
            String color = objectRoot.metadata() != null && objectRoot.metadata().containsKey("color") 
                    ? (String) objectRoot.metadata().get("color") 
                    : "";
            return new ImageObject.Point(atom, color);
        }
        return null;
    }

}

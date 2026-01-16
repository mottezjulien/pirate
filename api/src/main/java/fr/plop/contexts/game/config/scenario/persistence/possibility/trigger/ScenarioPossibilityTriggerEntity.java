package fr.plop.contexts.game.config.scenario.persistence.possibility.trigger;


import fr.plop.contexts.game.config.Image.domain.ImageObject;
import fr.plop.contexts.game.config.board.domain.model.BoardSpace;
import fr.plop.contexts.game.config.condition.persistence.ConditionEntity;
import fr.plop.contexts.game.config.consequence.Consequence;
import fr.plop.contexts.game.config.scenario.domain.model.Possibility;
import fr.plop.contexts.game.config.scenario.domain.model.PossibilityTrigger;
import fr.plop.contexts.game.config.scenario.domain.model.ScenarioConfig;
import fr.plop.contexts.game.config.talk.domain.TalkItem;
import fr.plop.contexts.game.config.talk.domain.TalkItemNext;
import fr.plop.contexts.game.session.time.GameSessionTimeUnit;

import jakarta.persistence.*;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import java.util.*;

@Entity
@Table(name = "TEST2_SCENARIO_POSSIBILITY_TRIGGER")
public class ScenarioPossibilityTriggerEntity {

    private static final String KEY_PRIMARY = "PRIMARY";
    private static final String KEY_MINUTES = "MINUTES";
    private static final String KEY_OTHER_POSSIBILITY = "OTHER_POSSIBILITY";
    private static final String KEY_SPACE_TYPE = "SPACE_TYPE";
    private static final String VALUE_SPACE_TYPE_IN = "IN";
    private static final String VALUE_SPACE_TYPE_OUT = "OUT";
    private static final String KEY_TALK_TYPE = "TALK_TYPE";
    private static final String KEY_TALK_OPTION = "TALK_OPTION";
    private static final String KEY_TALK_INPUT_VALUE = "TALK_INPUT_VALUE";
    private static final String KEY_TALK_INPUT_MATCH_TYPE = "TALK_INPUT_MATCH_TYPE";
    private static final String VALUE_TALK_TYPE_OPTION_SELECT = "OPTION_SELECT";
    private static final String VALUE_TALK_TYPE_END = "END";
    private static final String VALUE_TALK_TYPE_INPUT_TEXT = "INPUT_TEXT";
    private static final String KEY_CONFIRM_ID = "CONFIRM_ID";
    private static final String KEY_CONFIRM_EXPECTED_ANSWER = "EXPECTED_ANSWER";
    private static final String KEY_LOGICAL_TYPE = "LOGICAL_TYPE";
    private static final String VALUE_LOGICAL_AND = "AND";
    private static final String VALUE_LOGICAL_OR = "OR";
    private static final String VALUE_LOGICAL_NOT = "NOT";

    public enum Type {
        TIME, SPACE, STEP, TALK, OBJECT, CONFIRM, LOGICAL
    }

    @Id
    protected String id;

    @Enumerated(EnumType.STRING)
    private Type type;

    @ElementCollection
    @CollectionTable(name="TEST2_SCENARIO_POSSIBILITY_TRIGGER_VALUES", joinColumns=@JoinColumn(name="trigger_id"))
    @MapKeyColumn(name="map_key")
    @Column(name="map_value")
    private final Map<String, String> keyValues = new HashMap<>();

    /*@ManyToOne
    @JoinColumn(name = "parent_id")
    private ScenarioPossibilityTriggerEntity parent;

    @OneToMany(mappedBy = "parent", orphanRemoval = true)
    private List<ScenarioPossibilityTriggerEntity> children = new ArrayList<>();
     */
    @ManyToMany
    @JoinTable(name = "TEST2_RELATION_SCENARIO_POSSIBILITY_TRIGGER_SUB",
            joinColumns = @JoinColumn(name = "trigger_id"),
            inverseJoinColumns = @JoinColumn(name = "sub_trigger_id"))
    private Set<ScenarioPossibilityTriggerEntity> subs = new HashSet<>();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public Map<String, String> getKeyValues() {
        return keyValues;
    }

    public Set<ScenarioPossibilityTriggerEntity> getSubs() {
        return subs;
    }

    public PossibilityTrigger toModel() {
        final PossibilityTrigger.Id id = new PossibilityTrigger.Id(this.id);
        return switch (type) {
            case TIME -> {
                GameSessionTimeUnit timeUnit = GameSessionTimeUnit.ofMinutes(Integer.parseInt(keyValues.get(KEY_MINUTES)));
                if(keyValues.containsKey(KEY_OTHER_POSSIBILITY)) {
                    yield new PossibilityTrigger.RelativeTimeAfterOtherPossibility(id,
                            new Possibility.Id(keyValues.get(KEY_OTHER_POSSIBILITY)), timeUnit);
                }
                yield  new PossibilityTrigger.AbsoluteTime(id, timeUnit);
            }
            case SPACE -> {
                BoardSpace.Id spaceId = new BoardSpace.Id(keyValues.get(KEY_PRIMARY));
                yield switch (keyValues.get(KEY_SPACE_TYPE)) {
                    case VALUE_SPACE_TYPE_IN -> new PossibilityTrigger.SpaceGoIn(id, spaceId);
                    case VALUE_SPACE_TYPE_OUT -> new PossibilityTrigger.SpaceGoOut(id, spaceId);
                    default -> throw new IllegalStateException("Unexpected value: " + keyValues.get(KEY_SPACE_TYPE));
                };
            }
            case STEP ->{
                ScenarioConfig.Step.Id stepId = new ScenarioConfig.Step.Id(keyValues.get(KEY_PRIMARY));
                yield new PossibilityTrigger.StepActive(id, stepId);
            }
            case TALK -> {
                TalkItem.Id talkItemId = new TalkItem.Id(keyValues.get(KEY_PRIMARY));
                yield switch (keyValues.get(KEY_TALK_TYPE)) {
                    case VALUE_TALK_TYPE_OPTION_SELECT -> {
                        TalkItemNext.Options.Option.Id optionId = new TalkItemNext.Options.Option.Id(keyValues.get(KEY_TALK_OPTION));
                        yield new PossibilityTrigger.TalkOptionSelect(id, talkItemId, optionId);
                    }
                    case VALUE_TALK_TYPE_END -> new PossibilityTrigger.TalkEnd(id, talkItemId);
                    case VALUE_TALK_TYPE_INPUT_TEXT -> {
                        String inputValue = keyValues.get(KEY_TALK_INPUT_VALUE);
                        PossibilityTrigger.TalkInputText.MatchType matchType = PossibilityTrigger.TalkInputText.MatchType.valueOf(keyValues.get(KEY_TALK_INPUT_MATCH_TYPE));
                        yield new PossibilityTrigger.TalkInputText(id, talkItemId, inputValue, matchType);
                    }
                    default -> throw new IllegalStateException("Unexpected value: " + keyValues.get(KEY_TALK_TYPE));
                };
            }
            case OBJECT -> {
                ImageObject.Id objectId = new ImageObject.Id(keyValues.get(KEY_PRIMARY));
                yield new PossibilityTrigger.ImageObjectClick(id, objectId);
            }
            case CONFIRM -> {
                Consequence.Id confirmId = new Consequence.Id(keyValues.get(KEY_CONFIRM_ID));
                boolean expectedAnswer = Boolean.parseBoolean(keyValues.get(KEY_CONFIRM_EXPECTED_ANSWER));
                yield new PossibilityTrigger.ConfirmAnswer(id, confirmId, expectedAnswer);
            }
            case LOGICAL -> {
                List<PossibilityTrigger> childTriggers = subs.stream()
                        .map(ScenarioPossibilityTriggerEntity::toModel)
                        .toList();
                yield switch (keyValues.get(KEY_LOGICAL_TYPE)) {
                    case VALUE_LOGICAL_AND -> new PossibilityTrigger.And(id, childTriggers);
                    case VALUE_LOGICAL_OR -> new PossibilityTrigger.Or(id, childTriggers);
                    case VALUE_LOGICAL_NOT -> new PossibilityTrigger.Not(id, childTriggers.getFirst());
                    default -> throw new IllegalStateException("Unexpected logical type: " + keyValues.get(KEY_LOGICAL_TYPE));
                };
            }
        };
    }

    public static ScenarioPossibilityTriggerEntity fromModel(PossibilityTrigger model) {
        ScenarioPossibilityTriggerEntity entity = new ScenarioPossibilityTriggerEntity();
        entity.setId(model.id().value());
        switch (model) {
            case PossibilityTrigger.AbsoluteTime absoluteTime -> {
                entity.setType(Type.TIME);
                entity.getKeyValues().put(KEY_MINUTES, Integer.toString(absoluteTime.value().toMinutes()));
            }
            case PossibilityTrigger.RelativeTimeAfterOtherPossibility relativeTimeAfterOtherPossibility -> {
                entity.setType(Type.TIME);
                entity.getKeyValues().put(KEY_MINUTES, Integer.toString(relativeTimeAfterOtherPossibility.value().toMinutes()));
                entity.getKeyValues().put(KEY_OTHER_POSSIBILITY, relativeTimeAfterOtherPossibility.otherPossibilityId().value());
            }
            case PossibilityTrigger.SpaceGoIn spaceGoIn -> {
                entity.setType(Type.SPACE);
                entity.getKeyValues().put(KEY_SPACE_TYPE, VALUE_SPACE_TYPE_IN);
                entity.getKeyValues().put(KEY_PRIMARY, spaceGoIn.spaceId().value());
            }
            case PossibilityTrigger.SpaceGoOut spaceGoOut -> {
                entity.setType(Type.SPACE);
                entity.getKeyValues().put(KEY_SPACE_TYPE, VALUE_SPACE_TYPE_OUT);
                entity.getKeyValues().put(KEY_PRIMARY, spaceGoOut.spaceId().value());
            }
            case PossibilityTrigger.StepActive stepActive -> {
                entity.setType(Type.STEP);
                entity.getKeyValues().put(KEY_PRIMARY, stepActive.stepId().value());
            }
            case PossibilityTrigger.TalkOptionSelect selectTalkOption -> {
                entity.setType(Type.TALK);
                entity.getKeyValues().put(KEY_TALK_TYPE, VALUE_TALK_TYPE_OPTION_SELECT);
                entity.getKeyValues().put(KEY_PRIMARY, selectTalkOption.talkId().value());
                entity.getKeyValues().put(KEY_TALK_OPTION, selectTalkOption.optionId().value());
            }
            case PossibilityTrigger.TalkEnd talkEnd -> {
                entity.setType(Type.TALK);
                entity.getKeyValues().put(KEY_TALK_TYPE, VALUE_TALK_TYPE_END);
                entity.getKeyValues().put(KEY_PRIMARY, talkEnd.talkId().value());
            }
            case PossibilityTrigger.TalkInputText talkInputText -> {
                entity.setType(Type.TALK);
                entity.getKeyValues().put(KEY_TALK_TYPE, VALUE_TALK_TYPE_INPUT_TEXT);
                entity.getKeyValues().put(KEY_PRIMARY, talkInputText.talkId().value());
                entity.getKeyValues().put(KEY_TALK_INPUT_VALUE, talkInputText.value());
                entity.getKeyValues().put(KEY_TALK_INPUT_MATCH_TYPE, talkInputText.matchType().name());
            }
            case PossibilityTrigger.ImageObjectClick imageObjectClick -> {
                entity.setType(Type.OBJECT);
                entity.getKeyValues().put(KEY_PRIMARY, imageObjectClick.objectId().value());
            }
            case PossibilityTrigger.ConfirmAnswer confirmAnswer -> {
                entity.setType(Type.CONFIRM);
                entity.getKeyValues().put(KEY_CONFIRM_ID, confirmAnswer.confirmId().value());
                entity.getKeyValues().put(KEY_CONFIRM_EXPECTED_ANSWER, Boolean.toString(confirmAnswer.expectedAnswer()));
            }
            case PossibilityTrigger.And and -> {
                entity.setType(Type.LOGICAL);
                entity.getKeyValues().put(KEY_LOGICAL_TYPE, VALUE_LOGICAL_AND);
                and.triggers().forEach(child -> entity.subs.add(fromModel(child)));
            }
            case PossibilityTrigger.Or or -> {
                entity.setType(Type.LOGICAL);
                entity.getKeyValues().put(KEY_LOGICAL_TYPE, VALUE_LOGICAL_OR);
                or.triggers().forEach(child -> entity.subs.add(fromModel(child)));
            }
            case PossibilityTrigger.Not not -> {
                entity.setType(Type.LOGICAL);
                entity.getKeyValues().put(KEY_LOGICAL_TYPE, VALUE_LOGICAL_NOT);
                entity.subs.add(fromModel(not.trigger()));
            }
        }
        return entity;
    }

}

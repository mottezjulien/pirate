package fr.plop.contexts.game.config.scenario.persistence.possibility.trigger;


import fr.plop.contexts.game.config.board.domain.model.BoardSpace;
import fr.plop.contexts.game.config.scenario.domain.model.Possibility;
import fr.plop.contexts.game.config.scenario.domain.model.PossibilityTrigger;
import fr.plop.contexts.game.config.talk.domain.TalkItem;
import fr.plop.contexts.game.session.time.GameSessionTimeUnit;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapKeyColumn;
import jakarta.persistence.Table;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

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
    private static final String VALUE_TALK_TYPE_OPTION_SELECT = "OPTION_SELECT";
    private static final String VALUE_TALK_TYPE_END = "END";

    public enum Type {
        TIME, SPACE, STEP, TALK, MAP;
    }

    @Id
    protected String id;

    @Enumerated(EnumType.STRING)
    private Type type;

    @ElementCollection
    @CollectionTable(name="TEST2_SCENARIO_POSSIBILITY_TRIGGER_VALUES", joinColumns=@JoinColumn(name="trigger_id"))
    @MapKeyColumn(name="map_key")
    @Column(name="map_value")
    private Map<String, String> keyValues = new HashMap<>();

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

    public void setKeyValues(Map<String, String> keyValues) {
        this.keyValues = keyValues;
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
            case STEP -> new PossibilityTrigger.StepActive(id);
            case TALK -> {
                TalkItem.Id talkItemId = new TalkItem.Id(keyValues.get(KEY_PRIMARY));
                yield switch (keyValues.get(KEY_TALK_TYPE)) {
                    case VALUE_TALK_TYPE_OPTION_SELECT -> {
                        TalkItem.Options.Option.Id optionId = new TalkItem.Options.Option.Id(keyValues.get(KEY_TALK_OPTION));
                        yield new PossibilityTrigger.TalkOptionSelect(id, talkItemId, optionId);
                    }
                    case VALUE_TALK_TYPE_END -> new PossibilityTrigger.TalkEnd(id, talkItemId);
                    default -> throw new IllegalStateException("Unexpected value: " + keyValues.get(KEY_TALK_TYPE));
                };
            }
            case MAP -> new PossibilityTrigger.ClickMapObject(id, keyValues.get(KEY_PRIMARY));
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
            case PossibilityTrigger.ClickMapObject clickMapObject -> {
                entity.setType(Type.MAP);
                entity.getKeyValues().put(KEY_PRIMARY, clickMapObject.objectReference());
            }

        }
        return entity;
    }

    /*
    public PossibilityTrigger toModel() {
        return switch (this) {
            case ScenarioPossibilityTriggerAbsoluteTimeEntity absoluteTime -> absoluteTime.toModel();
            case ScenarioPossibilityTriggerRelativeTimeAfterOtherTriggerEntity relativeTime -> relativeTime.toModel();
            case ScenarioPossibilityTriggerGoInSpaceEntity goInSpace -> goInSpace.toModel();
            case ScenarioPossibilityTriggerGoOutSpaceEntity goOutSpace -> goOutSpace.toModel();
            default -> throw new IllegalStateException("Unknown type");
        };
    }

    public static NewScenarioPossibilityTriggerAbstractEntity fromModel(PossibilityTrigger model) {
        return switch (model) {
            case PossibilityTrigger.AbsoluteTime absoluteTime -> {
                ScenarioPossibilityTriggerAbsoluteTimeEntity entity = new ScenarioPossibilityTriggerAbsoluteTimeEntity();
                entity.setId(absoluteTime.id().value());
                entity.setMinutes(absoluteTime.value().toMinutes());
                yield entity;
            }
            case PossibilityTrigger.RelativeTimeAfterOtherPossibility relativeTimeAfterOther -> {
                ScenarioPossibilityTriggerRelativeTimeAfterOtherTriggerEntity entity = new ScenarioPossibilityTriggerRelativeTimeAfterOtherTriggerEntity();
                entity.setId(relativeTimeAfterOther.id().value());
                entity.setMinutes(relativeTimeAfterOther.value().toMinutes());
                entity.setOtherPossibilityId(relativeTimeAfterOther.otherPossibilityId().value());
                yield entity;
            }
            case PossibilityTrigger.GoInSpace goInSpace -> {
                ScenarioPossibilityTriggerGoInSpaceEntity entity = new ScenarioPossibilityTriggerGoInSpaceEntity();
                entity.setId(goInSpace.id().value());
                entity.setSpaceId(goInSpace.spaceId().value());
                yield entity;
            }
            case PossibilityTrigger.GoOutSpace goOutSpace -> {
                ScenarioPossibilityTriggerGoInSpaceEntity entity = new ScenarioPossibilityTriggerGoInSpaceEntity();
                entity.setId(goOutSpace.id().value());
                entity.setSpaceId(goOutSpace.spaceId().value());
                yield entity;
            }

            case PossibilityTrigger.ActiveStep activeStep -> {
                ScenarioPossibilityTriggerActiveStepEntity entity = new ScenarioPossibilityTriggerGoInSpaceEntity();
                entity.setId(goOutSpace.id().value());
                entity.setSpaceId(goOutSpace.spaceId().value());
                yield entity;
            }
            case PossibilityTrigger.SelectTalkOption talkOption -> {
                ScenarioPossibilityTalkOptionsEntity entity = new ScenarioPossibilityTalkOptionsEntity();
                entity.setId(talkOption.id().value());
                entity.setTalkOptionsId(talkOption.optionId().value());
                yield entity;
            }
        };
    }*/

}

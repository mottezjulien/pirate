package fr.plop.contexts.game.config.scenario.persistence.possibility.condition;

import fr.plop.contexts.game.config.board.domain.model.BoardSpace;
import fr.plop.contexts.game.config.scenario.domain.model.PossibilityCondition;
import fr.plop.contexts.game.config.scenario.domain.model.ScenarioConfig;
import fr.plop.generic.enumerate.BeforeOrAfter;
import jakarta.persistence.*;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Entity
@Table(name = "TEST2_SCENARIO_POSSIBILITY_CONDITION")
public class ScenarioPossibilityConditionEntity {

    private static final String KEY_PRIMARY = "PRIMARY"; // stepId, spaceId, etc.
    private static final String KEY_MINUTES = "MINUTES";
    private static final String KEY_OTHER_TRIGGER = "OTHER_TRIGGER";
    private static final String KEY_SPACE_TYPE = "SPACE_TYPE";
    private static final String KEY_BEFORE_AFTER = "BEFORE_AFTER";

    public static final String VALUE_SPACE_TYPE_IN = "IN";
    public static final String VALUE_SPACE_TYPE_OUT = "OUT";

    public enum Type {
        TIME_ABSOLUTE, TIME_RELATIVE_AFTER_TRIGGER, SPACE, STEP
    }

    @Id
    protected String id;

    @Enumerated(EnumType.STRING)
    private Type type;

    @ElementCollection
    @CollectionTable(name = "TEST2_SCENARIO_POSSIBILITY_CONDITION_VALUES", joinColumns = @JoinColumn(name = "condition_id"))
    @MapKeyColumn(name = "map_key")
    @Column(name = "map_value")
    private Map<String, String> keyValues = new HashMap<>();

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public Type getType() { return type; }
    public void setType(Type type) { this.type = type; }

    public Map<String, String> getKeyValues() { return keyValues; }
    public void setKeyValues(Map<String, String> keyValues) { this.keyValues = keyValues; }

    public PossibilityCondition toModel() {
        final PossibilityCondition.Id id = new PossibilityCondition.Id(this.id);
        return switch (type) {
            case TIME_ABSOLUTE -> new PossibilityCondition.AbsoluteTime(
                    id,
                    Duration.ofMinutes(Integer.parseInt(keyValues.get(KEY_MINUTES))),
                    BeforeOrAfter.valueOf(keyValues.get(KEY_BEFORE_AFTER))
            );
            case TIME_RELATIVE_AFTER_TRIGGER -> new PossibilityCondition.RelativeTimeAfterOtherTrigger(
                    id,
                    new PossibilityCondition.Id(keyValues.get(KEY_OTHER_TRIGGER)),
                    Duration.ofMinutes(Integer.parseInt(keyValues.get(KEY_MINUTES)))
            );
            case SPACE -> {
                BoardSpace.Id spaceId = new BoardSpace.Id(keyValues.get(KEY_PRIMARY));
                String st = keyValues.get(KEY_SPACE_TYPE);
                if (VALUE_SPACE_TYPE_IN.equals(st)) {
                    yield new PossibilityCondition.InsideSpace(id, spaceId);
                } else if (VALUE_SPACE_TYPE_OUT.equals(st)) {
                    yield new PossibilityCondition.OutsideSpace(id, spaceId);
                }
                throw new IllegalStateException("Unexpected SPACE_TYPE: " + st);
            }
            case STEP -> new PossibilityCondition.StepIn(id, new ScenarioConfig.Step.Id(keyValues.get(KEY_PRIMARY)));
        };
    }

    public static ScenarioPossibilityConditionEntity fromModel(PossibilityCondition condition) {
        ScenarioPossibilityConditionEntity entity = new ScenarioPossibilityConditionEntity();
        entity.setId(condition.id().value());
        switch (condition) {
            case PossibilityCondition.AbsoluteTime absoluteTime -> {
                entity.setType(Type.TIME_ABSOLUTE);
                entity.getKeyValues().put(KEY_MINUTES, Integer.toString((int) absoluteTime.duration().toMinutes()));
                entity.getKeyValues().put(KEY_BEFORE_AFTER, absoluteTime.beforeOrAfter().name());
            }
            case PossibilityCondition.RelativeTimeAfterOtherTrigger relative -> {
                entity.setType(Type.TIME_RELATIVE_AFTER_TRIGGER);
                entity.getKeyValues().put(KEY_MINUTES, Integer.toString((int) relative.duration().toMinutes()));
                entity.getKeyValues().put(KEY_OTHER_TRIGGER, relative.otherTriggerId().value());
            }
            case PossibilityCondition.StepIn inStep -> {
                entity.setType(Type.STEP);
                entity.getKeyValues().put(KEY_PRIMARY, inStep.stepId().value());
            }
            case PossibilityCondition.InsideSpace insideSpace -> {
                entity.setType(Type.SPACE);
                entity.getKeyValues().put(KEY_SPACE_TYPE, VALUE_SPACE_TYPE_IN);
                entity.getKeyValues().put(KEY_PRIMARY, insideSpace.spaceId().value());
            }
            case PossibilityCondition.OutsideSpace outsideSpace -> {
                entity.setType(Type.SPACE);
                entity.getKeyValues().put(KEY_SPACE_TYPE, VALUE_SPACE_TYPE_OUT);
                entity.getKeyValues().put(KEY_PRIMARY, outsideSpace.spaceId().value());
            }
            case PossibilityCondition.StepTarget stepTarget -> {
                // Not previously persisted; leaving unhandled until mapping defined
                throw new IllegalStateException("StepTarget persistence not defined");
            }
        }
        return entity;
    }
}
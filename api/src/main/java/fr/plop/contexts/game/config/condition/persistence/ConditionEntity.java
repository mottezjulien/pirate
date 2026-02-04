package fr.plop.contexts.game.config.condition.persistence;

import fr.plop.contexts.game.config.board.domain.model.BoardSpace;
import fr.plop.contexts.game.config.condition.Condition;
import fr.plop.contexts.game.config.inventory.domain.model.GameConfigInventoryItem;
import fr.plop.contexts.game.config.scenario.domain.model.ScenarioConfig;
import fr.plop.contexts.game.config.talk.domain.TalkCharacter;
import fr.plop.contexts.game.instance.time.GameInstanceTimeUnit;
import fr.plop.generic.enumerate.BeforeOrAfter;
import jakarta.persistence.*;

import java.util.*;
import java.util.stream.Collectors;

@Entity
@Table(name = "LO_CONDITION")
public class ConditionEntity {

    private static final String KEY_PRIMARY = "PRIMARY";
    private static final String KEY_TIME_UNIT = "MINUTES";
    private static final String KEY_BOARD_SPACE_TYPE = "SPACE_TYPE";
    private static final String KEY_BEFORE_AFTER = "BEFORE_AFTER";
    private static final String VALUE_BOARD_SPACE_TYPE_IN = "IN";
    private static final String VALUE_BOARD_SPACE_TYPE_OUT = "OUT";

    private static final String KEY_SCENARIO_TYPE = "SCENARIO_TYPE";
    private static final String VALUE_SCENARIO_TYPE_STEP = "STEP";
    private static final String VALUE_SCENARIO_TYPE_TARGET = "TARGET";
    private static final String VALUE_OPERATOR_TYPE_AND = "AND";
    private static final String VALUE_OPERATOR_TYPE_OR = "OR";
    private static final String VALUE_OPERATOR_TYPE_NOT = "NOT";
    private static final String KEY_TALK_TYPE = "TALK_TYPE";
    private static final String VALUE_TALK_TYPE_CHARACTER = "CHARACTER";

    public enum Type {
        TIME, BOARD, SCENARIO, OPERATOR, TALK, INVENTORY;
    }

    @Id
    protected String id;

    @Enumerated(EnumType.STRING)
    private Type type;

    @ElementCollection
    @CollectionTable(name = "LO_CONDITION_VALUES", joinColumns = @JoinColumn(name = "condition_id"))
    @MapKeyColumn(name = "map_key")
    @Column(name = "map_value")
    private Map<String, String> keyValues = new HashMap<>();


    @ManyToMany
    @JoinTable(name = "LO_RELATION_CONDITION_SUB",
            joinColumns = @JoinColumn(name = "condition_id"),
            inverseJoinColumns = @JoinColumn(name = "sub_condition_id"))
    private Set<ConditionEntity> subs = new HashSet<>();

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public Type getType() { return type; }
    public void setType(Type type) { this.type = type; }

    public Map<String, String> getKeyValues() { return keyValues; }
    public Set<ConditionEntity> getSubs() {
        return subs;
    }

    public void setSubs(Set<ConditionEntity> subs) {
        this.subs = subs;
    }

    public Condition toModel() {
        final Condition.Id id = new Condition.Id(this.id);
        return switch (type) {
            case TIME -> new Condition.AbsoluteTime(id,
                    GameInstanceTimeUnit.ofMinutes(Integer.parseInt(keyValues.get(KEY_TIME_UNIT))),
                    BeforeOrAfter.valueOf(keyValues.get(KEY_BEFORE_AFTER)));
            case BOARD -> {
                BoardSpace.Id spaceId = new BoardSpace.Id(keyValues.get(KEY_PRIMARY));
                String type = keyValues.get(KEY_BOARD_SPACE_TYPE);
                yield switch (type) {
                    case VALUE_BOARD_SPACE_TYPE_IN -> new Condition.InsideSpace(id, spaceId);
                    case VALUE_BOARD_SPACE_TYPE_OUT -> new Condition.OutsideSpace(id, spaceId);
                    default -> throw new IllegalStateException("Unexpected space status: " + type);
                };
            }
            case SCENARIO -> {
                String type = keyValues.get(KEY_SCENARIO_TYPE);
                yield switch (type) {
                    case VALUE_SCENARIO_TYPE_STEP -> new Condition.Step(id, new ScenarioConfig.Step.Id(keyValues.get(KEY_PRIMARY)));
                    case VALUE_SCENARIO_TYPE_TARGET -> new Condition.Target(id, new ScenarioConfig.Target.Id(keyValues.get(KEY_PRIMARY)));
                    default -> throw new IllegalStateException("Unexpected scenario status: " + type);
                };
            }
            case OPERATOR -> {
                String operatorType = keyValues.get(KEY_PRIMARY);
                List<Condition> subModels = subs.stream().map(ConditionEntity::toModel).toList();
               yield switch (operatorType) {
                   case VALUE_OPERATOR_TYPE_AND -> new Condition.And(id, subModels);
                   case VALUE_OPERATOR_TYPE_OR -> new Condition.Or(id, subModels);
                   case VALUE_OPERATOR_TYPE_NOT -> {
                       if(subModels.size() != 1){
                           throw new IllegalArgumentException("Only one sub-condition allowed for NOT");
                       }
                       yield new Condition.Not(id, subModels.getFirst());
                   }
                   default -> throw new IllegalStateException("Unexpected operator status: " + operatorType);
                };
            }
            case TALK -> {
                TalkCharacter.Id characterId = new TalkCharacter.Id(keyValues.get(KEY_PRIMARY));
                yield new Condition.TalkWithCharacter(id, characterId);
            }
            case INVENTORY -> {
                GameConfigInventoryItem.Id itemId = new GameConfigInventoryItem.Id(keyValues.get(KEY_PRIMARY));
                yield new Condition.InventoryHasItem(id, itemId);
            }
        };
    }

    public static ConditionEntity fromModel(Condition condition) {
        ConditionEntity entity = new ConditionEntity();
        entity.setId(condition.id().value());
        switch (condition) {
            case Condition.AbsoluteTime absoluteTime -> {
                entity.setType(Type.TIME);
                entity.getKeyValues().put(KEY_TIME_UNIT, Integer.toString(absoluteTime.value().toMinutes()));
                entity.getKeyValues().put(KEY_BEFORE_AFTER, absoluteTime.beforeOrAfter().name());
            }
            case Condition.InsideSpace insideSpace -> {
                entity.setType(Type.BOARD);
                entity.getKeyValues().put(KEY_BOARD_SPACE_TYPE, VALUE_BOARD_SPACE_TYPE_IN);
                entity.getKeyValues().put(KEY_PRIMARY, insideSpace.spaceId().value());
            }
            case Condition.OutsideSpace outsideSpace -> {
                entity.setType(Type.BOARD);
                entity.getKeyValues().put(KEY_BOARD_SPACE_TYPE, VALUE_BOARD_SPACE_TYPE_OUT);
                entity.getKeyValues().put(KEY_PRIMARY, outsideSpace.spaceId().value());
            }
            case Condition.Step step -> {
                entity.setType(Type.SCENARIO);
                entity.getKeyValues().put(KEY_SCENARIO_TYPE, VALUE_SCENARIO_TYPE_STEP);
                entity.getKeyValues().put(KEY_PRIMARY, step.stepId().value());
            }
            case Condition.Target target -> {
                entity.setType(Type.SCENARIO);
                entity.getKeyValues().put(KEY_SCENARIO_TYPE, VALUE_SCENARIO_TYPE_TARGET);
                entity.getKeyValues().put(KEY_PRIMARY, target.targetId().value());
            }
            case Condition.And and -> {
                entity.setType(Type.OPERATOR);
                entity.getKeyValues().put(KEY_PRIMARY, VALUE_OPERATOR_TYPE_AND);
                entity.setSubs(and.conditions().stream().map(ConditionEntity::fromModel).collect(Collectors.toSet()));
            }
            case Condition.Or or -> {
                entity.setType(Type.OPERATOR);
                entity.getKeyValues().put(KEY_PRIMARY, VALUE_OPERATOR_TYPE_OR);
                entity.setSubs(or.conditions().stream().map(ConditionEntity::fromModel).collect(Collectors.toSet()));
            }
            case Condition.Not not -> {
                entity.setType(Type.OPERATOR);
                entity.getKeyValues().put(KEY_PRIMARY, VALUE_OPERATOR_TYPE_NOT);
                entity.setSubs(Set.of(ConditionEntity.fromModel(not.condition())));
            }
            case Condition.TalkWithCharacter talkWithCharacter -> {
                entity.setType(Type.TALK);
                entity.getKeyValues().put(KEY_TALK_TYPE, VALUE_TALK_TYPE_CHARACTER);
                entity.getKeyValues().put(KEY_PRIMARY, talkWithCharacter.characterId().value());
            }
            case Condition.InventoryHasItem inventoryHasItem -> {
                entity.setType(Type.INVENTORY);
                entity.getKeyValues().put(KEY_PRIMARY, inventoryHasItem.itemId().value());
            }
        }
        return entity;
    }
}
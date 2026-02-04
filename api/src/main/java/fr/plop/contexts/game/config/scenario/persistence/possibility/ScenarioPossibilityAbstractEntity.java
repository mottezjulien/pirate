package fr.plop.contexts.game.config.scenario.persistence.possibility;


import fr.plop.contexts.game.config.scenario.domain.model.Possibility;
import fr.plop.contexts.game.config.condition.persistence.ConditionEntity;
import fr.plop.contexts.game.config.scenario.persistence.possibility.consequence.entity.ConsequenceAbstractEntity;
import fr.plop.contexts.game.config.scenario.persistence.possibility.recurrence.ScenarioPossibilityRecurrenceAbstractEntity;
import fr.plop.contexts.game.config.scenario.persistence.possibility.trigger.ScenarioPossibilityTriggerEntity;
import jakarta.persistence.*;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Entity
@Table(name = "LO_SCENARIO_POSSIBILITY")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "type")
public abstract class ScenarioPossibilityAbstractEntity {

    @Id
    private String id;

    @ManyToOne
    @JoinColumn(name = "recurrence_id")
    private ScenarioPossibilityRecurrenceAbstractEntity recurrence;

    @ManyToOne
    @JoinColumn(name = "trigger_id")
    private ScenarioPossibilityTriggerEntity trigger;

    @ManyToOne
    @JoinColumn(name = "condition_id")
    private ConditionEntity nullableCondition;

    @ManyToMany
    @JoinTable(name = "LO_RELATION_SCENARIO_POSSIBILITY_CONSEQUENCE",
            joinColumns = @JoinColumn(name = "possibility_id"),
            inverseJoinColumns = @JoinColumn(name = "consequence_id"))
    private Set<ConsequenceAbstractEntity> consequences = new HashSet<>();


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public ScenarioPossibilityRecurrenceAbstractEntity getRecurrence() {
        return recurrence;
    }

    public void setRecurrence(ScenarioPossibilityRecurrenceAbstractEntity recurrence) {
        this.recurrence = recurrence;
    }

    public ScenarioPossibilityTriggerEntity getTrigger() {
        return trigger;
    }

    public void setTrigger(ScenarioPossibilityTriggerEntity trigger) {
        this.trigger = trigger;
    }

    public void setNullableCondition(ConditionEntity nullableCondition) {
        this.nullableCondition = nullableCondition;
    }

    public Set<ConsequenceAbstractEntity> getConsequences() {
        return consequences;
    }

    public void setConsequences(Set<ConsequenceAbstractEntity> consequences) {
        this.consequences = consequences;
    }

    public Possibility toModel() {
        return new Possibility(new Possibility.Id(id),
                recurrence.toModel(),
                trigger.toModel(),
                Optional.ofNullable(nullableCondition).map(ConditionEntity::toModel),
                consequences.stream().map(ConsequenceAbstractEntity::toModel).toList());
    }
}

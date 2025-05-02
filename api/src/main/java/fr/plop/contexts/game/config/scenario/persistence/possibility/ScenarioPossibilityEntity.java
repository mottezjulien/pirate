package fr.plop.contexts.game.config.scenario.persistence.possibility;


import fr.plop.contexts.game.config.scenario.domain.model.Possibility;
import fr.plop.contexts.game.config.scenario.persistence.core.ScenarioStepEntity;
import fr.plop.contexts.game.config.scenario.persistence.possibility.condition.entity.ScenarioPossibilityConditionAbstractEntity;
import fr.plop.contexts.game.config.scenario.persistence.possibility.consequence.entity.ScenarioPossibilityConsequenceAbstractEntity;
import fr.plop.contexts.game.config.scenario.persistence.possibility.trigger.entity.ScenarioPossibilityTriggerAbstractEntity;
import fr.plop.generic.enumerate.AndOrOr;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "TEST2_SCENARIO_POSSIBILITY")
public class ScenarioPossibilityEntity {

    @Id
    private String id;

    @ManyToOne
    @JoinColumn(name = "step_id")
    private ScenarioStepEntity step;

    @ManyToOne
    @JoinColumn(name = "trigger_id")
    private ScenarioPossibilityTriggerAbstractEntity trigger;

    @ManyToMany
    @JoinTable(name = "TEST2_RELATION_SCENARIO_POSSIBILITY_CONDITION",
            joinColumns = @JoinColumn(name = "possibility_id"),
            inverseJoinColumns = @JoinColumn(name = "condition_id"))
    private Set<ScenarioPossibilityConditionAbstractEntity> conditions = new HashSet<>();

    @Enumerated(EnumType.STRING)
    @Column(name = "condition_type")
    private AndOrOr conditionType;

    @ManyToMany
    @JoinTable(name = "TEST2_RELATION_SCENARIO_POSSIBILITY_CONSEQUENCE",
            joinColumns = @JoinColumn(name = "possibility_id"),
            inverseJoinColumns = @JoinColumn(name = "consequence_id"))
    private Set<ScenarioPossibilityConsequenceAbstractEntity> consequences = new HashSet<>();


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public ScenarioStepEntity getStep() {
        return step;
    }

    public void setStep(ScenarioStepEntity step) {
        this.step = step;
    }

    public ScenarioPossibilityTriggerAbstractEntity getTrigger() {
        return trigger;
    }

    public void setTrigger(ScenarioPossibilityTriggerAbstractEntity trigger) {
        this.trigger = trigger;
    }

    public Set<ScenarioPossibilityConditionAbstractEntity> getConditions() {
        return conditions;
    }

    public void setConditions(Set<ScenarioPossibilityConditionAbstractEntity> conditions) {
        this.conditions = conditions;
    }

    public AndOrOr getConditionType() {
        return conditionType;
    }

    public void setConditionType(AndOrOr conditionType) {
        this.conditionType = conditionType;
    }

    public Set<ScenarioPossibilityConsequenceAbstractEntity> getConsequences() {
        return consequences;
    }

    public void setConsequences(Set<ScenarioPossibilityConsequenceAbstractEntity> consequences) {
        this.consequences = consequences;
    }

    public Possibility toModel() {
        return new Possibility(new Possibility.Id(id), trigger.abstractToModel(),
                conditions.stream().map(ScenarioPossibilityConditionAbstractEntity::abstractToModel).toList(),
                conditionType,
                consequences.stream().map(ScenarioPossibilityConsequenceAbstractEntity::abstractToModel).toList());
    }
}

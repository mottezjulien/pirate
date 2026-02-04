package fr.plop.contexts.game.config.scenario.persistence.core;

import fr.plop.contexts.game.config.scenario.domain.model.ScenarioConfig;
import fr.plop.contexts.game.config.scenario.persistence.possibility.ScenarioPossibilityStepEntity;
import fr.plop.subs.i18n.persistence.I18nEntity;
import jakarta.persistence.*;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Entity
@Table(name = "LO_SCENARIO_STEP")
public class ScenarioStepEntity {

    @Id
    private String id;

    @ManyToOne
    @JoinColumn(name = "label_i18n_id")
    private I18nEntity label;

    @ManyToOne
    @JoinColumn(name = "description_i18n_id")
    private I18nEntity optDescription;

    @Column(name = "step_order")
    private int order;

    @ManyToOne
    @JoinColumn(name = "scenario_id")
    private ScenarioConfigEntity scenario;

    @OneToMany(mappedBy = "step")
    private Set<ScenarioTargetEntity> targets = new HashSet<>();

    @OneToMany(mappedBy = "step")
    private Set<ScenarioPossibilityStepEntity> possibilities = new HashSet<>();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public I18nEntity getLabel() {
        return label;
    }

    public void setLabel(I18nEntity label) {
        this.label = label;
    }

    public ScenarioConfigEntity getScenario() {
        return scenario;
    }

    public void setScenario(ScenarioConfigEntity scenario) {
        this.scenario = scenario;
    }

    public Set<ScenarioTargetEntity> getTargets() {
        return targets;
    }

    public void setTargets(Set<ScenarioTargetEntity> targets) {
        this.targets = targets;
    }

    public Set<ScenarioPossibilityStepEntity> getPossibilities() {
        return possibilities;
    }

    public void setPossibilities(Set<ScenarioPossibilityStepEntity> possibilities) {
        this.possibilities = possibilities;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public ScenarioConfig.Step toModel() {
        return new ScenarioConfig.Step(new ScenarioConfig.Step.Id(id), label.toModel(),
                Optional.ofNullable(optDescription).map(I18nEntity::toModel),
                order, targets.stream().map(ScenarioTargetEntity::toModel).toList(),
                possibilities.stream().map(ScenarioPossibilityStepEntity::toModel).toList());


    }

}

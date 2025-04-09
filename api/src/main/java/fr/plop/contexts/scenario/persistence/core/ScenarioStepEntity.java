package fr.plop.contexts.scenario.persistence.core;

import fr.plop.contexts.i18n.persistence.I18nEntity;
import fr.plop.contexts.scenario.domain.model.Scenario;
import fr.plop.contexts.scenario.persistence.possibility.ScenarioPossibilityEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Entity
@Table(name = "TEST2_SCENARIO_STEP")
public class ScenarioStepEntity {

    @Id
    private String id;

    @ManyToOne
    @JoinColumn(name = "label_i18n_id")
    private I18nEntity label;

    @ManyToOne
    @JoinColumn(name = "scenario_id")
    private ScenarioEntity scenario;

    @OneToMany(mappedBy = "step")
    private Set<ScenarioTargetEntity> targets = new HashSet<>();

    @OneToMany(mappedBy = "step")
    private Set<ScenarioPossibilityEntity> possibilities = new HashSet<>();


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

    public ScenarioEntity getScenario() {
        return scenario;
    }

    public void setScenario(ScenarioEntity scenario) {
        this.scenario = scenario;
    }

    public Set<ScenarioTargetEntity> getTargets() {
        return targets;
    }

    public void setTargets(Set<ScenarioTargetEntity> targets) {
        this.targets = targets;
    }

    public Set<ScenarioPossibilityEntity> getPossibilities() {
        return possibilities;
    }

    public void setPossibilities(Set<ScenarioPossibilityEntity> possibilities) {
        this.possibilities = possibilities;
    }

    public Scenario.Step toModel() {
        return new Scenario.Step(
                new Scenario.Step.Id(id),
                Optional.ofNullable(label).map(I18nEntity::toModel),
                targets.stream().map(ScenarioTargetEntity::toModel).toList(),
                possibilities.stream().map(ScenarioPossibilityEntity::toModel).toList()
        );

    }

}

package fr.plop.contexts.game.config.scenario.persistence.core;

import fr.plop.contexts.game.config.scenario.domain.model.ScenarioConfig;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "TEST2_SCENARIO_CONFIG")
public class ScenarioConfigEntity {

    @Id
    private String id;

    private String label;

    @OneToMany(mappedBy = "scenario")
    private Set<ScenarioStepEntity> steps = new HashSet<>();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public Set<ScenarioStepEntity> getSteps() {
        return steps;
    }

    public void setSteps(Set<ScenarioStepEntity> steps) {
        this.steps = steps;
    }

    public ScenarioConfig toModel() {
        return new ScenarioConfig(new ScenarioConfig.Id(id), label,
                steps.stream().map(ScenarioStepEntity::toModel).toList());
    }
}

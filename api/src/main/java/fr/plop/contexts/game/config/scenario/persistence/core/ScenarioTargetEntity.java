package fr.plop.contexts.game.config.scenario.persistence.core;

import fr.plop.contexts.game.config.scenario.domain.model.ScenarioConfig;
import fr.plop.subs.i18n.persistence.I18nEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.util.Optional;

@Entity
@Table(name = "TEST2_SCENARIO_TARGET")
public class ScenarioTargetEntity {

    @Id
    private String id;

    @ManyToOne
    @JoinColumn(name = "step_id")
    private ScenarioStepEntity step;

    @ManyToOne
    @JoinColumn(name = "label_i18n_id")
    private I18nEntity label;

    @ManyToOne
    @JoinColumn(name = "description_i18n_id")
    private I18nEntity description;

    private boolean optional;

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

    public I18nEntity getLabel() {
        return label;
    }

    public void setLabel(I18nEntity label) {
        this.label = label;
    }

    public I18nEntity getDescription() {
        return description;
    }

    public void setDescription(I18nEntity description) {
        this.description = description;
    }

    public boolean isOptional() {
        return optional;
    }

    public void setOptional(boolean optional) {
        this.optional = optional;
    }

    public ScenarioConfig.Target toModel() {
        return new ScenarioConfig.Target(
                new ScenarioConfig.Target.Id(id),
                Optional.ofNullable(label).map(l -> label.toModel()),
                Optional.ofNullable(description).map(I18nEntity::toModel),
                optional
        );
    }
}

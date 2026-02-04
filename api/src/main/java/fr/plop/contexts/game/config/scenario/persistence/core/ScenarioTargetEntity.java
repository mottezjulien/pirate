package fr.plop.contexts.game.config.scenario.persistence.core;

import fr.plop.contexts.game.config.scenario.domain.model.ScenarioConfig;
import fr.plop.subs.i18n.persistence.I18nEntity;
import jakarta.persistence.*;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Entity
@Table(name = "LO_SCENARIO_TARGET")
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

    @OneToMany
    @JoinTable(
            name = "LO_SCENARIO_TARGET_HINT",
            joinColumns = @JoinColumn(name = "target_id"),
            inverseJoinColumns = @JoinColumn(name = "i18n_id")
    )
    private final Set<I18nEntity> hints = new HashSet<>();

    @ManyToOne
    @JoinColumn(name = "answer_i18n_id")
    private I18nEntity answer;


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
                label.toModel(),
                Optional.ofNullable(description).map(I18nEntity::toModel),
                optional, hints.stream().map(I18nEntity::toModel).toList(),
                Optional.ofNullable(answer).map(I18nEntity::toModel));
    }
}

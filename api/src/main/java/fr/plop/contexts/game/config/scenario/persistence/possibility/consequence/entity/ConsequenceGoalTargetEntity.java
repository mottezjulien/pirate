package fr.plop.contexts.game.config.scenario.persistence.possibility.consequence.entity;


import fr.plop.contexts.game.config.consequence.Consequence;
import fr.plop.contexts.game.config.scenario.domain.model.ScenarioConfig;
import fr.plop.contexts.game.instance.scenario.domain.model.ScenarioSessionState;
import jakarta.persistence.*;

@Entity
@DiscriminatorValue("GOAL_TARGET")
public final class ConsequenceGoalTargetEntity extends
        ConsequenceAbstractEntity {

    @Column(name = "target_id")
    private String targetId;

    @Enumerated(EnumType.STRING)
    private ScenarioSessionState state;

    public String getTargetId() {
        return targetId;
    }

    public void setTargetId(String targetId) {
        this.targetId = targetId;
    }

    public ScenarioSessionState getState() {
        return state;
    }

    public void setState(ScenarioSessionState state) {
        this.state = state;
    }

    public Consequence toModel() {
        return new Consequence.ScenarioTarget(new Consequence.Id(id), new ScenarioConfig.Target.Id(targetId), state);
    }

}

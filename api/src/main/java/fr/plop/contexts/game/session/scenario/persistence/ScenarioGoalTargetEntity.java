package fr.plop.contexts.game.session.scenario.persistence;

import fr.plop.contexts.game.config.scenario.persistence.core.ScenarioTargetEntity;
import fr.plop.contexts.game.session.scenario.domain.model.ScenarioGoal;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "TEST2_SCENARIO_GOAL_TARGET")
public class ScenarioGoalTargetEntity {

    @Id
    private String id;

    @ManyToOne
    @JoinColumn(name = "goal_id")
    private ScenarioGoalEntity goal;

    @ManyToOne
    @JoinColumn(name = "target_id")
    private ScenarioTargetEntity target;

    @Enumerated(EnumType.STRING)
    private ScenarioGoal.State state;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public ScenarioGoalEntity getGoal() {
        return goal;
    }

    public void setGoal(ScenarioGoalEntity goal) {
        this.goal = goal;
    }

    public ScenarioTargetEntity getTarget() {
        return target;
    }

    public void setTarget(ScenarioTargetEntity target) {
        this.target = target;
    }

    public ScenarioGoal.State getState() {
        return state;
    }

    public void setState(ScenarioGoal.State state) {
        this.state = state;
    }

}

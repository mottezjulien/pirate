package fr.plop.contexts.game.session.scenario.persistence;


import fr.plop.contexts.game.config.scenario.persistence.core.ScenarioStepEntity;
import fr.plop.contexts.game.session.core.persistence.GamePlayerEntity;
import fr.plop.contexts.game.session.scenario.domain.model.ScenarioGoal;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "TEST2_SCENARIO_GOAL")
public class ScenarioGoalEntity {

    @Id
    private String id;

    @ManyToOne
    @JoinColumn(name = "player_id")
    private GamePlayerEntity player;

    @ManyToOne
    @JoinColumn(name = "step_id")
    private ScenarioStepEntity step;

    @Enumerated(EnumType.STRING)
    private ScenarioGoal.State state;

    @OneToMany(mappedBy = "goal")
    private Set<ScenarioGoalTargetEntity> targets = new HashSet<>();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public GamePlayerEntity getPlayer() {
        return player;
    }

    public void setPlayer(GamePlayerEntity player) {
        this.player = player;
    }

    public ScenarioStepEntity getStep() {
        return step;
    }

    public void setStep(ScenarioStepEntity step) {
        this.step = step;
    }

    public void setState(ScenarioGoal.State state) {
        this.state = state;
    }

    public ScenarioGoal.State getState() {
        return state;
    }

    public Set<ScenarioGoalTargetEntity> getTargets() {
        return targets;
    }

    public void setTargets(Set<ScenarioGoalTargetEntity> targets) {
        this.targets = targets;
    }
}

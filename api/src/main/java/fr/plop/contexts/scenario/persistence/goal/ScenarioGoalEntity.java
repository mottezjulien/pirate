package fr.plop.contexts.scenario.persistence.goal;


import fr.plop.contexts.game.persistence.GamePlayerEntity;
import fr.plop.contexts.scenario.domain.model.ScenarioGoalState;
import fr.plop.contexts.scenario.persistence.core.ScenarioStepEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

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
    private ScenarioGoalState state;

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

    public void setState(ScenarioGoalState state) {
        this.state = state;
    }

    public ScenarioGoalState getState() {
        return state;
    }
}

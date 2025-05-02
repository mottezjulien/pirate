package fr.plop.contexts.game.session.scenario.persistence;


import fr.plop.contexts.game.config.scenario.domain.model.ScenarioConfig;
import fr.plop.contexts.game.session.core.domain.model.GamePlayer;
import fr.plop.contexts.game.session.core.persistence.GamePlayerEntity;
import fr.plop.contexts.game.session.scenario.domain.model.ScenarioGoal;
import fr.plop.contexts.game.config.scenario.persistence.core.ScenarioStepEntity;
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
    private ScenarioGoal.State state;

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

    public ScenarioGoal toModel() {
        return new ScenarioGoal(new GamePlayer.Id(player.getId()), new ScenarioConfig.Step.Id(step.getId()), state);
    }

}

package fr.plop.contexts.game.instance.scenario.persistence;


import fr.plop.contexts.game.config.scenario.domain.model.ScenarioConfig;
import fr.plop.contexts.game.config.scenario.persistence.core.ScenarioStepEntity;
import fr.plop.contexts.game.instance.core.domain.model.GamePlayer;
import fr.plop.contexts.game.instance.core.persistence.GamePlayerEntity;
import fr.plop.contexts.game.instance.scenario.domain.model.ScenarioSessionState;
import fr.plop.generic.tools.StringTools;
import jakarta.persistence.*;

@Entity
@Table(name = "LO_SCENARIO_GOAL_STEP")
public class ScenarioGoalStepEntity {

    @Id
    private String id;

    @ManyToOne
    @JoinColumn(name = "player_id")
    private GamePlayerEntity player;

    @ManyToOne
    @JoinColumn(name = "step_id")
    private ScenarioStepEntity step;

    @Enumerated(EnumType.STRING)
    private ScenarioSessionState state;

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

    public void setState(ScenarioSessionState state) {
        this.state = state;
    }

    public ScenarioSessionState getState() {
        return state;
    }

    public static ScenarioGoalStepEntity build(GamePlayer.Id playerId, ScenarioConfig.Step.Id stepId, ScenarioSessionState state) {
        ScenarioGoalStepEntity entity = new ScenarioGoalStepEntity();
        entity.setId(StringTools.generate());
        entity.setState(state);

        GamePlayerEntity playerEntity = new GamePlayerEntity();
        playerEntity.setId(playerId.value());
        entity.setPlayer(playerEntity);

        ScenarioStepEntity stepEntity = new ScenarioStepEntity();
        stepEntity.setId(stepId.value());
        entity.setStep(stepEntity);

        return entity;
    }

}

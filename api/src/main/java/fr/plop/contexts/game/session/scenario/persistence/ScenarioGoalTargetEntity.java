package fr.plop.contexts.game.session.scenario.persistence;

import fr.plop.contexts.game.config.scenario.domain.model.ScenarioConfig;
import fr.plop.contexts.game.config.scenario.persistence.core.ScenarioTargetEntity;
import fr.plop.contexts.game.session.core.domain.model.GamePlayer;
import fr.plop.contexts.game.session.core.persistence.GamePlayerEntity;
import fr.plop.contexts.game.session.scenario.domain.model.ScenarioSessionState;
import fr.plop.generic.tools.StringTools;
import jakarta.persistence.*;

@Entity
@Table(name = "TEST2_SCENARIO_GOAL_TARGET")
public class ScenarioGoalTargetEntity {

    @Id
    private String id;

    @ManyToOne
    @JoinColumn(name = "player_id")
    private GamePlayerEntity player;

    @ManyToOne
    @JoinColumn(name = "target_id")
    private ScenarioTargetEntity target;

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

    public ScenarioTargetEntity getTarget() {
        return target;
    }

    public void setTarget(ScenarioTargetEntity target) {
        this.target = target;
    }

    public ScenarioSessionState getState() {
        return state;
    }

    public void setState(ScenarioSessionState state) {
        this.state = state;
    }

    public static ScenarioGoalTargetEntity build(GamePlayer.Id playerId, ScenarioConfig.Target.Id targetId,
                                                 ScenarioSessionState state) {
        ScenarioGoalTargetEntity entity = new ScenarioGoalTargetEntity();
        entity.setId(StringTools.generate());
        entity.setState(state);

        GamePlayerEntity playerEntity = new GamePlayerEntity();
        playerEntity.setId(playerId.value());
        entity.setPlayer(playerEntity);

        ScenarioTargetEntity target = new ScenarioTargetEntity();
        target.setId(targetId.value());
        entity.setTarget(target);

        return entity;
    }

}

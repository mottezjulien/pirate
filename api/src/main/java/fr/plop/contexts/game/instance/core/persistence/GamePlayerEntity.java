package fr.plop.contexts.game.instance.core.persistence;

import fr.plop.contexts.game.config.board.domain.model.BoardSpace;
import fr.plop.contexts.game.config.scenario.domain.model.ScenarioConfig;
import fr.plop.contexts.game.instance.board.persistence.BoardPositionEntity;
import fr.plop.contexts.game.instance.core.domain.model.GamePlayer;
import fr.plop.contexts.game.instance.scenario.persistence.ScenarioGoalStepEntity;
import fr.plop.contexts.user.User;
import fr.plop.contexts.user.persistence.UserEntity;
import fr.plop.subs.i18n.persistence.I18nEntity;
import jakarta.persistence.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "LO_GAME_PLAYER")
public class GamePlayerEntity {

    @Id
    private String id;

    @ManyToOne
    @JoinColumn(name = "session_id")
    private GameInstanceEntity session;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private UserEntity user;

    @Enumerated
    private GamePlayer.State state;

    @OneToOne
    @JoinColumn(name = "last_position_id")
    private BoardPositionEntity lastPosition;

    @OneToMany(mappedBy = "player")
    private Set<ScenarioGoalStepEntity> goals = new HashSet<>();

    @OneToMany(mappedBy = "player")
    private Set<GamePlayerActionEntity> actions = new HashSet<>();

    @ManyToOne
    @JoinColumn(name = "end_game_reason_i18n_id")
    private I18nEntity endGameReason;


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public GameInstanceEntity getSession() {
        return session;
    }

    public void setSession(GameInstanceEntity session) {
        this.session = session;
    }

    public UserEntity getUser() {
        return user;
    }

    public void setUser(UserEntity user) {
        this.user = user;
    }

    public GamePlayer.State getState() {
        return state;
    }

    public void setState(GamePlayer.State state) {
        this.state = state;
    }

    public BoardPositionEntity getLastPosition() {
        return lastPosition;
    }

    public void setLastPosition(BoardPositionEntity lastPosition) {
        this.lastPosition = lastPosition;
    }

    public Set<ScenarioGoalStepEntity> getGoals() {
        return goals;
    }

    public void setGoals(Set<ScenarioGoalStepEntity> goals) {
        this.goals = goals;
    }

    public Set<GamePlayerActionEntity> getActions() {
        return actions;
    }

    public void setActions(Set<GamePlayerActionEntity> actions) {
        this.actions = actions;
    }

    public I18nEntity getEndGameReason() {
        return endGameReason;
    }

    public void setEndGameReason(I18nEntity endGameReason) {
        this.endGameReason = endGameReason;
    }

    public static GamePlayerEntity fromModelId(GamePlayer.Id modelId) {
        GamePlayerEntity entity = new GamePlayerEntity();
        entity.setId(modelId.value());
        return entity;
    }

    public GamePlayer toModel() {
        /*List<BoardSpace.Id> spacesInIds = List.of();
        if (lastPosition != null) {
            spacesInIds = lastPosition.getSpaces().stream()
                    .map(spaceEntity -> new BoardSpace.Id(spaceEntity.getId()))
                    .toList();
        }
        List<ScenarioConfig.Step.Id> stepActiveIds = goals.stream()
                .map(entity -> new ScenarioConfig.Step.Id(entity.getStep().getId()))
                .toList();*/
        return new GamePlayer(toModelId(), user.toModelId(), state);
    }

    public GamePlayer.Id toModelId() {
        return new GamePlayer.Id(id);
    }


}

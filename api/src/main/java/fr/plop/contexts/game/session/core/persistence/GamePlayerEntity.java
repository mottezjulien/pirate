package fr.plop.contexts.game.session.core.persistence;

import fr.plop.contexts.connect.persistence.entity.ConnectionUserEntity;
import fr.plop.contexts.game.config.board.domain.model.BoardSpace;
import fr.plop.contexts.game.config.scenario.domain.model.ScenarioConfig;
import fr.plop.contexts.game.session.board.persistence.BoardPositionEntity;
import fr.plop.contexts.game.session.core.domain.model.GamePlayer;
import fr.plop.contexts.game.session.scenario.persistence.ScenarioGoalEntity;
import fr.plop.contexts.i18n.persistence.I18nEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "TEST2_GAME_PLAYER")
public class GamePlayerEntity {

    @Id
    private String id;

    @ManyToOne
    @JoinColumn(name = "session_id")
    private GameSessionEntity session;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private ConnectionUserEntity user;

    @Enumerated
    private GamePlayer.State state;

    //TODO: List Events ? Move from Player ?
    @ManyToOne
    @JoinColumn(name = "reason_i18n_id")
    private I18nEntity reason;

    @OneToOne
    @JoinColumn(name = "last_position_id")
    private BoardPositionEntity position;

    @OneToMany(mappedBy = "player")
    private Set<ScenarioGoalEntity> goals = new HashSet<>();


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public GameSessionEntity getSession() {
        return session;
    }

    public void setSession(GameSessionEntity game) {
        this.session = game;
    }

    public ConnectionUserEntity getUser() {
        return user;
    }

    public void setUser(ConnectionUserEntity user) {
        this.user = user;
    }

    public GamePlayer.State getState() {
        return state;
    }

    public void setState(GamePlayer.State state) {
        this.state = state;
    }

    public void setReason(I18nEntity reason) {
        this.reason = reason;
    }

    public I18nEntity getReason() {
        return reason;
    }

    public BoardPositionEntity getPosition() {
        return position;
    }

    public void setPosition(BoardPositionEntity position) {
        this.position = position;
    }

    public Set<ScenarioGoalEntity> getGoals() {
        return goals;
    }

    public void setGoals(Set<ScenarioGoalEntity> goals) {
        this.goals = goals;
    }

    public GamePlayer toModel() {
        List<BoardSpace.Id> spacesInIds = List.of();
        if (position != null) {
            spacesInIds = position.getSpaces().stream()
                    .map(spaceEntity -> new BoardSpace.Id(spaceEntity.getId()))
                    .toList();
        }
        List<ScenarioConfig.Step.Id> stepActiveIds = goals.stream()
                .map(entity -> new ScenarioConfig.Step.Id(entity.getStep().getId()))
                .toList();
        return new GamePlayer(new GamePlayer.Id(id), stepActiveIds, spacesInIds);
    }
}

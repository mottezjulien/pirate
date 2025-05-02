package fr.plop.contexts.game.session.core.persistence;

import fr.plop.contexts.connect.domain.ConnectUser;
import fr.plop.contexts.connect.persistence.ConnectionUserEntity;
import fr.plop.contexts.game.config.board.domain.model.BoardSpace;
import fr.plop.contexts.game.session.board.persistence.BoardPositionEntity;
import fr.plop.contexts.game.session.core.domain.model.GamePlayer;
import fr.plop.contexts.game.session.core.domain.model.GameSession;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import java.util.List;

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

    @OneToOne
    @JoinColumn(name = "last_position_id")
    private BoardPositionEntity position;

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

    public BoardPositionEntity getPosition() {
        return position;
    }

    public void setPosition(BoardPositionEntity position) {
        this.position = position;
    }

    public GamePlayer toModel() {
        GameSession.Id sessionId = new GameSession.Id(session.getId());
        ConnectUser.Id userId = new ConnectUser.Id(user.getId());
        List<BoardSpace.Id> positions = position.getSpaces().stream()
                .map(spaceEntity -> new BoardSpace.Id(spaceEntity.getId()))
                .toList();
        return new GamePlayer(new GamePlayer.Atom(new GamePlayer.Id(id), sessionId), userId, positions);
    }
}

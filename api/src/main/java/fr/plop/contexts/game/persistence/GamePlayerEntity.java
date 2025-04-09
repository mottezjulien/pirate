package fr.plop.contexts.game.persistence;

import fr.plop.contexts.connect.persistence.ConnectionUserEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "TEST2_GAME_PLAYER")
public class GamePlayerEntity {

    @Id
    private String id;

    @ManyToOne
    @JoinColumn(name = "game_id")
    private GameEntity game;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private ConnectionUserEntity user;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public GameEntity getGame() {
        return game;
    }

    public void setGame(GameEntity game) {
        this.game = game;
    }

    public ConnectionUserEntity getUser() {
        return user;
    }

    public void setUser(ConnectionUserEntity user) {
        this.user = user;
    }
}

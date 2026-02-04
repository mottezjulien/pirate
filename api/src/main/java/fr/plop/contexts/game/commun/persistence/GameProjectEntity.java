package fr.plop.contexts.game.commun.persistence;


import jakarta.persistence.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "LO_GAME_PROJECT")
public class GameProjectEntity {

    @Id
    private String id;

    @Column(unique = true)
    private String code;

    @OneToOne
    @JoinColumn(name = "game_active_id")
    private GameEntity active;

    @OneToMany(mappedBy = "project")
    private Set<GameEntity> games = new HashSet<>();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public GameEntity getActive() {
        return active;
    }

    public void setActive(GameEntity active) {
        this.active = active;
    }

    public Set<GameEntity> getGames() {
        return games;
    }

    public void setGames(Set<GameEntity> games) {
        this.games = games;
    }
}

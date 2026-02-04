package fr.plop.contexts.game.commun.persistence;


import fr.plop.contexts.game.commun.domain.Game;
import jakarta.persistence.*;

@Entity
@Table(name = "LO_GAME")
public class GameEntity {

    @Id
    private String id;

    private String version;

    @ManyToOne
    @JoinColumn(name = "project_id")
    private GameProjectEntity project;



    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public GameProjectEntity getProject() {
        return project;
    }

    public void setProject(GameProjectEntity project) {
        this.project = project;
    }


    public static GameEntity fromModelId(Game.Id gameId) {
        GameEntity entity = new GameEntity();
        entity.setId(gameId.value());
        return entity;
    }


}

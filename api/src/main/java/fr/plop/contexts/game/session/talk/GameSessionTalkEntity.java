package fr.plop.contexts.game.session.talk;


import fr.plop.contexts.game.session.core.persistence.GamePlayerEntity;
import jakarta.persistence.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "TEST2_SESSION_TALK")
public class GameSessionTalkEntity {

    @Id
    private String id;

    @ManyToOne
    @JoinColumn(name = "player_id")
    private GamePlayerEntity player;

    @OneToMany(mappedBy = "talk")
    private final Set<GameSessionTalkItemEntity> items = new HashSet<>();

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

    public Set<GameSessionTalkItemEntity> getItems() {
        return items;
    }
}

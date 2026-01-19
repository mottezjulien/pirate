package fr.plop.contexts.game.session.talk;


import fr.plop.contexts.game.config.talk.persistence.TalkItemEntity;
import jakarta.persistence.*;

@Entity
@Table(name = "TEST2_SESSION_TALK_ITEM")
public class GameSessionTalkItemEntity {

    @Id
    private String id;

    @ManyToOne
    @JoinColumn(name = "talk_id")
    private GameSessionTalkEntity talk;

    @ManyToOne
    @JoinColumn(name = "config_item_id")
    private TalkItemEntity config;


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public TalkItemEntity getConfig() {
        return config;
    }

    public void setConfig(TalkItemEntity config) {
        this.config = config;
    }

    public GameSessionTalkEntity getTalk() {
        return talk;
    }

    public void setTalk(GameSessionTalkEntity talk) {
        this.talk = talk;
    }
}

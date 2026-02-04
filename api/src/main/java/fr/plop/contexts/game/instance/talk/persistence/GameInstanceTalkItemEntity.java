package fr.plop.contexts.game.instance.talk.persistence;


import fr.plop.contexts.game.config.talk.persistence.TalkCharacterEntity;
import fr.plop.contexts.game.config.talk.persistence.TalkItemEntity;
import jakarta.persistence.*;

@Entity
@Table(name = "LO_SESSION_TALK_ITEM")
public class GameInstanceTalkItemEntity {

    @Id
    private String id;

    @ManyToOne
    @JoinColumn(name = "talk_id")
    private GameInstanceTalkEntity talk;

    @ManyToOne
    @JoinColumn(name = "config_character_id")
    private TalkCharacterEntity configCharacter;

    @ManyToOne
    @JoinColumn(name = "config_item_id")
    private TalkItemEntity configItem;


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }


    public void setConfigItem(TalkItemEntity configItem) {
        this.configItem = configItem;
    }

    public TalkItemEntity getConfigItem() {
        return configItem;
    }

    public void setTalk(GameInstanceTalkEntity talk) {
        this.talk = talk;
    }

    public void setConfigCharacter(TalkCharacterEntity configCharacter) {
        this.configCharacter = configCharacter;
    }

    public TalkCharacterEntity getConfigCharacter() {
        return configCharacter;
    }
}

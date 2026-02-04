package fr.plop.contexts.game.instance.talk.persistence;


import fr.plop.contexts.game.config.talk.domain.TalkCharacter;
import fr.plop.contexts.game.config.talk.domain.TalkItem;
import fr.plop.contexts.game.instance.core.persistence.GamePlayerEntity;
import fr.plop.contexts.game.instance.talk.GameInstanceTalk;
import jakarta.persistence.*;

import java.util.*;

@Entity
@Table(name = "LO_SESSION_TALK")
public class GameInstanceTalkEntity {

    @Id
    private String id;

    @ManyToOne
    @JoinColumn(name = "player_id")
    private GamePlayerEntity player;

    @OneToMany(mappedBy = "talk")
    private final Set<GameInstanceTalkItemEntity> items = new HashSet<>();

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

    public Set<GameInstanceTalkItemEntity> getItems() {
        return items;
    }

    public static GameInstanceTalkEntity fromModelId(GameInstanceTalk.Id id) {
        GameInstanceTalkEntity entity = new GameInstanceTalkEntity();
        entity.setId(id.value());
        return entity;
    }

    public GameInstanceTalk toModel() {
        Map<TalkCharacter.Id, List<TalkItem.Id>> map = new HashMap<>();
        items.forEach(item -> {
            TalkCharacter.Id characterId = new  TalkCharacter.Id(item.getConfigCharacter().getId());
            List<TalkItem.Id> list = map.getOrDefault(characterId, new ArrayList<>());
            list.add(new TalkItem.Id(item.getConfigItem().getId()));
            map.put(characterId, list);
        });
        return new GameInstanceTalk(new GameInstanceTalk.Id(id), map);
    }
}

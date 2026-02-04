package fr.plop.contexts.game.instance.talk;


import fr.plop.contexts.game.config.talk.domain.TalkCharacter;
import fr.plop.contexts.game.config.talk.domain.TalkItem;
import fr.plop.contexts.game.config.talk.persistence.TalkCharacterEntity;
import fr.plop.contexts.game.config.talk.persistence.TalkItemEntity;
import fr.plop.contexts.game.instance.core.domain.model.GamePlayer;
import fr.plop.contexts.game.instance.core.persistence.GamePlayerEntity;
import fr.plop.contexts.game.instance.talk.persistence.GameInstanceTalkEntity;
import fr.plop.contexts.game.instance.talk.persistence.GameInstanceTalkItemEntity;
import fr.plop.contexts.game.instance.talk.persistence.GameInstanceTalkItemRepository;
import fr.plop.contexts.game.instance.talk.persistence.GameInstanceTalkRepository;
import fr.plop.generic.tools.StringTools;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class GameInstanceTalkAdapter implements GameInstanceTalkUseCase.Port {

    private final GameInstanceTalkRepository talkRepository;
    private final GameInstanceTalkItemRepository talkItemRepository;

    public GameInstanceTalkAdapter(GameInstanceTalkRepository talkRepository, GameInstanceTalkItemRepository talkItemRepository) {
        this.talkRepository = talkRepository;
        this.talkItemRepository = talkItemRepository;
    }

    @Override
    public Optional<GameInstanceTalk> findByPlayerId(GamePlayer.Id playerId) {
        Optional<GameInstanceTalkEntity> opt = talkRepository.fullByPlayerId(playerId.value());
        return opt.map(GameInstanceTalkEntity::toModel);
    }

    @Override
    public GameInstanceTalk createTalk(GamePlayer.Id playerId) {
        GameInstanceTalkEntity entity = new GameInstanceTalkEntity();
        entity.setId(StringTools.generate());
        entity.setPlayer(GamePlayerEntity.fromModelId(playerId));
        System.out.println("talkRepository.count()1:" + talkRepository.count());
        talkRepository.save(entity);
        System.out.println("talkRepository.count()2:" + talkRepository.count());
        return entity.toModel();
    }

    @Override
    public void insert(GameInstanceTalk.Id sessionTalkId, TalkCharacter.Id characterId, TalkItem.Id talkId) {
        GameInstanceTalkItemEntity sessionTalkItem = new GameInstanceTalkItemEntity();
        sessionTalkItem.setId(StringTools.generate());
        sessionTalkItem.setTalk(GameInstanceTalkEntity.fromModelId(sessionTalkId));
        sessionTalkItem.setConfigItem(TalkItemEntity.fromModelId(talkId));
        sessionTalkItem.setConfigCharacter(TalkCharacterEntity.fromModelId(characterId));
        talkItemRepository.save(sessionTalkItem);
    }
}

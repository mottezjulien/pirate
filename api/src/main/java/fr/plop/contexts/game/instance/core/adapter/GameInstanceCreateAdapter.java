package fr.plop.contexts.game.instance.core.adapter;

import fr.plop.contexts.game.config.Image.persistence.ImageConfigEntity;
import fr.plop.contexts.game.config.board.persistence.entity.BoardConfigEntity;
import fr.plop.contexts.game.config.map.persistence.MapConfigEntity;
import fr.plop.contexts.game.config.scenario.persistence.core.ScenarioConfigEntity;
import fr.plop.contexts.game.config.talk.persistence.TalkConfigEntity;
import fr.plop.contexts.game.config.template.domain.model.Template;
import fr.plop.contexts.game.config.template.persistence.TemplateEntity;
import fr.plop.contexts.game.config.template.persistence.TemplateRepository;
import fr.plop.contexts.game.instance.core.domain.model.GameInstance;
import fr.plop.contexts.game.instance.core.domain.model.GamePlayer;
import fr.plop.contexts.game.instance.core.domain.usecase.GameInstanceUseCase;
import fr.plop.contexts.game.instance.core.persistence.GameInstanceEntity;
import fr.plop.contexts.game.instance.core.persistence.GameInstanceRepository;
import fr.plop.contexts.game.instance.core.persistence.GamePlayerEntity;
import fr.plop.contexts.game.instance.core.persistence.GamePlayerRepository;
import fr.plop.contexts.user.User;
import fr.plop.contexts.user.persistence.UserEntity;
import fr.plop.generic.tools.StringTools;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Optional;
import java.util.stream.Stream;

@Component
public class GameInstanceCreateAdapter implements GameInstanceUseCase.Port {

    private final TemplateRepository templateRepository;
    private final GameInstanceRepository instanceRepository;
    private final GamePlayerRepository playerRepository;

    public GameInstanceCreateAdapter(TemplateRepository templateRepository, GameInstanceRepository instanceRepository, GamePlayerRepository playerRepository) {
        this.templateRepository = templateRepository;
        this.instanceRepository = instanceRepository;
        this.playerRepository = playerRepository;
    }

    @Override
    public Stream<GameInstance.Atom> findOpenedByUserId(User.Id userId) {
        return instanceRepository
                .fullOpenedByUserId(userId.value()).stream().map(GameInstanceEntity::toModelAtom);
    }

    @Override
    public Optional<GameInstance.Atom> findById(GameInstance.Id instanceId) {
        return instanceRepository
                .fullById(instanceId.value())
                .map(GameInstanceEntity::toModelAtom);
    }

    @Override
    public Optional<Template> findTemplateById(Template.Id templateId) {
        return templateRepository
                .fullById(templateId.value())
                .stream().findFirst()
                .map(TemplateEntity::toModel);
    }

    @Override
    public GameInstance create(Template template) {
        GameInstance.State state = GameInstance.State.INIT;

        GameInstanceEntity entity = new GameInstanceEntity();
        entity.setState(state);
        entity.setId(StringTools.generate());
        TemplateEntity templateEntity = new TemplateEntity();
        templateEntity.setId(template.id().value());
        entity.setTemplate(templateEntity);
        //entity.setLabel(template.label());
        entity.setStartAt(Instant.now());

        ScenarioConfigEntity scenarioEntity = new ScenarioConfigEntity();
        scenarioEntity.setId(template.scenario().id().value());
        entity.setScenario(scenarioEntity);

        BoardConfigEntity boardEntity = new BoardConfigEntity();
        boardEntity.setId(template.board().id().value());
        entity.setBoard(boardEntity);

        MapConfigEntity mapEntity = new MapConfigEntity();
        mapEntity.setId(template.map().id().value());
        entity.setMap(mapEntity);

        TalkConfigEntity talkEntity = new TalkConfigEntity();
        talkEntity.setId(template.talk().id().value());
        entity.setTalk(talkEntity);

        ImageConfigEntity imageEntity = new ImageConfigEntity();
        imageEntity.setId(template.image().id().value());
        entity.setImage(imageEntity);

        entity = instanceRepository.save(entity);

        return GameInstance.buildWithoutPlayer(new GameInstance.Id(entity.getId()),
                state, template.scenario(), template.board(), template.map(),
                template.talk(), template.image(), template.inventory());
    }

    @Override
    public GamePlayer insertUserId(GameInstance.Id isntanceId, User.Id userId) {
        GamePlayerEntity playerEntity = new GamePlayerEntity();
        playerEntity.setId(StringTools.generate());

        GameInstanceEntity instanceEntity = new GameInstanceEntity();
        instanceEntity.setId(isntanceId.value());
        playerEntity.setInstance(instanceEntity);


        playerEntity.setUser(UserEntity.buildWithModelId(userId));

        playerEntity.setState(GamePlayer.State.INIT);

        playerRepository.save(playerEntity);

        return playerEntity.toModel();
    }


}

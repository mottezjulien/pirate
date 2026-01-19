package fr.plop.contexts.game.session.core.adapter;

import fr.plop.contexts.user.User;
import fr.plop.contexts.user.persistence.UserEntity;
import fr.plop.contexts.game.config.Image.persistence.ImageConfigEntity;
import fr.plop.contexts.game.config.board.persistence.entity.BoardConfigEntity;
import fr.plop.contexts.game.config.map.persistence.MapConfigEntity;
import fr.plop.contexts.game.config.scenario.persistence.core.ScenarioConfigEntity;
import fr.plop.contexts.game.config.talk.persistence.TalkConfigEntity;
import fr.plop.contexts.game.config.template.domain.model.Template;
import fr.plop.contexts.game.config.template.persistence.TemplateEntity;
import fr.plop.contexts.game.config.template.persistence.TemplateRepository;
import fr.plop.contexts.game.session.core.domain.model.GamePlayer;
import fr.plop.contexts.game.session.core.domain.model.GameSession;
import fr.plop.contexts.game.session.core.domain.model.GameSessionContext;
import fr.plop.contexts.game.session.core.domain.usecase.GameSessionUseCase;
import fr.plop.contexts.game.session.core.persistence.GamePlayerEntity;
import fr.plop.contexts.game.session.core.persistence.GamePlayerRepository;
import fr.plop.contexts.game.session.core.persistence.GameSessionEntity;
import fr.plop.contexts.game.session.core.persistence.GameSessionRepository;
import fr.plop.generic.tools.StringTools;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Optional;

@Component
public class GameSessionCreateAdapter implements GameSessionUseCase.Port {
    private final TemplateRepository templateRepository;
    private final GameSessionRepository sessionRepository;
    private final GamePlayerRepository playerRepository;

    public GameSessionCreateAdapter(TemplateRepository templateRepository, GameSessionRepository sessionRepository, GamePlayerRepository playerRepository) {
        this.templateRepository = templateRepository;
        this.sessionRepository = sessionRepository;
        this.playerRepository = playerRepository;
    }


    @Override
    public Optional<GameSessionContext> findCurrentGameSession(User.Id userId) {
        return sessionRepository.findByIdFetchPlayerAndUser(userId.value()).stream()
                .filter(session -> session.getState() != GameSession.State.OVER)
                .map(entity -> {
                    GamePlayer.Id playerId = entity.getPlayers().stream()
                            .filter(player -> player.getUser().is(userId.value()))  //.getId().equals(userId.value()))
                            .findFirst()
                            .map(player -> new GamePlayer.Id(player.getId()))
                            .orElseThrow();
                    return new GameSessionContext(new GameSession.Id(entity.getId()),playerId);
                })
                .findFirst();
    }

    @Override
    public Optional<Template> findTemplateById(Template.Id templateId) {
        return templateRepository
                .fullById(templateId.value())
                .stream().findFirst()
                .map(TemplateEntity::toModel);
    }

    @Override
    public GameSession create(Template template) {
        GameSession.State state = GameSession.State.INIT;

        GameSessionEntity entity = new GameSessionEntity();
        entity.setState(state);
        entity.setId(StringTools.generate());
        TemplateEntity templateEntity = new TemplateEntity();
        templateEntity.setId(template.id().value());
        entity.setTemplate(templateEntity);
        entity.setLabel(template.label());
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

        entity = sessionRepository.save(entity);

        return GameSession.buildWithoutPlayer(new GameSession.Id(entity.getId()),
                template.label(), state, template.scenario(), template.board(), template.map(),
                template.talk(), template.image(), template.inventory());
    }

    @Override
    public GamePlayer.Id insertUserId(GameSession.Id sessionId, User.Id userId) {
        GamePlayerEntity playerEntity = new GamePlayerEntity();
        playerEntity.setId(StringTools.generate());

        GameSessionEntity session = new GameSessionEntity();
        session.setId(sessionId.value());
        playerEntity.setSession(session);


        playerEntity.setUser(UserEntity.buildWithModelId(userId));

        playerEntity.setState(GamePlayer.State.ACTIVE);

        playerRepository.save(playerEntity);

        return new GamePlayer.Id(playerEntity.getId());
    }


}

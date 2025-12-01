package fr.plop.contexts.game.session.core.adapter;

import fr.plop.contexts.connect.domain.ConnectUser;
import fr.plop.contexts.connect.persistence.entity.ConnectionUserEntity;
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
import fr.plop.contexts.game.session.core.domain.usecase.GameSessionCreateUseCase;
import fr.plop.contexts.game.session.core.persistence.GamePlayerEntity;
import fr.plop.contexts.game.session.core.persistence.GamePlayerRepository;
import fr.plop.contexts.game.session.core.persistence.GameSessionEntity;
import fr.plop.contexts.game.session.core.persistence.GameSessionRepository;
import fr.plop.contexts.game.session.scenario.domain.model.ScenarioSessionPlayer;
import fr.plop.contexts.game.session.scenario.persistence.ScenarioGoalStepEntity;
import fr.plop.contexts.game.session.scenario.persistence.ScenarioGoalStepRepository;
import fr.plop.contexts.game.session.scenario.persistence.ScenarioGoalTargetEntity;
import fr.plop.contexts.game.session.scenario.persistence.ScenarioGoalTargetRepository;
import fr.plop.generic.tools.StringTools;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Component
public class GameSessionCreateAdapter implements GameSessionCreateUseCase.Port {
    private final TemplateRepository templateRepository;
    private final GameSessionRepository sessionRepository;
    private final GamePlayerRepository playerRepository;
    private final ScenarioGoalStepRepository scenarioGoalStepRepository;
    private final ScenarioGoalTargetRepository scenarioGoalTargetRepository;

    public GameSessionCreateAdapter(TemplateRepository templateRepository, GameSessionRepository sessionRepository, GamePlayerRepository playerRepository, ScenarioGoalStepRepository scenarioGoalStepRepository, ScenarioGoalTargetRepository scenarioGoalTargetRepository) {
        this.templateRepository = templateRepository;
        this.sessionRepository = sessionRepository;
        this.playerRepository = playerRepository;
        this.scenarioGoalStepRepository = scenarioGoalStepRepository;
        this.scenarioGoalTargetRepository = scenarioGoalTargetRepository;
    }

    @Override
    public Optional<GameSession.Atom> findCurrentGameSession(ConnectUser.Id userId) {
        return sessionRepository.findByUserId(userId.value()).stream()
                .filter(session -> session.getState() != GameSession.State.OVER)
                .map(entity -> new GameSession.Atom(new GameSession.Id(entity.getId()), entity.getLabel()))
                .findFirst();
    }

    @Override
    public Optional<Template> findTemplateByCode(Template.Code code) {
        List<TemplateEntity> templates = templateRepository.findByCodeFetchAll(code.value());
        if (templates.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(templates.getFirst().toModel());
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

        GameSession.Atom atom = new GameSession.Atom(new GameSession.Id(entity.getId()), template.label());
        return GameSession.buildWithoutPlayer(atom, state, template.scenario(), template.board(), template.map(),
                template.talk(), template.image());
    }

    @Override
    public GamePlayer.Id insert(GameSession.Id sessionId, ConnectUser.Id userId) {
        GamePlayerEntity playerEntity = new GamePlayerEntity();
        playerEntity.setId(StringTools.generate());

        GameSessionEntity session = new GameSessionEntity();
        session.setId(sessionId.value());
        playerEntity.setSession(session);

        ConnectionUserEntity user = new ConnectionUserEntity();
        user.setId(userId.value());
        playerEntity.setUser(user);

        playerEntity.setState(GamePlayer.State.ACTIVE);

        playerRepository.save(playerEntity);

        return new GamePlayer.Id(playerEntity.getId());
    }

    @Override
    public void insertScenarioSessionPlayer(GamePlayer.Id playerId, ScenarioSessionPlayer sessionPlayer) {
        sessionPlayer.bySteps().forEach((stepId, state) ->
                scenarioGoalStepRepository.save(ScenarioGoalStepEntity.build(playerId, stepId, state)));
        sessionPlayer.byTargets().forEach((targetId, state) ->
                scenarioGoalTargetRepository.save(ScenarioGoalTargetEntity.build(playerId, targetId, state)));
    }

}

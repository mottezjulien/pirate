package fr.plop.contexts.game.adapter;

import fr.plop.contexts.board.persistence.entity.BoardEntity;
import fr.plop.contexts.connect.domain.ConnectUser;
import fr.plop.contexts.connect.persistence.ConnectionUserEntity;
import fr.plop.contexts.game.domain.model.Game;
import fr.plop.contexts.game.domain.model.GamePlayer;
import fr.plop.contexts.game.domain.usecase.GameCreateUseCase;
import fr.plop.contexts.game.persistence.GameEntity;
import fr.plop.contexts.game.persistence.GamePlayerEntity;
import fr.plop.contexts.game.persistence.GamePlayerRepository;
import fr.plop.contexts.game.persistence.GameRepository;
import fr.plop.contexts.scenario.domain.model.Scenario;
import fr.plop.contexts.scenario.domain.model.ScenarioGoal;
import fr.plop.contexts.scenario.persistence.core.ScenarioEntity;
import fr.plop.contexts.scenario.persistence.core.ScenarioStepEntity;
import fr.plop.contexts.scenario.persistence.goal.ScenarioGoalEntity;
import fr.plop.contexts.scenario.persistence.goal.ScenarioGoalRepository;
import fr.plop.contexts.template.domain.model.Template;
import fr.plop.contexts.template.persistence.TemplateEntity;
import fr.plop.contexts.template.persistence.TemplateRepository;
import fr.plop.generic.tools.StringTools;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class GameCreateAdapter implements GameCreateUseCase.DataOutput {
    private final TemplateRepository templateRepository;
    private final GameRepository gameRepository;
    private final GamePlayerRepository gamePlayerRepository;
    private final ScenarioGoalRepository scenarioGoalRepository;

    public GameCreateAdapter(TemplateRepository templateRepository, GameRepository gameRepository, GamePlayerRepository gamePlayerRepository, ScenarioGoalRepository scenarioGoalRepository) {
        this.templateRepository = templateRepository;
        this.gameRepository = gameRepository;
        this.gamePlayerRepository = gamePlayerRepository;
        this.scenarioGoalRepository = scenarioGoalRepository;
    }


    @Override
    public Optional<Template> findTemplateByCode(Template.Code code) {
        List<TemplateEntity> templates = templateRepository.findByCodeFetchAll(code.value());
        if(templates.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(templates.getFirst().toModel());
    }

    @Override
    public Game create(Template template) {
        GameEntity entity = new GameEntity();
        entity.setState(Game.State.INIT);
        entity.setId(StringTools.generate());
        entity.setTemplateId(template.id().value());
        entity.setTemplateVersion(template.version());
        entity.setLabel(template.label());

        ScenarioEntity scenarioEntity = new ScenarioEntity();
        scenarioEntity.setId(template.scenario().id().value());
        entity.setScenario(scenarioEntity);

        BoardEntity boardEntity = new BoardEntity();
        boardEntity.setId(template.board().id().value());
        entity.setBoard(boardEntity);

        entity = gameRepository.save(entity);

        Game.Atom atom = new Game.Atom(new Game.Id(entity.getId()), template.label());
        return new Game(atom, Game.State.INIT, List.of(), template.scenario(), template.board());

    }

    @Override
    public GamePlayer.Id insert(Game.Id gameId, ConnectUser.Id userId) {
        GamePlayerEntity gamePlayerEntity = new GamePlayerEntity();
        gamePlayerEntity.setId(StringTools.generate());

        GameEntity game = new GameEntity();
        game.setId(gameId.value());
        gamePlayerEntity.setGame(game);

        ConnectionUserEntity user = new ConnectionUserEntity();
        user.setId(userId.value());
        gamePlayerEntity.setUser(user);

        gamePlayerRepository.save(gamePlayerEntity);

        return new GamePlayer.Id(gamePlayerEntity.getId());
    }

    @Override
    public void insertGoal(GamePlayer.Id playerId, Scenario.Step.Id id) {
        ScenarioGoalEntity entity = new ScenarioGoalEntity();
        entity.setId(StringTools.generate());
        entity.setState(ScenarioGoal.State.ACTIVE);

        GamePlayerEntity playerEntity = new GamePlayerEntity();
        playerEntity.setId(playerId.value());
        entity.setPlayer(playerEntity);

        ScenarioStepEntity step = new ScenarioStepEntity();
        step.setId(id.value());
        entity.setStep(step);



        scenarioGoalRepository.save(entity);
    }
}

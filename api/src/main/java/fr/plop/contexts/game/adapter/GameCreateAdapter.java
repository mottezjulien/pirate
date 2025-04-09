package fr.plop.contexts.game.adapter;

import fr.plop.contexts.board.persistence.entity.BoardEntity;
import fr.plop.contexts.connect.domain.ConnectUser;
import fr.plop.contexts.connect.persistence.ConnectionUserEntity;
import fr.plop.contexts.game.domain.model.Game;
import fr.plop.contexts.game.domain.usecase.GameCreateUseCase;
import fr.plop.contexts.game.persistence.GameEntity;
import fr.plop.contexts.game.persistence.GamePlayerEntity;
import fr.plop.contexts.game.persistence.GamePlayerRepository;
import fr.plop.contexts.game.persistence.GameRepository;
import fr.plop.contexts.scenario.persistence.core.ScenarioEntity;
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

    public GameCreateAdapter(TemplateRepository templateRepository, GameRepository gameRepository, GamePlayerRepository gamePlayerRepository) {
        this.templateRepository = templateRepository;
        this.gameRepository = gameRepository;
        this.gamePlayerRepository = gamePlayerRepository;
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
    public Game.Atom create(Template template) {
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

        return new Game.Atom(new Game.Id(entity.getId()), template.label());

    }

    @Override
    public void insert(Game.Id gameId, ConnectUser.Id userId) {
        GamePlayerEntity gamePlayerEntity = new GamePlayerEntity();
        gamePlayerEntity.setId(StringTools.generate());

        GameEntity game = new GameEntity();
        game.setId(gameId.value());
        gamePlayerEntity.setGame(game);

        ConnectionUserEntity user = new ConnectionUserEntity();
        user.setId(userId.value());
        gamePlayerEntity.setUser(user);

        gamePlayerRepository.save(gamePlayerEntity);
    }
}

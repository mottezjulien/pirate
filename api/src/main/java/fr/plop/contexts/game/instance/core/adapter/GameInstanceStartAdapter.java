package fr.plop.contexts.game.instance.core.adapter;

import fr.plop.contexts.game.instance.core.domain.model.GameInstance;
import fr.plop.contexts.game.instance.core.domain.model.GamePlayer;
import fr.plop.contexts.game.instance.core.domain.usecase.GameInstanceStartUseCase;
import fr.plop.contexts.game.instance.core.persistence.GameInstanceEntity;
import fr.plop.contexts.game.instance.core.persistence.GameInstanceRepository;
import fr.plop.contexts.game.instance.core.persistence.GamePlayerRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class GameInstanceStartAdapter implements GameInstanceStartUseCase.Port {

    private final GameInstanceRepository instanceRepository;

    private final GamePlayerRepository playerRepository;

    public GameInstanceStartAdapter(GameInstanceRepository instanceRepository, GamePlayerRepository playerRepository) {
        this.instanceRepository = instanceRepository;
        this.playerRepository = playerRepository;
    }


    @Override
    public Optional<GameInstance.Atom> find(GameInstance.Id id) {
        return instanceRepository.fullById(id.value()).map(GameInstanceEntity::toModelAtom);
    }

    @Override
    public void active(GameInstance.Id sessionId) {
        instanceRepository.findById(sessionId.value())
                .ifPresent(entity -> {
                    entity.setState(GameInstance.State.ACTIVE);
                    instanceRepository.save(entity);
                });
    }

    @Override
    public void active(GamePlayer.Id id) {
        playerRepository.findById(id.value())
                .ifPresent(entity -> {
                    entity.setState(GamePlayer.State.ACTIVE);
                    playerRepository.save(entity);
                });
    }

}

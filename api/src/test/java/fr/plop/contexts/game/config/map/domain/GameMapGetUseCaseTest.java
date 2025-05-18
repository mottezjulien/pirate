package fr.plop.contexts.game.config.map.domain;

import fr.plop.contexts.game.config.board.domain.model.BoardSpace;
import fr.plop.contexts.game.session.core.domain.model.GamePlayer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;
/*
public class GameMapGetUseCaseTest {

    private final InMemory inMemory = new InMemory();
    private final GameMapGetUseCase useCase = new GameMapGetUseCase(inMemory);
    private final GamePlayer player = mock(GamePlayer.class);
    private final GamePlayer.Id playerId = new GamePlayer.Id("myPlayerId");

    @BeforeEach
    void setUp() {
        inMemory.clear();
        reset(player);
        when(player.id()).thenReturn(playerId);
    }

    @Test
    public void test() {
        BoardSpace.Id spaceId1 = new BoardSpace.Id("spaceId1");
        when(player.spaceIds()).thenReturn(List.of(spaceId1));

        GameMap map = new GameMap();
        inMemory.put(spaceId1, map);

        List<GameMap> maps = useCase.apply(player);
        assertThat(maps).hasSize(1)
                .containsExactly(map);
    }

}

class InMemory implements GameMapGetUseCase.OutputPort {

    private final Map<BoardSpace.Id, GameMap> data = new HashMap<>();

    public void clear() {
        data.clear();
    }

    public void put(BoardSpace.Id spaceId, GameMap map) {
        data.put(spaceId, map);
    }

}*/
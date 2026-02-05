package fr.plop.contexts.game.instance.core.domain.usecase;

import fr.plop.contexts.game.config.board.domain.model.BoardSpace;
import fr.plop.contexts.game.instance.core.domain.GameInstanceException;
import fr.plop.contexts.game.instance.core.domain.model.GameInstance;
import fr.plop.contexts.game.instance.core.domain.model.GameInstanceContext;
import fr.plop.contexts.game.instance.core.domain.model.GamePlayer;
import fr.plop.contexts.game.instance.core.domain.port.GamePlayerGetPort;
import fr.plop.contexts.game.instance.event.domain.GameEvent;
import fr.plop.contexts.game.instance.event.domain.GameEventOrchestrator;
import fr.plop.contexts.game.instance.push.PushPort;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class GameInstanceMoveUseCaseTest {

    private final GameInstanceMoveUseCase.OutPort outPort = mock(GameInstanceMoveUseCase.OutPort.class);
    private final GamePlayerGetPort gamePlayerGetPort = mock(GamePlayerGetPort.class);
    private final GameEventOrchestrator eventOrchestrator = mock(GameEventOrchestrator.class);
    private final PushPort pushPort = mock(PushPort.class);
    private final GameInstanceMoveUseCase useCase = new GameInstanceMoveUseCase(outPort, gamePlayerGetPort, eventOrchestrator, pushPort);
    private final GameInstanceContext context = new GameInstanceContext(new GameInstance.Id(), new GamePlayer.Id());
    private final BoardSpace.Id spaceAId = new BoardSpace.Id("spaceA");
    private final BoardSpace.Id spaceBId = new BoardSpace.Id("spaceB");
    private final BoardSpace.Id spaceCId = new BoardSpace.Id("spaceC");
    private final BoardSpace.Id spaceDId = new BoardSpace.Id("spaceD");
    private final BoardSpace.Id spaceEId = new BoardSpace.Id("spaceE");

    @Test
    public void whenSameSpacesDoNothing() throws GameInstanceException {
        when(gamePlayerGetPort.findSpaceIdsByPlayerId(context.playerId()))
                .thenReturn(List.of(spaceAId, spaceBId));

        useCase.apply(context, List.of(spaceAId, spaceBId));

        verify(eventOrchestrator, never()).fire(any(), any());
        verify(outPort, never()).savePosition(any(), any());
    }

    @Test
    public void onGoInSpaceOnlyOne() throws GameInstanceException {
        when(gamePlayerGetPort.findSpaceIdsByPlayerId(context.playerId()))
                .thenReturn(List.of());

        useCase.apply(context, List.of(spaceAId));

        verify(eventOrchestrator).fire(context, new GameEvent.GoIn(spaceAId));
        verify(outPort).savePosition(context.playerId(), List.of(spaceAId));
    }


    @Test
    public void onGoOutSpaceOnlyOne() throws GameInstanceException {
        when(gamePlayerGetPort.findSpaceIdsByPlayerId(context.playerId()))
                .thenReturn(List.of(spaceAId));

        useCase.apply(context, List.of());

        verify(eventOrchestrator).fire(context, new GameEvent.GoOut(spaceAId));
        verify(outPort).savePosition(context.playerId(), List.of());
    }

    @Test
    public void onGoInAndOutSpaceMultiple() throws GameInstanceException {
        when(gamePlayerGetPort.findSpaceIdsByPlayerId(context.playerId()))
                .thenReturn(List.of(spaceAId, spaceBId, spaceCId));

        useCase.apply(context, List.of(spaceCId, spaceDId, spaceEId));

        verify(eventOrchestrator).fire(context, new GameEvent.GoOut(spaceAId));
        verify(eventOrchestrator).fire(context, new GameEvent.GoOut(spaceBId));
        verify(eventOrchestrator).fire(context, new GameEvent.GoIn(spaceDId));
        verify(eventOrchestrator).fire(context, new GameEvent.GoIn(spaceEId));

        verify(outPort).savePosition(context.playerId(), List.of(spaceCId, spaceDId, spaceEId));
    }

}

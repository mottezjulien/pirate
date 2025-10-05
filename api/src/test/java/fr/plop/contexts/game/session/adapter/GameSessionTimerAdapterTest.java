package fr.plop.contexts.game.session.adapter;

public class GameSessionTimerAdapterTest {

    /*
    private final GameEventBroadCast broadCast = mock(GameEventBroadCast.class);
    private final GameTimeTickUseCase.Port port = mock(GameTimeTickUseCase.Port.class);
    private final GameTimeTickUseCase useCase = new GameTimeTickUseCase(broadCast, port);

    private final GameSession.Id sessionId = new GameSession.Id();
    private final GamePlayer.Id p1 = new GamePlayer.Id();
    private final GamePlayer.Id p2 = new GamePlayer.Id();

    @Test
    public void tick_should_fire_TimeClick_for_all_active_players() {
        // Given
        GameSessionTimeUnit time = GameSessionTimeUnit.ofMinutes(42);
        when(port.activePlayers(sessionId)).thenReturn(List.of(p1, p2));

        // When
        useCase.tick(sessionId, time);

        // Then
        verify(broadCast).fire(new GameEvent.TimeClick(time), new GameEventContext(sessionId, p1));
        verify(broadCast).fire(new GameEvent.TimeClick(time), new GameEventContext(sessionId, p2));
        verifyNoMoreInteractions(broadCast);
    }
    */
}
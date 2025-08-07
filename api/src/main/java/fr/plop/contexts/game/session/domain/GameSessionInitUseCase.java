package fr.plop.contexts.game.session.domain;

public class GameSessionInitUseCase {

    public interface OutPort {
        void deleteAll();
    }

    private final OutPort outPort;

    public GameSessionInitUseCase(OutPort outPort) {
        this.outPort = outPort;
    }

    public void clean() {
        outPort.deleteAll();
    }
}
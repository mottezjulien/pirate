package fr.plop.contexts.game.session.push;

public interface PushPort {

    void push(PushEvent event) throws PushException;
}

package fr.plop.contexts.game.config.consequence.handler;


import fr.plop.contexts.game.config.consequence.Consequence;
import fr.plop.contexts.game.instance.core.domain.model.GameInstanceContext;
import fr.plop.contexts.game.instance.core.domain.usecase.GameOverUseCase;
import org.springframework.stereotype.Component;

@Component
public class ConsequenceOverHandler implements ConsequenceHandler {

    private final GameOverUseCase gameOverUseCase;

    public ConsequenceOverHandler(GameOverUseCase gameOverUseCase) {
        this.gameOverUseCase = gameOverUseCase;
    }

    @Override
    public boolean supports(Consequence consequence) {
        return consequence instanceof Consequence.SessionEnd;
    }

    @Override
    public void handle(GameInstanceContext context, Consequence consequence) {
        if(consequence instanceof Consequence.SessionEnd sessionEnd){
            gameOverUseCase.apply(context, sessionEnd.gameOver());
        }
    }
}

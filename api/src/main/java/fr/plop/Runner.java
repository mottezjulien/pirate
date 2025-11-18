package fr.plop;

import fr.plop.contexts.game.config.template.domain.usecase.TemplateInitUseCase;
import fr.plop.contexts.game.session.domain.GameSessionInitUseCase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;

@SpringBootApplication
public class Runner {

    public static void main(String[] args) {
        SpringApplication.run(Runner.class, args);
    }

   @Autowired
    private TemplateInitUseCase.OutPort templateOutPort;

    @Autowired
    private GameSessionInitUseCase.OutPort gameOutPort;

    @EventListener(ApplicationReadyEvent.class)
    public void afterStartup() {
        GameSessionInitUseCase gameInitUseCase = new GameSessionInitUseCase(gameOutPort);
        gameInitUseCase.clean();
        TemplateInitUseCase templateInitUseCase = new TemplateInitUseCase(templateOutPort);
        templateInitUseCase.apply();
    }

}

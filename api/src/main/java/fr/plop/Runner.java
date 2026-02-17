package fr.plop;

import fr.plop.contexts.game.config.template.domain.usecase.TemplateInitUseCase;
import fr.plop.contexts.game.instance.core.domain.port.GameInstanceClearPort;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class Runner {

    public static void main(String[] args) {
        SpringApplication.run(Runner.class, args);
    }

   @Autowired
    private TemplateInitUseCase.OutPort templateOutPort;

    @Autowired
    private GameInstanceClearPort clear;

    @EventListener(ApplicationReadyEvent.class)
    public void afterStartup() {
        clear.clearAll();
        TemplateInitUseCase templateInitUseCase = new TemplateInitUseCase(templateOutPort);
        templateInitUseCase.apply();
    }

}

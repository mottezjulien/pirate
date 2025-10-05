package fr.plop.contexts.game.session.adapter;

import fr.plop.contexts.connect.domain.ConnectAuth;
import fr.plop.contexts.connect.domain.ConnectException;
import fr.plop.contexts.connect.domain.ConnectUseCase;
import fr.plop.contexts.connect.domain.ConnectUser;
import fr.plop.contexts.connect.domain.ConnectionCreateAuthUseCase;
import fr.plop.contexts.game.config.consequence.Consequence;
import fr.plop.contexts.game.config.scenario.domain.model.Possibility;
import fr.plop.contexts.game.config.scenario.domain.model.PossibilityTrigger;
import fr.plop.contexts.game.config.scenario.domain.model.ScenarioConfig;
import fr.plop.contexts.game.config.template.domain.TemplateInitUseCase;
import fr.plop.contexts.game.config.template.domain.model.Template;
import fr.plop.contexts.game.session.core.domain.GameException;
import fr.plop.contexts.game.session.core.domain.model.GamePlayer;
import fr.plop.contexts.game.session.core.domain.model.GameSession;
import fr.plop.contexts.game.session.core.domain.usecase.GameSessionCreateUseCase;
import fr.plop.contexts.game.session.core.persistence.GamePlayerRepository;
import fr.plop.contexts.game.session.event.domain.GameEventBroadCast;
import fr.plop.contexts.game.session.scenario.adapter.GameEventScenarioAdapter;
import fr.plop.contexts.game.session.scenario.domain.model.ScenarioGoal;
import fr.plop.contexts.game.session.time.GameSessionTimeUnit;
import fr.plop.contexts.game.session.time.GameSessionTimer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.Duration;
import java.util.List;

import static org.awaitility.Awaitility.await;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@SpringBootTest
@TestPropertySource(properties = "spring.main.allow-bean-definition-overriding=true")
public class GameSessionTimerAdapterIntegrationTest {

    @TestConfiguration
    static class Config {
        @Bean
        @Primary
        public GameSessionTimer gameSessionTimer(GamePlayerRepository gamePlayerRepository, GameEventBroadCast broadCast) {
            return new GameSessionTimerAdapter(gamePlayerRepository, broadCast, Duration.ofSeconds(1));
        }
    }

    @Autowired
    private ConnectUseCase connectUseCase;

    @Autowired
    private GameSessionCreateUseCase createGameUseCase;

    @Autowired
    private TemplateInitUseCase.OutPort templateInitUseCase;

    @Autowired
    private ConnectionCreateAuthUseCase createAuthUseCase;

    @MockitoBean
    private GameEventScenarioAdapter scenarioAdapter;

    @Test
    public void fireTimeClickEvent() throws GameException, ConnectException {

        Template.Code code = new Template.Code("absolute-time");

        Template template = template(code);

        templateInitUseCase.create(template);

        ConnectAuth auth = createAuthUseCase.byDeviceId("anyDeviceId");
        GameSession.Atom session = createGameUseCase.apply(code, auth.connect().user().id());

        ConnectUser connectUser = connectUseCase.findUserIdBySessionIdAndRawToken(session.id(), auth.token());
        GamePlayer player = connectUser.player().orElseThrow();

        Consequence.ScenarioStep consequence1 = (Consequence.ScenarioStep) template.scenario().steps().getFirst().possibilities().getFirst().consequences().getFirst();
        Consequence.ScenarioStep consequence2 = (Consequence.ScenarioStep) template.scenario().steps().getFirst().possibilities().get(1).consequences().getFirst();

        verify(scenarioAdapter, never()).updateStateOrCreateGoal(player.id(), consequence1);
        verify(scenarioAdapter, never()).updateStateOrCreateGoal(player.id(), consequence2);

        await().pollDelay(Duration.ofSeconds(2)).until(() -> {
            verify(scenarioAdapter).updateStateOrCreateGoal(player.id(), consequence1);
            verify(scenarioAdapter, never()).updateStateOrCreateGoal(player.id(), consequence2);
            return true;
        });

        await().pollDelay(Duration.ofSeconds(4)).until(() -> {
            verify(scenarioAdapter).updateStateOrCreateGoal(player.id(), consequence1);
            verify(scenarioAdapter).updateStateOrCreateGoal(player.id(), consequence2);
            return true;
        });

    }


    private Template template(Template.Code code) {
        ScenarioConfig.Step step = new ScenarioConfig.Step(List.of(), List.of(possibility(new GameSessionTimeUnit(1)), possibility(new GameSessionTimeUnit(3))));
        ScenarioConfig scenario = new ScenarioConfig(List.of(step));
        return new Template(code, scenario);
    }

    private static Possibility possibility(GameSessionTimeUnit value) {
        PossibilityTrigger.AbsoluteTime trigger = new PossibilityTrigger.AbsoluteTime(value);
        Consequence.ScenarioStep consequence = new Consequence.ScenarioStep(new ScenarioConfig.Step.Id(), ScenarioGoal.State.ACTIVE);
        return new Possibility(trigger, consequence);
    }

}
package fr.plop.contexts.game.session.adapter;

import fr.plop.contexts.connect.domain.*;
import fr.plop.contexts.game.config.consequence.Consequence;
import fr.plop.contexts.game.config.scenario.domain.model.Possibility;
import fr.plop.contexts.game.config.scenario.domain.model.PossibilityTrigger;
import fr.plop.contexts.game.config.scenario.domain.model.ScenarioConfig;
import fr.plop.contexts.game.config.template.domain.model.Template;
import fr.plop.contexts.game.config.template.domain.usecase.TemplateInitUseCase;
import fr.plop.contexts.game.session.core.domain.GameException;
import fr.plop.contexts.game.session.core.domain.model.GamePlayer;
import fr.plop.contexts.game.session.core.domain.model.GameSession;
import fr.plop.contexts.game.session.core.domain.usecase.GameSessionCreateUseCase;
import fr.plop.contexts.game.session.core.domain.usecase.GameSessionStartUseCase;
import fr.plop.contexts.game.session.event.adapter.action.GameEventActionScenarioAdapter;
import fr.plop.contexts.game.session.scenario.domain.model.ScenarioSessionState;
import fr.plop.contexts.game.session.time.GameSessionTimeUnit;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.Duration;
import java.util.List;

import static org.awaitility.Awaitility.await;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@SpringBootTest(properties = {"game.session.timer.duration=1000"})
public class GameSessionTimerAdapterIntegrationTest {

    @Autowired
    private ConnectUseCase connectUseCase;

    @Autowired
    private GameSessionCreateUseCase createGameUseCase;

    @Autowired
    private GameSessionStartUseCase startUseCase;

    @Autowired
    private TemplateInitUseCase.OutPort templateInitUseCase;

    @Autowired
    private ConnectionCreateAuthUseCase createAuthUseCase;

    @MockitoBean
    private GameEventActionScenarioAdapter scenarioAdapter;

    @Test
    public void fireTimeClickEvent() throws GameException, ConnectException {

        Template.Code code = new Template.Code("absolute-time");

        Template template = template(code);

        templateInitUseCase.create(template);

        ConnectAuth auth = createAuthUseCase.byDeviceId("anyDeviceId");
        GameSession.Atom session = createGameUseCase.apply(code, auth.connect().user().id());

        ConnectUser connectUser = connectUseCase.findUserIdBySessionIdAndRawToken(session.id(), auth.token());
        GamePlayer.Id playerId = connectUser.playerId().orElseThrow();

        session = startUseCase.apply(session.id(), playerId);

        List<Possibility> possibilities = template.scenario().steps().getFirst().possibilities();
        Consequence.ScenarioStep consequence1 = (Consequence.ScenarioStep) possibilities.getFirst().consequences().getFirst();
        Consequence.ScenarioStep consequence2 = (Consequence.ScenarioStep) possibilities.get(1).consequences().getFirst();

        verify(scenarioAdapter, never()).updateStateOrCreateGoalStep(playerId, consequence1);
        verify(scenarioAdapter, never()).updateStateOrCreateGoalStep(playerId, consequence2);

        await().pollDelay(Duration.ofSeconds(2)).until(() -> {
            verify(scenarioAdapter).updateStateOrCreateGoalStep(playerId, consequence1);
            verify(scenarioAdapter, never()).updateStateOrCreateGoalStep(playerId, consequence2);
            return true;
        });

        await().pollDelay(Duration.ofSeconds(4)).until(() -> {
            verify(scenarioAdapter).updateStateOrCreateGoalStep(playerId, consequence1);
            verify(scenarioAdapter).updateStateOrCreateGoalStep(playerId, consequence2);
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
        Consequence.ScenarioStep consequence = new Consequence.ScenarioStep(new ScenarioConfig.Step.Id(), ScenarioSessionState.ACTIVE);
        return new Possibility(trigger, List.of(consequence));
    }

}
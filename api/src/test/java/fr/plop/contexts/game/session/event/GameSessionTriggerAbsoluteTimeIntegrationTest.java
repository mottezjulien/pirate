package fr.plop.contexts.game.session.event;

import fr.plop.contexts.connect.domain.ConnectAuth;
import fr.plop.contexts.connect.domain.ConnectException;
import fr.plop.contexts.connect.domain.ConnectUseCase;
import fr.plop.contexts.connect.domain.ConnectUser;
import fr.plop.contexts.connect.domain.ConnectionCreateAuthUseCase;
import fr.plop.contexts.game.config.scenario.domain.model.Possibility;
import fr.plop.contexts.game.config.scenario.domain.model.PossibilityConsequence;
import fr.plop.contexts.game.config.scenario.domain.model.PossibilityRecurrence;
import fr.plop.contexts.game.config.scenario.domain.model.PossibilityTrigger;
import fr.plop.contexts.game.config.scenario.domain.model.ScenarioConfig;
import fr.plop.contexts.game.config.template.domain.TemplateInitUseCase;
import fr.plop.contexts.game.config.template.domain.model.Template;
import fr.plop.contexts.game.session.core.domain.GameException;
import fr.plop.contexts.game.session.core.domain.model.GamePlayer;
import fr.plop.contexts.game.session.core.domain.model.GameSession;
import fr.plop.contexts.game.session.core.domain.usecase.GameSessionCreateUseCase;
import fr.plop.contexts.game.session.scenario.adapter.GameEventScenarioAdapter;
import fr.plop.contexts.game.session.scenario.domain.model.ScenarioGoal;
import fr.plop.contexts.game.session.time.TimeUnit;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.Duration;
import java.util.List;

import static org.awaitility.Awaitility.await;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;


@SpringBootTest
@TestPropertySource(properties = {"game.session.timer.cron=*/1 * * * * *"})
public class GameSessionTriggerAbsoluteTimeIntegrationTest {

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
    public void fireClickEvent() throws GameException, ConnectException {

        Template.Code code = new Template.Code("absolute-time");

        PossibilityTrigger.AbsoluteTime trigger1 = new PossibilityTrigger.AbsoluteTime(new PossibilityTrigger.Id(), new TimeUnit(1));
        PossibilityConsequence.Goal consequence1 = new PossibilityConsequence.Goal(new PossibilityConsequence.Id(), new ScenarioConfig.Step.Id(), ScenarioGoal.State.ACTIVE);
        Possibility possibility1 = new Possibility(new PossibilityRecurrence.Always(), trigger1, List.of(), List.of(consequence1));

        PossibilityTrigger.AbsoluteTime trigger2 = new PossibilityTrigger.AbsoluteTime(new PossibilityTrigger.Id(), new TimeUnit(3));
        PossibilityConsequence.Goal consequence2 = new PossibilityConsequence.Goal(new PossibilityConsequence.Id(), new ScenarioConfig.Step.Id(), ScenarioGoal.State.ACTIVE);
        Possibility possibility2 = new Possibility(new PossibilityRecurrence.Always(), trigger2, List.of(), List.of(consequence2));

        ScenarioConfig.Step step1 = new ScenarioConfig.Step(List.of(), List.of(possibility1, possibility2));

        List<ScenarioConfig.Step> steps = List.of(step1);
        ScenarioConfig scenario = new ScenarioConfig("scenario absolute time", steps);
        Template template = new Template(code, scenario);

        templateInitUseCase.create(template);

        ConnectAuth auth = createAuthUseCase.byDeviceId("anyDeviceId");
        GameSession.Atom session = createGameUseCase.apply(code, auth.connect().user().id());

        ConnectUser connectUser = connectUseCase.findUserIdBySessionIdAndRawToken(session.id(), auth.token());
        GamePlayer player = connectUser.player().orElseThrow();

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

}

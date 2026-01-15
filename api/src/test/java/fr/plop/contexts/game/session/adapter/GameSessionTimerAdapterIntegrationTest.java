package fr.plop.contexts.game.session.adapter;

import fr.plop.contexts.connect.domain.ConnectAuthGameSession;
import fr.plop.contexts.connect.domain.ConnectAuthUser;
import fr.plop.contexts.connect.domain.ConnectException;
import fr.plop.contexts.connect.usecase.ConnectAuthGameSessionUseCase;
import fr.plop.contexts.connect.usecase.ConnectAuthUserCreateUseCase;
import fr.plop.contexts.game.config.consequence.Consequence;
import fr.plop.contexts.game.config.scenario.domain.model.Possibility;
import fr.plop.contexts.game.config.scenario.domain.model.PossibilityTrigger;
import fr.plop.contexts.game.config.scenario.domain.model.ScenarioConfig;
import fr.plop.contexts.game.config.template.domain.model.Template;
import fr.plop.contexts.game.config.template.domain.usecase.TemplateInitUseCase;
import fr.plop.contexts.game.session.core.domain.GameException;
import fr.plop.contexts.game.session.core.domain.model.GameSessionContext;
import fr.plop.contexts.game.session.core.domain.usecase.GameSessionUseCase;
import fr.plop.contexts.game.session.core.domain.usecase.GameSessionStartUseCase;
import fr.plop.contexts.game.session.scenario.adapter.GameSessionScenarioGoalAdapter;
import fr.plop.contexts.game.session.scenario.domain.model.ScenarioSessionState;
import fr.plop.contexts.game.session.time.GameSessionTimeUnit;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.Duration;
import java.util.AbstractMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@SpringBootTest(properties = {"game.session.timer.duration=1000"})
public class GameSessionTimerAdapterIntegrationTest {

    @Autowired
    private GameSessionUseCase createGameUseCase;

    @Autowired
    private GameSessionStartUseCase startUseCase;

    @Autowired
    private TemplateInitUseCase.OutPort templateInitUseCase;

    @Autowired
    private ConnectAuthUserCreateUseCase createAuthUseCase;

    @Autowired
    private ConnectAuthGameSessionUseCase authGameSessionUseCase;

    @Autowired
    private GameSessionScenarioGoalAdapter scenarioAdapter;

    @Test
    public void fireTimeClickEvent() throws GameException, ConnectException {

        Template template = template();
        templateInitUseCase.create(template);

        ConnectAuthUser authUser = createAuthUseCase.byDeviceId("anyDeviceId");
        GameSessionContext context = createGameUseCase.create(template.id(), authUser.userId());
        ConnectAuthGameSession authSession = authGameSessionUseCase.create(authUser, context);

        ScenarioConfig.Step.Id step0 = template.scenario().steps().getFirst().id();
        ScenarioConfig.Step.Id step1 = template.scenario().steps().get(1).id();

        startUseCase.apply(authSession);

        assertThat(scenarioAdapter.findSteps(context.playerId()))
                .containsOnly(new AbstractMap.SimpleEntry<>(step0, ScenarioSessionState.ACTIVE)); //DEFAULT

        await().pollDelay(Duration.ofSeconds(2)).until(() -> {
            Map<ScenarioConfig.Step.Id, ScenarioSessionState> steps = scenarioAdapter.findSteps(context.playerId());
            assertThat(steps)
                    .containsOnly(new AbstractMap.SimpleEntry<>(step0, ScenarioSessionState.ACTIVE),
                            new AbstractMap.SimpleEntry<>(step1, ScenarioSessionState.ACTIVE));
            return true;
        });

        await().pollDelay(Duration.ofSeconds(4)).until(() -> {
            Map<ScenarioConfig.Step.Id, ScenarioSessionState> steps = scenarioAdapter.findSteps(context.playerId());
            assertThat(steps)
                    .containsOnly(new AbstractMap.SimpleEntry<>(step0, ScenarioSessionState.FAILURE), new AbstractMap.SimpleEntry<>(step1, ScenarioSessionState.ACTIVE));
            return true;
        });

    }


    private Template template() {
        ScenarioConfig.Step.Id stepId0 = new ScenarioConfig.Step.Id();
        ScenarioConfig.Step.Id stepId1 = new ScenarioConfig.Step.Id();

        PossibilityTrigger.AbsoluteTime trigger0 = new PossibilityTrigger.AbsoluteTime(new GameSessionTimeUnit(1));
        Consequence.ScenarioStep consequence0 = new Consequence.ScenarioStep(stepId1, ScenarioSessionState.ACTIVE);
        Possibility possibility0 = new Possibility(trigger0, List.of(consequence0));

        PossibilityTrigger.AbsoluteTime trigger1 = new PossibilityTrigger.AbsoluteTime(new GameSessionTimeUnit(3));
        Consequence.ScenarioStep consequence1 = new Consequence.ScenarioStep(stepId0, ScenarioSessionState.FAILURE);
        Possibility possibility1 = new Possibility(trigger1, List.of(consequence1));

        ScenarioConfig.Step step0 = new ScenarioConfig.Step(stepId0, List.of(possibility0, possibility1));
        ScenarioConfig.Step step1 = new ScenarioConfig.Step(stepId1, List.of());
        ScenarioConfig scenario = new ScenarioConfig(List.of(step0, step1));

        return Template.builder().code(new Template.Code("absolute-time")).scenario(scenario).build();
    }


}
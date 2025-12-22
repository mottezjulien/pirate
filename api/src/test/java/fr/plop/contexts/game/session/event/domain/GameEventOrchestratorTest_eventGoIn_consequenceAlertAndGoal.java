package fr.plop.contexts.game.session.event.domain;


import fr.plop.contexts.connect.domain.ConnectAuthGameSession;
import fr.plop.contexts.connect.domain.ConnectAuthUser;
import fr.plop.contexts.connect.domain.ConnectException;
import fr.plop.contexts.connect.usecase.ConnectAuthGameSessionUseCase;
import fr.plop.contexts.connect.usecase.ConnectAuthUserCreateUseCase;
import fr.plop.contexts.game.config.board.domain.model.BoardConfig;
import fr.plop.contexts.game.config.board.domain.model.BoardSpace;
import fr.plop.contexts.game.config.consequence.Consequence;
import fr.plop.contexts.game.config.scenario.domain.model.Possibility;
import fr.plop.contexts.game.config.scenario.domain.model.PossibilityTrigger;
import fr.plop.contexts.game.config.scenario.domain.model.ScenarioConfig;
import fr.plop.contexts.game.config.template.domain.model.Template;
import fr.plop.contexts.game.config.template.domain.usecase.TemplateInitUseCase;
import fr.plop.contexts.game.session.core.domain.GameException;
import fr.plop.contexts.game.session.core.domain.model.GameSessionContext;
import fr.plop.contexts.game.session.core.domain.port.GameSessionClearPort;
import fr.plop.contexts.game.session.core.domain.usecase.GameMoveUseCase;
import fr.plop.contexts.game.session.core.domain.usecase.GameSessionCreateUseCase;
import fr.plop.contexts.game.session.core.domain.usecase.GameSessionStartUseCase;
import fr.plop.contexts.game.session.scenario.domain.model.ScenarioSessionState;
import fr.plop.contexts.game.session.situation.domain.port.GameSessionSituationGetPort;
import fr.plop.generic.position.Point;
import fr.plop.generic.position.Rect;
import fr.plop.subs.i18n.domain.I18n;
import fr.plop.subs.i18n.domain.Language;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class GameEventOrchestratorTest_eventGoIn_consequenceAlertAndGoal {

    private static final String CODE = "eventGoIn_consequenceAlertAndGoal";

    @Autowired
    private GameEventOrchestrator eventOrchestrator;

    @Autowired
    private GameSessionClearPort sessionClear;

    @Autowired
    private TemplateInitUseCase.OutPort templatePort;

    @Autowired
    private ConnectAuthUserCreateUseCase createAuthUseCase;

    @Autowired
    private GameSessionCreateUseCase createUseCase;

    @Autowired
    private ConnectAuthGameSessionUseCase authGameSessionUseCase;

    @Autowired
    private GameSessionStartUseCase startUseCase;

    @Autowired
    private GameMoveUseCase moveUseCase;

    @Autowired
    private GameSessionSituationGetPort situationPort;

    private final ScenarioConfig.Step.Id stepId = new ScenarioConfig.Step.Id();

    @BeforeEach
    void setUp() {
        sessionClear.clearAll();
        templatePort.deleteAll();
        createTemplate();
    }

    private void createTemplate() {
        BoardSpace.Id spaceId = new BoardSpace.Id();
        BoardSpace space = new BoardSpace(spaceId, List.of(new Rect(new Point(0, 0), new Point(10, 10))));

        I18n i18nMessage = new I18n(Map.of(Language.FR, "Vous êtes piégé"));
        Consequence.DisplayMessage message = new Consequence.DisplayMessage(new Consequence.Id(), i18nMessage);
        Consequence.ScenarioStep goal = new Consequence.ScenarioStep(new Consequence.Id(), stepId, ScenarioSessionState.FAILURE);
        List<Consequence> consequences = List.of(message, goal);

        PossibilityTrigger.SpaceGoIn trigger = new PossibilityTrigger.SpaceGoIn(new PossibilityTrigger.Id(), spaceId);
        Possibility possibility = new Possibility(trigger, consequences);
        ScenarioConfig.Step step = new ScenarioConfig.Step(stepId, List.of(possibility));
        List<ScenarioConfig.Step> steps = List.of(step);


        Template template = new Template(new Template.Code(CODE), new ScenarioConfig(steps), new BoardConfig(List.of(space)));
        templatePort.create(template);
    }


    @Test
    public void _default() throws GameException, ConnectException {
        GameSessionContext context = start();
        assertThat(situationPort.get(context).scenario().stepIds()).containsOnly(stepId); //Default
    }
    @Test
    public void moveInTrap() throws GameException, InterruptedException, ConnectException {
        GameSessionContext context = start();
        Point position = new Point(5, 5);
        moveUseCase.apply(context, position);
        Thread.sleep(100);

        assertThat(situationPort.get(context).scenario().stepIds()).isEmpty();
    }

    @Test
    public void moveOtherSpace_doNothing() throws GameException, InterruptedException, ConnectException {
        GameSessionContext context = start();
        Point position = new Point(15, 25);
        moveUseCase.apply(context, position);
        Thread.sleep(100);

        assertThat(situationPort.get(context).scenario().stepIds()).containsOnly(stepId);
    }

    private GameSessionContext start() throws GameException, ConnectException {
        final ConnectAuthUser auth = createAuthUseCase.byDeviceId("anyDeviceId");
        final GameSessionContext context = createUseCase.apply(new Template.Code(CODE), auth.userId());
        final ConnectAuthGameSession authGameSession = authGameSessionUseCase.create(auth, context);
        startUseCase.apply(authGameSession);
        return context;
    }

}

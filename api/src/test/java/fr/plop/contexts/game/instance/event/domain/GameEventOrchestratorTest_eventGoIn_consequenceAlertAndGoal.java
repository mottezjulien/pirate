package fr.plop.contexts.game.instance.event.domain;


import fr.plop.contexts.connect.domain.ConnectAuthGameInstance;
import fr.plop.contexts.connect.domain.ConnectAuthUser;
import fr.plop.contexts.connect.domain.ConnectException;
import fr.plop.contexts.connect.usecase.ConnectAuthGameInstanceUseCase;
import fr.plop.contexts.connect.usecase.ConnectAuthUserCreateUseCase;
import fr.plop.contexts.game.commun.domain.Game;
import fr.plop.contexts.game.commun.domain.GameProject;
import fr.plop.contexts.game.config.board.domain.model.BoardConfig;
import fr.plop.contexts.game.config.board.domain.model.BoardSpace;
import fr.plop.contexts.game.config.consequence.Consequence;
import fr.plop.contexts.game.config.scenario.domain.model.Possibility;
import fr.plop.contexts.game.config.scenario.domain.model.PossibilityTrigger;
import fr.plop.contexts.game.config.scenario.domain.model.ScenarioConfig;
import fr.plop.contexts.game.config.template.domain.model.Template;
import fr.plop.contexts.game.config.template.domain.usecase.TemplateInitUseCase;
import fr.plop.contexts.game.instance.core.domain.GameInstanceException;
import fr.plop.contexts.game.instance.core.domain.model.GameInstance;
import fr.plop.contexts.game.instance.core.domain.model.GameInstanceContext;
import fr.plop.contexts.game.instance.core.domain.port.GameInstanceClearPort;
import fr.plop.contexts.game.instance.core.domain.usecase.GameInstanceMoveUseCase;
import fr.plop.contexts.game.instance.core.domain.usecase.GameInstanceUseCase;
import fr.plop.contexts.game.instance.core.domain.usecase.GameInstanceStartUseCase;
import fr.plop.contexts.game.instance.scenario.domain.model.ScenarioSessionState;
import fr.plop.contexts.game.instance.situation.domain.port.GameInstanceSituationGetPort;
import fr.plop.generic.position.Point;
import fr.plop.generic.position.Rectangle;
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


    @Autowired
    private GameEventOrchestrator eventOrchestrator;

    @Autowired
    private GameInstanceClearPort sessionClear;

    @Autowired
    private TemplateInitUseCase.OutPort templatePort;

    @Autowired
    private ConnectAuthUserCreateUseCase createAuthUseCase;

    @Autowired
    private GameInstanceUseCase createUseCase;

    @Autowired
    private ConnectAuthGameInstanceUseCase authGameUseCase;

    @Autowired
    private GameInstanceStartUseCase startUseCase;

    @Autowired
    private GameInstanceMoveUseCase moveUseCase;

    @Autowired
    private GameInstanceSituationGetPort situationPort;

    private final Template.Id templateId = new Template.Id();

    private final ScenarioConfig.Step.Id stepId = new ScenarioConfig.Step.Id();


    @BeforeEach
    void setUp() {
        sessionClear.clearAll();
        templatePort.deleteAll();
        createTemplate();
    }

    private void createTemplate() {
        BoardSpace.Id spaceId = new BoardSpace.Id();
        BoardSpace space = new BoardSpace(spaceId, List.of(new Rectangle(Point.from(0, 0), Point.from(10, 10))));

        I18n i18nMessage = new I18n(Map.of(Language.FR, "Vous êtes piégé"));
        Consequence.DisplayAlert message = new Consequence.DisplayAlert(new Consequence.Id(), i18nMessage);
        Consequence.ScenarioStep goal = new Consequence.ScenarioStep(new Consequence.Id(), stepId, ScenarioSessionState.FAILURE);
        List<Consequence> consequences = List.of(message, goal);

        PossibilityTrigger.SpaceGoIn trigger = new PossibilityTrigger.SpaceGoIn(new PossibilityTrigger.Id(), spaceId);
        Possibility possibility = new Possibility(trigger, consequences);
        ScenarioConfig.Step step = new ScenarioConfig.Step(stepId, List.of(possibility));
        List<ScenarioConfig.Step> steps = List.of(step);

        Template template = Template.builder()
                .id(templateId)
                .scenario(new ScenarioConfig(steps))
                .board(new BoardConfig(List.of(space)))
                .build();
        Game.Id gameId = templatePort.findOrCreateGame(new GameProject.Code("test-goin"), new Game.Version("1.0.0"));
        templatePort.createOrUpdate(gameId, template);
    }


    @Test
    public void _default() throws GameInstanceException, ConnectException {
        GameInstanceContext context = start();
        assertThat(situationPort.get(context).scenario().stepIds()).containsOnly(stepId); //Default
    }
    @Test
    public void moveInTrap() throws GameInstanceException, InterruptedException, ConnectException {
        GameInstanceContext context = start();
        Point position = Point.from(5, 5);
        moveUseCase.apply(context, position);
        Thread.sleep(100);

        assertThat(situationPort.get(context).scenario().stepIds()).isEmpty();
    }

    @Test
    public void moveOtherSpace_doNothing() throws GameInstanceException, InterruptedException, ConnectException {
        GameInstanceContext context = start();
        Point position = Point.from(15, 25);
        moveUseCase.apply(context, position);
        Thread.sleep(100);

        assertThat(situationPort.get(context).scenario().stepIds()).containsOnly(stepId);
    }

    private GameInstanceContext start() throws GameInstanceException, ConnectException {
        final ConnectAuthUser auth = createAuthUseCase.byDeviceId("anyDeviceId");
        final GameInstance.Atom atom = createUseCase.create(templateId, auth.userId());
        GameInstanceContext context = atom.byUserId(auth.userId()).orElseThrow();
        final ConnectAuthGameInstance authGameInstance = authGameUseCase.create(auth, context);
        startUseCase.apply(authGameInstance);
        return context;
    }

}

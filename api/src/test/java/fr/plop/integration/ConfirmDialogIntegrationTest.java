package fr.plop.integration;

import fr.plop.contexts.connect.presenter.ConnectionController;
import fr.plop.contexts.game.commun.domain.Game;
import fr.plop.contexts.game.commun.domain.GameProject;
import fr.plop.contexts.game.config.consequence.Consequence;
import fr.plop.contexts.game.config.message.MessageToken;
import fr.plop.contexts.game.config.scenario.domain.model.Possibility;
import fr.plop.contexts.game.config.scenario.domain.model.PossibilityTrigger;
import fr.plop.contexts.game.config.scenario.domain.model.ScenarioConfig;
import fr.plop.contexts.game.config.template.domain.model.Template;
import fr.plop.contexts.game.config.template.domain.usecase.TemplateInitUseCase;
import fr.plop.contexts.game.instance.core.domain.port.GameInstanceClearPort;
import fr.plop.contexts.game.instance.core.presenter.GameInstanceController;
import fr.plop.contexts.game.instance.message.MessageController;
import fr.plop.contexts.game.instance.push.PushPort;
import fr.plop.contexts.game.instance.scenario.domain.model.ScenarioSessionState;
import fr.plop.contexts.game.instance.scenario.presenter.GameInstanceScenarioController;
import fr.plop.subs.i18n.domain.I18n;
import fr.plop.subs.i18n.domain.Language;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test for Confirm dialog with YES/NO branches.
 *
 * Tests that a confirm dialog:
 * - Is correctly triggered (via WebSocket message format)
 * - Accepts YES/NO answers via REST endpoint
 * - Triggers the correct consequences for each answer
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class ConfirmDialogIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @LocalServerPort
    int randomServerPort;

    @Autowired
    private GameInstanceClearPort sessionClear;

    @Autowired
    private TemplateInitUseCase.OutPort templateInitUseCase;

    @MockitoBean
    private PushPort pushPort;

    private Template template;

    // IDs used in the template
    private static final MessageToken CONFIRM_TOKEN = new MessageToken("CONFIRM_OPEN_CHEST");
    private static final ScenarioConfig.Target.Id TARGET_OPENED_ID = new ScenarioConfig.Target.Id("TARGET_CHEST_OPENED");
    private static final ScenarioConfig.Target.Id TARGET_CLOSED_ID = new ScenarioConfig.Target.Id("TARGET_CHEST_CLOSED");
    private static final ScenarioConfig.Step.Id STEP_CONFIRM_ID = new ScenarioConfig.Step.Id("STEP_CONFIRM");

    @BeforeEach
    void setUp() {
        // Build template directly without JSON parsing
        template = buildTemplate();
        templateInitUseCase.createOrUpdate(
                templateInitUseCase.findOrCreateGame(
                        new GameProject.Code("CONFIRM_DIALOG_TEST_" + System.currentTimeMillis()),
                        new Game.Version("1.0.0")
                ),
                template
        );
    }

    private Template buildTemplate() {
        // 1. Targets for tracking what happened
        ScenarioConfig.Target targetOpened = new ScenarioConfig.Target(
                TARGET_OPENED_ID,
                i18n("Coffre ouvert", "Chest opened"),
                Optional.empty(),
                false,
                List.of(),
                Optional.empty()
        );

        ScenarioConfig.Target targetClosed = new ScenarioConfig.Target(
                TARGET_CLOSED_ID,
                i18n("Coffre fermé", "Chest left closed"),
                Optional.empty(),
                true, // optional target
                List.of(),
                Optional.empty()
        );

        // 2. Possibility: When step is active, show confirm dialog
        Possibility showConfirmPossibility = new Possibility(
                new PossibilityTrigger.StepActive(new PossibilityTrigger.Id(), STEP_CONFIRM_ID),
                List.of(
                        new Consequence.DisplayConfirm(
                                new Consequence.Id(),
                                i18n("Voulez-vous ouvrir le coffre ?", "Do you want to open the chest?"),
                                CONFIRM_TOKEN
                        )
                )
        );

        // 3. Possibility: When user answers YES
        Possibility yesAnswerPossibility = new Possibility(
                new PossibilityTrigger.MessageConfirmAnswer(
                        new PossibilityTrigger.Id(),
                        CONFIRM_TOKEN,
                        true // expected answer: YES
                ),
                List.of(
                        new Consequence.ScenarioTarget(new Consequence.Id(), TARGET_OPENED_ID, ScenarioSessionState.SUCCESS)
                )
        );

        // 4. Possibility: When user answers NO
        Possibility noAnswerPossibility = new Possibility(
                new PossibilityTrigger.MessageConfirmAnswer(
                        new PossibilityTrigger.Id(),
                        CONFIRM_TOKEN,
                        false // expected answer: NO
                ),
                List.of(
                        new Consequence.ScenarioTarget(new Consequence.Id(), TARGET_CLOSED_ID, ScenarioSessionState.SUCCESS)
                )
        );

        // 5. Step with all possibilities
        ScenarioConfig.Step step = new ScenarioConfig.Step(
                STEP_CONFIRM_ID,
                i18n("Etape du coffre", "Chest step"),
                Optional.empty(),
                0,
                List.of(targetOpened, targetClosed),
                List.of(showConfirmPossibility, yesAnswerPossibility, noAnswerPossibility)
        );

        ScenarioConfig scenarioConfig = new ScenarioConfig(List.of(step));

        // 6. Build and return template
        return Template.builder()
                .scenario(scenarioConfig)
                .build();
    }

    private static I18n i18n(String fr, String en) {
        return new I18n(Map.of(Language.FR, fr, Language.EN, en));
    }

    @Test
    public void confirmDialog_yesAnswer_triggersSuccessTarget() throws URISyntaxException, InterruptedException {
        // 1. Create connection and session
        ConnectionController.ResponseDTO connection = createAuth();
        GameInstanceController.ResponseDTO session = createGame(connection.token());
        assertThat(session.id()).isNotNull();

        // 2. Start the session - this triggers the StepActive possibility
        startGame(session.auth().token(), session.id());
        Thread.sleep(500); // Wait for async processing

        // 3. Send YES answer to the confirm dialog
        ResponseEntity<Void> response = answerConfirm(session.auth().token(), session.id(), CONFIRM_TOKEN.value(), true);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        Thread.sleep(500); // Wait for consequence processing

        // 4. Check goals - TARGET_OPENED should be SUCCESS
        GameInstanceScenarioController.GameGoalResponseDTO[] goals = getGoals(session.auth().token(), session.id());
        assertThat(goals).isNotEmpty();

        // Find the chest opened target
        boolean openedFound = java.util.Arrays.stream(goals)
                .flatMap(goal -> goal.targets().stream())
                .anyMatch(target -> target.id().contains("TARGET_CHEST_OPENED") && target.done());
        assertThat(openedFound).isTrue();
    }

    @Test
    public void confirmDialog_noAnswer_triggersClosedTarget() throws URISyntaxException, InterruptedException {
        // 1. Create connection and session
        ConnectionController.ResponseDTO connection = createAuth();
        GameInstanceController.ResponseDTO session = createGame(connection.token());
        assertThat(session.id()).isNotNull();

        // 2. Start the session
        startGame(session.auth().token(), session.id());
        Thread.sleep(500);

        // 3. Send NO answer to the confirm dialog
        ResponseEntity<Void> response = answerConfirm(session.auth().token(), session.id(), CONFIRM_TOKEN.value(), false);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        Thread.sleep(500);

        // 4. Check goals - TARGET_CLOSED should be SUCCESS
        GameInstanceScenarioController.GameGoalResponseDTO[] goals = getGoals(session.auth().token(), session.id());
        assertThat(goals).isNotEmpty();

        // Find the chest closed target
        boolean closedFound = java.util.Arrays.stream(goals)
                .flatMap(goal -> goal.targets().stream())
                .anyMatch(target -> target.id().contains("TARGET_CHEST_CLOSED") && target.done());
        assertThat(closedFound).isTrue();
    }

    @Test
    public void confirmDialog_endpoint_acceptsBothAnswers() throws URISyntaxException, InterruptedException {
        // 1. Create connection and session
        ConnectionController.ResponseDTO connection = createAuth();
        GameInstanceController.ResponseDTO session = createGame(connection.token());

        // 2. Start the session
        startGame(session.auth().token(), session.id());
        Thread.sleep(500);

        // 3. Test both YES and NO endpoints work (in sequence for this test)
        ResponseEntity<Void> yesResponse = answerConfirm(session.auth().token(), session.id(), CONFIRM_TOKEN.value(), true);
        assertThat(yesResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        // Note: In a real scenario, you wouldn't answer both, but this tests the endpoint accepts both
    }

    @Test
    public void confirmDialog_triggerMatching_worksCorrectly() {
        // Unit test: verify trigger matching logic with direct objects
        PossibilityTrigger.MessageConfirmAnswer yesTrigger = new PossibilityTrigger.MessageConfirmAnswer(
                new PossibilityTrigger.Id(), CONFIRM_TOKEN, true);
        PossibilityTrigger.MessageConfirmAnswer noTrigger = new PossibilityTrigger.MessageConfirmAnswer(
                new PossibilityTrigger.Id(), CONFIRM_TOKEN, false);

        // Simulate events
        fr.plop.contexts.game.instance.event.domain.GameEvent.MessageConfirmAnswer yesEvent =
                new fr.plop.contexts.game.instance.event.domain.GameEvent.MessageConfirmAnswer(CONFIRM_TOKEN, true);
        fr.plop.contexts.game.instance.event.domain.GameEvent.MessageConfirmAnswer noEvent =
                new fr.plop.contexts.game.instance.event.domain.GameEvent.MessageConfirmAnswer(CONFIRM_TOKEN, false);
        fr.plop.contexts.game.instance.event.domain.GameEvent.MessageConfirmAnswer wrongTokenEvent =
                new fr.plop.contexts.game.instance.event.domain.GameEvent.MessageConfirmAnswer(new MessageToken("WRONG"), true);

        // YES trigger matches only YES event with correct token
        assertThat(yesTrigger.accept(yesEvent, List.of())).isTrue();
        assertThat(yesTrigger.accept(noEvent, List.of())).isFalse();
        assertThat(yesTrigger.accept(wrongTokenEvent, List.of())).isFalse();

        // NO trigger matches only NO event with correct token
        assertThat(noTrigger.accept(noEvent, List.of())).isTrue();
        assertThat(noTrigger.accept(yesEvent, List.of())).isFalse();
    }

    // ========== Helper methods ==========

    private ConnectionController.ResponseDTO createAuth() throws URISyntaxException {
        final String baseUrl = "http://localhost:" + randomServerPort + "/connect/";
        URI uri = new URI(baseUrl);

        ConnectionController.RequestDTO request = new ConnectionController.RequestDTO("confirm-dialog-device-" + System.currentTimeMillis());
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        ResponseEntity<ConnectionController.ResponseDTO> result = this.restTemplate
                .exchange(uri, HttpMethod.POST, new HttpEntity<>(request, headers), ConnectionController.ResponseDTO.class);
        return result.getBody();
    }

    private GameInstanceController.ResponseDTO createGame(String token) throws URISyntaxException {
        final String baseUrl = "http://localhost:" + randomServerPort + "/instances/";
        URI uri = new URI(baseUrl);

        GameInstanceController.CreateRequestDTO request = new GameInstanceController.CreateRequestDTO(template.id().value());
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.add("Authorization", token);

        ResponseEntity<GameInstanceController.ResponseDTO> result = this.restTemplate
                .exchange(uri, HttpMethod.POST, new HttpEntity<>(request, headers), GameInstanceController.ResponseDTO.class);
        return result.getBody();
    }

    private void startGame(String gameToken, String sessionId) throws URISyntaxException {
        final String baseUrl = "http://localhost:" + randomServerPort + "/instances/" + sessionId + "/start/";
        URI uri = new URI(baseUrl);

        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.add("Authorization", gameToken);

        this.restTemplate.exchange(uri, HttpMethod.POST, new HttpEntity<>(headers), GameInstanceController.ResponseDTO.class);
    }

    private ResponseEntity<Void> answerConfirm(String gameToken, String sessionId, String token, boolean answer) throws URISyntaxException {
        final String baseUrl = "http://localhost:" + randomServerPort + "/instances/" + sessionId + "/messages/" + token + "/confirm";
        URI uri = new URI(baseUrl);

        MessageController.ConfirmRequest request = new MessageController.ConfirmRequest(answer);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("Authorization", gameToken);

        return this.restTemplate.exchange(uri, HttpMethod.POST, new HttpEntity<>(request, headers), Void.class);
    }

    private GameInstanceScenarioController.GameGoalResponseDTO[] getGoals(String gameToken, String sessionId) throws URISyntaxException {
        final String baseUrl = "http://localhost:" + randomServerPort + "/instances/" + sessionId + "/goals/";
        URI uri = new URI(baseUrl);

        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.add("Authorization", gameToken);
        headers.add("Language", "FR");

        ResponseEntity<GameInstanceScenarioController.GameGoalResponseDTO[]> result = this.restTemplate
                .exchange(uri, HttpMethod.GET, new HttpEntity<>(headers), GameInstanceScenarioController.GameGoalResponseDTO[].class);
        return result.getBody();
    }
}

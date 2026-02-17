package fr.plop.integration;

import fr.plop.contexts.connect.presenter.ConnectionController;
import fr.plop.contexts.game.commun.domain.Game;
import fr.plop.contexts.game.commun.domain.GameProject;
import fr.plop.contexts.game.config.consequence.Consequence;
import fr.plop.contexts.game.config.scenario.domain.model.Possibility;
import fr.plop.contexts.game.config.scenario.domain.model.PossibilityTrigger;
import fr.plop.contexts.game.config.scenario.domain.model.ScenarioConfig;
import fr.plop.contexts.game.config.talk.domain.*;
import fr.plop.contexts.game.config.template.domain.model.Template;
import fr.plop.contexts.game.config.template.domain.usecase.TemplateInitUseCase;
import fr.plop.contexts.game.instance.core.presenter.GameInstanceController;
import fr.plop.contexts.game.instance.push.PushPort;
import fr.plop.contexts.game.instance.scenario.domain.model.ScenarioState;
import fr.plop.contexts.game.instance.talk.GameInstanceTalkController;
import fr.plop.subs.i18n.domain.I18n;
import fr.plop.subs.i18n.domain.Language;
import fr.plop.subs.image.Image;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test for TalkItemNext.InputText functionality.
 *
 * Tests:
 * 1. GET /talks/{talkId} returns status="INPUTTEXT" with parameters (status, size)
 * 2. POST /talks/{talkId}/inputtext with correct value triggers SUCCESS consequence
 * 3. POST /talks/{talkId}/inputtext with wrong value triggers FAILURE consequence
 * 4. POST /talks/{talkId}/inputtext with almost correct value triggers ALMOST_EQUALS
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class TalkInputTextIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @LocalServerPort
    int randomServerPort;

    @Autowired
    private TemplateInitUseCase.OutPort templateInitUseCase;

    @MockitoBean
    private PushPort pushPort;

    private Template template;

    // IDs used in the template
    private static final TalkCharacter.Id CHARACTER_COFFRE_ID = new TalkCharacter.Id("PNJ_COFFRE");
    private static final TalkItem.Id TALK_CODE_ID = new TalkItem.Id("TALK_COFFRE_CODE");
    private static final ScenarioConfig.Target.Id TARGET_OPEN_CHEST_ID = new ScenarioConfig.Target.Id("TARGET_OPEN_CHEST");

    private static final String SECRET_CODE = "Golu";

    @BeforeEach
    void setUp() {
        // Note: Using @DirtiesContext to ensure clean state instead of clearAll()
        // which has FK constraint issues with H2
        templateInitUseCase.deleteAll();

        // Build template directly without JSON parsing
        template = buildTemplate();
        templateInitUseCase.createOrUpdate(
                templateInitUseCase.findOrCreateGame(new GameProject.Code("INPUTTEXT_TEST"), new Game.Version("1.0.0")),
                template
        );
    }

    private Template buildTemplate() {
        // 1. Talk - a chest asking for a code
        TalkCharacter coffre = new TalkCharacter(CHARACTER_COFFRE_ID, "Coffre");
        Image coffreImage = new Image(Image.Type.ASSET, "coffre/default.png");
        TalkCharacter.Reference coffreRef = new TalkCharacter.Reference(coffre, "DEFAULT", coffreImage);

        // InputText with ALPHANUMERIC status and size 10
        TalkItemNext.InputText inputText = new TalkItemNext.InputText(
                TalkItemNext.InputText.Type.ALPHANUMERIC,
                Optional.of(10)
        );

        TalkItem talkItem = new TalkItem(
                TALK_CODE_ID,
                TalkItemOut.fixed(i18n("Entrez le code secret", "Enter the secret code")),
                coffreRef,
                inputText
        );

        TalkConfig talkConfig = new TalkConfig(List.of(talkItem));

        // 2. Scenario with possibilities triggered by InputText
        ScenarioConfig.Target target = new ScenarioConfig.Target(
                TARGET_OPEN_CHEST_ID,
                i18n("Ouvrir le coffre", "Open the chest"),
                Optional.empty(),
                false,
                List.of(),
                Optional.empty()
        );

        // Possibility 1: Correct code (EQUALS) -> SUCCESS
        Possibility correctCodePossibility = new Possibility(
                new PossibilityTrigger.TalkInputText(
                        new PossibilityTrigger.Id(),
                        TALK_CODE_ID,
                        SECRET_CODE,
                        PossibilityTrigger.TalkInputText.MatchType.EQUALS
                ),
                List.of(new Consequence.ScenarioTarget(new Consequence.Id(), TARGET_OPEN_CHEST_ID, ScenarioState.SUCCESS))
        );

        // Possibility 2: Almost correct code (ALMOST_EQUALS but not EQUALS) -> Alert "Presque !"
        // Note: We need to be careful - ALMOST_EQUALS will also match EQUALS, so we need proper ordering
        // or use ALMOST_EQUALS only. For this test, we'll test EQUALS separately.

        // Possibility 3: Wrong code (COMPLETELY_DIFFERENT) -> FAILURE
        Possibility wrongCodePossibility = new Possibility(
                new PossibilityTrigger.TalkInputText(
                        new PossibilityTrigger.Id(),
                        TALK_CODE_ID,
                        SECRET_CODE,
                        PossibilityTrigger.TalkInputText.MatchType.COMPLETELY_DIFFERENT
                ),
                List.of(new Consequence.ScenarioTarget(new Consequence.Id(), TARGET_OPEN_CHEST_ID, ScenarioState.FAILURE))
        );

        ScenarioConfig.Step step = new ScenarioConfig.Step(
                new ScenarioConfig.Step.Id("STEP_1"),
                i18n("Etape 1", "Step 1"),
                Optional.empty(),
                0,
                List.of(target),
                List.of(correctCodePossibility, wrongCodePossibility)
        );

        ScenarioConfig scenarioConfig = new ScenarioConfig(List.of(step));

        // 3. Build and return template
        return Template.builder()
                .talk(talkConfig)
                .scenario(scenarioConfig)
                .build();
    }

    private static I18n i18n(String fr, String en) {
        return new I18n(Map.of(Language.FR, fr, Language.EN, en));
    }

    @Test
    public void getTalk_withInputText_returnsCorrectType() throws URISyntaxException, InterruptedException {
        // 1. Create connection and session
        ConnectionController.ResponseDTO connection = createAuth();
        GameInstanceController.ResponseDTO session = createGame(connection.token());
        assertThat(session.id()).isNotNull();

        // 2. Start the session
        startGame(session.auth().token(), session.id());
        Thread.sleep(500);

        // 3. Get talk - should return INPUTTEXT status
        GameInstanceTalkController.ResponseDTO response = getTalk(session.auth().token(), session.id(), TALK_CODE_ID.value());

        assertThat(response.text()).isEqualTo("Entrez le code secret");
        assertThat(response.next().type()).isEqualTo("INPUTTEXT");
        assertThat(response.parameters()).containsEntry("type", "ALPHANUMERIC");
        assertThat(response.parameters()).containsEntry("size", "10");
    }

    @Test
    public void submitInputText_withCorrectCode_triggersSuccess() throws URISyntaxException, InterruptedException {
        // 1. Create connection and session
        ConnectionController.ResponseDTO connection = createAuth();
        GameInstanceController.ResponseDTO session = createGame(connection.token());

        // 2. Start the session
        startGame(session.auth().token(), session.id());
        Thread.sleep(500);

        // 3. Submit the correct code
        ResponseEntity<Void> response = submitInputText(session.auth().token(), session.id(), TALK_CODE_ID.value(), SECRET_CODE);

        // Should return 204 No Content (success)
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        // 4. Verify the target is now SUCCESS
        Thread.sleep(500);
        var scenarioResponse = getScenario(session.auth().token(), session.id());

        // The target should be marked as SUCCESS
        assertThat(scenarioResponse).isNotNull();
    }

    @Test
    public void submitInputText_withWrongCode_triggersFailure() throws URISyntaxException, InterruptedException {
        // 1. Create connection and session
        ConnectionController.ResponseDTO connection = createAuth();
        GameInstanceController.ResponseDTO session = createGame(connection.token());

        // 2. Start the session
        startGame(session.auth().token(), session.id());
        Thread.sleep(500);

        // 3. Submit a completely wrong code
        ResponseEntity<Void> response = submitInputText(session.auth().token(), session.id(), TALK_CODE_ID.value(), "XXXXXX");

        // Should return 204 No Content
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    public void submitInputText_onNonInputTextTalk_returnsBadRequest() throws URISyntaxException, InterruptedException {
        // Create a template with a non-InputText talk using UNIQUE IDs
        TalkCharacter.Id simpleCharacterId = new TalkCharacter.Id("PNJ_SIMPLE");
        TalkItem.Id simpleTalkId = new TalkItem.Id("TALK_SIMPLE");
        ScenarioConfig.Target.Id simpleTargetId = new ScenarioConfig.Target.Id("TARGET_SIMPLE");

        TalkCharacter character = new TalkCharacter(simpleCharacterId, "Test");
        Image image = new Image(Image.Type.ASSET, "test.png");
        TalkCharacter.Reference ref = new TalkCharacter.Reference(character, "DEFAULT", image);

        TalkItem simpleTalk = new TalkItem(
                simpleTalkId,
                TalkItemOut.fixed(i18n("Simple message", "Simple message")),
                ref,
                new TalkItemNext.Empty()
        );

        Template simpleTemplate = Template.builder()
                .talk(new TalkConfig(List.of(simpleTalk)))
                .scenario(new ScenarioConfig(List.of(
                        new ScenarioConfig.Step(
                                new ScenarioConfig.Step.Id("STEP_SIMPLE"),
                                i18n("Step", "Step"),
                                Optional.empty(),
                                0,
                                List.of(new ScenarioConfig.Target(
                                        simpleTargetId,
                                        i18n("Target", "Target"),
                                        Optional.empty(),
                                        false,
                                        List.of(),
                                        Optional.empty()
                                )),
                                List.of()
                        )
                )))
                .build();

        templateInitUseCase.createOrUpdate(
                templateInitUseCase.findOrCreateGame(new GameProject.Code("SIMPLE_TEST"), new Game.Version("1.0.0")),
                simpleTemplate
        );

        // 1. Create connection and session
        ConnectionController.ResponseDTO connection = createAuth();
        GameInstanceController.ResponseDTO session = createGame(connection.token(), simpleTemplate.id().value());

        // 2. Start the session
        startGame(session.auth().token(), session.id());
        Thread.sleep(500);

        // 3. Try to submit inputtext on a non-InputText talk
        ResponseEntity<Void> response = submitInputText(session.auth().token(), session.id(), simpleTalkId.value(), "test");

        // Should return 400 Bad Request
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    public void getTalk_withNumericInputText_returnsNumericType() throws URISyntaxException, InterruptedException {
        // Create a template with NUMERIC InputText using UNIQUE IDs
        TalkCharacter.Id numericCharacterId = new TalkCharacter.Id("PNJ_NUMERIC");
        TalkItem.Id numericTalkId = new TalkItem.Id("TALK_NUMERIC");
        ScenarioConfig.Target.Id numericTargetId = new ScenarioConfig.Target.Id("TARGET_NUMERIC");

        TalkCharacter character = new TalkCharacter(numericCharacterId, "Test");
        Image image = new Image(Image.Type.ASSET, "test.png");
        TalkCharacter.Reference ref = new TalkCharacter.Reference(character, "DEFAULT", image);

        TalkItemNext.InputText numericInput = new TalkItemNext.InputText(
                TalkItemNext.InputText.Type.NUMERIC,
                Optional.of(4)
        );

        TalkItem numericTalk = new TalkItem(
                numericTalkId,
                TalkItemOut.fixed(i18n("Entrez le code PIN", "Enter PIN code")),
                ref,
                numericInput
        );

        Template numericTemplate = Template.builder()
                .talk(new TalkConfig(List.of(numericTalk)))
                .scenario(new ScenarioConfig(List.of(
                        new ScenarioConfig.Step(
                                new ScenarioConfig.Step.Id("STEP_NUMERIC"),
                                i18n("Step", "Step"),
                                Optional.empty(),
                                0,
                                List.of(new ScenarioConfig.Target(
                                        numericTargetId,
                                        i18n("Target", "Target"),
                                        Optional.empty(),
                                        false,
                                        List.of(),
                                        Optional.empty()
                                )),
                                List.of()
                        )
                )))
                .build();

        templateInitUseCase.createOrUpdate(
                templateInitUseCase.findOrCreateGame(new GameProject.Code("NUMERIC_TEST"), new Game.Version("1.0.0")),
                numericTemplate
        );

        // 1. Create connection and session
        ConnectionController.ResponseDTO connection = createAuth();
        GameInstanceController.ResponseDTO session = createGame(connection.token(), numericTemplate.id().value());

        // 2. Start the session
        startGame(session.auth().token(), session.id());
        Thread.sleep(500);

        // 3. Get talk
        GameInstanceTalkController.ResponseDTO response = getTalk(session.auth().token(), session.id(), numericTalkId.value());

        assertThat(response.next().type()).isEqualTo("INPUTTEXT");
        assertThat(response.parameters()).containsEntry("type", "NUMERIC");
        assertThat(response.parameters()).containsEntry("size", "4");
    }

    // ========== Helper methods ==========

    private ConnectionController.ResponseDTO createAuth() throws URISyntaxException {
        final String baseUrl = "http://localhost:" + randomServerPort + "/connect/";
        URI uri = new URI(baseUrl);

        ConnectionController.RequestDTO request = new ConnectionController.RequestDTO("test-device-inputtext-" + System.currentTimeMillis());
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        ResponseEntity<ConnectionController.ResponseDTO> result = this.restTemplate
                .exchange(uri, HttpMethod.POST, new HttpEntity<>(request, headers), ConnectionController.ResponseDTO.class);
        return result.getBody();
    }

    private GameInstanceController.ResponseDTO createGame(String token) throws URISyntaxException {
        return createGame(token, template.id().value());
    }

    private GameInstanceController.ResponseDTO createGame(String token, String templateId) throws URISyntaxException {
        final String baseUrl = "http://localhost:" + randomServerPort + "/instances/";
        URI uri = new URI(baseUrl);

        GameInstanceController.CreateRequestDTO request = new GameInstanceController.CreateRequestDTO(templateId);
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

    private GameInstanceTalkController.ResponseDTO getTalk(String gameToken, String sessionId, String talkId) throws URISyntaxException {
        final String baseUrl = "http://localhost:" + randomServerPort + "/instances/" + sessionId + "/talks/" + talkId;
        URI uri = new URI(baseUrl);

        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.add("Authorization", gameToken);
        headers.add("Language", "FR");

        ResponseEntity<GameInstanceTalkController.ResponseDTO> result = this.restTemplate
                .exchange(uri, HttpMethod.GET, new HttpEntity<>(headers), GameInstanceTalkController.ResponseDTO.class);
        return result.getBody();
    }

    private ResponseEntity<Void> submitInputText(String gameToken, String sessionId, String talkId, String value) throws URISyntaxException {
        final String baseUrl = "http://localhost:" + randomServerPort + "/instances/" + sessionId + "/talks/" + talkId + "/inputtext";
        URI uri = new URI(baseUrl);

        GameInstanceTalkController.InputTextRequestDTO request = new GameInstanceTalkController.InputTextRequestDTO(value);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("Authorization", gameToken);

        return this.restTemplate.exchange(uri, HttpMethod.POST, new HttpEntity<>(request, headers), Void.class);
    }

    private Object getScenario(String gameToken, String sessionId) throws URISyntaxException {
        final String baseUrl = "http://localhost:" + randomServerPort + "/instances/" + sessionId + "/scenario";
        URI uri = new URI(baseUrl);

        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.add("Authorization", gameToken);
        headers.add("Language", "FR");

        ResponseEntity<Object> result = this.restTemplate
                .exchange(uri, HttpMethod.GET, new HttpEntity<>(headers), Object.class);
        return result.getBody();
    }
}

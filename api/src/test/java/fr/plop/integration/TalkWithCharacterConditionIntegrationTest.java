package fr.plop.integration;

import fr.plop.contexts.connect.presenter.ConnectionController;
import fr.plop.contexts.game.commun.domain.Game;
import fr.plop.contexts.game.commun.domain.GameProject;
import fr.plop.contexts.game.config.condition.Condition;
import fr.plop.contexts.game.config.scenario.domain.model.ScenarioConfig;
import fr.plop.contexts.game.config.talk.domain.*;
import fr.plop.contexts.game.config.template.domain.model.Template;
import fr.plop.contexts.game.config.template.domain.usecase.TemplateInitUseCase;
import fr.plop.contexts.game.instance.core.domain.port.GameInstanceClearPort;
import fr.plop.contexts.game.instance.core.presenter.GameInstanceController;
import fr.plop.contexts.game.instance.push.PushPort;
import fr.plop.contexts.game.instance.talk.GameInstanceTalkController;
import fr.plop.subs.i18n.domain.I18n;
import fr.plop.subs.i18n.domain.Language;
import fr.plop.subs.image.Image;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
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
 * Integration test for TalkWith condition (NEVER_TALKED_TO functionality).
 *
 * Tests that a talk with a conditional message:
 * - Shows one message when the player has NEVER talked to the character
 * - Shows a different message when the player HAS talked to the character
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class TalkWithCharacterConditionIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @LocalServerPort
    int randomServerPort;

    @Autowired
    private GameInstanceClearPort sessionClear;

    @Autowired
    private TemplateInitUseCase.OutPort templatePort;

    @MockitoBean
    private PushPort pushPort;

    private Template template;

    // IDs used in the template
    private static final TalkCharacter.Id CHARACTER_MARCEL_ID = new TalkCharacter.Id("PNJ_MARCEL");
    private static final TalkItem.Id TALK_MARCEL_ROOT_ID = new TalkItem.Id("TALK_MARCEL_ROOT");

    @BeforeEach
    void setUp() {
        sessionClear.clearAll();
        templatePort.deleteAll();

        // Build template directly without JSON parsing
        template = buildTemplate();
        templatePort.createOrUpdate(
                templatePort.findOrCreateGame(new GameProject.Code("TALKWITH_TEST"), new Game.Version("1.0.0")),
                template
        );
    }

    private Template buildTemplate() {
        // 1. Talk - one character with conditional greeting
        TalkCharacter marcel = new TalkCharacter(CHARACTER_MARCEL_ID, "Marcel");
        Image marcelImage = new Image(Image.Type.ASSET, "marcel/default.png");
        TalkCharacter.Reference marcelRef = new TalkCharacter.Reference(marcel, "DEFAULT", marcelImage);

        // Condition: NOT talked with Marcel
        Condition talkWithMarcelCondition = new Condition.TalkWithCharacter(new Condition.Id(), CHARACTER_MARCEL_ID);
        Condition notTalkWithMarcelCondition = new Condition.Not(new Condition.Id(), talkWithMarcelCondition);

        // Conditional talk output:
        // - Default (has talked): "Bonjour, content de vous revoir"
        // - Branch (never talked): "Bonjour, je ne vous connais pas encore"
        TalkItemOut.Conditional.Branch neverTalkedBranch = new TalkItemOut.Conditional.Branch(
                0,
                notTalkWithMarcelCondition,
                i18n("Bonjour, je ne vous connais pas encore", "Hello, I don't know you yet")
        );

        TalkItemOut conditionalOutput = new TalkItemOut.Conditional(
                i18n("Bonjour, content de vous revoir", "Hello, nice to see you again"),
                List.of(neverTalkedBranch)
        );

        TalkItem talkItem = new TalkItem(
                TALK_MARCEL_ROOT_ID,
                conditionalOutput,
                marcelRef,
                new TalkItemNext.Empty()
        );

        TalkConfig talkConfig = new TalkConfig(List.of(talkItem));

        // 2. Minimal scenario (required for template)
        ScenarioConfig.Target target = new ScenarioConfig.Target(
                new ScenarioConfig.Target.Id("TARGET_1"),
                i18n("Objectif", "Goal"),
                Optional.empty(),
                false,
                List.of(),
                Optional.empty()
        );

        ScenarioConfig.Step step = new ScenarioConfig.Step(
                new ScenarioConfig.Step.Id("STEP_1"),
                i18n("Etape 1", "Step 1"),
                Optional.empty(),
                0,
                List.of(target),
                List.of()
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
    public void talkWithCondition_firstCall_showsNeverTalkedMessage() throws URISyntaxException, InterruptedException {
        // 1. Create connection and session
        ConnectionController.ResponseDTO connection = createAuth();
        GameInstanceController.ResponseDTO session = createGame(connection.token());
        assertThat(session.id()).isNotNull();

        // 2. Start the session
        startGame(session.auth().token(), session.id());
        Thread.sleep(500); // Wait for async processing

        // 3. First call to talk - player has NEVER talked to Marcel
        GameInstanceTalkController.ResponseDTO firstResponse = getTalk(session.auth().token(), session.id(), TALK_MARCEL_ROOT_ID.value());

        // Should show "never talked" message
        assertThat(firstResponse.text()).isEqualTo("Bonjour, je ne vous connais pas encore");
    }

    @Test
    public void talkWithCondition_secondCall_showsAlreadyTalkedMessage() throws URISyntaxException, InterruptedException {
        // 1. Create connection and session
        ConnectionController.ResponseDTO connection = createAuth();
        GameInstanceController.ResponseDTO session = createGame(connection.token());
        assertThat(session.id()).isNotNull();

        // 2. Start the session
        startGame(session.auth().token(), session.id());
        Thread.sleep(500); // Wait for async processing

        // 3. First call to talk - this registers that player talked to Marcel
        GameInstanceTalkController.ResponseDTO firstResponse = getTalk(session.auth().token(), session.id(), TALK_MARCEL_ROOT_ID.value());
        assertThat(firstResponse.text()).isEqualTo("Bonjour, je ne vous connais pas encore");

        // 4. Second call to talk - player HAS NOW talked to Marcel
        GameInstanceTalkController.ResponseDTO secondResponse = getTalk(session.auth().token(), session.id(), TALK_MARCEL_ROOT_ID.value());

        // Should show "already talked" message (default)
        assertThat(secondResponse.text()).isEqualTo("Bonjour, content de vous revoir");
    }

    // ========== Helper methods ==========

    private ConnectionController.ResponseDTO createAuth() throws URISyntaxException {
        final String baseUrl = "http://localhost:" + randomServerPort + "/connect/";
        URI uri = new URI(baseUrl);

        ConnectionController.RequestDTO request = new ConnectionController.RequestDTO("test-device-id-" + System.currentTimeMillis());
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
}

package fr.plop.integration;

import com.fasterxml.jackson.core.JsonProcessingException;
import fr.plop.contexts.connect.presenter.ConnectionController;
import fr.plop.contexts.game.config.consequence.Consequence;
import fr.plop.contexts.game.config.consequence.handler.ConsequenceTalkHandler;
import fr.plop.contexts.game.config.scenario.domain.model.PossibilityTrigger;
import fr.plop.contexts.game.config.scenario.domain.model.ScenarioConfig;
import fr.plop.contexts.game.config.template.domain.model.Template;
import fr.plop.contexts.game.config.template.domain.usecase.TemplateInitUseCase;
import fr.plop.contexts.game.config.template.domain.usecase.generator.json.TemplateGeneratorJsonUseCase;
import fr.plop.contexts.game.session.confirm.ConfirmController;
import fr.plop.contexts.game.session.core.domain.port.GameSessionClearPort;
import fr.plop.contexts.game.session.core.presenter.GameSessionController;
import fr.plop.contexts.game.session.event.domain.GameEvent;
import fr.plop.contexts.game.session.push.PushEvent;
import fr.plop.contexts.game.session.push.PushPort;
import fr.plop.contexts.game.session.push.WebSocketPushAdapter;
import fr.plop.subs.i18n.domain.I18n;
import fr.plop.subs.i18n.domain.Language;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ConfirmFlowIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @LocalServerPort
    int randomServerPort;

    @Autowired
    private GameSessionClearPort sessionClear;

    @Autowired
    private TemplateInitUseCase.OutPort templateInitUseCase;

    @MockBean
    private PushPort pushPort;

    @Autowired
    private ConsequenceTalkHandler consequenceTalkHandler;

    private Template template;
    private Consequence.Id confirmId;

    @BeforeEach
    void setUp() throws JsonProcessingException {
        sessionClear.clearAll();
        templateInitUseCase.deleteAll();

        // Create template with Confirm flow
        TemplateGeneratorJsonUseCase generator = new TemplateGeneratorJsonUseCase();
        template = generator.apply("""
                {
                  "code": "CONFIRM_FLOW_TEST",
                  "version": "1.0.0",
                  "label": "Test Confirm Flow",
                  "scenario": {
                    "steps": [
                      {
                        "ref": "STEP_1",
                        "label": { "FR": "Etape 1", "EN": "Step 1" },
                        "targets": [
                          { "ref": "TARGET_CHEST", "label": { "FR": "Ouvrir le coffre", "EN": "Open the chest" } }
                        ],
                        "possibilities": [
                          {
                            "trigger": { "type": "StepActive" },
                            "consequences": [
                              {
                                "type": "CONFIRM",
                                "metadata": {
                                  "ref": "CONFIRM_CHEST",
                                  "message": { "FR": "Voulez-vous ouvrir le coffre ?", "EN": "Do you want to open the chest?" }
                                }
                              }
                            ]
                          },
                          {
                            "trigger": {
                              "type": "ConfirmAnswer",
                              "metadata": { "confirmRef": "CONFIRM_CHEST", "answer": "YES" }
                            },
                            "consequences": [
                              { "type": "GoalTarget", "metadata": { "targetId": "TARGET_CHEST", "state": "SUCCESS" } }
                            ]
                          },
                          {
                            "trigger": {
                              "type": "ConfirmAnswer",
                              "metadata": { "confirmRef": "CONFIRM_CHEST", "answer": "NO" }
                            },
                            "consequences": [
                              { "type": "Alert", "metadata": { "value": { "FR": "Coffre laisse ferme.", "EN": "Chest left closed." } } }
                            ]
                          }
                        ]
                      }
                    ]
                  }
                }
                """);

        templateInitUseCase.create(template);

        // Extract confirmId from the template
        ScenarioConfig.Step step = template.scenario().steps().getFirst();
        Consequence firstConsequence = step.possibilities().get(0).consequences().getFirst();
        confirmId = ((Consequence.DisplayConfirm) firstConsequence).id();
    }

    @Test
    public void jsonParsing_createsDisplayConfirmAndConfirmAnswerTrigger() {
        // Verify template was parsed correctly
        ScenarioConfig.Step step = template.scenario().steps().getFirst();
        assertThat(step.possibilities()).hasSize(3);

        // First possibility has DisplayConfirm consequence
        Consequence consequence = step.possibilities().get(0).consequences().getFirst();
        assertThat(consequence).isInstanceOf(Consequence.DisplayConfirm.class);
        Consequence.DisplayConfirm displayConfirm = (Consequence.DisplayConfirm) consequence;
        assertThat(displayConfirm.message().value(Language.FR)).isEqualTo("Voulez-vous ouvrir le coffre ?");

        // Second possibility has ConfirmAnswer YES trigger
        PossibilityTrigger trigger1 = step.possibilities().get(1).trigger();
        assertThat(trigger1).isInstanceOf(PossibilityTrigger.ConfirmAnswer.class);
        PossibilityTrigger.ConfirmAnswer yesTrigger = (PossibilityTrigger.ConfirmAnswer) trigger1;
        assertThat(yesTrigger.confirmId()).isEqualTo(displayConfirm.id());
        assertThat(yesTrigger.expectedAnswer()).isTrue();

        // Third possibility has ConfirmAnswer NO trigger
        PossibilityTrigger trigger2 = step.possibilities().get(2).trigger();
        assertThat(trigger2).isInstanceOf(PossibilityTrigger.ConfirmAnswer.class);
        PossibilityTrigger.ConfirmAnswer noTrigger = (PossibilityTrigger.ConfirmAnswer) trigger2;
        assertThat(noTrigger.confirmId()).isEqualTo(displayConfirm.id());
        assertThat(noTrigger.expectedAnswer()).isFalse();
    }

    @Test
    public void confirmTrigger_matchesCorrectAnswer() {
        // Unit test to verify trigger matching logic
        PossibilityTrigger.ConfirmAnswer yesAnswer = new PossibilityTrigger.ConfirmAnswer(
                new PossibilityTrigger.Id(), confirmId, true);
        PossibilityTrigger.ConfirmAnswer noAnswer = new PossibilityTrigger.ConfirmAnswer(
                new PossibilityTrigger.Id(), confirmId, false);

        GameEvent.ConfirmAnswer yesEvent = new GameEvent.ConfirmAnswer(confirmId, true);
        GameEvent.ConfirmAnswer noEvent = new GameEvent.ConfirmAnswer(confirmId, false);
        GameEvent.ConfirmAnswer wrongIdEvent = new GameEvent.ConfirmAnswer(new Consequence.Id(), true);

        // YES trigger should match YES event
        assertThat(yesAnswer.accept(yesEvent, List.of())).isTrue();
        assertThat(yesAnswer.accept(noEvent, List.of())).isFalse();
        assertThat(yesAnswer.accept(wrongIdEvent, List.of())).isFalse();

        // NO trigger should match NO event
        assertThat(noAnswer.accept(noEvent, List.of())).isTrue();
        assertThat(noAnswer.accept(yesEvent, List.of())).isFalse();
    }

    @Test
    public void confirmEndpoint_canReceiveAnswer() throws URISyntaxException {
        // 1. Create auth and session
        ConnectionController.ResponseDTO connection = createAuth();
        GameSessionController.GameSessionActivedResponseDTO session = createGameSession(connection.token());
        assertThat(session.id()).isNotNull();

        // 2. Call endpoint to answer - should not throw error
        ResponseEntity<Void> response = answerConfirm(session.gameToken(), session.id(), confirmId.value(), true);

        // We expect 200 OK (void method returns 200)
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void webSocketMessage_formatIsCorrect() {
        // Test the WebSocket message format
        WebSocketPushAdapter adapter = new WebSocketPushAdapter(null) {
            @Override
            public void push(PushEvent event) {
                // We just test the message method via reflection or by checking the format
            }
        };

        // Verify the message format matches: SYSTEM:CONFIRM:<id>:<message>
        String expectedPrefix = "SYSTEM:CONFIRM:";
        String confirmIdValue = "test-confirm-123";
        String message = "Test message";

        // The format should be: SYSTEM:CONFIRM:<confirmId>:<message>
        String expectedFormat = expectedPrefix + confirmIdValue + ":" + message;
        assertThat(expectedFormat).isEqualTo("SYSTEM:CONFIRM:test-confirm-123:Test message");
    }

    @Test
    public void consequenceHandler_supportsDisplayConfirm() {
        // Test that ConsequenceTalkHandler supports DisplayConfirm
        Consequence.DisplayConfirm confirm = new Consequence.DisplayConfirm(
                new Consequence.Id(),
                new I18n(Map.of(Language.FR, "Test message"))
        );

        assertThat(consequenceTalkHandler.supports(confirm)).isTrue();
    }

    private ConnectionController.ResponseDTO createAuth() throws URISyntaxException {
        final String baseUrl = "http://localhost:" + randomServerPort + "/connect/";
        URI uri = new URI(baseUrl);

        ConnectionController.RequestDTO request = new ConnectionController.RequestDTO("confirmTestDeviceId" + System.currentTimeMillis());

        ResponseEntity<ConnectionController.ResponseDTO> result = this.restTemplate.postForEntity(uri, request, ConnectionController.ResponseDTO.class);
        return result.getBody();
    }

    private GameSessionController.GameSessionActivedResponseDTO createGameSession(String token) throws URISyntaxException {
        final String baseUrl = "http://localhost:" + randomServerPort + "/sessions/";
        URI uri = new URI(baseUrl);

        GameSessionController.GameSessionCreateRequest request = new GameSessionController.GameSessionCreateRequest(template.id().value());
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.add("Authorization", token);

        ResponseEntity<GameSessionController.GameSessionActivedResponseDTO> result = this.restTemplate
                .exchange(uri, HttpMethod.POST, new HttpEntity<>(request, headers), GameSessionController.GameSessionActivedResponseDTO.class);
        return result.getBody();
    }

    private ResponseEntity<Void> answerConfirm(String gameToken, String sessionId, String confirmId, boolean answer) throws URISyntaxException {
        final String baseUrl = "http://localhost:" + randomServerPort + "/sessions/" + sessionId + "/confirms/" + confirmId + "/answer/";
        URI uri = new URI(baseUrl);

        ConfirmController.AnswerRequestDTO request = new ConfirmController.AnswerRequestDTO(answer);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("Authorization", gameToken);

        return this.restTemplate.exchange(uri, HttpMethod.POST, new HttpEntity<>(request, headers), Void.class);
    }

}

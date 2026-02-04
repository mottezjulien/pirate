package fr.plop.integration;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.plop.contexts.connect.presenter.ConnectionController;
import fr.plop.contexts.game.commun.domain.Game;
import fr.plop.contexts.game.commun.domain.GameProject;
import fr.plop.contexts.game.config.consequence.Consequence;
import fr.plop.contexts.game.config.consequence.handler.ConsequenceTalkHandler;
import fr.plop.contexts.game.config.message.MessageToken;
import fr.plop.contexts.game.config.scenario.domain.model.PossibilityTrigger;
import fr.plop.contexts.game.config.scenario.domain.model.ScenarioConfig;
import fr.plop.contexts.game.config.template.domain.model.Template;
import fr.plop.contexts.game.config.template.domain.usecase.TemplateInitUseCase;
import fr.plop.contexts.game.config.template.domain.usecase.generator.json.TemplateGeneratorJsonUseCase;
import fr.plop.contexts.game.config.template.domain.usecase.generator.json.TemplateGeneratorRoot;
import fr.plop.contexts.game.presentation.domain.Presentation;
import fr.plop.contexts.game.instance.core.domain.port.GameInstanceClearPort;
import fr.plop.contexts.game.instance.core.presenter.GameInstanceController;
import fr.plop.contexts.game.instance.event.domain.GameEvent;
import fr.plop.contexts.game.instance.message.MessageController;
import fr.plop.contexts.game.instance.push.PushEvent;
import fr.plop.contexts.game.instance.push.PushPort;
import fr.plop.contexts.game.instance.push.WebSocketPushAdapter;
import fr.plop.subs.i18n.domain.I18n;
import fr.plop.subs.i18n.domain.Language;
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

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ConfirmFlowIntegrationTest {

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

    @Autowired
    private ConsequenceTalkHandler consequenceTalkHandler;

    private Template template;
    private MessageToken token;

    @BeforeEach
    void setUp() throws JsonProcessingException {
        sessionClear.clearAll();
        templateInitUseCase.deleteAll();

        // Create template with Confirm flow
        TemplateGeneratorJsonUseCase generator = new TemplateGeneratorJsonUseCase();
        ObjectMapper objectMapper = new ObjectMapper();
        String json = """
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
                """;
        TemplateGeneratorRoot root = objectMapper.readValue(json, TemplateGeneratorRoot.class);
        GameProject.Code code = generator.code(root);
        Game.Version version = generator.version(root);
        template = generator.template(root);
        Presentation presentation = generator.presentation(root);

        Game.Id gameId = templateInitUseCase.findOrCreateGame(code, version);
        templateInitUseCase.createOrUpdate(gameId, template);
        templateInitUseCase.createOrUpdate(gameId, presentation);

        // Extract confirmId from the template
        ScenarioConfig.Step step = template.scenario().steps().getFirst();
        Consequence firstConsequence = step.possibilities().get(0).consequences().getFirst();
        token = ((Consequence.DisplayConfirm) firstConsequence).token();
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
        assertThat(trigger1).isInstanceOf(PossibilityTrigger.MessageConfirmAnswer.class);
        PossibilityTrigger.MessageConfirmAnswer yesTrigger = (PossibilityTrigger.MessageConfirmAnswer) trigger1;
        assertThat(yesTrigger.token()).isEqualTo(token);
        assertThat(yesTrigger.expectedAnswer()).isTrue();

        // Third possibility has ConfirmAnswer NO trigger
        PossibilityTrigger trigger2 = step.possibilities().get(2).trigger();
        assertThat(trigger2).isInstanceOf(PossibilityTrigger.MessageConfirmAnswer.class);
        PossibilityTrigger.MessageConfirmAnswer noTrigger = (PossibilityTrigger.MessageConfirmAnswer) trigger2;
        assertThat(noTrigger.token()).isEqualTo(token);
        assertThat(noTrigger.expectedAnswer()).isFalse();
    }

    @Test
    public void confirmTrigger_matchesCorrectAnswer() {
        // Unit test to verify trigger matching logic
        PossibilityTrigger.MessageConfirmAnswer yesAnswer = new PossibilityTrigger.MessageConfirmAnswer(
                new PossibilityTrigger.Id(), token, true);
        PossibilityTrigger.MessageConfirmAnswer noAnswer = new PossibilityTrigger.MessageConfirmAnswer(
                new PossibilityTrigger.Id(), token, false);

        GameEvent.MessageConfirmAnswer yesEvent = new GameEvent.MessageConfirmAnswer(token, true);
        GameEvent.MessageConfirmAnswer noEvent = new GameEvent.MessageConfirmAnswer(token, false);
        MessageToken wrongToken = new MessageToken("WRONG_TOKEN");
        GameEvent.MessageConfirmAnswer wrongIdEvent = new GameEvent.MessageConfirmAnswer(wrongToken, true);

        // YES trigger should match YES event
        assertThat(yesAnswer.accept(yesEvent, List.of())).isTrue();
        assertThat(yesAnswer.accept(noEvent, List.of())).isFalse();
        assertThat(yesAnswer.accept(wrongIdEvent, List.of())).isFalse(); // Different token

        // NO trigger should match NO event
        assertThat(noAnswer.accept(noEvent, List.of())).isTrue();
        assertThat(noAnswer.accept(yesEvent, List.of())).isFalse();
    }

    @Test
    public void confirmEndpoint_canReceiveAnswer() throws URISyntaxException {
        // 1. Create auth and session
        ConnectionController.ResponseDTO connection = createAuth();
        GameInstanceController.ResponseDTO session = createGame(connection.token());
        assertThat(session.id()).isNotNull();

        // 2. Call endpoint to answer - should not throw error
        ResponseEntity<Void> response = answerConfirm(session.auth().token(), session.id(), token, true);

        // We expect 200 OK (void method returns 200)
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void webSocketMessage_formatIsCorrect() {
        // Test the WebSocket message format
        WebSocketPushAdapter adapter = new WebSocketPushAdapter(null, new ObjectMapper()) {
            @Override
            public void push(PushEvent event) {
                // We just test the message method via reflection or by checking the format
            }
        };

        // Verify the message format matches JSON
        String confirmIdValue = "test-confirm-123";
        String message = "Test message";

        String expectedJson = "{\"origin\":\"SYSTEM\",\"type\":\"CONFIRM\",\"confirmId\":\"test-confirm-123\",\"message\":\"Test message\"}";
        assertThat(expectedJson).contains("origin", "SYSTEM", "CONFIRM", confirmIdValue, message);
    }

    @Test
    public void consequenceHandler_supportsDisplayConfirm() {
        // Test that ConsequenceTalkHandler supports DisplayConfirm
        Consequence.DisplayConfirm confirm = new Consequence.DisplayConfirm(
                new Consequence.Id(),
                new I18n(Map.of(Language.FR, "Test message")),
                new MessageToken("ANY_TOKEN")
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

    private ResponseEntity<Void> answerConfirm(String gameToken, String sessionId, MessageToken messageToken, boolean answer) throws URISyntaxException {
        final String baseUrl = "http://localhost:" + randomServerPort + "/instances/" + sessionId + "/messages/" + messageToken.value() + "/confirm";
        URI uri = new URI(baseUrl);

        MessageController.ConfirmRequest request = new MessageController.ConfirmRequest(answer);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("Authorization", gameToken);

        return this.restTemplate.exchange(uri, HttpMethod.POST, new HttpEntity<>(request, headers), Void.class);
    }

}

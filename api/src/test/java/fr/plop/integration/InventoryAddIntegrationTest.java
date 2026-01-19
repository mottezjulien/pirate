package fr.plop.integration;

import com.fasterxml.jackson.core.JsonProcessingException;
import fr.plop.contexts.connect.presenter.ConnectionController;
import fr.plop.contexts.game.config.template.domain.model.Template;
import fr.plop.contexts.game.config.template.domain.usecase.TemplateInitUseCase;
import fr.plop.contexts.game.config.template.domain.usecase.generator.json.TemplateGeneratorJsonUseCase;
import fr.plop.contexts.game.session.core.domain.port.GameSessionClearPort;
import fr.plop.contexts.game.session.core.presenter.GameSessionController;
import fr.plop.contexts.game.session.inventory.presenter.GameSessionInventoryController;
import fr.plop.contexts.game.session.push.PushPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class InventoryAddIntegrationTest {

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

    private Template template;

    @BeforeEach
    void setUp() throws JsonProcessingException {
        sessionClear.clearAll();
        templateInitUseCase.deleteAll();

        // Create template with inventory item and auto-add via StepActive trigger
        TemplateGeneratorJsonUseCase generator = new TemplateGeneratorJsonUseCase();
        template = generator.apply("""
                {
                  "code": "INVENTORY_ADD_TEST",
                  "version": "1.0.0",
                  "label": "Test Inventory Add",
                  "inventory": {
                    "items": [
                      {
                        "ref": "ITEM_KEY",
                        "label": { "FR": "Cle doree", "EN": "Golden key" },
                        "image": { "type": "ASSET", "value": "items/key.png" },
                        "description": { "FR": "Une cle brillante", "EN": "A shiny key" },
                        "type": "UNIQUE"
                      },
                      {
                        "ref": "ITEM_COIN",
                        "label": { "FR": "Piece d'or", "EN": "Gold coin" },
                        "image": { "type": "ASSET", "value": "items/coin.png" },
                        "type": "COLLECTION"
                      }
                    ]
                  },
                  "scenario": {
                    "steps": [
                      {
                        "ref": "STEP_1",
                        "label": { "FR": "Etape 1", "EN": "Step 1" },
                        "targets": [
                          { "ref": "TARGET_1", "label": { "FR": "Objectif", "EN": "Goal" } }
                        ],
                        "possibilities": [
                          {
                            "trigger": { "type": "StepActive" },
                            "consequences": [
                              {
                                "type": "INVENTORY_ADD",
                                "metadata": { "itemRef": "ITEM_KEY" }
                              }
                            ]
                          }
                        ]
                      }
                    ]
                  }
                }
                """);

        templateInitUseCase.create(template);
    }

    @Test
    public void inventoryParsing_createsItems() {
        // Verify inventory items were parsed
        assertThat(template.inventory().items()).hasSize(2);
        assertThat(template.inventory().items().get(0).label().value(fr.plop.subs.i18n.domain.Language.FR)).isEqualTo("Cle doree");
        assertThat(template.inventory().items().get(1).label().value(fr.plop.subs.i18n.domain.Language.FR)).isEqualTo("Piece d'or");
    }

    @Test
    public void createSession_triggersInventoryAdd_itemAppearsInList() throws URISyntaxException, InterruptedException {
        // 1. Create auth
        ConnectionController.ResponseDTO connection = createAuth();

        // 2. Create game session
        GameSessionController.GameSessionActivedResponseDTO session = createGameSession(connection.token());
        assertThat(session.id()).isNotNull();

        // 3. Start the session - this triggers StepActive and adds the item
        startGameSession(session.gameToken(), session.id());

        // 4. Wait for async event processing
        Thread.sleep(500);

        // 5. Get inventory list
        List<GameSessionInventoryController.SimpleResponseDTO> inventory = getInventoryList(session.gameToken(), session.id());

        // 6. Verify the item was added
        assertThat(inventory).hasSize(1);
        assertThat(inventory.get(0).label()).isEqualTo("Cle doree");
        assertThat(inventory.get(0).count()).isEqualTo(1);
    }

    @Test
    public void collectionItem_canBeAddedMultipleTimes() throws URISyntaxException, JsonProcessingException, InterruptedException {
        // Create a template that adds the same COLLECTION item twice
        sessionClear.clearAll();
        templateInitUseCase.deleteAll();

        TemplateGeneratorJsonUseCase generator = new TemplateGeneratorJsonUseCase();
        Template templateMultiAdd = generator.apply("""
                {
                  "code": "INVENTORY_MULTI_ADD_TEST",
                  "version": "1.0.0",
                  "label": "Test Multi Add",
                  "inventory": {
                    "items": [
                      {
                        "ref": "ITEM_COIN",
                        "label": { "FR": "Piece d'or", "EN": "Gold coin" },
                        "image": { "type": "ASSET", "value": "items/coin.png" },
                        "type": "COLLECTION"
                      }
                    ]
                  },
                  "scenario": {
                    "steps": [
                      {
                        "ref": "STEP_1",
                        "label": { "FR": "Etape 1", "EN": "Step 1" },
                        "targets": [
                          { "ref": "TARGET_1", "label": { "FR": "Objectif", "EN": "Goal" } }
                        ],
                        "possibilities": [
                          {
                            "trigger": { "type": "StepActive" },
                            "consequences": [
                              { "type": "INVENTORY_ADD", "metadata": { "itemRef": "ITEM_COIN" } },
                              { "type": "INVENTORY_ADD", "metadata": { "itemRef": "ITEM_COIN" } },
                              { "type": "INVENTORY_ADD", "metadata": { "itemRef": "ITEM_COIN" } }
                            ]
                          }
                        ]
                      }
                    ]
                  }
                }
                """);

        templateInitUseCase.create(templateMultiAdd);

        // Create session
        ConnectionController.ResponseDTO connection = createAuth();
        GameSessionController.GameSessionActivedResponseDTO session = createGameSessionForTemplate(connection.token(), templateMultiAdd.id().value());

        // Start the session
        startGameSession(session.gameToken(), session.id());

        // Wait for async event processing
        Thread.sleep(500);

        // Get inventory
        List<GameSessionInventoryController.SimpleResponseDTO> inventory = getInventoryList(session.gameToken(), session.id());

        // Verify count is 3
        assertThat(inventory).hasSize(1);
        assertThat(inventory.get(0).label()).isEqualTo("Piece d'or");
        assertThat(inventory.get(0).count()).isEqualTo(3);
    }

    private ConnectionController.ResponseDTO createAuth() throws URISyntaxException {
        final String baseUrl = "http://localhost:" + randomServerPort + "/connect/";
        URI uri = new URI(baseUrl);

        ConnectionController.RequestDTO request = new ConnectionController.RequestDTO("inventoryTestDeviceId" + System.currentTimeMillis());

        ResponseEntity<ConnectionController.ResponseDTO> result = this.restTemplate.postForEntity(uri, request, ConnectionController.ResponseDTO.class);
        return result.getBody();
    }

    private GameSessionController.GameSessionActivedResponseDTO createGameSession(String token) throws URISyntaxException {
        return createGameSessionForTemplate(token, template.id().value());
    }

    private GameSessionController.GameSessionActivedResponseDTO createGameSessionForTemplate(String token, String templateId) throws URISyntaxException {
        final String baseUrl = "http://localhost:" + randomServerPort + "/sessions/";
        URI uri = new URI(baseUrl);

        GameSessionController.GameSessionCreateRequest request = new GameSessionController.GameSessionCreateRequest(templateId);
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.add("Authorization", token);

        ResponseEntity<GameSessionController.GameSessionActivedResponseDTO> result = this.restTemplate
                .exchange(uri, HttpMethod.POST, new HttpEntity<>(request, headers), GameSessionController.GameSessionActivedResponseDTO.class);
        return result.getBody();
    }

    private void startGameSession(String gameToken, String sessionId) throws URISyntaxException {
        final String baseUrl = "http://localhost:" + randomServerPort + "/sessions/" + sessionId + "/start/";
        URI uri = new URI(baseUrl);

        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.add("Authorization", gameToken);

        this.restTemplate.exchange(uri, HttpMethod.POST, new HttpEntity<>(headers), GameSessionController.GameSessionStoppedResponseDTO.class);
    }

    private List<GameSessionInventoryController.SimpleResponseDTO> getInventoryList(String gameToken, String sessionId) throws URISyntaxException {
        final String baseUrl = "http://localhost:" + randomServerPort + "/sessions/" + sessionId + "/inventory/";
        URI uri = new URI(baseUrl);

        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.add("Authorization", gameToken);
        headers.add("Language", "FR");

        ResponseEntity<List<GameSessionInventoryController.SimpleResponseDTO>> result = this.restTemplate
                .exchange(uri, HttpMethod.GET, new HttpEntity<>(headers),
                        new ParameterizedTypeReference<List<GameSessionInventoryController.SimpleResponseDTO>>() {});
        return result.getBody();
    }
}

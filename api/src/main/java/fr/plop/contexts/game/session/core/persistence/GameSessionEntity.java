package fr.plop.contexts.game.session.core.persistence;

import fr.plop.contexts.game.config.board.persistence.entity.BoardConfigEntity;
import fr.plop.contexts.game.config.scenario.persistence.core.ScenarioConfigEntity;
import fr.plop.contexts.game.session.core.domain.model.GameSession;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "TEST2_GAME_SESSION")
public class GameSessionEntity {

    @Id
    private String id;

    @Column(name = "template_id")
    private String templateId;

    @Column(name = "template_version")
    private String templateVersion;

    private String label;

    @OneToMany(mappedBy = "session")
    private Set<GamePlayerEntity> players = new HashSet<>();

    @ManyToOne
    @JoinColumn(name = "scenario_config_id")
    private ScenarioConfigEntity scenario;

    @ManyToOne
    @JoinColumn(name = "board_config_id")
    private BoardConfigEntity board;

    @Enumerated(EnumType.STRING)
    private GameSession.State state;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTemplateId() {
        return templateId;
    }

    public void setTemplateId(String templateId) {
        this.templateId = templateId;
    }

    public String getTemplateVersion() {
        return templateVersion;
    }

    public void setTemplateVersion(String templateVersion) {
        this.templateVersion = templateVersion;
    }

    public Set<GamePlayerEntity> getPlayers() {
        return players;
    }

    public void setPlayers(Set<GamePlayerEntity> players) {
        this.players = players;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public ScenarioConfigEntity getScenario() {
        return scenario;
    }

    public void setScenario(ScenarioConfigEntity scenario) {
        this.scenario = scenario;
    }

    public BoardConfigEntity getBoard() {
        return board;
    }

    public void setBoard(BoardConfigEntity board) {
        this.board = board;
    }

    public GameSession.State getState() {
        return state;
    }

    public void setState(GameSession.State state) {
        this.state = state;
    }

    /*public Game toModel() {
        return new Game(new Game.Id(id), label,
                new Template.Id(templateId), templateVersion,
                scenario.toModel(), board.toModel(), state);
    }*/

}

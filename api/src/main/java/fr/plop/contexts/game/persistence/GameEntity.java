package fr.plop.contexts.game.persistence;

import fr.plop.contexts.board.persistence.entity.BoardEntity;
import fr.plop.contexts.game.domain.model.Game;
import fr.plop.contexts.scenario.persistence.core.ScenarioEntity;
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
@Table(name = "TEST2_GAME")
public class GameEntity {

    @Id
    private String id;

    @Column(name = "template_id")
    private String templateId;

    @Column(name = "template_version")
    private String templateVersion;

    private String label;

    @OneToMany(mappedBy = "game")
    private Set<GamePlayerEntity> players = new HashSet<>();

    @ManyToOne
    @JoinColumn(name = "scenario_id")
    private ScenarioEntity scenario;

    @ManyToOne
    @JoinColumn(name = "board_id")
    private BoardEntity board;

    @Enumerated(EnumType.STRING)
    private Game.State state;

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

    public ScenarioEntity getScenario() {
        return scenario;
    }

    public void setScenario(ScenarioEntity scenario) {
        this.scenario = scenario;
    }

    public BoardEntity getBoard() {
        return board;
    }

    public void setBoard(BoardEntity board) {
        this.board = board;
    }

    public Game.State getState() {
        return state;
    }

    public void setState(Game.State state) {
        this.state = state;
    }

    /*public Game toModel() {
        return new Game(new Game.Id(id), label,
                new Template.Id(templateId), templateVersion,
                scenario.toModel(), board.toModel(), state);
    }*/

}

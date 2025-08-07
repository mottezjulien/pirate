package fr.plop.contexts.game.session.core.persistence;

import fr.plop.contexts.game.config.board.persistence.entity.BoardConfigEntity;
import fr.plop.contexts.game.config.map.persistence.MapConfigEntity;
import fr.plop.contexts.game.config.scenario.persistence.core.ScenarioConfigEntity;
import fr.plop.contexts.game.config.template.persistence.TemplateEntity;
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

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "TEST2_GAME_SESSION")
public class GameSessionEntity {

    @Id
    private String id;

    @ManyToOne
    @JoinColumn(name = "template_id")
    private TemplateEntity template;

    private String label;

    @OneToMany(mappedBy = "session")
    private Set<GamePlayerEntity> players = new HashSet<>();

    @ManyToOne
    @JoinColumn(name = "scenario_config_id")
    private ScenarioConfigEntity scenario;

    @ManyToOne
    @JoinColumn(name = "board_config_id")
    private BoardConfigEntity board;

    @ManyToOne
    @JoinColumn(name = "map_config_id")
    private MapConfigEntity map;

    @Enumerated(EnumType.STRING)
    private GameSession.State state;

    @Column(name = "start_at")
    private Instant startAt;

    @Column(name = "over_at")
    private Instant overAt;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public TemplateEntity getTemplate() {
        return template;
    }

    public void setTemplate(TemplateEntity template) {
        this.template = template;
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

    public MapConfigEntity getMap() {
        return map;
    }

    public void setMap(MapConfigEntity map) {
        this.map = map;
    }

    public GameSession.State getState() {
        return state;
    }

    public void setState(GameSession.State state) {
        this.state = state;
    }

    public Instant getStartAt() {
        return startAt;
    }

    public void setStartAt(Instant startAt) {
        this.startAt = startAt;
    }

    public Instant getOverAt() {
        return overAt;
    }

    public void setOverAt(Instant overAt) {
        this.overAt = overAt;
    }
}

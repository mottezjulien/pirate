package fr.plop.contexts.game.config.template.persistence;

import fr.plop.contexts.game.config.board.persistence.entity.BoardConfigEntity;
import fr.plop.contexts.game.config.map.persistence.MapConfigEntity;
import fr.plop.contexts.game.config.scenario.persistence.core.ScenarioConfigEntity;
import fr.plop.contexts.game.config.talk.persistence.TalkConfigEntity;
import fr.plop.contexts.game.config.template.domain.model.Template;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.Duration;

@Entity
@Table(name = "TEST2_TEMPLATE")
public class TemplateEntity {

    @Id
    private String id;

    private String code;

    private String label;

    private String version;

    @ManyToOne
    @JoinColumn(name = "scenario_id")
    private ScenarioConfigEntity scenario;

    @ManyToOne
    @JoinColumn(name = "board_id")
    private BoardConfigEntity board;

    @ManyToOne
    @JoinColumn(name = "map_id")
    private MapConfigEntity map;

    @ManyToOne
    @JoinColumn(name = "talk_id")
    private TalkConfigEntity talk;

    @Column(name = "duration_in_minute")
    private long durationInMinute;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
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

    public TalkConfigEntity getTalk() {
        return talk;
    }

    public void setTalk(TalkConfigEntity talk) {
        this.talk = talk;
    }

    public long getDurationInMinute() {
        return durationInMinute;
    }

    public void setDurationInMinute(long durationInMinute) {
        this.durationInMinute = durationInMinute;
    }

    public Template toModel() {
        Template.Id id = new Template.Id(this.id);
        Template.Atom atom = new Template.Atom(id, new Template.Code(code));
        Duration duration = Duration.ofMinutes(durationInMinute);
        return new Template(atom, label, version, duration, scenario.toModel(),
                board.toModel(), map.toModel(), talk.toModel());
    }
}

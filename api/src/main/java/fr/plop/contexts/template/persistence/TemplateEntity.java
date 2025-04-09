package fr.plop.contexts.template.persistence;

import fr.plop.contexts.board.persistence.entity.BoardEntity;
import fr.plop.contexts.scenario.persistence.core.ScenarioEntity;
import fr.plop.contexts.template.domain.model.Template;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

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
    private ScenarioEntity scenario;

    @ManyToOne
    @JoinColumn(name = "board_id")
    private BoardEntity board;

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

    public Template toModel() {
        Template.Id id = new Template.Id(this.id);
        Template.Atom atom = new Template.Atom(id, new Template.Code(code));
        return new Template(atom, label, version, scenario.toModel(), board.toModel());
    }
}

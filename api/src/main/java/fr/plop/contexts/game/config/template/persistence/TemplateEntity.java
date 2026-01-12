package fr.plop.contexts.game.config.template.persistence;

import fr.plop.contexts.game.config.Image.persistence.ImageConfigEntity;
import fr.plop.contexts.game.config.board.persistence.entity.BoardConfigEntity;
import fr.plop.contexts.game.config.map.persistence.MapConfigEntity;
import fr.plop.contexts.game.config.scenario.persistence.core.ScenarioConfigEntity;
import fr.plop.contexts.game.config.talk.persistence.TalkConfigEntity;
import fr.plop.contexts.game.config.template.domain.model.Template;
import fr.plop.generic.position.Address;
import fr.plop.generic.position.Location;
import fr.plop.generic.position.Point;
import fr.plop.generic.position.Rectangle;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Duration;

@Entity
@Table(name = "TEST2_TEMPLATE")
public class TemplateEntity {

    @Id
    private String id;

    private String code;

    private String label;

    private String version;

    //Between 1 and 5
    private int level;

    private String description;

    private String departureAddress;
    private BigDecimal departureBottomLeftLat;
    private BigDecimal departureBottomLeftLng;
    private BigDecimal departureTopRightLat;
    private BigDecimal departureTopRightLng;


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

    @ManyToOne
    @JoinColumn(name = "image_id")
    private ImageConfigEntity image;

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

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDepartureAddress() {
        return departureAddress;
    }

    public void setDepartureAddress(String departureAddress) {
        this.departureAddress = departureAddress;
    }

    public BigDecimal getDepartureBottomLeftLat() {
        return departureBottomLeftLat;
    }

    public void setDepartureBottomLeftLat(BigDecimal departureBottomLeftLat) {
        this.departureBottomLeftLat = departureBottomLeftLat;
    }

    public BigDecimal getDepartureBottomLeftLng() {
        return departureBottomLeftLng;
    }

    public void setDepartureBottomLeftLng(BigDecimal departureBottomLeftLng) {
        this.departureBottomLeftLng = departureBottomLeftLng;
    }

    public BigDecimal getDepartureTopRightLat() {
        return departureTopRightLat;
    }

    public void setDepartureTopRightLat(BigDecimal departureTopRightLat) {
        this.departureTopRightLat = departureTopRightLat;
    }

    public BigDecimal getDepartureTopRightLng() {
        return departureTopRightLng;
    }

    public void setDepartureTopRightLng(BigDecimal departureTopRightLng) {
        this.departureTopRightLng = departureTopRightLng;
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

    public ImageConfigEntity getImage() {
        return image;
    }

    public void setImage(ImageConfigEntity image) {
        this.image = image;
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

    public void setDurationInMinute(long durationInMinute) {
        this.durationInMinute = durationInMinute;
    }

    public Template toModel() {
        Template.Id id = new Template.Id(this.id);
        Template.Atom atom = new Template.Atom(id, new Template.Code(code));
        Template.Descriptor descriptor = toModelDescriptor();

        Duration duration = Duration.ofMinutes(durationInMinute);
        return new Template(atom, label, version, descriptor, duration, scenario.toModel(),
                board.toModel(), map.toModel(), talk.toModel(), image.toModel());
    }

    private Template.Descriptor toModelDescriptor() {
        Rectangle rectangle = Rectangle.ofPoints(Point.from(departureBottomLeftLat, departureBottomLeftLng), Point.from(departureTopRightLat, departureTopRightLng));
        Location departure = new Location(Address.fromString(departureAddress), rectangle);
        return new Template.Descriptor(Template.Descriptor.Level.from(level), description, departure);
    }
}

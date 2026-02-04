package fr.plop.contexts.game.config.template.persistence;

import fr.plop.contexts.game.commun.persistence.GameEntity;
import fr.plop.contexts.game.config.Image.persistence.ImageConfigEntity;
import fr.plop.contexts.game.config.board.persistence.entity.BoardConfigEntity;
import fr.plop.contexts.game.config.inventory.persistence.GameConfigInventoryEntity;
import fr.plop.contexts.game.config.map.persistence.MapConfigEntity;
import fr.plop.contexts.game.config.scenario.persistence.core.ScenarioConfigEntity;
import fr.plop.contexts.game.config.talk.persistence.TalkConfigEntity;
import fr.plop.contexts.game.config.template.domain.model.Template;
import jakarta.persistence.*;

import java.time.Duration;

@Entity
@Table(name = "LO_TEMPLATE")
public class TemplateEntity {

    @Id
    private String id;

    @ManyToOne
    @JoinColumn(name = "game_id")
    private GameEntity game;

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

    @ManyToOne
    @JoinColumn(name = "inventory_id")
    private GameConfigInventoryEntity inventory;

    @Column(name = "duration_in_minute")
    private long durationInMinute;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public GameEntity getGame() {
        return game;
    }

    public void setGame(GameEntity game) {
        this.game = game;
    }

    public long getDurationInMinute() {
        return durationInMinute;
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

    public GameConfigInventoryEntity getInventory() {
        return inventory;
    }

    public void setInventory(GameConfigInventoryEntity inventory) {
        this.inventory = inventory;
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
        //Template.Atom atom = new Template.Atom(id, new Template.Code(code));
        //Template.Descriptor descriptor = toModelDescriptor();

        Duration duration = Duration.ofMinutes(durationInMinute);
        return new Template(id, duration, scenario.toModel(),
                board.toModel(), map.toModel(), talk.toModel(), image.toModel(), inventory.toModel());
    }

    /*private Template.Descriptor toModelDescriptor() {
        Rectangle rectangle = Rectangle.ofPoints(Point.from(departureBottomLeftLat, departureBottomLeftLng), Point.from(departureTopRightLat, departureTopRightLng));
        Location departure = new Location(Address.fromString(departureAddress), rectangle);
        return new Template.Descriptor(Template.Descriptor.Level.from(level), Template.Descriptor.Visibility.Visible, description, departure);
    }*/
}

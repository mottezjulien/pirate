package fr.plop.contexts.game.session.core.persistence;


import fr.plop.contexts.game.config.scenario.domain.model.Possibility;
import fr.plop.contexts.game.config.scenario.persistence.possibility.ScenarioPossibilityEntity;
import fr.plop.contexts.game.session.core.domain.model.GameAction;
import fr.plop.contexts.game.session.core.domain.model.GamePlayer;
import fr.plop.contexts.game.session.time.GameSessionTimeUnit;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "TEST2_GAME_PLAYER_ACTION")
public class GamePlayerActionEntity {

    @Id
    private String id;

    @ManyToOne
    @JoinColumn(name = "player_id")
    private GamePlayerEntity player;

    @ManyToOne
    @JoinColumn(name = "possibility_id")
    private ScenarioPossibilityEntity possibility;

    @Column(name = "time_click_minute")
    private int timeClickMinute;

    private Instant date;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public GamePlayerEntity getPlayer() {
        return player;
    }

    public void setPlayer(GamePlayerEntity player) {
        this.player = player;
    }

    public ScenarioPossibilityEntity getPossibility() {
        return possibility;
    }

    public void setPossibility(ScenarioPossibilityEntity possibility) {
        this.possibility = possibility;
    }

    public int getTimeClickMinute() {
        return timeClickMinute;
    }

    public void setTimeInMinutes(int timeClickMinute) {
        this.timeClickMinute = timeClickMinute;
    }

    public Instant getDate() {
        return date;
    }

    public void setDate(Instant date) {
        this.date = date;
    }

    public GameAction toModel() {
        GamePlayer.Id playerId = new GamePlayer.Id(player.getId());
        Possibility.Id possibilityId = new Possibility.Id(possibility.getId());
        return new GameAction(playerId, possibilityId, GameSessionTimeUnit.ofMinutes(timeClickMinute));
    }
}

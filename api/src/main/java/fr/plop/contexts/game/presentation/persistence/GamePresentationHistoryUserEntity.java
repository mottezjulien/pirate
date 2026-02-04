package fr.plop.contexts.game.presentation.persistence;


import fr.plop.contexts.game.config.scenario.persistence.core.ScenarioTargetEntity;
import fr.plop.contexts.user.persistence.UserEntity;
import jakarta.persistence.*;

import java.util.HashSet;
import java.util.Set;


@Entity
@Table(name = "LO_PRESENTATION_HISTORY_USER")
public class GamePresentationHistoryUserEntity {

    @Id
    private String id;

    @ManyToOne
    @JoinColumn(name = "history_id")
    private GamePresentationHistoryEntity history;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private UserEntity user;


    @ManyToMany
    @JoinTable(name = "LO_PRESENTATION_HISTORY_USER_ACHIEVEMENT",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "achievement_id"))
    private Set<GamePresentationAchievementEntity>  achievements = new HashSet<>();

    @ManyToOne
    @JoinColumn(name = "scenario_target_id")
    private ScenarioTargetEntity scenarioTarget;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public GamePresentationHistoryEntity getHistory() {
        return history;
    }

    public void setHistory(GamePresentationHistoryEntity history) {
        this.history = history;
    }

    public UserEntity getUser() {
        return user;
    }

    public void setUser(UserEntity user) {
        this.user = user;
    }

    public Set<GamePresentationAchievementEntity> getAchievements() {
        return achievements;
    }

    public void setAchievements(Set<GamePresentationAchievementEntity> achievements) {
        this.achievements = achievements;
    }

    public ScenarioTargetEntity getScenarioTarget() {
        return scenarioTarget;
    }

    public void setScenarioTarget(ScenarioTargetEntity scenarioTarget) {
        this.scenarioTarget = scenarioTarget;
    }
}

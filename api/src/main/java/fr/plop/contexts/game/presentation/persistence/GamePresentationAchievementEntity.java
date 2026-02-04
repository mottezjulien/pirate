package fr.plop.contexts.game.presentation.persistence;

import fr.plop.contexts.game.config.Image.persistence.ImageGenericEntity;
import fr.plop.contexts.game.presentation.domain.Presentation;
import fr.plop.subs.i18n.persistence.I18nEntity;
import jakarta.persistence.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "LO_PRESENTATION_ACHIEVEMENT")
public class GamePresentationAchievementEntity {

    @Id
    private String id;

    @ManyToOne
    @JoinColumn(name = "presentation_id")
    private GamePresentationEntity presentation;

    @ManyToOne
    @JoinColumn(name = "label_id")
    private I18nEntity label;

    @ManyToOne
    @JoinColumn(name = "image_generic_id")
    private ImageGenericEntity image;

    @ManyToMany(mappedBy = "achievements")
    private Set<GamePresentationHistoryUserEntity> users = new HashSet<>();

    public Presentation.Achievement toModel() {
        return new Presentation.Achievement(new Presentation.Achievement.Id(id));
    }
}

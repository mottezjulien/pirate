package fr.plop.contexts.game.presentation.persistence;

import fr.plop.contexts.game.commun.persistence.GameEntity;
import fr.plop.contexts.game.presentation.domain.Presentation;
import fr.plop.contexts.user.User;
import fr.plop.generic.position.Address;
import fr.plop.generic.position.Location;
import fr.plop.generic.position.Point;
import fr.plop.generic.position.Rectangle;
import fr.plop.subs.i18n.persistence.I18nEntity;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "LO_PRESENTATION")
public class GamePresentationEntity {

    @Id
    private String id;

    @ManyToOne
    @JoinColumn(name = "game_id")
    private GameEntity game;

    @ManyToOne
    @JoinColumn(name = "label_id")
    private I18nEntity label;

    @ManyToOne
    @JoinColumn(name = "description_id")
    private I18nEntity description;

    private int level;

    @Enumerated(EnumType.STRING)
    private Presentation.Visibility visibility;

    private String departureAddress;
    private BigDecimal departureBottomLeftLat;
    private BigDecimal departureBottomLeftLng;
    private BigDecimal departureTopRightLat;
    private BigDecimal departureTopRightLng;

    @ManyToOne
    @JoinColumn(name = "author_id")
    private GamePresentationAuthor author;

    @ElementCollection(targetClass = Presentation.ParticipantType.class)
    @JoinTable(name = "LO_PRESENTATION_PARTICIPANT_TYPE", joinColumns = @JoinColumn(name = "presentation_id"))
    @Column(name = "participant_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private Set<Presentation.ParticipantType> participantTypes = new HashSet<>();

    @Column(name = "participant_min")
    private Integer participantMin;

    @Column(name = "participant_max")
    private Integer participantMax;

    @ElementCollection(targetClass = Presentation.GameType.class)
    @JoinTable(name = "LO_PRESENTATION_GAME_TYPE", joinColumns = @JoinColumn(name = "presentation_id"))
    @Column(name = "game_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private Set<Presentation.GameType> gameTypes = new HashSet<>();

    @OneToMany(mappedBy = "presentation")
    private final Set<GamePresentationAchievementEntity> achievements = new HashSet<>();

    @OneToMany(mappedBy = "presentation")
    private final Set<GamePresentationReviewEntity> reviews = new HashSet<>();

    private float reviewRating;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Set<GamePresentationAchievementEntity> getAchievements() {
        return achievements;
    }


    public Set<Presentation.ParticipantType> getParticipantTypes() {
        return participantTypes;
    }

    public void setParticipantTypes(Set<Presentation.ParticipantType> participantTypes) {
        this.participantTypes = participantTypes;
    }

    public Set<Presentation.GameType> getGameTypes() {
        return gameTypes;
    }

    public void setGameTypes(Set<Presentation.GameType> gameTypes) {
        this.gameTypes = gameTypes;
    }

    public GamePresentationAuthor getAuthor() {
        return author;
    }

    public void setAuthor(GamePresentationAuthor author) {
        this.author = author;
    }

    public BigDecimal getDepartureTopRightLng() {
        return departureTopRightLng;
    }

    public void setDepartureTopRightLng(BigDecimal departureTopRightLng) {
        this.departureTopRightLng = departureTopRightLng;
    }

    public BigDecimal getDepartureTopRightLat() {
        return departureTopRightLat;
    }

    public void setDepartureTopRightLat(BigDecimal departureTopRightLat) {
        this.departureTopRightLat = departureTopRightLat;
    }

    public BigDecimal getDepartureBottomLeftLng() {
        return departureBottomLeftLng;
    }

    public void setDepartureBottomLeftLng(BigDecimal departureBottomLeftLng) {
        this.departureBottomLeftLng = departureBottomLeftLng;
    }

    public BigDecimal getDepartureBottomLeftLat() {
        return departureBottomLeftLat;
    }

    public void setDepartureBottomLeftLat(BigDecimal departureBottomLeftLat) {
        this.departureBottomLeftLat = departureBottomLeftLat;
    }

    public String getDepartureAddress() {
        return departureAddress;
    }

    public void setDepartureAddress(String departureAddress) {
        this.departureAddress = departureAddress;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public Presentation.Visibility getVisibility() {
        return visibility;
    }

    public void setVisibility(Presentation.Visibility visibility) {
        this.visibility = visibility;
    }

    public void setParticipantMin(Integer participantMin) {
        this.participantMin = participantMin;
    }

    public void setParticipantMax(Integer participantMax) {
        this.participantMax = participantMax;
    }

    public I18nEntity getDescription() {
        return description;
    }

    public void setDescription(I18nEntity description) {
        this.description = description;
    }

    public I18nEntity getLabel() {
        return label;
    }

    public void setLabel(I18nEntity label) {
        this.label = label;
    }

    public GameEntity getGame() {
        return game;
    }

    public void setGame(GameEntity game) {
        this.game = game;
    }

    public Set<GamePresentationReviewEntity> getReviews() {
        return reviews;
    }

    public float getReviewRating() {
        return reviewRating;
    }

    public void setReviewRating(float reviewRating) {
        this.reviewRating = reviewRating;
    }

    public Presentation toModel(User.Id userId) {
        Point bottomLeft = new Point(departureBottomLeftLat, departureBottomLeftLng);
        Point topRight = new Point(departureTopRightLat, departureTopRightLng);
        Rectangle rectangle = new Rectangle(bottomLeft, topRight);
        Location departure = new Location(Address.fromString(departureAddress), rectangle);
        return new Presentation(new Presentation.Id(id), label.toModel(), description.toModel(),
                Presentation.Level.from(level), visibility, departure, List.copyOf(gameTypes),
                participantMin, participantMax, List.copyOf(participantTypes),
                achievements.stream().map(GamePresentationAchievementEntity::toModel).toList(),
                reviews.stream().map(GamePresentationReviewEntity::toModel).toList());
    }
}

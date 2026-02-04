package fr.plop.contexts.user.persistence;

import fr.plop.contexts.game.instance.core.persistence.GamePlayerEntity;
import fr.plop.contexts.user.User;
import fr.plop.generic.tools.StringTools;
import fr.plop.subs.i18n.domain.Language;
import jakarta.persistence.*;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Entity
@Table(name = "LO_USER")
public class UserEntity {

    @Id
    private String id;

    @Enumerated(EnumType.STRING)
    private User.Type type;

    @Enumerated(EnumType.STRING)
    private Language language;

    @Column(name = "nickname")
    private String nullableNickName;

    @Column(name = "optEmail")
    private String nullableEmail;

    @OneToMany(mappedBy = "user")
    private final Set<GamePlayerEntity> players = new HashSet<>();

    public boolean is(String otherId) {
        return id.equals(otherId);
    }

    public String getId() {
        return id;
    }

    public Language getLanguage() {
        return language;
    }

    public void setType(User.Type type) {
        this.type = type;
    }

    public void setLanguage(Language language) {
        this.language = language;
    }

    public void setNickName(String nickName) {
        this.nullableNickName = nickName;
    }

    public void setEmail(String email) {
        this.nullableEmail = email;
    }

    public static UserEntity buildNone() {
        UserEntity entity = new UserEntity();
        entity.id = StringTools.generate();
        entity.type = User.Type.NONE;
        entity.language = Language.byDefault();
        return entity;
    }

    public static UserEntity buildWithModelId(User.Id userId) {
        UserEntity entity = new UserEntity();
        entity.id = userId.value();
        return entity;
    }

    public User toModel() {
        return new User(toModelId(), type, language,
                Optional.ofNullable(nullableNickName), Optional.ofNullable(nullableEmail));
    }

    public User.Id toModelId() {
        return new User.Id(id);
    }
}

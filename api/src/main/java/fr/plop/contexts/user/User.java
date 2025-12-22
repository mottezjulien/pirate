package fr.plop.contexts.user;
import fr.plop.subs.i18n.domain.Language;
import java.util.Optional;

public record User(Id id, Type type, Language language, Optional<String> optNickName, Optional<String> optEmail) {

    public record Id(String value) {

    }

    public enum Type {
        NONE,
        ONBOARDED,

    }

    public User(Id id) {
        this(id, Type.NONE, Language.byDefault(), Optional.empty(), Optional.empty());
    }

    public User type(Type type) {
        return new User(id, type, language, optNickName, optEmail);
    }

    public User language(Language language) {
        return new User(id, type, language, optNickName, optEmail);
    }

    public User nickName(String nickName) {
        return new User(id, type, language, Optional.ofNullable(nickName), optEmail);
    }

    public User email(String email) {
        return new User(id, type, language, optNickName, Optional.ofNullable(email));
    }

}

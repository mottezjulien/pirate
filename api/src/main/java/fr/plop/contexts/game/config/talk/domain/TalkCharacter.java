package fr.plop.contexts.game.config.talk.domain;

import fr.plop.subs.image.Image;

public record TalkCharacter(String name, Image image) {
    public static TalkCharacter nobody() {
        return new TalkCharacter("", Image.no());
    }
}

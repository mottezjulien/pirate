package fr.plop.contexts.game.config.talk.domain;

import fr.plop.generic.tools.StringTools;
import fr.plop.subs.image.Image;

public record TalkCharacter(Id id, String name) {

    public record Id(String value){
        public Id() {
            this(StringTools.generate());
        }
    }

    public record Reference(Id id, TalkCharacter character, String value, Image image) {

        public record Id(String value){
            public Id() {
                this(StringTools.generate());
            }
        }

        public Reference(TalkCharacter character, String header, Image asset) {
            this(new Id(), character, header, asset);
        }

        public boolean hasSameValue(Reference other) {
            return other.value.equals(value);
        }

    }

    public TalkCharacter(String name) {
        this(new Id(), name);
    }

    public boolean hasSameName(TalkCharacter other) {
        return other.name.equals(name);
    }

}


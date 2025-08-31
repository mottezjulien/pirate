package fr.plop.contexts.game.config.talk;


/*
public record Talk(Id id, Item root) {

    public record Id(String value) {

    }

    public record Item(Id id, I18n value, Next next) {

        public record Id(String value) {

        }

    }

    public sealed interface Next permits Next.Stop, Next.Continue, Next.QCM {

        sealed interface Simple permits Next.Stop, Next.Continue { }
        record Continue(Item.Id itemId) implements Next, Simple { }
        record Stop() implements Next, Simple { }

        record QCM(List<Option> options) implements Next {
            public record Option(Id id, I18n label, Simple next, List<Consequence> consequences) {
                public record Id(String value) {

                }
            }
        }

    }

}*/

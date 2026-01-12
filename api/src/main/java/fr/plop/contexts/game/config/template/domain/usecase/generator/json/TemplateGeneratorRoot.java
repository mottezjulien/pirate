package fr.plop.contexts.game.config.template.domain.usecase.generator.json;

import java.util.List;
import java.util.Map;

public record TemplateGeneratorRoot(String code, String version, String label, int duration, int level, String description, Departure departure,
                                    Board board, List<TemplateGeneratorMap> maps, Talk talk, Scenario scenario, TemplateGeneratorImage image) {


    public record Departure(String address, Rectangle rectangle) {

    }


    public record Image(String type, String value) {

    }

    public record Position(double top, double left) {

    }

    public  record Point(double lat, double lng) {

    }

    public record Rectangle(Point bottomLeft, Point topRight) {

        public fr.plop.generic.position.Rectangle toModel() {
            return new fr.plop.generic.position.Rectangle(
                    fr.plop.generic.position.Point.from(bottomLeft().lat(), bottomLeft().lng()),
                    fr.plop.generic.position.Point.from(topRight().lat(), topRight().lng()));
        }
    }

    public record Board(List<Spaces> spaces) {

        public record Spaces(String ref, String label, String priority, List<Rectangle> rectangles) {

        }
    }


    public record TemplateGeneratorMap(String label, String priority, Image image, Image pointer,
                      List<MapSpace> spaces, List<MapObject> objects) {
        public record MapSpace(Position position, String ref) {

        }

        public record MapObject(Position position, String priority, Image image, MetaDataPoint point, Condition condition) {
            public record MetaDataPoint(String color) {

            }
        }
    }

    public record Talk(List<Character> characters, List<Item> items) {

        public record Character(String ref, List<CharacterImage> images) {
            public record CharacterImage(String ref, Image value) {

            }
        }

        public record Item(String ref,  Map<String, String> value, List<Option> options, String next, ItemCharacter character) {
            public record ItemCharacter(String character, String image) {

            }

            public record Option(String ref, Map<String, String> value, String next, Condition condition) {

            }
        }
    }

    public record Scenario(List<Step> steps) {
        public record Step(String ref,  Map<String, String> label, List<Target> targets, List<Possibility> possibilities) {

            public record Target(String ref,  Map<String, String> label,  Map<String, String> description, boolean optional) {

            }

            public record Possibility(Trigger trigger, List<Consequence> consequences, Condition condition, Recurrence recurrence) {

                public record Trigger(String type, String value) {

                }

                public record Recurrence(String type, Integer value) {

                }

            }

        }
    }
    public record Consequence(String type, Map<String, Object> metadata) {

    }

    public record Condition(String type, Map<String, Object> metadata, List<Condition> children) {

    }

    public record TemplateGeneratorImage(List<ImageItem> items) {
        public record ImageItem(TemplateGeneratorImageGeneric generic) {
            public record TemplateGeneratorImageGeneric(Image value, List<ImageObject> objects) {
                public record ImageObject(String type, Map<String, Object> metadata) {

                }
            }
        }
    }

}






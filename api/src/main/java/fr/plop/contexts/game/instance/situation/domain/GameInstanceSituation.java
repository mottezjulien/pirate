package fr.plop.contexts.game.instance.situation.domain;

import fr.plop.contexts.game.config.board.domain.model.BoardSpace;
import fr.plop.contexts.game.config.inventory.domain.model.GameConfigInventoryItem;
import fr.plop.contexts.game.config.scenario.domain.model.ScenarioConfig;
import fr.plop.contexts.game.config.talk.domain.TalkCharacter;
import fr.plop.contexts.game.config.talk.domain.TalkItem;
import fr.plop.contexts.game.instance.time.GameInstanceTimeUnit;

import java.util.List;
import java.util.Map;

public record GameInstanceSituation(Board board, Scenario scenario, Talk talk, Inventory inventory, Time time) {

    public GameInstanceSituation() {
        this(new Board(), new Scenario(), new Talk(), new Inventory(), new Time());
    }

    public record Board(List<BoardSpace.Id> spaceIds) {

        public Board() {
            this(List.of());
        }
    }

    public record Scenario(List<ScenarioConfig.Step.Id> stepIds, List<ScenarioConfig.Target.Id> targetIds) {
        public Scenario() {
            this(List.of(), List.of());
        }
    }

    public record Talk(Map<TalkCharacter.Id, List<TalkItem.Id>> talks) {

        public Talk() {
            this(Map.of());
        }

        public boolean hasTalk(TalkCharacter.Id characterId) {
            return talks.containsKey(characterId);
        }
    }

    public record Inventory(List<GameConfigInventoryItem.Id> itemIds) {
        public Inventory() {
            this(List.of());
        }

        public boolean has(GameConfigInventoryItem.Id itemId) {
            return itemIds.contains(itemId);
        }
    }

    public record Time(GameInstanceTimeUnit current) {
        public Time() {
            this(GameInstanceTimeUnit.ofMinutes(0));
        }
    }




}

package fr.plop.contexts.game.config.map.persistence;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("GLOBAL")
public class MapConfigItemGlobalEntity extends MapConfigItemAbstractEntity {

}

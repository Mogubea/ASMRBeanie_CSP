package me.mogubea.entities;

import org.bukkit.entity.Entity;
import org.bukkit.persistence.PersistentDataContainer;

public interface IMoguEntity {

    void postCreation();

    /**
     * Called when replacing a pre-existing vanilla entity with a custom entity. This method transfers valid {@link PersistentDataContainer} entries
     * over to the new custom entity.
     */
    void transferData(Entity oldEntity, PersistentDataContainer container);

}

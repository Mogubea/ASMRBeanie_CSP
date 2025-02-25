package me.mogubea.entities;

import io.papermc.paper.event.player.PlayerNameEntityEvent;
import me.mogubea.listeners.EventListener;
import me.mogubea.listeners.ListenerManager;
import org.bukkit.craftbukkit.entity.CraftEntity;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.world.EntitiesLoadEvent;
import org.jetbrains.annotations.NotNull;

public class CustomEntityListener extends EventListener {

    private final @NotNull CustomEntityManager entityManager;

    public CustomEntityListener(@NotNull ListenerManager manager, @NotNull CustomEntityManager entityManager) {
        super(manager);
        this.entityManager = entityManager;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onChunkLoad(EntitiesLoadEvent e) {
        for (int x = -1; ++x < e.getEntities().size();) {
            Entity entity = e.getEntities().get(x);

            if (entity.getPersistentDataContainer().isEmpty()) continue;
            if (((CraftEntity)entity).getHandle() instanceof IMoguEntity) continue;

            entityManager.tryToConvert(entity);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onEntityRename(PlayerNameEntityEvent e) {
        if (((CraftEntity)e.getEntity()).getHandle() instanceof IMoguEntity)
            e.setCancelled(true);
    }

}

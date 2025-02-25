package me.mogubea.items;

import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.jetbrains.annotations.NotNull;

public interface IEntityInteract {

    void onEntityInteract(@NotNull PlayerInteractEntityEvent event);

}

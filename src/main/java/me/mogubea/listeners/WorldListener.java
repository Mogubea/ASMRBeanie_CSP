package me.mogubea.listeners;

import me.mogubea.events.WorldDayChangeEvent;
import me.mogubea.main.Main;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.world.WorldSaveEvent;
import org.jetbrains.annotations.NotNull;

public class WorldListener extends EventListener {

    protected WorldListener(@NotNull ListenerManager manager) {
        super(manager);
    }

    @EventHandler
    public void onServerSave(WorldSaveEvent e) {
        if (e.getWorld().getEnvironment() == World.Environment.NETHER) {
            if (plugin.isEnabled())
                Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> plugin.getDatasourceCore().saveAll());
            else
                plugin.getDatasourceCore().saveAll();
        }
    }

    @EventHandler
    public void onWorldDayChange(WorldDayChangeEvent e) {
        plugin.getSLF4JLogger().info("New ingame day");
    }

}

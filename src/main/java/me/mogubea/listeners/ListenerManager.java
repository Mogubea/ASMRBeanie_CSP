package me.mogubea.listeners;

import me.mogubea.entities.CustomEntityListener;
import me.mogubea.main.InfinityCaveLogic;
import me.mogubea.main.Main;
import org.bukkit.plugin.PluginManager;
import org.jetbrains.annotations.NotNull;

import java.text.DecimalFormat;

public class ListenerManager {

    private final @NotNull Main plugin;
    private final @NotNull DecimalFormat format = new DecimalFormat("#.#");

    public ListenerManager(Main plugin) {
        this.plugin = plugin;

        PluginManager pluginManager = plugin.getServer().getPluginManager();
        pluginManager.registerEvents(plugin.getBlockTracker(), plugin);

        pluginManager.registerEvents(new CustomEntityListener(this, plugin.getCustomEntityManager()), plugin);

        pluginManager.registerEvents(new ConnectionListener(this), plugin);
        pluginManager.registerEvents(new ChatListener(this), plugin);
        pluginManager.registerEvents(new WorldListener(this), plugin);
        pluginManager.registerEvents(new BlockListener(this), plugin);
        pluginManager.registerEvents(new ItemListener(this), plugin);
        pluginManager.registerEvents(new PlayerListener(this), plugin);
        pluginManager.registerEvents(new EntityListener(this), plugin);
        pluginManager.registerEvents(new ContainerListener(this), plugin);
        pluginManager.registerEvents(new EnchantListener(this), plugin);
        pluginManager.registerEvents(new InfinityCaveLogic(this), plugin);

        pluginManager.registerEvents(new StatListener(this), plugin);

        // Process jobs last.
        pluginManager.registerEvents(new JobListener(this), plugin);
    }

    protected @NotNull Main getPlugin() {
        return plugin;
    }

    protected @NotNull DecimalFormat getDecimalFormat() {
        return format;
    }

}

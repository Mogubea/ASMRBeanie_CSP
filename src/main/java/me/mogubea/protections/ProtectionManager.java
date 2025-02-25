package me.mogubea.protections;

import me.mogubea.main.Main;
import org.jetbrains.annotations.NotNull;

public class ProtectionManager {

    private final @NotNull Main plugin;

    public ProtectionManager(@NotNull Main plugin) {
        this.plugin = plugin;
    }

    protected @NotNull Main getPlugin() {
        return plugin;
    }

}

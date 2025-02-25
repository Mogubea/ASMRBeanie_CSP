package me.mogubea.entities;

import me.mogubea.main.Main;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

/**
 * Load before anything else to ensure no issues.
 */
public class CustomEntityManager {

    protected static CustomEntityManager INSTANCE;
    private final Main plugin;
    private final Set<LivingEntity> healthTracked = new HashSet<>();

    public CustomEntityManager(@NotNull Main plugin) {
        this.plugin = plugin;
        INSTANCE = this;
    }

    protected void tryToConvert(@NotNull Entity oldEntity) {
        PersistentDataContainer container = oldEntity.getPersistentDataContainer();
        String identifier = container.get(CustomEntityType.KEY_ENTITY_TYPE, PersistentDataType.STRING);
        CustomEntityType<?> customEntity = identifier != null ? CustomEntityType.fromIdentifier(identifier) : null;

        if (customEntity != null)
            customEntity.replace(oldEntity);
        else
            getPlugin().getSLF4JLogger().error("Error converting vanilla to custom entity; " + identifier);
    }

    public boolean hasHealthBar(@NotNull LivingEntity entity) {
        return healthTracked.contains(entity);
    }

    protected void trackHealthBar(@NotNull LivingEntity entity) {
        healthTracked.add(entity);
    }

    protected void untrackHealthBar(@NotNull LivingEntity entity) {
        healthTracked.remove(entity);
    }

    protected @NotNull Main getPlugin() {
        return plugin;
    }

}

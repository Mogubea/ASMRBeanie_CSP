package me.mogubea.entities;

import me.mogubea.main.Main;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class CustomEntityType<T extends Entity> {

    protected static final NamespacedKey KEY_ENTITY_TYPE = Main.key("CUSTOM_ENTITY_ID");
    private static final Map<String, CustomEntityType<?>> byIdentifier = new HashMap<>();

    public static final CustomEntityType<MoguEntityTextIndicator> TEXT_INDICATOR;
    public static final CustomEntityType<MoguEntityMobHealthBar> MOB_HEALTH_BAR;
    public static final CustomEntityType<MoguEntityThrowaxe> THROWAXE_ENTITY;

    static {
        TEXT_INDICATOR = register("TEXT_INDICATOR", MoguEntityTextIndicator::new);
        MOB_HEALTH_BAR = register("MOB_HEALTH_BAR", MoguEntityMobHealthBar::new);
        THROWAXE_ENTITY = register("THROWAXE_ENTITY", MoguEntityThrowaxe::new);
    }

    private final String identifier;
    private final CustomEntityManager manager;
    private final EntityFactory<T> factory;

    private CustomEntityType(@NotNull CustomEntityManager manager, @NotNull String identifier, EntityFactory<T> factory) {
        this.manager = manager;
        this.identifier = identifier.toUpperCase();
        this.factory = factory;
    }

    private static <T extends Entity> @NotNull CustomEntityType<T> register(@NotNull String identifier, EntityFactory<T> test) {
        CustomEntityType<T> type = new CustomEntityType<>(CustomEntityManager.INSTANCE, identifier, test);
        byIdentifier.put(identifier, type);
        return type;
    }

    public static @Nullable CustomEntityType<?> fromIdentifier(@NotNull String identifier) {
        return byIdentifier.get(identifier);
    }

    /**
     * Spawn a custom entity.
     * @param location The location to spawn the entity
     * @return The custom entity
     */
    public @NotNull T spawn(@NotNull Location location) {
        return spawn(location, null);
    }

    public @NotNull T spawn(@NotNull Location location, @Nullable Consumer<T> consumer) {
        if (factory == null) throw new UnsupportedOperationException("Spawning this entity via CustomEntityType#spawn isn't possible.");

        ServerLevel level = ((CraftWorld)location.getWorld()).getHandle();
        T newEntity = factory.create(manager, location);
        org.bukkit.entity.Entity bukkitEnt = newEntity.getBukkitEntity();
        bukkitEnt.getPersistentDataContainer().set(KEY_ENTITY_TYPE, PersistentDataType.STRING, identifier);

        IMoguEntity beEnt = (IMoguEntity) newEntity;
        beEnt.postCreation();

        if (consumer != null)
            consumer.accept(newEntity);

        level.addFreshEntity(newEntity, CreatureSpawnEvent.SpawnReason.CUSTOM);

        return newEntity;
    }

    /**
     * Replace the loaded vanilla entity with the custom entity.
     * @param oldEntity The vanilla entity
     * @return The custom entity
     */
    protected @NotNull T replace(@NotNull org.bukkit.entity.Entity oldEntity) {
        if (factory == null) throw new UnsupportedOperationException("Replacing this entity via CustomEntityType#spawn isn't possible.");

        ServerLevel level = ((CraftWorld)oldEntity.getWorld()).getHandle();
        T newEntity = factory.create(manager, oldEntity.getLocation());
        org.bukkit.entity.Entity bukkitEnt = newEntity.getBukkitEntity();
        bukkitEnt.getPersistentDataContainer().set(KEY_ENTITY_TYPE, PersistentDataType.STRING, identifier);

        IMoguEntity beEnt = (IMoguEntity) newEntity;
        beEnt.transferData(oldEntity, oldEntity.getPersistentDataContainer());
        beEnt.postCreation();
        oldEntity.remove();

        level.addFreshEntity(newEntity, CreatureSpawnEvent.SpawnReason.CUSTOM);
        return newEntity;
    }

    private interface EntityFactory<T extends Entity> {
        T create(CustomEntityManager manager, Location location);
    }
}

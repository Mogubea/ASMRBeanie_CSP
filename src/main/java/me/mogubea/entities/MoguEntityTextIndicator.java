package me.mogubea.entities;

import net.kyori.adventure.text.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.Vec3;
import org.bukkit.Location;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.entity.Entity;
import org.bukkit.persistence.PersistentDataContainer;
import org.jetbrains.annotations.NotNull;

public class MoguEntityTextIndicator extends ArmorStand implements IMoguEntity {

    protected final org.bukkit.entity.ArmorStand bukkitEntity;
    private final CustomEntityManager manager;
    private int timeToLive = 20;

    protected MoguEntityTextIndicator(CustomEntityManager manager, Location location) {
        super(EntityType.ARMOR_STAND, ((CraftWorld)location.getWorld()).getHandle());
        this.manager = manager;

        setInvulnerable(true);
        setInvisible(true);
        setMarker(true);
        moveTo(location.getX() - 0.5 + random.nextDouble(), location.getY() - 0.3 + random.nextDouble(), location.getZ() - 0.5 + random.nextDouble());
        bukkitEntity = (org.bukkit.entity.ArmorStand) getBukkitEntity();
        bukkitEntity.setPersistent(false);
    }

    @Override
    public void tick() {
        moveTo(getX(), getY() + 0.09, getZ());
        if (--timeToLive < 0)
            discard();
    }

    public void setTTL(int time) {
        this.timeToLive = time;
    }

    public void setText(Component name) {
        bukkitEntity.customName(name);
        bukkitEntity.setCustomNameVisible(true);
    }

    @Override
    public @NotNull InteractionResult interactAt(@NotNull Player entityhuman, @NotNull Vec3 vec3d, @NotNull InteractionHand enumhand) {
        return InteractionResult.FAIL;
    }

    @Override
    public @NotNull PushReaction getPistonPushReaction() {
        return PushReaction.IGNORE;
    }

    @Override
    public void postCreation() {

    }

    @Override
    public void transferData(Entity oldEntity, PersistentDataContainer container) {
    }

    @Override
    public boolean shouldBeSaved() {
        return false;
    }

    @Override
    public boolean hurtServer(@NotNull ServerLevel level, @NotNull DamageSource damageSource, float amount) {
        return false;
    }

}

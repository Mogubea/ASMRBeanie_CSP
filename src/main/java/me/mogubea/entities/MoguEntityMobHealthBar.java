package me.mogubea.entities;

import me.mogubea.utils.MoguColor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.Vec3;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityRemoveEvent;
import org.bukkit.persistence.PersistentDataContainer;
import org.jetbrains.annotations.NotNull;

public class MoguEntityMobHealthBar extends Display.TextDisplay implements IMoguEntity {

    private final org.bukkit.entity.TextDisplay bukkitEntity;
    private final CustomEntityManager manager;
    private LivingEntity trackedMob;
    private double previousHealth;
    private int ticksSinceChange;
    private int barSize;

    protected MoguEntityMobHealthBar(CustomEntityManager manager, Location location) {
        super(EntityType.TEXT_DISPLAY, ((CraftWorld)location.getWorld()).getHandle());
        this.manager = manager;

        setInvulnerable(true);
//        setBillboardConstraints(BillboardConstraints.CENTER);
        moveTo(location.getX(), location.getY() + 0.5, location.getZ());
        bukkitEntity = (org.bukkit.entity.TextDisplay) getBukkitEntity();
//        bukkitEntity.setSeeThrough(true);
//        bukkitEntity.setGlowing(true);
        bukkitEntity.setPersistent(false);
    }

    @Override
    public void tick() {
        if (trackedMob == null || trackedMob.isDead() || ticksSinceChange++ > 200 || !isPassenger()) {
            discard();
            return;
        }

        updateText();
        if (tickCount == 1)
            setCustomNameVisible(true);
    }

    public void setTrackedMob(@NotNull LivingEntity entity) {
        if (entity.isDead()) return;

        trackedMob = entity;
        entity.addPassenger(bukkitEntity);
        manager.trackHealthBar(entity);

        AttributeInstance mobHealth = trackedMob.getAttribute(Attribute.MAX_HEALTH);
        double maxHealth = mobHealth == null ? 0 : mobHealth.getValue();
        barSize = Math.min((int) (maxHealth * 2), 40);
        if (barSize < 10) barSize = 10;
    }

    private void updateText() {
        if (previousHealth == trackedMob.getHealth()) return;
        previousHealth = trackedMob.getHealth();
        ticksSinceChange = 0;

        AttributeInstance mobHealth = trackedMob.getAttribute(Attribute.MAX_HEALTH);
        double maxHealth = mobHealth == null ? 0 : mobHealth.getValue();
        bukkitEntity.customName(Component.text("❤ ", MoguColor.MOB_HEALTH).append(getHealthBar(barSize, (int) Math.ceil(trackedMob.getHealth()), (int)maxHealth)));
    }

    private TextComponent getHealthBar(int amount, long value, long maxvalue) {
        TextComponent component = Component.empty();
        int todo = (int)(((float)value/(float)maxvalue) * (float)amount);
        int injectCenter = amount / 2;
        int total = 0;
        String center = value + "";

        for (int c = -1; ++c < 2;) {
            StringBuilder sb = new StringBuilder();

            for (int x = -1; ++x < todo;) {
                sb.append('⎸');
                if (++total == injectCenter)
                    sb.append(center);
            }
            component = component.append(Component.text(sb.toString(), TextColor.color(c == 0 ? MoguColor.MOB_HEALTH : MoguColor.MOB_HEALTH_BG)));
            todo = amount - todo;
        }

        return component.decoration(TextDecoration.ITALIC, false);
    }

    @Override
    public @NotNull InteractionResult interactAt(@NotNull Player entityHuman, @NotNull Vec3 vec3d, @NotNull InteractionHand enumHand) {
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
    public void remove(@NotNull RemovalReason entity_removalreason, EntityRemoveEvent.@NotNull Cause cause) {
        if (trackedMob != null)
            manager.untrackHealthBar(trackedMob);
        super.remove(entity_removalreason, cause);
    }

}

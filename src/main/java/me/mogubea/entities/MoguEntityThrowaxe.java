package me.mogubea.entities;

import net.kyori.adventure.text.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.Vec3;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.block.TileState;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.entity.Entity;
import org.bukkit.event.entity.EntityRemoveEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataHolder;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

public class MoguEntityThrowaxe extends ArmorStand implements IMoguEntity {

    protected final org.bukkit.entity.ArmorStand bukkitEntity;
    private final CustomEntityManager manager;
    private org.bukkit.entity.Player owner;
    private ItemStack handItem;
    private Vector velocity;
    private int timeToLive = 200;

    protected MoguEntityThrowaxe(CustomEntityManager manager, Location location) {
        super(EntityType.ARMOR_STAND, ((CraftWorld)location.getWorld()).getHandle());
        this.manager = manager;

        setInvulnerable(true);
        setInvisible(true);
        setMarker(true);
        moveTo(location.getX(), location.getY() + 0.5, location.getZ());
        bukkitEntity = (org.bukkit.entity.ArmorStand) getBukkitEntity();
        bukkitEntity.setRightArmRotations(bukkitEntity.getRightArmRotations().subtract(20, 0, 0));
        bukkitEntity.setLeftArmRotations(bukkitEntity.getRightArmRotations());
        bukkitEntity.setArms(true);
        bukkitEntity.setPersistent(false);
    }

    @Override
    public void tick() {
        if (--timeToLive < 0)
            discard();

        if (timeToLive <= 3) return;

        bukkitEntity.setRightArmRotations(bukkitEntity.getRightArmRotations().add(5, 0, 0));
        bukkitEntity.setLeftArmRotations(bukkitEntity.getRightArmRotations());
        if (velocity == null) return;
        moveTo(getX() + velocity.getX(), getY() + velocity.getY(), getZ() + velocity.getZ());
        velocity.setY(velocity.getY() - 0.05);

        bukkitEntity.getWorld().spawnParticle(Particle.ENCHANTED_HIT, bukkitEntity.getLocation(), 2);

        org.bukkit.block.Block blocc = bukkitEntity.getLocation().add(0, 1, 0).getBlock();

        if (!blocc.isSolid()) return;
        if (!blocc.isBuildable()) return;
        if (blocc.isPassable()) return;
        if (blocc.getState() instanceof PersistentDataHolder) return;
        if (!blocc.getDrops(handItem).isEmpty() && blocc.isPreferredTool(handItem)) {
            owner.sendMessage(Component.text("Collided with: " + blocc.getType()));

            ItemStack actualItem = owner.getEquipment().getItemInMainHand().clone();
            owner.getEquipment().setItemInMainHand(handItem);
            owner.breakBlock(blocc);
            owner.getEquipment().setItemInMainHand(actualItem);
        }

        timeToLive = 3;
    }

    public void setVariables(@NotNull org.bukkit.entity.Player player, ItemStack item, EquipmentSlot slot) {
        this.owner = player;
        velocity = player.getEyeLocation().getDirection().multiply(1.1);
        this.handItem = item.clone();
        bukkitEntity.setRotation(player.getLocation().getYaw(), player.getLocation().getPitch());
        bukkitEntity.setItem(slot, item);
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
    public boolean isDamageSourceBlocked(@NotNull DamageSource damageSource) {
        return true;
    }

    @Override
    public void remove(@NotNull RemovalReason entity_removalreason, EntityRemoveEvent.@NotNull Cause cause) {

        super.remove(entity_removalreason, cause);
    }

}

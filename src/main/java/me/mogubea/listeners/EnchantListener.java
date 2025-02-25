package me.mogubea.listeners;

import com.destroystokyo.paper.event.player.PlayerArmorChangeEvent;
import me.mogubea.enchants.MoguEnchantment;
import me.mogubea.entities.CustomEntityType;
import me.mogubea.events.PlayerVeinmineEvent;
import me.mogubea.main.Main;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Ageable;
import org.bukkit.damage.DamageType;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.event.entity.EntityDamageByBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemMendEvent;
import org.bukkit.event.player.PlayerToggleSprintEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.*;

@SuppressWarnings("UnstableApiUsage")
public class EnchantListener extends EventListener {

    private final NamespacedKey KEY_SWIFT_SPRINT_BONUS = Main.key("swift_sprint.move_speed");
    private final Set<Player> swiftSprinters = new HashSet<>();
    private final Set<Block> pendingVeinBreak = new HashSet<>(), pendingReplenish = new HashSet<>();

    protected EnchantListener(@NotNull ListenerManager manager) {
        super(manager);

        plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            boolean dmg = random.nextInt(40) == 0;

            swiftSprinters.forEach(player -> {
                player.getWorld().spawnParticle(Particle.ELECTRIC_SPARK, player.getLocation(), 2, 0.25, 0, 0.25, 0.1);

                if (!dmg) return;
                player.damageItemStack(EquipmentSlot.LEGS, 1);
            });
        }, 5L, 5L);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onItemInteract(PlayerInteractEvent e) {
        final Player player = e.getPlayer();

        if (e.getAction() == Action.PHYSICAL && e.useInteractedBlock() != Event.Result.DENY) {
            if (e.getClickedBlock() == null || e.getClickedBlock().getType() != Material.FARMLAND) return;
            if (player.getEquipment().getItem(EquipmentSlot.FEET).containsEnchantment(MoguEnchantment.SOFT_SOLES))
                e.setCancelled(true);
            return;
        }

        if (e.getHand() == null || !e.getAction().isRightClick()) return;
        if (e.getClickedBlock() == null && e.useInteractedBlock() != Event.Result.DENY) return;

        final ItemStack item = e.getItem();
        if (item == null || !item.hasItemMeta() || player.hasCooldown(item) || !item.containsEnchantment(MoguEnchantment.THROWAXE)) return;

        CustomEntityType.THROWAXE_ENTITY.spawn(player.getLocation().add(player.getVelocity()), (axe -> axe.setVariables(player, item, e.getHand())));

        player.setCooldown(item, 200);
        player.swingHand(e.getHand());
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityDamaged(EntityDamageByBlockEvent e) {
        if (!(e.getEntity() instanceof LivingEntity entity)) return;
        if (entity.getEquipment() == null) return;

        if (e.getDamager() != null && e.getDamager().getBlockData() instanceof Ageable) {
            for (ItemStack item : entity.getEquipment().getArmorContents()) {
                if (item == null || !item.containsEnchantment(Enchantment.THORNS)) continue;
                if (random.nextInt(100) == 0)
                    item.damage(1, entity);
                e.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityCombat(EntityDamageByEntityEvent e) {
        if (e.getDamageSource().getDamageType() == DamageType.THORNS) return;
        if (!(e.getEntity() instanceof LivingEntity damagee) || (!(damagee instanceof Player) && !(damagee instanceof Mob))) return;
        if (!(e.getDamageSource().getDirectEntity() instanceof Entity damager) || !(damager instanceof AbstractArrow) && !(damager instanceof Mob)) return;
        if (!(damagee.canUseEquipmentSlot(EquipmentSlot.HAND) && damagee.canUseEquipmentSlot(EquipmentSlot.OFF_HAND))) return;

        EquipmentSlot slot = EquipmentSlot.HAND;
        ItemStack toCheck = damagee.getEquipment().getItem(slot);
        if (!(toCheck.getType() == Material.SHIELD))
            toCheck = damagee.getEquipment().getItem(slot = EquipmentSlot.OFF_HAND);

        if (toCheck.getType() == Material.SHIELD && damagee instanceof Player player ? player.isBlocking() : random.nextInt(100) < 25) {
            damager.setVelocity(damager.getLocation().getDirection().multiply(-0.7));

            // Set attacker on fire
            if (toCheck.containsEnchantment(MoguEnchantment.INCENDIARY)) {
                int toGive = 140 + damagee.getFireTicks();
                if (damager.getFireTicks() < toGive)
                    damager.setFireTicks(toGive);

                if (damager instanceof Mob mob && damagee instanceof Player player)
                    mob.setKiller(player);

                if (damagee.getFireTicks() > 1) {
                    damagee.setFireTicks(0);
                    damagee.damageItemStack(slot, 1);
                }
            }

            // Thorns Damage, 20% per level to deal 2 - 4 hearts to the attacker
            if (damager instanceof Mob mob && random.nextInt(100) < (toCheck.getEnchantmentLevel(MoguEnchantment.SPIKED) * 20)) {
                mob.damage(random.nextDouble(2, 4.01), damagee);
                toCheck.damage(1, damagee);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onSprintToggle(PlayerToggleSprintEvent e) {
        AttributeInstance instance = Objects.requireNonNull(e.getPlayer().getAttribute(Attribute.MOVEMENT_SPEED));
        ItemStack item = e.getPlayer().getEquipment().getItem(EquipmentSlot.LEGS);
        if (!item.containsEnchantment(MoguEnchantment.SWIFT_SPRINT)) return;
        double swiftSprintBonus = 0.0033 * item.getEnchantmentLevel(MoguEnchantment.SWIFT_SPRINT);

        instance.removeModifier(KEY_SWIFT_SPRINT_BONUS);
        if (!e.isSprinting()) {
            swiftSprinters.remove(e.getPlayer());
        } else {
            swiftSprinters.add(e.getPlayer());
            instance.addTransientModifier(new AttributeModifier(KEY_SWIFT_SPRINT_BONUS, swiftSprintBonus, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlotGroup.LEGS));
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent e) {
        if (e.getPlayer().getGameMode().isInvulnerable()) return; // Creative / Spectator skip

        final @NotNull ItemStack handItem = e.getPlayer().getEquipment().getItemInMainHand();

        if (pendingVeinBreak.remove(e.getBlock())) { // Don't call enchantment checks if block is pending break, damage item some extra for each vein-mined block
            e.getPlayer().damageItemStack(EquipmentSlot.HAND, 1);
            return;
        }

        final @NotNull Block block = e.getBlock();
        final @NotNull Material blockType = block.getType();

        if (handItem.getEnchantments().isEmpty()) return; // Check for Enchants

        if (!plugin.getItemManager().getReplenishableBlockSeed(blockType).isAir() && handItem.containsEnchantment(MoguEnchantment.REPLENISH) && block.getBlockData() instanceof Ageable ageable) {
            if (ageable.getAge() < ageable.getMaximumAge()) { // To prevent any excess block event calls such as jobs.
                e.setCancelled(true);
                ageable.setAge(0);
                e.getBlock().setBlockData(ageable);
            } else {
                pendingReplenish.add(block);
                e.getPlayer().damageItemStack(EquipmentSlot.HAND, 1);
                e.getPlayer().spawnParticle(Particle.HAPPY_VILLAGER, e.getBlock().getLocation().add(0.5, 0.4, 0.5), 3, 0.4, 0.3, 0.4);
            }
            return;
        }

        if (e.getPlayer().hasCooldown(handItem)) return; // Disallow checks on cooldown
        if (!block.isPreferredTool(handItem)) return; // Check valid tool

        doProspector(e, MoguEnchantment.PROSPECTOR, handItem, blockType, 3, false); // 3, 6, 9...
        doProspector(e, MoguEnchantment.TREECAPITATOR, handItem, blockType, 5, true); // 5, 10, 15...
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockDropItem(BlockDropItemEvent e) {
        if (!pendingReplenish.remove(e.getBlock())) return;
        if (!(e.getBlockState().getBlockData() instanceof Ageable ageable)) return;

        List<Item> drops = new ArrayList<>(e.getItems());

        for (int x = -1; ++x < drops.size();) {
            ItemStack item = e.getItems().get(x).getItemStack();
            if (item.getType() == plugin.getItemManager().getReplenishableBlockSeed(e.getBlockState().getType())) {
                if (item.getAmount() <= 1) {
                    e.getItems().remove(x);
                } else {
                    item.setAmount(item.getAmount() - 1);
                    e.getItems().get(x).setItemStack(item);
                }
                break;
            }
        }

        ageable.setAge(0);
        e.getBlock().setBlockData(ageable);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onArmourChange(PlayerArmorChangeEvent e) {
        if (e.getSlotType() != PlayerArmorChangeEvent.SlotType.LEGS) return;
        if (!e.getNewItem().containsEnchantment(MoguEnchantment.SWIFT_SPRINT)) {
            swiftSprinters.remove(e.getPlayer());
            Objects.requireNonNull(e.getPlayer().getAttribute(Attribute.MOVEMENT_SPEED)).removeModifier(KEY_SWIFT_SPRINT_BONUS);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onMendingRepair(PlayerItemMendEvent e) {
        if (e.getItem().containsEnchantment(MoguEnchantment.DISREPAIR_CURSE))
            e.setCancelled(true);
    }

    private void scanNearbyBlocks(int checks, int maxChecks, int maxBlockBreaks, List<Block> list, Block block, Material type, boolean hasToBeCartesian) {
        if (checks >= maxChecks || list.size() >= maxBlockBreaks) return;

        for (BlockFace face : BlockFace.values()) {
            if (hasToBeCartesian && face != BlockFace.SELF && !face.isCartesian()) continue;
            Block nearbyBlock = block.getRelative(face.getModX(), face.getModY(), face.getModZ());

            if (type != nearbyBlock.getType() || list.contains(nearbyBlock)) continue;
            if (checks++ >= maxChecks || list.size() >= maxBlockBreaks) return; // Check

            list.add(nearbyBlock);
            scanNearbyBlocks(checks, maxChecks, maxBlockBreaks, list, nearbyBlock, type, hasToBeCartesian);
        }
    }

    private boolean isValidVeinBlock(@NotNull Enchantment enchantment, Material material) {
        if (enchantment == MoguEnchantment.PROSPECTOR)
            return material.name().endsWith("_ORE");
        if (enchantment == MoguEnchantment.TREECAPITATOR) {
            String name = material.name();
            return material == Material.WARPED_STEM || material == Material.CRIMSON_STEM || (name.endsWith("_WOOD") || name.endsWith("_LOG")) && !name.startsWith("STRIPPED_");
        }

        return false;
    }

    private void doProspector(final @NotNull BlockBreakEvent e, final @NotNull Enchantment enchantment, final @NotNull ItemStack handItem, final @NotNull Material veinType, final int veinPerLevel, final boolean cartesian) {
        if (!isValidVeinBlock(enchantment, veinType)) return; // Check if block is a vein block
        if (!handItem.getItemMeta().hasEnchant(enchantment)) return; // Check for enchantment
        final int blocksToBreak = (handItem.getItemMeta().getEnchantLevel(enchantment) * veinPerLevel) - 1;

        List<Block> toBreak = new ArrayList<>();
        int checks = 0, maxChecks = blocksToBreak * (cartesian ? 10 : 30);

        scanNearbyBlocks(checks, maxChecks, blocksToBreak, toBreak, e.getBlock(), veinType, cartesian);

        if (toBreak.size() <= 1) return; // If there's no additional blocks then there is no need to do any of the veinmine stuff.
        e.setCancelled(true); // Cancel this one before it triggers the Job Listener.

        PlayerVeinmineEvent veinmineEvent = new PlayerVeinmineEvent(e.getPlayer(), e.getBlock(), toBreak);
        plugin.getServer().getPluginManager().callEvent(veinmineEvent);
        if (veinmineEvent.isCancelled()) return;

        e.getPlayer().setCooldown(handItem, 40 + 3 * toBreak.size());

        pendingVeinBreak.addAll(toBreak);

        while (!toBreak.isEmpty()) {
            Block block = toBreak.getFirst();
            e.getPlayer().breakBlock(block);
            toBreak.remove(block);
        }
    }

}

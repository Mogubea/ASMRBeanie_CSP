package me.mogubea.listeners;

import com.destroystokyo.paper.event.player.PlayerPostRespawnEvent;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.PotionContents;
import me.mogubea.profile.MoguProfile;
import me.mogubea.statistics.SimpleStatType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.*;
import org.bukkit.block.data.type.Beehive;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.CauldronLevelChangeEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerAttemptPickupItemEvent;
import org.bukkit.event.player.PlayerHarvestBlockEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;
import org.bukkit.util.RayTraceResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class PlayerListener extends EventListener {

    protected PlayerListener(@NotNull ListenerManager manager) {
        super(manager);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onRightClickPre(PlayerInteractEvent e) {
        if (e.getAction() == Action.PHYSICAL || e.getItem() == null) return;
        plugin.getItemManager().doItemEvent(e.getItem(), e);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onItemPickup(PlayerAttemptPickupItemEvent e) {
        final MoguProfile profile = MoguProfile.from(e.getPlayer());
        if (!profile.getPickupFilter().canPickupItem(e.getItem().getItemStack())) {
            e.getItem().setPickupDelay(10);
            e.getPlayer().spawnParticle(Particle.DUST_COLOR_TRANSITION, e.getItem().getLocation().add(0, 0.2, 0), 4, 0.125, 0.1, 0.125, new Particle.DustTransition(Color.RED, Color.BLACK, 0.3F));
            e.setCancelled(true);
        }
    }

    @SuppressWarnings("UnstableApiUsage")
    @EventHandler(priority = EventPriority.LOW)
    public void onWaterBottle(PlayerInteractEvent e) {
        final Player player = e.getPlayer();
        final Action action = e.getAction();

        if (action.isLeftClick()) return;

        final EquipmentSlot hand = e.getHand();
        final ItemStack handItem = e.getItem();

        if (hand == null || handItem == null || handItem.isEmpty()) return;
        final Block block = e.getClickedBlock();

        if (handItem.getType() == Material.GLASS_BOTTLE) {
            if (block != null && block.getBlockData() instanceof Beehive hive && hive.getHoneyLevel() >= hive.getMaximumHoneyLevel()) {
                List<ItemStack> stupid = new ArrayList<>(List.of(new ItemStack(Material.HONEY_BOTTLE)));
                if (!new PlayerHarvestBlockEvent(player, block, hand, stupid).callEvent())
                    e.setCancelled(true);

                return;
            }

            e.setCancelled(true);

            boolean doWater = false;

            if (block != null && block.getType() == Material.WATER_CAULDRON && block.getBlockData() instanceof Levelled level && level.getLevel() > 0) {
                doWater = true;

                BlockState blockState = block.getState(true);
                level = (Levelled) blockState.getBlockData();
                int newLevel = level.getLevel() - 1;

                if (newLevel < 1) {
                    blockState.setType(Material.CAULDRON);
                } else {
                    level.setLevel(level.getLevel() - 1);
                    blockState.setBlockData(level);
                }

                if (new CauldronLevelChangeEvent(block, player, CauldronLevelChangeEvent.ChangeReason.BOTTLE_FILL, blockState).callEvent())
                    block.setBlockData(blockState.getBlockData());
            } else {
                if (block != null && block.getBlockData() instanceof Waterlogged waterlogged && waterlogged.isWaterlogged()) {
                    doWater = true;
                } else {
                    RayTraceResult result = e.getPlayer().rayTraceBlocks(Objects.requireNonNull(player.getAttribute(Attribute.BLOCK_INTERACTION_RANGE)).getValue(), FluidCollisionMode.SOURCE_ONLY);
                    if (result != null && result.getHitBlock() != null && result.getHitBlock().getType() == Material.WATER)
                        doWater = true;
                }
            }

            if (!doWater) return;
            handItem.setAmount(handItem.getAmount() - 1);
            ItemStack waterBottle = new ItemStack(Material.POTION);
            waterBottle.setData(DataComponentTypes.POTION_CONTENTS, PotionContents.potionContents().potion(PotionType.WATER));
            player.give(Set.of(formatItem(waterBottle)));
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onRightClick(PlayerInteractEvent e) {
        if (e.getClickedBlock() == null || e.getHand() == null) return;

        final @NotNull Block block = e.getClickedBlock();
        final @NotNull World world = block.getWorld();
        final @NotNull Player player = e.getPlayer();
        final @NotNull EquipmentSlot hand = e.getHand();
        final @NotNull Material material = block.getType();
        final @Nullable ItemStack handItem = e.getItem();

        // Bonemealable Sugar Cane
        if (handItem != null && material == Material.SUGAR_CANE && handItem.getType() == Material.BONE_MEAL && e.getAction() == Action.RIGHT_CLICK_BLOCK) {
            Block highestCane = block, lowestCane = block;
            int caneHeight = 0;

            while (lowestCane.getRelative(0, -1, 0).getType() == material)
                lowestCane = lowestCane.getRelative(0, -1, 0);

            while (++caneHeight < 4 && lowestCane.getRelative(0, caneHeight, 0).getType() == material)
                highestCane = lowestCane.getRelative(0, caneHeight, 0);

            if (caneHeight > 1 && highestCane.getBlockData() instanceof Ageable ageable) {
                ageable.setAge(0);
                highestCane.setBlockData(ageable);
            }

            highestCane.getRelative(0, 1, 0).setType(material, true);
            world.playSound(block.getLocation().add(0.5, 0.5, 0.5), Sound.ITEM_BONE_MEAL_USE, 0.5F, 1F);
            world.spawnParticle(Particle.HAPPY_VILLAGER, block.getLocation().add(0.5, 0.5, 0.5), 5, 0.5, 0.5, 0.5);

            player.swingHand(hand);
            if (!player.getGameMode().isInvulnerable())
                player.getEquipment().getItem(hand).subtract(1);
            return;
        }

        // Repair anvils by 1 stage when using an iron block on them!
        if (handItem != null && handItem.getType() == Material.IRON_BLOCK && e.getAction() == Action.RIGHT_CLICK_BLOCK) {
            if ((material == Material.CHIPPED_ANVIL || material == Material.DAMAGED_ANVIL) && block.getBlockData() instanceof Directional anvil) {
                BlockFace facing = anvil.getFacing();
                Material repairTo = material == Material.DAMAGED_ANVIL ? Material.CHIPPED_ANVIL : Material.ANVIL;
                block.setType(repairTo);

                Directional newData = (Directional) block.getLocation().getBlock().getBlockData();
                newData.setFacing(facing);
                block.setBlockData(newData);
                handItem.setAmount(handItem.getAmount() - 1);

                world.spawnParticle(Particle.BLOCK, block.getLocation().add(0.5, 0.5, 0.5), 8, 0.2, 0.1, 0.2, newData);
                world.spawnParticle(Particle.HAPPY_VILLAGER, block.getLocation().add(0.5, 0.7, 0.5), 14, 0.3, 0.2, 0.3);
                world.playSound(block.getLocation().add(0.5, 0.5, 0.5), Sound.BLOCK_ANVIL_USE, 0.5F, 1.5F);

                addToStat(e.getPlayer(), SimpleStatType.INTERACTION, "fix_anvil_block", 1);
                e.setCancelled(true);
                return;
            }
        }

        // Shear mushroom block faces
        if (handItem != null && handItem.getType() == Material.SHEARS && e.getAction() == Action.RIGHT_CLICK_BLOCK) {
            switch(material) {
                case RED_MUSHROOM_BLOCK, BROWN_MUSHROOM_BLOCK, MUSHROOM_STEM -> {
                    BlockFace facing = e.getBlockFace();
                    MultipleFacing blockData = (MultipleFacing) block.getBlockData();
                    blockData.setFace(facing, !blockData.hasFace(facing));
                    block.setBlockData(blockData);

                    world.spawnParticle(Particle.BLOCK, block.getLocation().add(0.5, 0.5, 0.5), 5, blockData);
                    world.playSound(block.getLocation().add(0.5, 0.5, 0.5), Sound.BLOCK_PUMPKIN_CARVE, 0.8F, 0.8F);
                    addToStat(e.getPlayer(), SimpleStatType.INTERACTION, "shear_mushroom_block", 1);
                    player.swingHand(hand);
                    return;
                }
            }
        }

        // Till Mycelium and Podzol
        if (handItem != null && handItem.getType().name().endsWith("HOE") && e.getAction() == Action.RIGHT_CLICK_BLOCK) {
            if (material == Material.PODZOL || material == Material.MYCELIUM) {

                block.setType(Material.FARMLAND);
                world.playSound(block.getLocation().add(0.5, 0.8, 0.5), Sound.ITEM_HOE_TILL, 1F, 1F);
                player.swingHand(hand);
                return;
            }
        }

        // Re bark logs
        if (handItem != null && e.getAction() == Action.RIGHT_CLICK_BLOCK && handItem.getType().name().endsWith("_AXE")) {
            if (material.name().endsWith("_WOOD") || material.name().endsWith("_LOG")) {
                if (material.name().startsWith("STRIPPED_") && block.getBlockData() instanceof Orientable oldOrientation) {
                    world.playSound(block.getLocation().toCenterLocation(), Sound.ITEM_AXE_STRIP, 0.7F, 1F);
                    block.setType(Material.valueOf(material.name().substring(9)));
                    player.swingHand(hand);

                    Orientable newOrientation = (Orientable) block.getLocation().getBlock().getBlockData();
                    newOrientation.setAxis(oldOrientation.getAxis());
                    block.setBlockData(newOrientation);

                    handItem.damage(1, e.getPlayer());
                    addToStat(e.getPlayer(), SimpleStatType.INTERACTION, "rebark_stripped_log", 1);
                    e.setCancelled(true);
                    return;
                }
            }
        }

        // Prevent bucket usage on players
        if (e.getAction() == Action.RIGHT_CLICK_BLOCK && handItem != null && (handItem.getType() == Material.FLINT_AND_STEEL || handItem.getType() == Material.FIRE_CHARGE || (handItem.getType() != Material.MILK_BUCKET && handItem.getType().name().endsWith("_BUCKET")))) {
            if (block.getLocation().getNearbyPlayers(2).removeIf(player1 -> player1 != player)) {
                e.setCancelled(true);
                return;
            }
        }

        // Right click farming
        if (hand != EquipmentSlot.HAND) return;
        if (!(block.getBlockData() instanceof Ageable ageable)) return;
        if (ageable.getAge() < ageable.getMaximumAge()) return;
        if (e.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        switch(material) {
            case WHEAT, CARROTS, POTATOES, BEETROOTS, NETHER_WART, COCOA -> {
                Material seedDrop = plugin.getItemManager().getReplenishableBlockSeed(material);

                List<ItemStack> drops = new ArrayList<>(block.getDrops());
                if (drops.isEmpty()) return; // Seemingly impossible but worth a check as precaution if I change anything in the future
                drops.forEach(item -> item.subtract(item.getType() == seedDrop ? 1 : 0)); // Remove a seed.

                PlayerHarvestBlockEvent cropEvent = new PlayerHarvestBlockEvent(e.getPlayer(), block, hand, drops);
                plugin.getServer().getPluginManager().callEvent(cropEvent);
                if (cropEvent.isCancelled()) return;

                for (ItemStack item : drops)
                    world.dropItemNaturally(block.getLocation().add(0.5, 0.25, 0.5), item);

                world.playSound(block.getLocation().add(0.5, 0.5, 0.5), block.getBlockSoundGroup().getBreakSound(), 0.6F, 1F);
                world.spawnParticle(Particle.BLOCK, block.getLocation().add(0.5, 0.15, 0.5), 3, 0.3, 0.1, 0.3, block.getBlockData());
                ageable.setAge(0);
                block.setBlockData(ageable);
                player.swingMainHand();
                e.setCancelled(true);
            }
        }
    }

    private final Map<Player, Integer> preserveDeathExperience = new HashMap<>();

    @SuppressWarnings("UnstableApiUsage")
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onPlayerDeath(PlayerDeathEvent e) {
        Player player = e.getPlayer();
        MoguProfile profile = MoguProfile.from(player);
        Component deathMsg = e.deathMessage();
        preserveDeathExperience.put(player, player.calculateTotalExperiencePoints() / 4);

        if (deathMsg != null) {
            TextReplacementConfig c2 = TextReplacementConfig.builder().match(player.getName()).replacement(profile.getColouredName()).build();
            e.deathMessage(Component.text("☠ ", NamedTextColor.RED).append(deathMsg.color(NamedTextColor.GRAY)).replaceText(c2));
        }

        // Heal the monster that kills the player
        if (e.getDamageSource().getCausingEntity() instanceof Mob mob) {
            int toHeal = 20; // Flat

            AttributeInstance attribute = mob.getAttribute(Attribute.MAX_HEALTH);
            if (attribute != null)
                toHeal += (int) (attribute.getValue() / 4);

            mob.heal(toHeal, EntityRegainHealthEvent.RegainReason.CUSTOM);
        }

        // Don't drop anything if not in Survival.
        if (Boolean.TRUE.equals(player.getWorld().getGameRuleValue(GameRule.KEEP_INVENTORY)) || e.getKeepInventory() || player.getGameMode().isInvulnerable()) {
            e.setKeepInventory(true);
            e.setKeepLevel(true);
            e.setShouldDropExperience(false);
            e.getDrops().clear();
            return;
        }

        e.getItemsToKeep().clear();
        e.setDroppedExp(preserveDeathExperience.get(player));

        // Preserve armour but damage it 10%, down to 1.
        for (int x = -1; ++x < player.getInventory().getArmorContents().length;) {
            ItemStack item = player.getInventory().getArmorContents()[x];
            if (item == null || !item.getType().hasDefaultData(DataComponentTypes.MAX_DAMAGE)) continue;
            if (item.getItemMeta() instanceof Damageable damageable && damageable.getDamage() < (item.getType().getMaxDurability()*0.85)) {
                damageable.setDamage(Math.min(item.getType().getMaxDurability() - 1, damageable.getDamage() + (item.getType().getMaxDurability()/10)));
                item.setItemMeta(damageable);
            }

            e.getDrops().remove(item);
            e.getItemsToKeep().add(item);
        }

        // Preserve hot bar, each item has a chance to drop based on rarity.
        for (int x = -1; ++x < player.getInventory().getStorageContents().length;) {
            ItemStack item = player.getInventory().getStorageContents()[x];
            if (item == null) continue;
            boolean keep = x <= 8 || plugin.getRandom().nextInt(100) < plugin.getItemManager().getItemRarity(item).getChanceToSave();

            if (!keep) continue;

            e.getDrops().remove(item);
            e.getItemsToKeep().add(item);
        }

        // Keep off-hand
        ItemStack offHand = player.getInventory().getItemInOffHand();
        if (e.getDrops().contains(offHand)) {
            e.getDrops().remove(offHand);
            e.getItemsToKeep().add(offHand);
        }

        e.getItemsToKeep().removeIf(item -> item.containsEnchantment(Enchantment.VANISHING_CURSE));

        if (e.getDrops().isEmpty()) return;
        plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, () -> {
            Component itemsDropped = Component.text("Items dropped: ", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false);
            for (int i = -1; ++i < e.getDrops().size();)
                itemsDropped = itemsDropped.append(Component.text("\n" + e.getDrops().get(i).getAmount() + "x ").append(e.getDrops().get(i).effectiveName()));

            e.getPlayer().sendMessage(Component.text("You dropped " + e.getDrops().size() + " items as a result of your demise!", NamedTextColor.DARK_GRAY).decoration(TextDecoration.ITALIC, true).hoverEvent(HoverEvent.showText(itemsDropped)));
        }, 5L);
    }

    @EventHandler
    public void onPlayerRespawn(PlayerPostRespawnEvent e) {
        if (!preserveDeathExperience.containsKey(e.getPlayer())) return;
        int xpToSet = preserveDeathExperience.get(e.getPlayer());
        preserveDeathExperience.remove(e.getPlayer());

        if (e.getPlayer().getGameMode().isInvulnerable()) return;

        e.getPlayer().setExperienceLevelAndProgress(xpToSet);
        e.getPlayer().setFoodLevel(10);
        e.getPlayer().setHealth(15);
        e.getPlayer().setSaturation(1F);
        e.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 40, 0));
    }

}

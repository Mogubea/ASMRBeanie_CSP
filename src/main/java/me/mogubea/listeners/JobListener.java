package me.mogubea.listeners;

import io.papermc.paper.event.block.PlayerShearBlockEvent;
import me.mogubea.events.PlayerVeinmineEvent;
import me.mogubea.jobs.JobCleric;
import me.mogubea.profile.MoguProfile;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerHarvestBlockEvent;
import org.bukkit.event.player.PlayerShearEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

public class JobListener extends EventListener {

    protected JobListener(@NotNull ListenerManager manager) {
        super(manager);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onBlockMine(BlockBreakEvent event) {
        MoguProfile profile = MoguProfile.from(event.getPlayer());
        profile.getJob().doMiningEvent(profile, event);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onVeinmine(PlayerVeinmineEvent event) {
        MoguProfile profile = MoguProfile.from(event.getPlayer());
        profile.getJob().doVeinmineEvent(profile, event);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onFish(PlayerFishEvent event) {
        MoguProfile profile = MoguProfile.from(event.getPlayer());
        profile.getJob().doFishingEvent(profile, event);
    }

    @SuppressWarnings("UnstableApiUsage")
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onKill(EntityDeathEvent event) {
        if (event.getDamageSource().getCausingEntity() instanceof Player player) {
            MoguProfile profile = MoguProfile.from(player);
            profile.getJob().doKillEntityEvent(profile, event);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onBuild(BlockPlaceEvent event) {
        if (event.getItemInHand().getType().isBlock()) {
            MoguProfile profile = MoguProfile.from(event.getPlayer());
            profile.getJob().doBuildEvent(profile, event);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onHarvest(PlayerHarvestBlockEvent event) {
        MoguProfile profile = MoguProfile.from(event.getPlayer());
        profile.getJob().doHarvestBlockEvent(profile, event);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onHarvest(PlayerShearEntityEvent event) {
        MoguProfile profile = MoguProfile.from(event.getPlayer());
        profile.getJob().doShearEntityEvent(profile, event);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onHarvest(PlayerShearBlockEvent event) {
        MoguProfile profile = MoguProfile.from(event.getPlayer());
        profile.getJob().doShearBlockEvent(profile, event);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onEnchantItem(EnchantItemEvent event) {
        MoguProfile profile = MoguProfile.from(event.getEnchanter());
        profile.getJob().doEnchantEvent(profile, event);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onBrew(BrewEvent event) {
        if (event.getResults().isEmpty()) return;
        event.getResults().forEach(item -> {
            if (!item.hasItemMeta() || !(item.getItemMeta() instanceof PotionMeta)) return;
            item.editMeta(meta -> meta.getPersistentDataContainer().set(JobCleric.KEY_FRESH_POTION, PersistentDataType.BOOLEAN, true));
        });
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onRecipeSlotTake(InventoryClickEvent event) {
        if (event.getClickedInventory() == null || event.getClickedInventory().getType() != InventoryType.BREWING || event.getSlot() > 2) return;
        Location blockLocation = event.getClickedInventory().getLocation();
        if (blockLocation == null) return;

        checkPotion((Player) event.getWhoClicked(), event.getClickedInventory().getItem(event.getSlot()), blockLocation);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onRecipeSlotTake(InventoryDragEvent event) {
        if (event.getInventory().getType() != InventoryType.BREWING) return;
        Location blockLocation = event.getInventory().getLocation();
        if (blockLocation == null) return;

        event.getRawSlots().forEach(slot -> checkPotion((Player) event.getWhoClicked(), event.getInventory().getItem(slot), blockLocation));
    }

    private void checkPotion(Player player, ItemStack itemStack, Location blockLocation) {
        if (itemStack == null || !itemStack.hasItemMeta() || !(itemStack.getItemMeta() instanceof PotionMeta pMeta)) return;
        if (!pMeta.getPersistentDataContainer().has(JobCleric.KEY_FRESH_POTION)) return;

        MoguProfile profile = MoguProfile.from(player);
        profile.getJob().doFreshPotionEvent(profile, itemStack, blockLocation);
        itemStack.editMeta(meta -> meta.getPersistentDataContainer().remove(JobCleric.KEY_FRESH_POTION));
        formatItem(itemStack);
    }

    private record Destructor(@NotNull MoguProfile profile, int tick) {}
}

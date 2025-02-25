package me.mogubea.listeners;

import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.PotionContents;
import io.papermc.paper.event.block.BlockPreDispenseEvent;
import me.mogubea.items.ItemRarity;
import me.mogubea.items.MoguItems;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Container;
import org.bukkit.block.data.Levelled;
import org.bukkit.block.data.type.Dispenser;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.ItemFrame;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.event.inventory.BrewEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.event.world.LootGenerateEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionType;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class ItemListener extends EventListener {

    protected ItemListener(@NotNull ListenerManager manager) {
        super(manager);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onItemSpawn(ItemSpawnEvent e) {
        Item item = e.getEntity();
        ItemRarity itemRarity = plugin.getItemManager().getItemRarity(item.getItemStack());
        if (itemRarity == ItemRarity.ADMIN) { e.setCancelled(true); return; } // Don't spawn admin items ever
        if (itemRarity.isRarity(ItemRarity.UNCOMMON)) { // Uncommon+ items are invulnerable to lava and glow
            item.setInvulnerable(true);
            item.setPersistent(true);

            if (plugin.hasProtocolManager()) {
                item.setGlowing(true);
                item.setMetadata("rarity", new FixedMetadataValue(plugin, itemRarity.name()));
            }
        }

        // More common items de-spawn quicker
        item.setTicksLived(itemRarity.getExtraTicksLived() + 1);
        item.setItemStack(formatItem(item.getItemStack()));
    }

    // Format all ItemStacks generated through loot chests.
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onGenerateLoot(LootGenerateEvent e) {
        e.getLoot().forEach(this::formatItem);
    }

    // Format all ItemStacks generated through brewing.
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBrew(BrewEvent e) {
        e.getResults().forEach(this::formatItem);
    }

    // Format all ItemStacks generated through crafting.
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onCraft(PrepareItemCraftEvent e) {
        if (e.getRecipe() == null) return;
        e.getInventory().setResult(formatItem(e.getRecipe().getResult()));
    }

    // Format when an ItemStack takes damage for the first time.
    @SuppressWarnings("UnstableApiUsage")
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onFirstTimeDamage(PlayerItemDamageEvent e) {
        if (e.getItem().getType().hasDefaultData(DataComponentTypes.MAX_DAMAGE) && !e.getItem().isDataOverridden(DataComponentTypes.MAX_DAMAGE))
            formatItem(e.getItem());
    }

    // Format when water bottle formed from dispensing...
    @SuppressWarnings("UnstableApiUsage")
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onDispenserSilly(BlockPreDispenseEvent e) {
        if (e.getItemStack().getType() == Material.GLASS_BOTTLE) {
            Dispenser dispenser = (Dispenser) e.getBlock().getBlockData();
            Block facingBlock = e.getBlock().getRelative(dispenser.getFacing());

            if (facingBlock.getType() == Material.WATER && facingBlock.getBlockData() instanceof Levelled water && water.getLevel() == 0) {
                Container container = (Container) e.getBlock().getState();
                ItemStack waterBottle = new ItemStack(Material.POTION);
                waterBottle.setData(DataComponentTypes.POTION_CONTENTS, PotionContents.potionContents().potion(PotionType.WATER));

                ItemStack slotItem = Objects.requireNonNull(container.getInventory().getItem(e.getSlot()));
                slotItem.setAmount(slotItem.getAmount() - 1);

                container.getInventory().addItem(formatItem(waterBottle));
                container.setBlockData(container.getBlockData());
                e.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onHangingDead(HangingBreakEvent e) {
        if (e.getEntity() instanceof ItemFrame frame && !frame.isVisible()) {
            e.setCancelled(true);
            e.getEntity().remove();
            frame.getWorld().dropItem(frame.getLocation(), frame.getItem());
            frame.getWorld().dropItem(frame.getLocation(), frame.getType() == EntityType.GLOW_ITEM_FRAME ? MoguItems.INVISIBLE_GLOWING_ITEM_FRAME.getItemStack() : MoguItems.INVISIBLE_ITEM_FRAME.getItemStack());
        }
    }

}

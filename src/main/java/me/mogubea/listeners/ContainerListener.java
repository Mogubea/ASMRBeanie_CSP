package me.mogubea.listeners;

import com.destroystokyo.paper.event.block.AnvilDamagedEvent;

import me.mogubea.enchants.MoguEnchantment;
import me.mogubea.gui.MoguGui;
import me.mogubea.profile.MoguProfile;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.inventory.*;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.view.AnvilView;
import org.jetbrains.annotations.NotNull;

public class ContainerListener extends EventListener {

    protected ContainerListener(@NotNull ListenerManager manager) {
        super(manager);
    }

    @SuppressWarnings("UnstableApiUsage")
    @EventHandler
    public void onRepair(PrepareAnvilEvent e) {
        ItemStack result = e.getResult();
        if (result == null) return;

        AnvilInventory inv = e.getInventory();
        AnvilView view = e.getView();

        float costMultiplier = 1;
        int extraAnvilCost = 0;

        boolean applyingBook = inv.getSecondItem() != null && inv.getSecondItem().getType() == Material.ENCHANTED_BOOK;
        boolean similar = inv.getFirstItem() != null && inv.getSecondItem() != null && inv.getFirstItem().getType() == inv.getSecondItem().getType();

        costMultiplier *= applyingBook ? similar ? 0.3F : 1.5F : 1F;

        // TODO: In Future
        // Repairing Similar, get the durability of the most repaired item

        // Enchantment Costs, get the lowest cost of merging

        if (e.getInventory().getLocation() != null) {
            switch (e.getInventory().getLocation().getBlock().getType()) {
                case CHIPPED_ANVIL -> { costMultiplier *= 1.25F; extraAnvilCost = 1; }
                case DAMAGED_ANVIL -> { costMultiplier *= 1.5F; extraAnvilCost = 2; }
            }
        }

        if (!applyingBook && inv.getFirstItem().containsEnchantment(MoguEnchantment.DISREPAIR_CURSE) || (inv.getSecondItem() != null && inv.getSecondItem().containsEnchantment(MoguEnchantment.DISREPAIR_CURSE)))
            costMultiplier *= 1.5F;

        int cost = Math.min(30, (int) (view.getRepairCost() * costMultiplier) + extraAnvilCost);

        view.setRepairCost(cost);
        e.setResult(formatItem(result));
    }

    @SuppressWarnings("UnstableApiUsage")
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onOpenAnvil(InventoryOpenEvent e) {
        MoguGui customGui = MoguProfile.from(e.getPlayer().getUniqueId()).getMoguGui();
        if (customGui != null) {
            customGui.onInventoryOpened();
            return;
        }

        if (e.getView().getType() == InventoryType.ANVIL) {
            AnvilView view = (AnvilView) e.getView();
            view.setMaximumRepairCost(30);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onCloseCustomGUI(InventoryCloseEvent e) {
        MoguGui customGui = MoguProfile.from(e.getPlayer().getUniqueId()).getMoguGui();
        if (customGui != null) {
            customGui.onInventoryClosed(e);
        }
        MoguProfile.from(e.getPlayer().getUniqueId()).setMoguGui(null);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onClickCustomGUI(InventoryClickEvent e) {
        MoguGui customGui = MoguProfile.from(e.getView().getPlayer().getUniqueId()).getMoguGui();
        if (customGui != null && customGui.preInventoryClick(e))
            customGui.onInventoryClicked(e);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onClickCustomGUI(InventoryDragEvent e) {
        MoguGui customGui = MoguProfile.from(e.getView().getPlayer().getUniqueId()).getMoguGui();
        if (customGui != null)
            customGui.onInventoryDrag(e);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onAnvil(AnvilDamagedEvent e) {
        e.setCancelled(e.isBreaking());
    }

}

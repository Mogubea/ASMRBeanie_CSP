package me.mogubea.gui;

import me.mogubea.profile.PlayerPickupFilter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

public class MoguGuiPickupfilter extends MoguGui {

	public MoguGuiPickupfilter(@NotNull Player p) {
		super(p);
		if (!isReopened)
			prepareInventory(page);
	}

    @Override
	public void onInventoryClicked(@NotNull InventoryClickEvent e) {
		final int slot = e.getRawSlot();
		final ItemStack item = Objects.requireNonNull(e.getClickedInventory()).getItem(e.getSlot());
		if (item == null) return;

		if (slot >= presetSize) {
			if (myFilter().size() >= myFilter().getMaxSize()) {
				player.sendActionBar(Component.text("You can't add anymore blacklisted items!", NamedTextColor.RED));
				return;
			}

			if (!myFilter().addEntry(item)) return;
			player.playSound(player.getLocation(), Sound.ITEM_ARMOR_EQUIP_LEATHER, 0.2F, 0.8F);
			reopen(page);
		} else if (slot < presetSize-9) {
			if (!myFilter().removeEntry(item)) return;
			player.playSound(player.getLocation(), Sound.ITEM_ARMOR_EQUIP_LEATHER, 0.2F, 0.8F);
			reopen(page);
		}
	}

	@Override
	public void pageUp() {
		reopen(++page);
	}

	@Override
	public void pageDown() {
		reopen(--page);
	}

	private void prepareInventory(int page) {
		this.presetSize = 54;
		this.presetInv = new ItemStack[presetSize];

		// Update Contents
		final List<String> blacklist = myFilter().getList();
		int maxPages = 1 + Math.floorDiv(myFilter().size(), 45);
		for (int x = (page * 45) - 1; ++x < Math.min(45 + (page * 45), blacklist.size());) {
			ItemStack item = getPlugin().getItemManager().fromIdentifier(blacklist.get(x));
			presetInv[x - (page * 45)] = item;
		}

		for (int x = -1; ++x < 9;)
			presetInv[45 + x] = bBlank;

		presetInv[46] = page > 0 ? prevPage : bBlank;
		presetInv[52] = (page < 2 && (blacklist.size()+1)>(45*(page+1))) ? nextPage : bBlank;
		setName(Component.text("Item Blacklist" + (maxPages > 1 ? " ("+(page+1)+"/"+maxPages+")" : "") + " [" + myFilter().size() + "/" + myFilter().getMaxSize() + "]"));
	}

	private PlayerPickupFilter myFilter() {
		return getProfile().getPickupFilter();
	}

	private void reopen(int page) {
		MoguGuiPickupfilter newInv = new MoguGuiPickupfilter(player);
		newInv.isReopened = true;
		newInv.page = page;
		newInv.prepareInventory(page);
		newInv.openInventory();
	}
	
}

package me.mogubea.gui;

import io.papermc.paper.datacomponent.DataComponentTypes;
import me.mogubea.main.Main;
import me.mogubea.profile.MoguProfile;
import me.mogubea.utils.Utils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * A custom interface that can be instantiated by players.
 */
public abstract class MoguGui {

	protected static final ItemStack bBlank;
	protected static final ItemStack nextPage = newItem(Utils.getSkullWithCustomSkin("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNjgyYWQxYjljYjRkZDIxMjU5YzBkNzVhYTMxNWZmMzg5YzNjZWY3NTJiZTM5NDkzMzgxNjRiYWM4NGE5NmUifX19"), Component.text("Next Page", NamedTextColor.GRAY), null);
	protected static final ItemStack prevPage = newItem(Utils.getSkullWithCustomSkin("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMzdhZWU5YTc1YmYwZGY3ODk3MTgzMDE1Y2NhMGIyYTdkNzU1YzYzMzg4ZmYwMTc1MmQ1ZjQ0MTlmYzY0NSJ9fX0="), Component.text("Previous Page", NamedTextColor.GRAY), null);

	protected static final ItemStack closeUI = newItem(new ItemStack(Material.BARRIER), Component.text("Close", NamedTextColor.RED), null);

	static {
		bBlank = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
		bBlank.setData(DataComponentTypes.HIDE_TOOLTIP);
		bBlank.setData(DataComponentTypes.MAX_STACK_SIZE, 1);
	}


	protected final @NotNull Player player;
	protected final @NotNull Main plugin;

	protected int interactCooldown = 300;
	protected int presetSize = 27;
	protected InventoryType presetType;
	protected Component name = Component.text("Inventory");
	protected Component baseName;
	protected ItemStack[] presetInv;
	private final MoguProfile profile; // Real
	private MoguProfile targetProfile; // Target
	protected int page;
	protected int data;
	protected boolean isReopened = false;
	protected Inventory inventory;

	protected MoguGui(@NotNull Player player) {
		this(player, 0, 0);
	}

	protected MoguGui(@NotNull Player player, int page, int data) {
		this.player = player;
		MoguProfile ack = MoguProfile.from(player);
//		if (!ack.profileOverride.equals(ack.getUniqueId()))
//			tpp = PlayerProfile.from(ack.profileOverride);
//		else
		this.targetProfile = ack;
		this.profile = ack;
		this.plugin = profile.getPlugin();
		this.page = page;
		this.data = data;
	}
	
	public void onInventoryClosed(@NotNull InventoryCloseEvent e) {}

	public void onInventoryOpened() {}

	public void onInventoryClicked(@NotNull InventoryClickEvent e) {}

	public void onInventoryDrag(@NotNull InventoryDragEvent e) {
		for (int slot : e.getRawSlots()) {
			if (slot < inventory.getSize()) {
				e.setCancelled(true);
				return;
			}
		}
	}

	/**
	 * @param e The {@link InventoryClickEvent}.
	 * @return Whether {@link #onInventoryClicked(InventoryClickEvent)} will be fired or not.
	 */
	public boolean preInventoryClick(InventoryClickEvent e) {
		if (e.getAction() == InventoryAction.NOTHING) return false;

		e.setCancelled(true);
		final ItemStack i = e.getCurrentItem();
		if (i == null) return false;

		if (interactCooldown > 0 && profile.onCdElseAdd("guiClick", interactCooldown)) {
			return false;
		} else if (i.isSimilar(closeUI)) {
			close();
		} else if (i.isSimilar(nextPage)) {
			pageUp();
			player.playSound(player.getLocation(), Sound.ITEM_BOOK_PAGE_TURN, 0.3F, 1.0F);
		} else if (i.isSimilar(prevPage)) {
			pageDown();
			player.playSound(player.getLocation(), Sound.ITEM_BOOK_PAGE_TURN, 0.3F, 1.0F);
		} else {
			return true;
		}

		return false;
	}

	public void openInventory() {
		if (!isReopened)
			playOpenSound();
		if (this.presetType != null && presetType.isCreatable())
			this.inventory = Bukkit.createInventory(player, presetType, name);
		else
			this.inventory = Bukkit.createInventory(player, presetSize, name);
		if (presetInv != null)
			inventory.setContents(presetInv);

		player.openInventory(inventory);
		profile.setMoguGui(this);
		
		onInventoryOpened();
	}
	
	public void pageUp() {
		this.page++;
		if (page > 50)
			page = 50;
		onInventoryOpened();
	}
	
	public void pageDown() {
		this.page--;
		if (page < 0)
			page = 0;
		onInventoryOpened();
	}
	
	public void setPage(int page) {
		this.page = page;
		if (page < 0 || page > 50)
			this.page = 0;
		onInventoryOpened();
	}
	
	public void setProfile(MoguProfile pp) {
		this.targetProfile = pp;
		onInventoryOpened();
	}

	public @NotNull MoguProfile getViewerProfile() {
		return profile;
	}

	public @NotNull MoguProfile getProfile() {
		return targetProfile;
	}

	public @NotNull Player getViewer() {
		return player;
	}

	public @NotNull Inventory getInventory() {
		return inventory;
	}
	
	public int getPage() {
		return page;
	}
	
	public int getData() {
		return data;
	}
	
	public int setData(int data) {
		return this.data = data;
	}
	
	public int upData() {
		return this.data++;
	}
	
	public int downData() {
		return this.data--;
	}
	
	protected void setName(@NotNull Component name) {
		this.name = name;
	}
	
	protected void setName(@NotNull String name) {
		this.name = Component.text(name);
	}

	public @NotNull Component getName() {
		return name;
	}
	
	protected boolean isOverrideView() {
		return targetProfile.getDatabaseId() != profile.getDatabaseId();
	}
	
	protected void playOpenSound() {
		player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 0.25F, 1.0F);
	}
	
	public void close() {
		player.closeInventory();
	}
	
	public void refresh() {
		onInventoryOpened();
	}

	/**
	 * Fires every 20 in-game ticks.
	 */
	public void onTick() {

	}

	/**
	 * Get all the currently viewed instances of the specified {@link MoguGui},
	 * this method can be very useful in a forEach loop to {@link MoguGui#refresh()} with a criteria.
	 */
	public static @NotNull <T extends MoguGui> Collection<T> getAllViewers(@Nonnull final Class<T> clazz) {
		ArrayList<T> instances = new ArrayList<>();
		Bukkit.getOnlinePlayers().forEach((p) -> {
			MoguProfile pp = MoguProfile.from(p);
			if (pp.getMoguGui() != null && clazz.isInstance(pp.getMoguGui()))
				instances.add(clazz.cast(pp.getMoguGui()));
		});
		return instances;
	}

	public @NotNull Main getPlugin() {
		return plugin;
	}

	@NotNull
	protected static ItemStack newItem(@NotNull ItemStack it, Component name, List<TextComponent> lore) {
		ItemStack i = it.clone();
		ItemMeta meta = i.getItemMeta();
		meta.displayName(name.decoration(TextDecoration.ITALIC, false));
		meta.addItemFlags(ItemFlag.HIDE_DYE, ItemFlag.HIDE_ARMOR_TRIM, ItemFlag.HIDE_STORED_ENCHANTS, ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_UNBREAKABLE);
		meta.lore(lore);
		i.setItemMeta(meta);
		return i;
	}
	
	protected final static DecimalFormat df = new DecimalFormat("#,###");
	protected final static DecimalFormat dec = new DecimalFormat("#,###.##");
	
}

package me.mogubea.commands;

import me.mogubea.main.Main;
import me.mogubea.profile.MoguProfile;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

public class CommandEnderchest extends MoguCommand {
	
	public CommandEnderchest(Main plugin) {
		super(plugin, false, "enderchest", "ec");
		setDescription("Access your ender chest.");
	}
	
	@Override
	public boolean runCommand(MoguProfile profile, @Nonnull CommandSender sender, @Nonnull Command cmd, @Nonnull String str, @Nonnull String[] args) {
		final Player p = (Player)sender;
		final Player target = args.length > 0 && hasSubPerm(sender, "others", false) ? toPlayer(sender, args[0]) : p;
		final Inventory inv = p == target ? p.getEnderChest() : Bukkit.createInventory(target.getEnderChest().getHolder(), InventoryType.ENDER_CHEST, target.name().append(Component.text("'s Enderchest")));

		p.openInventory(inv);
		p.playSound(p, Sound.BLOCK_ENDER_CHEST_OPEN, 1F, 0.6F);
		return true;
	}

	@Override
	public @Nullable List<String> runTabComplete(@Nonnull CommandSender sender, @Nonnull Command cmd, @Nonnull String str, @Nonnull String[] args) {
		if (args.length == 1 && hasSubPerm(sender, "others", false))
			return getTabCompleter().completeOnlinePlayer(sender, args[0]);
		return Collections.emptyList();
	}

}

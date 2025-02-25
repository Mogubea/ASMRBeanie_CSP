package me.mogubea.commands;

import me.mogubea.main.Main;
import me.mogubea.profile.MoguProfile;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;

import javax.annotation.Nonnull;

public class CommandWorkbench extends MoguCommand {

	public CommandWorkbench(Main plugin) {
		super(plugin, false, "workbench", "wb", "craftingtable", "ct");
		setDescription("Open a virtual crafting table!");
	}
	
	@Override
	public boolean runCommand(MoguProfile profile, @Nonnull CommandSender sender, @Nonnull Command cmd, @Nonnull String str, @Nonnull String[] args) {
		((Player)sender).openInventory(Bukkit.createInventory((Player)sender, InventoryType.WORKBENCH));
		return true;
	}

}

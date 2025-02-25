package me.mogubea.commands;

import me.mogubea.main.Main;
import me.mogubea.profile.MoguProfile;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;

import javax.annotation.Nonnull;

public class CommandAnvil extends MoguCommand {

	public CommandAnvil(Main plugin) {
		super(plugin, false, "anvil");
		setDescription("Open a virtual anvil!");
	}
	
	@Override
	public boolean runCommand(MoguProfile profile, @Nonnull CommandSender sender, @Nonnull Command cmd, @Nonnull String str, @Nonnull String[] args) {
		((Player)sender).openInventory(Bukkit.createInventory((Player)sender, InventoryType.ANVIL));
		return true;
	}

}

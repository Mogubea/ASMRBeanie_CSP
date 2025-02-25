package me.mogubea.commands;

import me.mogubea.gui.MoguGuiPickupfilter;
import me.mogubea.main.Main;
import me.mogubea.profile.MoguProfile;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

public class CommandPickupfilter extends MoguCommand {
	
	public CommandPickupfilter(Main plugin) {
		super(plugin, "pickupfilter", "itemfilter", "blacklist", "pickupblacklist");
		setDescription("A command shortcut for accessing your pickup blacklist.");
	}
	
	@Override
	public boolean runCommand(MoguProfile profile, @Nonnull CommandSender sender, @Nonnull Command cmd, @Nonnull String str, @Nonnull String[] args) {
		new MoguGuiPickupfilter((Player)sender).openInventory();
		return true;
	}

	@Override
	public @Nullable List<String> runTabComplete(@Nonnull CommandSender sender, @Nonnull Command cmd, @Nonnull String str, @Nonnull String[] args) {
		return Collections.emptyList();
	}

}

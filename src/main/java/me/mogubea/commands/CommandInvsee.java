package me.mogubea.commands;

import me.mogubea.main.Main;
import me.mogubea.profile.MoguProfile;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

public class CommandInvsee extends MoguCommand {
	
	public CommandInvsee(Main plugin) {
		super(plugin, false, 1, "invsee", "showinv");
		setDescription("View another player's inventory.");
	}
	
	@Override
	public boolean runCommand(MoguProfile profile, @Nonnull CommandSender sender, @Nonnull Command cmd, @Nonnull String str, @Nonnull String[] args) {
		final Player target = toPlayer(sender, args[0], false);

		((Player)sender).openInventory(target.getInventory());
		return true;
	}

	@Override
	public @Nullable List<String> runTabComplete(@Nonnull CommandSender sender, @Nonnull Command cmd, @Nonnull String str, @Nonnull String[] args) {
		if (args.length == 1)
			return getPlugin().getCommandManager().getTabCompleter().completeOnlinePlayer(sender, args[0]);
		return Collections.emptyList();
	}

}

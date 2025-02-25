package me.mogubea.commands;

import me.mogubea.main.Main;
import me.mogubea.profile.MoguProfile;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.text.DecimalFormat;
import java.util.Collections;
import java.util.List;

public class CommandMoney extends MoguCommand {

	private final DecimalFormat format = new DecimalFormat("#,###");

	public CommandMoney(Main plugin) {
		super(plugin, true, "money", "bal", "balance", "beans", "coins", "wallet", "purse", "$", "coinen");
		setDescription("View your, or another player's balance.");
		setRequiresPermission(false);
	}
	
	@Override
	public boolean runCommand(MoguProfile profile, @Nonnull CommandSender sender, @Nonnull Command cmd, @Nonnull String str, @Nonnull String[] args) {
		if (!(sender instanceof Player) && args.length == 0)
			throw new CommandException(sender, "Please specify a player!");

		final Player target = args.length > 0 ? toPlayer(sender, args[0]) : (Player) sender;
		MoguProfile targetProfile = MoguProfile.from(target);

		Component msg = Component.text(format.format(targetProfile.getMoney()), NamedTextColor.GOLD).append(Component.text("$", NamedTextColor.GREEN));
		msg = ((sender == target) ? Component.text("You currently have ", NamedTextColor.GREEN) : targetProfile.getColouredName().append(Component.text(" currently has ", NamedTextColor.GREEN))).append(msg);
		sender.sendMessage(msg);
		return true;
	}

	@Override
	public @Nullable List<String> runTabComplete(@Nonnull CommandSender sender, @Nonnull Command cmd, @Nonnull String str, @Nonnull String[] args) {
		if (args.length == 1)
			return getTabCompleter().completeOnlinePlayer(sender, args[0]);
		return Collections.emptyList();
	}

}

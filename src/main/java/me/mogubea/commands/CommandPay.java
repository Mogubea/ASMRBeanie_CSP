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

public class CommandPay extends MoguCommand {

	private final DecimalFormat format = new DecimalFormat("#,###");

	public CommandPay(Main plugin) {
		super(plugin, false, 2, "pay");
		setDescription("Pay players some monay~");
		setRequiresPermission(false);
	}
	
	@Override
	public boolean runCommand(MoguProfile profile, @Nonnull CommandSender sender, @Nonnull Command cmd, @Nonnull String str, @Nonnull String[] args) {
		MoguProfile targetProfile = args.length > 0 ? toProfile(sender, args[0]) : profile;

		if (profile.getMoney() <= 0)
			throw new CommandException(sender, "You don't have any money!");

        long amountToPay = toIntMinMax(sender, args[1], 1, (int) Math.min(Integer.MAX_VALUE, profile.getMoney()));
		if (amountToPay > profile.getMoney())
			throw new CommandException(sender, "You don't have that much to send!");

		targetProfile.addMoney(amountToPay, "PLAYER_" + profile.getDatabaseId());
		profile.addMoney(amountToPay, "PLAYER_" + targetProfile.getDatabaseId());

		Component moneyComp = Component.text(format.format(amountToPay) + "$", NamedTextColor.GOLD);

		if (targetProfile.isPlayerOnline())
			targetProfile.getPlayer().sendMessage(profile.getColouredName().append(Component.text(" just sent you ", NamedTextColor.GREEN)).append(moneyComp));
		sender.sendMessage(Component.text("You have sent ", NamedTextColor.GREEN).append(moneyComp).append(Component.text(" to ", NamedTextColor.GREEN)).append(targetProfile.getColouredName()));
		return true;
	}

	@Override
	public @Nullable List<String> runTabComplete(@Nonnull CommandSender sender, @Nonnull Command cmd, @Nonnull String str, @Nonnull String[] args) {
		if (args.length == 1)
			return getTabCompleter().completeOnlinePlayer(sender, args[0]);
		return Collections.emptyList();
	}

}

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
import java.util.Collections;
import java.util.List;

public class CommandHug extends MoguCommand {

	public CommandHug(Main plugin) {
		super(plugin, false, 1, "hug", "cuddle", "huggies");
		setCooldown(10000);
		setDescription("Huggies!");
		setRequiresPermission(false);
	}
	
	@Override
	public boolean runCommand(MoguProfile profile, @Nonnull CommandSender sender, @Nonnull Command cmd, @Nonnull String str, @Nonnull String[] args) {
		if (args[0].equalsIgnoreCase("everyone") || args[0].equalsIgnoreCase("all")) {
			getPlugin().getServer().broadcast(profile.getColouredName().append(Component.text(" hugged everynyan!", NamedTextColor.YELLOW)));
			return true;
		}

		final Player target = toPlayer(sender, args[0]);
		getPlugin().getServer().broadcast(profile.getColouredName().append(Component.text(" gave ", NamedTextColor.YELLOW).append(MoguProfile.from(target).getColouredName()).append(Component.text(" a hug!", NamedTextColor.YELLOW))));
		return true;
	}

	@Override
	public @Nullable List<String> runTabComplete(@Nonnull CommandSender sender, @Nonnull Command cmd, @Nonnull String str, @Nonnull String[] args) {
		if (args.length == 1)
			return getTabCompleter().completeOnlinePlayer(sender, args[0]);
		return Collections.emptyList();
	}

}

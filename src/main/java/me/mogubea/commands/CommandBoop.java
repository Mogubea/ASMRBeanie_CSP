package me.mogubea.commands;

import me.mogubea.main.Main;
import me.mogubea.profile.MoguProfile;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class CommandBoop extends MoguCommand {

	private final Set<String> allWords = Set.of("everyone", "everynyan", "all", "everybody", "@a");

	public CommandBoop(Main plugin) {
		super(plugin, false, 1, "boop");
		setCooldown(10000);
		setDescription("Boop!");
		setRequiresPermission(false);
	}
	
	@Override
	public boolean runCommand(MoguProfile profile, @Nonnull CommandSender sender, @Nonnull Command cmd, @Nonnull String str, @Nonnull String[] args) {
		if (allWords.contains(args[0].toLowerCase())) {
			getPlugin().getServer().broadcast(profile.getColouredName().append(Component.text(" booped everynyan!", NamedTextColor.YELLOW)));
			return true;
		}

		final Player target = toPlayer(sender, args[0]);
		getPlugin().getServer().broadcast(profile.getColouredName().append(Component.text(" booped ", NamedTextColor.YELLOW).append(MoguProfile.from(target).getColouredName()).append(Component.text("!", NamedTextColor.YELLOW))));
		return true;
	}

	@Override
	public @Nullable List<String> runTabComplete(@Nonnull CommandSender sender, @Nonnull Command cmd, @Nonnull String str, @Nonnull String[] args) {
		if (args.length == 1)
			return getTabCompleter().completeOnlinePlayer(sender, args[0]);
		return Collections.emptyList();
	}

}

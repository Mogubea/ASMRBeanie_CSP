package me.mogubea.commands;

import me.mogubea.main.Main;
import me.mogubea.profile.MoguProfile;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.attribute.Attribute;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class CommandHeal extends MoguCommand {

	public CommandHeal(Main plugin) {
		super(plugin, true, 0, "heal");
		setDescription("Heal a player!");
	}
	
	@Override
	public boolean runCommand(MoguProfile profile, @Nonnull CommandSender sender, @Nonnull Command cmd, @Nonnull String str, @Nonnull String[] args) {
		final Player target = args.length >= 1 ? toPlayer(sender, args[0]) : sender instanceof Player player ? player : null;
		if (target == null) throw new CommandException(sender, "Specify a player to heal!");

		target.heal(Objects.requireNonNull(target.getAttribute(Attribute.MAX_HEALTH)).getValue());
		target.sendMessage(MoguProfile.from(target).getColouredName().append(Component.text(" has been healed.", NamedTextColor.GRAY)));
		return true;
	}

	@Override
	public @Nullable List<String> runTabComplete(@Nonnull CommandSender sender, @Nonnull Command cmd, @Nonnull String str, @Nonnull String[] args) {
		if (args.length == 1)
			return getTabCompleter().completeOnlinePlayer(sender, args[0]);
		return Collections.emptyList();
	}

}

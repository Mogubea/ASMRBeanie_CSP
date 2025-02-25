package me.mogubea.commands;

import me.mogubea.main.Main;
import me.mogubea.profile.MoguProfile;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import javax.annotation.Nonnull;

public class CommandSill extends MoguCommand {

	public CommandSill(Main plugin) {
		super(plugin, true, "sill", "silly");
		setRequiresPermission(false);
		setDescription("Silly.");
	}
	
	@Override
	public boolean runCommand(MoguProfile profile, @Nonnull CommandSender sender, @Nonnull Command cmd, @Nonnull String str, @Nonnull String[] args) {
		sender.sendMessage(Component.text("Sill.", NamedTextColor.GRAY));
		return true;
	}

}

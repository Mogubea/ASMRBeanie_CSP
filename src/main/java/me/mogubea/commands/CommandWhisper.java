package me.mogubea.commands;

import me.mogubea.main.Main;
import me.mogubea.profile.MoguProfile;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

public class CommandWhisper extends MoguCommand {

	private final Map<CommandSender, CommandSender> replyTarget = new HashMap<>();
	private final Set<String> replyCommands;

	public CommandWhisper(Main plugin) {
		super(plugin, true, 1, "whisper", "message", "msg", "dm", "pm", "m", "tell", "reply", "r", "respond");
		replyCommands = Set.of("reply", "r", "respond");
		setDescription("Private messages");
		setRequiresPermission(false);
	}
	
	@Override
	public boolean runCommand(MoguProfile profile, @Nonnull CommandSender sender, @Nonnull Command cmd, @Nonnull String str, @Nonnull String[] args) {
		boolean isReply = replyCommands.contains(str);
		if (isReply && !replyTarget.containsKey(sender))
			throw new CommandException(sender, "You have no one to respond to!");

		CommandSender target = isReply ? replyTarget.get(sender) : toPlayer(sender, args[0], false);
		if (target == null)
			throw new CommandException(sender, "There was a problem sending your message, maybe they are offline?");

		StringBuilder builder = new StringBuilder();
		Arrays.spliterator(args, isReply ? 0 : 1, args.length).forEachRemaining(word -> builder.append(word).append(" "));
		Component message = Component.text(getPlugin().getWordFilter().filter(builder.toString()), NamedTextColor.GRAY).decorate(TextDecoration.ITALIC);

		Component prefix = Component.text("[", NamedTextColor.DARK_GRAY).append(toName(sender, sender)).append(Component.text(" -> ", NamedTextColor.DARK_GRAY)).append(toName(target, sender)).append(Component.text("]: ", NamedTextColor.DARK_GRAY));
		sender.sendMessage(prefix.append(message));

		prefix = Component.text("[", NamedTextColor.DARK_GRAY).append(toName(sender, target)).append(Component.text(" -> ", NamedTextColor.DARK_GRAY)).append(toName(target, target)).append(Component.text("]: ", NamedTextColor.DARK_GRAY));
		target.sendMessage(prefix.append(message));
		replyTarget.put(target, sender);
		return true;
	}

	@Override
	public @Nullable List<String> runTabComplete(@Nonnull CommandSender sender, @Nonnull Command cmd, @Nonnull String str, @Nonnull String[] args) {
		if (args.length == 1)
			return getTabCompleter().completeOnlinePlayer(sender, args[0]);
		return Collections.emptyList();
	}

}

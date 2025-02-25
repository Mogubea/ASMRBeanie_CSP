package me.mogubea.commands;

import me.mogubea.main.Main;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Constructor;
import java.util.*;

public class CommandManager {

	private final List<Command> myCommands = new ArrayList<>();
	private Map<String, MoguCommand> moguCommands = new HashMap<>();
	
	private final Main plugin;
	private final TabCompleter tabCompleter;
	
	public CommandManager(Main plugin) {
		this.plugin = plugin;
		this.tabCompleter = new TabCompleter();
		long millis = System.currentTimeMillis();

		registerCommand(new CommandI(plugin));
		registerCommand(new CommandEnderchest(plugin));
//		registerCommand(new CommandAnvil(plugin));
		registerCommand(new CommandGamemode(plugin));
		registerCommand(new CommandModify(plugin));
		registerCommand(new CommandBoop(plugin));
		registerCommand(new CommandHug(plugin));
//		registerCommand(new CommandWorkbench(plugin));
		registerCommand(new CommandPay(plugin));
		registerCommand(new CommandMoney(plugin));
		registerCommand(new CommandSetjob(plugin));
		registerCommand(new CommandDebug(plugin));
		registerCommand(new CommandWhisper(plugin));
		registerCommand(new CommandInvsee(plugin));
		registerCommand(new CommandHeal(plugin));
		registerCommand(new CommandSmite(plugin));
		registerCommand(new CommandSill(plugin));
		registerCommand(new CommandFly(plugin));
		registerCommand(new CommandPickupfilter(plugin));

		moguCommands = Map.copyOf(moguCommands);
		plugin.getSLF4JLogger().info("Registered " + moguCommands.size() + " commands in " + (System.currentTimeMillis() - millis) + "ms");
	}

	/**
	 * Register a {@link MoguCommand} to the server.
	 * @param moguCommand The {@link MoguCommand} instance.
	 */
	private void registerCommand(MoguCommand moguCommand) {
		String[] aliases = moguCommand.getAliases();
		String cmdName = aliases[0];

		if (moguCommands.containsKey(cmdName))
			throw new RuntimeException("The command /" + cmdName + " has already been registered.");

		try {
			Constructor<PluginCommand> c = PluginCommand.class.getDeclaredConstructor(String.class, Plugin.class);
			c.setAccessible(true);
			PluginCommand command = c.newInstance(cmdName, plugin);

			command.setAliases(Arrays.asList(aliases));
			if (moguCommand.requiresPermission()) {
				command.setPermission(moguCommand.getPermissionString());
				plugin.getPermissionManager().addRecognisedPermission(moguCommand.getPermissionString());
			}
			command.setDescription(moguCommand.getDescription());
			command.setExecutor(moguCommand);
			Bukkit.getCommandMap().register(plugin.getName(), command);

			myCommands.add(command);
			moguCommands.put(cmdName, moguCommand);
		} catch (Exception e) {
			plugin.getSLF4JLogger().trace("Error registering commands", e);
		}
	}

	/**
	 * Unregister all the custom commands.
	 */
	public void unregisterCommands() {
		int size = getMyCommands().size();
		for (int x = -1; ++x < size;)
			myCommands.get(x).unregister(Bukkit.getCommandMap());
	}

	public @NotNull List<Command> getMyCommands() {
		return myCommands;
	}

	/**
	 * Grab an unmodifiable map of the custom {@link MoguCommand}s.
	 */
	public @NotNull Map<String, MoguCommand> getMoguCommands() {
		return moguCommands;
	}

	public @NotNull TabCompleter getTabCompleter() {
		return tabCompleter;
	}

}

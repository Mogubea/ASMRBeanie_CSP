package me.mogubea.commands;

import me.mogubea.jobs.Job;
import me.mogubea.jobs.JobManager;
import me.mogubea.main.Main;
import me.mogubea.profile.MoguProfile;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class CommandSetjob extends MoguCommand {

	public CommandSetjob(Main plugin) {
		super(plugin, false, 1, "setjob", "setclass", "jobbies", "jobset");
		setDescription("Set your job and earn some moolah~");
		setRequiresPermission(false);
	}
	
	@Override
	public boolean runCommand(MoguProfile profile, @Nonnull CommandSender sender, @Nonnull Command cmd, @Nonnull String str, @Nonnull String[] args) {
		if (!hasSubPerm(sender, "cooldown", false) && Objects.requireNonNull(profile).getDayOfLastJobChange() == getPlugin().getCurrentGameDay()) {
			sender.sendActionBar(Component.text("You must wait until the next in-game day before changing jobs!", NamedTextColor.RED));
			return true;
		}

		final Job job = getPlugin().getJobManager().getJob(args[0]);
		if (job == Job.getNone()) throw new CommandException(sender, "Couldn't find a job with the name \"" + args[0] + "\"~");
		if (job == profile.getJob()) throw new CommandException(sender, Component.text("You are already a ",NamedTextColor.RED).append(job.getDisplayName()));

		profile.changeJob(job);
		getPlugin().getServer().broadcast(profile.getColouredName().append(Component.text(" is now a ", NamedTextColor.YELLOW)).append(job.getDisplayName()));
		sender.sendMessage(job.getDescription().color(NamedTextColor.GRAY));
		return true;
	}

	@Override
	public @Nullable List<String> runTabComplete(@Nonnull CommandSender sender, @Nonnull Command cmd, @Nonnull String str, @Nonnull String[] args) {
		if (args.length == 1)
			return (List<String>) getTabCompleter().completeObject(args[0], Job::getLowerName, getPlugin().getJobManager().getJobs());
		return Collections.emptyList();
	}

}

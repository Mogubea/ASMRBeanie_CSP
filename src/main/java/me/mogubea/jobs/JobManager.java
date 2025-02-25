package me.mogubea.jobs;

import me.mogubea.main.Main;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;

public class JobManager {

    private final @NotNull List<@NotNull Job> jobs;
    private final @NotNull Main plugin;

    public JobManager(@NotNull Main plugin) {
        this.plugin = plugin;
        jobs = List.of(new JobBuilder(this), new JobMiner(this), new JobLumberjack(this), new JobFarmer(this), new JobFisherman(this), new JobHunter(this), new JobCleric(this));
    }

    public final @NotNull Job getJob(@Nullable String jobName) {
        if (jobName != null) {
            jobName = jobName.toLowerCase();
            for (int x = -1; ++x < jobs.size();) {
                if (jobs.get(x).getAliases().contains(jobName))
                    return jobs.get(x);
            }
        }

        return Job.getNone();
    }

    public final @Unmodifiable @NotNull List<@NotNull Job> getJobs() {
        return jobs;
    }

    protected final @NotNull Main getPlugin() {
        return plugin;
    }

}

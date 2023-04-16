package org.mcphackers.mcp.tasks;

import org.mcphackers.mcp.MCP;

import java.util.Arrays;
import java.util.concurrent.Callable;

public abstract class TaskStaged extends Task {
    private final Stage[] stages;

    public TaskStaged(MCP mcp, Side side) {
        super(mcp, side);
        this.stages = setStages();
    }

    protected Task task(Callable<Void> task) {
        return new Task(this.mcp, this.side) {
            @Override
            protected void doTask() throws Exception {
                task.call();
            }
        };
    }

    @Override
    protected void doTask() throws Exception {
        // Calculate percentages for each stage
        double notCounted = Arrays.stream(stages).filter(s -> s.percentage < 0.0D).count();
        double percentage = 1.0D / notCounted;

        for (Stage stage : stages) {
            if (stage.percentage < 0.0D) stage.percentage = percentage;
            int progress = getProgress() + (int) (stage.percentage * 100D); // TODO: check this
            updateProgress(stage.name, progress);
            stage.task.run();
        }
    }

    protected abstract Stage[] setStages();

    protected static class Stage {
        protected final Task task;
        protected final String name;
        protected double percentage;

        protected Stage(Task task, String name, double percentage) {
            this.task = task;
            this.name = name;
            this.percentage = percentage;
        }

        protected Stage(Task task, String name) {
            this(task, name, -1.0D);
        }

        protected Stage(Task task) {
            this(task, task.getClass().getSimpleName(), -1.0D);
        }
    }
}

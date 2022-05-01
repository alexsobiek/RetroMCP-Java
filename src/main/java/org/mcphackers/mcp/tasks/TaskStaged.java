package org.mcphackers.mcp.tasks;

import org.mcphackers.mcp.MCP;
import org.mcphackers.mcp.ProgressListener;
import org.mcphackers.mcp.TaskRunnable;
import org.mcphackers.mcp.plugin.MCPPlugin.TaskEvent;

public abstract class TaskStaged extends Task {
	
	public int step;
	public Stage[] stages;
	
	public TaskStaged(Side side, MCP instance, ProgressListener listener) {
		super(side, instance, listener);
	}

	public TaskStaged(Side side, MCP instance) {
		super(side, instance);
	}
	
	public TaskStaged(MCP instance) {
		super(instance);
	}
	
	protected final void step() {
		step++;
		triggerEvent(TaskEvent.TASK_STEP);
	}
	
	protected abstract Stage[] setStages();
	
	public void doTask() throws Exception {
		stages = setStages();
		mcp.setPluginOverrides(this);
		while(step < stages.length) {
			updateProgress();
			stages[step].doTask();
			step();
		}
	}
	
	public void overrideStage(int stageIndex, TaskRunnable task) {
		if(stageIndex < stages.length) {
			stages[stageIndex].setOperation(task);
		}
	}
	
	protected void updateProgress() {
		if(step < stages.length) {
			if(stages[step].completion >= 0 && stages[step].completion <= 100) {
				setProgress(stages[step].stageName, stages[step].completion);
			}
			else {
				setProgress(stages[step].stageName);
			}
		}
		else {
			setProgress("Idle");
		}
	}
	
	public Stage stage(String name, int percentage, TaskRunnable task) {
		return new Stage(name, percentage, task);
	}
	
	public Stage stage(String name, TaskRunnable task) {
		return new Stage(name, -1, task);
	}
	
	public class Stage {
		private TaskRunnable runnable;
		private String stageName;
		private int completion;
		
		public Stage(String name, int i, TaskRunnable task) {
			setOperation(task);
			stageName = name;
			completion = i;
		}
		
		private void setOperation(TaskRunnable task) {
			runnable = task;
		}
		
		private void doTask() throws Exception {
			runnable.doTask();
		}
	}

}

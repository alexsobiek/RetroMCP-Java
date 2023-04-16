package org.mcphackers.mcp.tasks.mode;

import org.mcphackers.mcp.tasks.Task;

public class TaskModeBuilder {

	private String name;
	private boolean usesProgressBars = true;
	private Class<? extends Task> taskClass;
	private TaskParameter[] params = new TaskParameter[] {};
	private TaskMode.Requirement requirement;

	public TaskModeBuilder setName(String name) {
		this.name = name;
		return this;
	}

	public TaskModeBuilder setProgressBars(boolean enabled) {
		this.usesProgressBars = enabled;
		return this;
	}

	public TaskModeBuilder setTaskClass(Class<? extends Task> taskClass) {
		this.taskClass = taskClass;
		return this;
	}

	public TaskModeBuilder setParameters(TaskParameter[] params) {
		this.params = params;
		return this;
	}

	public TaskModeBuilder addRequirement(TaskMode.Requirement condition) {
		this.requirement = condition;
		return this;
	}

	public TaskMode build() {
		return new TaskMode(name, taskClass, params, usesProgressBars, requirement);
	}
}

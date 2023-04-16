package org.mcphackers.mcp.tasks;

import org.mcphackers.mcp.MCP;

public class TaskBuild extends TaskStaged {
    public TaskBuild(MCP mcp, Side side) {
        super(mcp, side);
    }

    @Override
    protected Stage[] setStages() {
        return new Stage[]{
                // new Stage(new TaskRecompile(this.mcp, this.side), )
        };
    }
}

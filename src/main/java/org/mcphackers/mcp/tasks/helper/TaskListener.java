package org.mcphackers.mcp.tasks.helper;

import org.mcphackers.mcp.tasks.Task;
import org.mcphackers.mcp.plugin.MCPPlugin;

public interface TaskListener {
    void onStateChange(Task.State state);

    void onLog(TaskLog log);

    void onProgress(TaskProgress progress);

    boolean onUpdateDialogue(String changelog, String version);

    String onPromptInput(String title, String msg);

    void onEvent(MCPPlugin.TaskEvent event);
}

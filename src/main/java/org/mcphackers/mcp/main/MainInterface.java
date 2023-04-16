package org.mcphackers.mcp.main;

import jdk.jfr.internal.LogLevel;
import org.mcphackers.mcp.MCP;
import org.mcphackers.mcp.plugin.MCPPlugin;
import org.mcphackers.mcp.tasks.Task;
import org.mcphackers.mcp.tasks.helper.TaskListener;
import org.mcphackers.mcp.tasks.helper.TaskLog;
import org.mcphackers.mcp.tasks.helper.TaskProgress;
import org.mcphackers.mcp.tasks.mode.TaskMode;
import org.mcphackers.mcp.tasks.mode.TaskParameter;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class MainInterface extends MCP {
    protected boolean isGUI = false;

    public boolean isGUI() {
        return isGUI;
    }

    /**
     * Sets progress bars based on list of running tasks and task mode
     * @param tasks
     * @param mode
     */
    public abstract void setProgressBars(List<Task> tasks, TaskMode mode);

    /**
     * Resets progress bars to inactive state
     */
    public abstract void clearProgressBars();

    /**
     * Sets display string for progress bar at specified barIndex
     * @param barIndex
     * @param progressMessage
     */
    public abstract void setProgress(int barIndex, String progressMessage);

    /**
     * Sets progress value for progress bar at specified barIndex (Must be in range from 0-100)
     * @param barIndex
     * @param progress
     */
    public abstract void setProgress(int barIndex, int progress);

    /**
     * Sets display string and progress value for progress bar at specified barIndex (Must be in range from 0-100)
     * @param barIndex
     * @param progressMessage
     * @param progress
     */
    public void setProgress(int barIndex, String progressMessage, int progress) {
        setProgress(barIndex, progress);
        setProgress(barIndex, progressMessage);
    }


    public abstract boolean yesNoInput(String title, String msg);

    /**
     * Implementation of string input
     */
    public abstract String inputString(String title, String msg);

    /**
     * Implementation of any important messages
     */
    public abstract void showMessage(String title, String msg, int type);

    public abstract void showMessage(String title, String msg, Throwable e);

    /**
     * Displayed by TaskUpdateMCP
     * @param changelog
     * @param version
     * @return <code>true</code> if the user chose to install update
     */
    public abstract boolean updateDialogue(String changelog, String version);

    /**
     * Changes a parameter in current options and saves changes to disk, shows an error message if fails
     * @param param
     * @param value
     * @throws IllegalArgumentException
     */
    public boolean safeSetParameter(TaskParameter param, String value) {
        boolean res = super.safeSetParameter(param, value);
        if (!res)
            showMessage(param.getDesc(), TRANSLATOR.translateKey("options.invalidValue"), Task.ERROR);
        return res;
    }

    @Override
    public void log(String msg, Level level) {
        if (level.equals(Level.WARNING) || level.equals(Level.SEVERE)) {
            System.err.println(msg);
        } else {
            System.out.println(msg);
        }
    }

    @Override
    public boolean performTasks(TaskMode mode, Task.Side side) {
        List<Task> tasks = getTasks(mode);
        int i = 0;
        for (Task task : tasks) {
            int ind = i;
            task.setListener(new TaskListener() {
                final int index = ind;

                @Override
                public void onStateChange(Task.State state) {

                }

                @Override
                public void onLog(TaskLog log) {
                    log(log.getMessage(), log.getLevel());
                }

                @Override
                public void onProgress(TaskProgress progress) {
                    setProgress(index, progress.message(), progress.progress());
                }

                @Override
                public boolean onUpdateDialogue(String changelog, String version) {
                    return false;
                }

                @Override
                public String onPromptInput(String title, String msg) {
                    return null;
                }

                @Override
                public void onEvent(MCPPlugin.TaskEvent event) {

                }
            });
            i++;
        }
        return super.performTasks(mode, side);
    }
}

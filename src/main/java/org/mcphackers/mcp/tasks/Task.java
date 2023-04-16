package org.mcphackers.mcp.tasks;

import org.mcphackers.mcp.MCP;
import org.mcphackers.mcp.tasks.helper.TaskListener;
import org.mcphackers.mcp.tasks.helper.TaskLog;
import org.mcphackers.mcp.tasks.helper.TaskProgress;
import org.mcphackers.mcp.tasks.helper.TaskProgressProducer;
import org.mcphackers.mcp.plugin.MCPPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Level;

public abstract class Task implements TaskProgressProducer, Runnable {
    public static final byte INFO = 0;          // TODO: replace
    public static final byte WARNING = 1;       // TODO: replace
    public static final byte ERROR = 2;         // TODO: replace

    protected final MCP mcp;
    protected final Side side;
    private final UUID id = UUID.randomUUID();
    private State state = State.NOT_STARTED;
    private Optional<TaskListener> listener;
    private final List<TaskLog> logs = new ArrayList<>();
    private final TaskProgress progress = new TaskProgress();
    public Task(MCP mcp, Side side) {
        this.mcp = mcp;
        this.side = side;
    }
    public Task(MCP mcp) {
        this(mcp, Side.ANY);
    }

    public Task setListener(TaskListener listener) {
        this.listener = Optional.of(listener);
        listener.onStateChange(state); // update this listeners state
        return this;
    }

    protected boolean updateDialogue(String changelog, String version) {
        return listener.map(l -> l.onUpdateDialogue(changelog, version)).orElse(false);
    }

    protected String promptInput(String title, String message) {
        return listener.map(l -> l.onPromptInput(title, message)).orElse(null);
    }

    protected void updateProgress(TaskProgress progress) {
        listener.ifPresent(l -> l.onProgress(progress));
    }

    public void updateProgress(String message) {
        progress.message(message);
        updateProgress(progress);
    }

    public void updateProgress(int progress) {
        this.progress.progress(progress);
        updateProgress(this.progress);
    }

    protected int getProgress() {
        return progress.progress();
    }

    protected String getProgressMessage() {
        return progress.message();
    }

    public void updateProgress(String message, int progress) {
        this.progress.message(message);
        this.progress.progress(progress);
        updateProgress(this.progress);
    }

    protected void callEvent(MCPPlugin.TaskEvent event) {
        listener.ifPresent(l -> l.onEvent(event));
    }

    protected void log(String message, Level level) {
        TaskLog log = new TaskLog(message, level);
        logs.add(log);
        listener.ifPresent(l -> l.onLog(log));
    }

    protected final String getLocalizedStage(String stage, Object... formatting) {
        return MCP.TRANSLATOR.translateKeyWithFormatting("task.stage." + stage, formatting);
    }

    @Override
    public void run() {
        if (state != State.NOT_STARTED)
            throw new IllegalStateException("Task is not in NOT_STARTED state"); // TODO: localize
        setState(State.RUNNING);
        try {
            this.doTask();
        } catch (Exception e) {
            setState(State.FAILED);
            log(e.getMessage(), Level.SEVERE);
        }
    }

    protected abstract void doTask() throws Exception;

    public Side getSide() {
        return side;
    }

    public State getState() {
        return state;
    }

    protected void setState(State state) {
        this.state = state;
        listener.ifPresent(l -> l.onStateChange(state));
    }

    public UUID getId() {
        return id;
    }

    public enum Side {
        ANY(-1, "any"),
        CLIENT(0, "client"),
        SERVER(1, "server"),
        MERGED(2, "merged");

        public static final Side[] ALL = {CLIENT, SERVER, MERGED};
        public static final Side[] VALUES = Side.values();

        public final int index;
        public final String name;

        Side(int index, String name) {
            this.index = index;
            this.name = name;
        }

        public String getName() {
            return MCP.TRANSLATOR.translateKey("side." + name);
        }
    }

    public enum State {
        NOT_STARTED,
        RUNNING,
        COMPLETED,
        FAILED
    }
}

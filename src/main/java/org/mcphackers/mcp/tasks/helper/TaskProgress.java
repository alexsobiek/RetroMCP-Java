package org.mcphackers.mcp.tasks.helper;

public class TaskProgress {
    private String message;
    private int progress;

    public TaskProgress(String message, int progress) {
        this.message = message;
        this.progress = progress;
    }

    public TaskProgress(String message) {
        this(message, -1);
    }

    public TaskProgress(int progress) {
        this(null, progress);
    }

    public TaskProgress() {
        this(null, -1);
    }

    public String message() {
        return message;
    }

    public void message(String message) {
        this.message = message;
    }

    public int progress() {
        return progress;
    }

    public void progress(int progress) {
        this.progress = progress;
    }
}

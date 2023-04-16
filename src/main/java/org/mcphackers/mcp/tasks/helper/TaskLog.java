package org.mcphackers.mcp.tasks.helper;

import java.util.logging.Level;

public class TaskLog {
    private final String message;
    private final Level level;

    public TaskLog(String message, Level level) {
        this.message = message;
        this.level = level;
    }

    public String getMessage() {
        return message;
    }

    public Level getLevel() {
        return level;
    }
}

package org.mcphackers.mcp.tasks.helper;

public interface TaskProgressProducer {
    void updateProgress(String progressMessage);

    void updateProgress(int progress);

    default void updateProgress(String progressMessage, int progress) {
        updateProgress(progressMessage);
        updateProgress(progress);
    }
}

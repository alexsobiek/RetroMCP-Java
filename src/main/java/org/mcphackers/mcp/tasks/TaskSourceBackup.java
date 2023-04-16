package org.mcphackers.mcp.tasks;

import org.mcphackers.mcp.MCP;
import org.mcphackers.mcp.MCPPaths;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.mcphackers.mcp.MCPPaths.SOURCE;

public class TaskSourceBackup extends Task {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.BASIC_ISO_DATE;

    public TaskSourceBackup(MCP mcp, Side side) {
        super(mcp, side);
    }

    @Override
    protected void doTask() throws Exception {
        packSourceIntoZip();
    }

    private void packSourceIntoZip() throws IOException {
        String filename = "src-backup-" + side.name + "-" + DATE_FORMATTER.format(LocalDate.now());
        Path backupPath = MCPPaths.get(mcp, filename + ".zip");
        for (int i = 1; Files.exists(backupPath); i++) {
            backupPath = MCPPaths.get(mcp, filename + "-" + i + ".zip");
        }

        Path srcPath = MCPPaths.get(mcp, SOURCE, side);
        try (ZipOutputStream zip = new ZipOutputStream(Files.newOutputStream(backupPath))) {
            List<String> srcFiles = new ArrayList<>();
            try (Stream<Path> paths = Files.walk(srcPath)) {
                paths.forEach(file -> {
                    if (file.getFileName().toString().endsWith(".java")) {
                        srcFiles.add(file.toString());
                    }
                });
            }
            long nFiles = srcFiles.size();

            int i = 0;
            for (String path : srcFiles) {
                if (path.endsWith(".java")) {
                    try {
                        zip.putNextEntry(new ZipEntry(path));
                        zip.write(Files.readAllBytes(Paths.get(path)));
                        zip.closeEntry();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                updateProgress(MCP.TRANSLATOR.translateKey("task.stage.backupsrc") + " " + path, (int) ((i / (float) nFiles) * 100));
                i++;
            }
        }
    }
}

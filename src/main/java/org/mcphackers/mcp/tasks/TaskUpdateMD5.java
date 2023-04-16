package org.mcphackers.mcp.tasks;

import org.mcphackers.mcp.MCP;
import org.mcphackers.mcp.MCPPaths;
import org.mcphackers.mcp.tools.Util;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.stream.Stream;

import static org.mcphackers.mcp.MCPPaths.*;

public class TaskUpdateMD5 extends TaskStaged {
    public TaskUpdateMD5(MCP mcp, Side side) {
        super(mcp, side);
    }

    @Override
    protected Stage[] setStages() {
        return new Stage[]{
                new Stage(new TaskRecompile(this.mcp, this.side), getLocalizedStage("recompile")),
                new Stage(new Update(this.mcp, this.side, false), getLocalizedStage("updatemd5"), 50)
        };
    }

    static class Update extends Task {
        private final boolean reobf;

        public Update(MCP mcp, Side side, boolean reobf) {
            super(mcp, side);
            this.reobf = reobf;
        }

        @Override
        protected void doTask() throws Exception {
            final Path binPath 	= MCPPaths.get(mcp, BIN, side);
            final Path md5 = MCPPaths.get(mcp, reobf ? MD5_RO : MD5, side);

            if (!Files.exists(binPath)) throw new IOException(side.name + " classes not found!");

            try(BufferedWriter writer = Files.newBufferedWriter(md5)) {
                final int[] progress = {0};
                int total;
                try(Stream<Path> pathStream = Files.walk(binPath)) {
                    total = (int) pathStream.parallel()
                            .filter(p -> !p.toFile().isDirectory())
                            .count();
                }
                Files.walkFileTree(binPath, new SimpleFileVisitor<Path>() {
                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                        String md5_hash = Util.getMD5(file);
                        String fileName = binPath.relativize(file).toString().replace("\\", "/").replace(".class", "");
                        writer.append(fileName).append(" ").append(md5_hash).append("\n");
                        progress[0]++;
                        updateProgress(50 + (int)((double) progress[0] /(double)total * 50));
                        return FileVisitResult.CONTINUE;
                    }
                });
            }
        }
    }
}

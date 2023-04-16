package org.mcphackers.mcp.tasks;

import codechicken.diffpatch.PatchOperation;
import codechicken.diffpatch.util.PatchMode;
import org.mcphackers.mcp.MCP;
import org.mcphackers.mcp.MCPPaths;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Path;
import java.util.logging.Level;

import static org.mcphackers.mcp.MCPPaths.PATCH;
import static org.mcphackers.mcp.MCPPaths.SOURCE;

public class TaskApplyPatch extends Task {
    public TaskApplyPatch(MCP mcp, Side side) {
        super(mcp, side);
    }

    @Override
    protected void doTask() throws Exception {
        final Path patchesPath 	= MCPPaths.get(mcp, PATCH, getSide());
        final Path srcPath 		= MCPPaths.get(mcp, SOURCE, getSide());
        patch(this, srcPath, srcPath, patchesPath);
    }

    protected static void patch(Task task, Path base, Path out, Path patches) throws IOException {
        ByteArrayOutputStream logger = new ByteArrayOutputStream();
        PatchOperation patchOperation = PatchOperation.builder()
                .basePath(base)
                .patchesPath(patches)
                .outputPath(out)
                .mode(PatchMode.OFFSET)
                .build();
        boolean success = patchOperation.doPatch();
        patchOperation.getSummary().print(new PrintStream(logger), false);
        if (!success) {
            task.log(logger.toString(), Level.INFO);
            task.log("Patching failed!", Level.SEVERE);
        }
    }
}

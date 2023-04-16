package org.mcphackers.mcp.tasks;

import org.json.JSONObject;
import org.mcphackers.mcp.MCP;
import org.mcphackers.mcp.MCPPaths;
import org.mcphackers.mcp.main.MainInterface;
import org.mcphackers.mcp.tasks.mode.TaskMode;
import org.mcphackers.mcp.tools.FileUtil;
import org.mcphackers.mcp.tools.Util;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Level;

public class TaskDownloadUpdate extends Task {
    private static final String API = "https://api.github.com/repos/MCPHackers/RetroMCP-Java/releases/latest";

    public TaskDownloadUpdate(MCP mcp, Side side) {
        super(mcp, side);
    }

    @Override
    protected void doTask() throws Exception {
        if (mcp instanceof MainInterface) {
            MainInterface mcpi = (MainInterface) mcp;
            URL updateURL = new URL(API);
            InputStream in = updateURL.openStream();
            JSONObject releaseJson = Util.parseJSON(in);
            String latestVersion = releaseJson.getString("tag_name");
            String notes = releaseJson.getString("body");
            if(!latestVersion.equals(MCP.VERSION)) {
                boolean confirmed = updateDialogue(notes, latestVersion);
                if(confirmed) {
                    log("Downloading update...", Level.INFO);
                    for(Object obj : releaseJson.getJSONArray("assets")) {
                        if(obj instanceof JSONObject) {
                            JSONObject assetObj = (JSONObject)obj;
                            if(!assetObj.getString("name").endsWith(mcpi.isGUI() ? "-GUI.jar" : "-CLI.jar")) {
                                continue;
                            }
                            FileUtil.downloadFile(assetObj.getString("browser_download_url"), Paths.get(MCPPaths.UPDATE_JAR));
                            break;
                        }
                    }
                    Path jarPath = Paths.get(MCP.class
                            .getProtectionDomain()
                            .getCodeSource()
                            .getLocation()
                            .toURI());
                    if(!Files.isDirectory(jarPath)) {
                        String[] cmd = new String[] {
                                Util.getJava(),
                                "-cp",
                                MCPPaths.UPDATE_JAR,
                                "org.mcphackers.mcp.Update",
                                jarPath.toString()
                        };
                        Util.runCommand(cmd);
                        System.exit(0);
                    }
                    else {
                        throw new IOException("Running from a folder! Aborting");
                    }
                }
                else {
                    log("Cancelling update!", Level.WARNING);
                }
            } else mcpi.showMessage(TaskMode.UPDATE_MCP.getFullName(), MCP.TRANSLATOR.translateKey("mcp.upToDate"), org.mcphackers.mcp.tasks.Task.INFO);
        } else throw new UnsupportedOperationException("Cannot update non CLI or GUI RetroMCP");
    }
}

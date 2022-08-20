package org.mcphackers.mcp;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;

import org.mcphackers.mcp.tasks.Task.Side;

public class MCPPaths {
	
	//Directories
	public static final String JARS = 		"jars/";
	public static final String LIB = 		"lib/";
	public static final String TEMP = 		"temp/";
	public static final String SRC = 		"src/";
	public static final String BIN = 		"bin/";
	public static final String REOBF = 		"reobf/";
	public static final String CONF = 		"conf/";
	public static final String BUILD = 		"build/";
	public static final String WORKSPACE =  "workspace/";
	
	//Files and subdirectories
	public static final String JAR_ORIGINAL = 		 JARS + "minecraft_%s.jar";

	public static final String NATIVES = 			 LIB + "natives";

	public static final String TEMP_SIDE = 	 		 TEMP + "%s";
	public static final String TEMP_SRC = 	 		 TEMP + "%s/src";
	public static final String REMAPPED = 	 		 TEMP + "%s/remapped.jar";
	public static final String MD5 = 		 		 TEMP + "%s/original.md5";
	public static final String MD5_RO = 		 	 TEMP + "%s/modified.md5";
	public static final String REOBF_JAR = 	 		 TEMP + "%s/reobf.jar";

	public static final String MAPPINGS = 			 CONF + "mappings.tiny";
	public static final String EXC = 		 		 CONF + "exceptions.exc";
	@Deprecated
	public static final String JAVADOCS = 		 	 CONF + "javadocs.txt";
	public static final String PATCHES = 	 		 CONF + "patches_%s";
	public static final String VERSION = 	 		 CONF + "version.json";

	public static final String SOURCE = 	 		 SRC + "minecraft_%s";
	public static final String COMPILED = 		 	 BIN + "minecraft_%s";

	public static final String REOBF_SIDE = 		 REOBF + "minecraft_%s";
	public static final String BUILD_ZIP = 	 		 BUILD + "minecraft_%s.zip";
	public static final String BUILD_JAR = 	 		 BUILD + "minecraft_%s.jar";

	public static final String UPDATE_JAR =		 	 "update.jar";
	
	private static final Set<String> stripClient = new HashSet<String>() {
		private static final long serialVersionUID = 1079122339538512318L;
		{
			add(JAR_ORIGINAL);
			add(SOURCE);
			add(COMPILED);
			add(REOBF_SIDE);
			add(BUILD_ZIP);
			add(BUILD_JAR);
		}
	};
	
	public static Path get(MCP mcp, String path, Side side) {
		if(side == Side.CLIENT && stripClient.contains(path)) {
			return get(mcp, path.replace("_%s", ""));
		}
		return get(mcp, String.format(path, side.name));
	}

	public static Path get(MCP mcp, String path) {
		return mcp.getWorkingDir().resolve(Paths.get(path));
	}
}

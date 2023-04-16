package org.mcphackers.mcp.tasks;

import org.mcphackers.mcp.MCP;
import org.mcphackers.mcp.MCPPaths;
import org.mcphackers.mcp.tasks.mode.TaskParameter;
import org.mcphackers.mcp.tools.ClassUtils;
import org.mcphackers.mcp.tools.FileUtil;
import org.mcphackers.mcp.tools.fernflower.Decompiler;
import org.mcphackers.mcp.tools.injector.GLConstants;
import org.mcphackers.mcp.tools.mappings.MappingUtil;
import org.mcphackers.mcp.tools.project.EclipseProjectWriter;
import org.mcphackers.mcp.tools.project.IdeaProjectWriter;
import org.mcphackers.rdi.injector.data.ClassStorage;
import org.mcphackers.rdi.injector.data.Mappings;
import org.mcphackers.rdi.injector.transform.Transform;
import org.mcphackers.rdi.nio.IOUtil;
import org.mcphackers.rdi.nio.MappingsIO;
import org.mcphackers.rdi.nio.RDInjector;
import org.objectweb.asm.tree.ClassNode;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.mcphackers.mcp.MCPPaths.*;
import static org.mcphackers.mcp.MCPPaths.PATCHES;

public class TaskDecompile extends TaskStaged {
    private int classVersion = -1;

    public TaskDecompile(MCP mcp, Side side) {
        super(mcp, side);
    }

    @Override
    protected Stage[] setStages() {
        final Path rdiOut = MCPPaths.get(mcp, REMAPPED, side);
        final Path ffOut = MCPPaths.get(mcp, SOURCE_UNPATCHED, side);
        final Path srcPath = MCPPaths.get(mcp, SOURCE, side);
        final Path patchesPath = MCPPaths.get(mcp, PATCHES, side);

        final boolean guessGenerics = mcp.getOptions().getBooleanParameter(TaskParameter.GUESS_GENERICS);

        return new Stage[]{
                new Stage(task(() -> {
                    ClassStorage storage = applyInjector();
                    for(ClassNode node : storage) classVersion = Math.max(classVersion, node.version);
                    return null;
                }), getLocalizedStage("prepare")),
                new Stage(task(() -> {
                    new Decompiler(this, rdiOut, ffOut, mcp.getLibraries(), mcp.getOptions().getStringParameter(TaskParameter.INDENTATION_STRING), guessGenerics).decompile();
                    new EclipseProjectWriter().createProject(mcp, side, ClassUtils.getSourceFromClassVersion(classVersion));
                    new IdeaProjectWriter().createProject(mcp, side, ClassUtils.getSourceFromClassVersion(classVersion));
                    return null;
                }), getLocalizedStage("decompile")),
                new Stage(task(() -> {
                    if(mcp.getOptions().getBooleanParameter(TaskParameter.PATCHES) && Files.exists(patchesPath))
                        TaskApplyPatch.patch(this, ffOut, ffOut, patchesPath);
                    return null;
                }), getLocalizedStage("patch")),
                new Stage(task(() -> {
                    if(!mcp.getOptions().getBooleanParameter(TaskParameter.DECOMPILE_RESOURCES)) {
                        for(Path p : FileUtil.walkDirectory(ffOut, p -> !Files.isDirectory(p) && !p.getFileName().toString().endsWith(".java"))) {
                            Files.delete(p);
                        }
                    }
                    Files.createDirectories(srcPath);
                    FileUtil.compress(ffOut, MCPPaths.get(mcp, SOURCE_JAR, side));
                    if(mcp.getOptions().getBooleanParameter(TaskParameter.OUTPUT_SRC)) {
                        FileUtil.deletePackages(ffOut, mcp.getOptions().getStringArrayParameter(TaskParameter.IGNORED_PACKAGES));
                        FileUtil.copyDirectory(ffOut, srcPath);
                    }
                    Files.createDirectories(MCPPaths.get(mcp, GAMEDIR, side));
                    return null;
                }), getLocalizedStage("copysrc")),
                new Stage(new TaskUpdateMD5(mcp, side), getLocalizedStage("recompile")),
        };
    }

    public static Mappings getMappings(Path mappingsPath, ClassStorage storage, Task.Side side) throws IOException {
        if(!Files.exists(mappingsPath)) {
            return null;
        }
        boolean joined = MappingUtil.readNamespaces(mappingsPath).contains("official");
        Mappings mappings = MappingsIO.read(mappingsPath, joined ? "official" : side.name, "named");
        for(String name : storage.getAllClasses()) {
            if(name.indexOf('/') == -1 && !mappings.classes.containsKey(name)) {
                mappings.classes.put(name, "net/minecraft/src/" + name);
            }
        }
        return mappings;
    }

    public ClassStorage applyInjector() throws IOException {
        final Path rdiOut = MCPPaths.get(mcp, REMAPPED, side);
        final Path mappingsPath = MCPPaths.get(mcp, MAPPINGS);
        final boolean guessGenerics = mcp.getOptions().getBooleanParameter(TaskParameter.GUESS_GENERICS);
        final boolean stripGenerics = mcp.getOptions().getBooleanParameter(TaskParameter.STRIP_GENERICS);
        final boolean hasLWJGL = side == Task.Side.CLIENT || side == Task.Side.MERGED;

        RDInjector injector = new RDInjector();
        Path path;
        Mappings mappings;

        if(side == Task.Side.MERGED) {
            path = MCPPaths.get(mcp, JAR_ORIGINAL, Task.Side.SERVER);
            injector.setStorage(new ClassStorage(IOUtil.readJar(path)));
            injector.addResources(path);
            if(stripGenerics) {
                injector.stripLVT();
                injector.addTransform(Transform::stripSignatures);
            }
            mappings = getMappings(mappingsPath, injector.getStorage(), Task.Side.SERVER);
            if(mappings != null) {
                injector.applyMappings(mappings);
            }
            injector.transform();
            ClassStorage serverStorage = injector.getStorage();

            path = MCPPaths.get(mcp, JAR_ORIGINAL, Task.Side.CLIENT);
            injector.setStorage(new ClassStorage(IOUtil.readJar(path)));
            injector.addResources(path);
            if(stripGenerics) {
                injector.stripLVT();
                injector.addTransform(Transform::stripSignatures);
            }
            mappings = getMappings(mappingsPath, injector.getStorage(), Task.Side.CLIENT);
            if(mappings != null) {
                injector.applyMappings(mappings);
            }
            injector.mergeWith(serverStorage);
        } else {
            path = MCPPaths.get(mcp, JAR_ORIGINAL, side);
            injector.setStorage(new ClassStorage(IOUtil.readJar(path)));
            injector.addResources(path);
            if(stripGenerics) {
                injector.stripLVT();
                injector.addTransform(Transform::stripSignatures);
            }
            mappings = getMappings(mappingsPath, injector.getStorage(), side);
            if(mappings != null) {
                injector.applyMappings(mappings);
            }
        }
        injector.addTransform(Transform::decomposeVars);
        injector.addTransform(Transform::replaceCommonConstants);
        if(hasLWJGL)
            injector.addVisitor(new GLConstants(null));
        injector.restoreSourceFile();
        injector.fixInnerClasses();
        injector.fixImplicitConstructors();
        if(guessGenerics)
            injector.guessGenerics();
        final Path exc = MCPPaths.get(mcp, EXC);
        if(Files.exists(exc)) {
            injector.fixExceptions(exc);
        }
        if(side == Task.Side.MERGED) {
            Path acc = MCPPaths.get(mcp, MCPPaths.ACCESS, Task.Side.CLIENT);
            if(Files.exists(acc)) {
                injector.fixAccess(acc);
            }
            acc = MCPPaths.get(mcp, MCPPaths.ACCESS, Task.Side.SERVER);
            if(Files.exists(acc)) {
                injector.fixAccess(acc);
            }
        } else {
            final Path acc = MCPPaths.get(mcp, MCPPaths.ACCESS, side);
            if(Files.exists(acc)) {
                injector.fixAccess(acc);
            }
        }
        injector.transform();
        injector.write(rdiOut);
        return injector.getStorage();
    }
}

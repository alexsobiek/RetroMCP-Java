package org.mcphackers.mcp;

import org.mcphackers.mcp.main.MainGUI;
import org.mcphackers.mcp.plugin.MCPPlugin;
import org.mcphackers.mcp.plugin.MCPPlugin.MCPEvent;
import org.mcphackers.mcp.plugin.MCPPlugin.TaskEvent;
import org.mcphackers.mcp.tasks.Task;
import org.mcphackers.mcp.tasks.TaskExecutor;
import org.mcphackers.mcp.tasks.TaskStaged;
import org.mcphackers.mcp.tasks.mode.TaskMode;
import org.mcphackers.mcp.tasks.mode.TaskParameter;
import org.mcphackers.mcp.tools.ClassUtils;
import org.mcphackers.mcp.tools.FileUtil;
import org.mcphackers.mcp.tools.versions.DownloadData;
import org.mcphackers.mcp.tools.versions.json.Version;

import javax.swing.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.logging.Level;

public abstract class MCP {
    public static final String VERSION = "v1.0.1";
    public static final String githubURL = "https://github.com/MCPHackers/RetroMCP-Java";
    public static final TranslatorUtil TRANSLATOR = new TranslatorUtil();
    private static final Map<String, MCPPlugin> plugins = new HashMap<>();
    public static Theme THEME = Theme.THEMES_MAP.get(UIManager.getCrossPlatformLookAndFeelClassName());

    static {
        loadPlugins();
    }

    protected MCP() {
        Update.attemptToDeleteUpdateJar();
        changeLanguage(Language.get(Locale.getDefault()));
        triggerEvent(MCPEvent.ENV_STARTUP);
        System.gc();
    }

    private static void loadPlugins() {
        Path pluginsDir = Paths.get("plugins");
        if (Files.exists(pluginsDir)) {
            List<Path> jars = new ArrayList<>();
            try {
                FileUtil.collectJars(pluginsDir, jars);
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                for (Path p : jars) {
                    List<Class<MCPPlugin>> classes = ClassUtils.getClasses(p, MCPPlugin.class);
                    for (Class<MCPPlugin> cls : classes) {
                        if (!ClassUtils.isClassAbstract(cls)) {
                            MCPPlugin plugin = cls.newInstance();
                            plugin.init();
                            plugins.put(plugin.pluginId() + plugin.hashCode(), plugin);
                        } else {
                            System.err.println(TRANSLATOR.translateKey("mcp.incompatiblePlugin") + cls.getName());
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * @return The working directory
     */
    public abstract Path getWorkingDir();

//	public boolean postTasks(TaskMode mode, Task.Side side) {
//		return postTasks(getTasks(mode), side);
//	}
//
//
//
//
//	/**
//	 * Creates instances of TaskMode and executes them
//	 * @param mode task to execute
//	 * @param side side to execute
//	 * @return <code>true</code> if task was successfully executed
//	 */
//	public final boolean performTask(TaskMode mode, Task.Side side) {
//		return performTask(mode, side, true);
//	}
//
//	/**
//	 * Creates instances of TaskMode and executes them
//	 * @param mode task to execute
//	 * @param side side to execute
//	 * @param completionMsg display completion message when finished
//	 * @return <code>true</code> if task was successfully executed
//	 */
//	public final boolean performTask(TaskMode mode, Task.Side side, boolean completionMsg) {
//		List<Task> tasks = mode.getTasks(this);
//		if(tasks.size() == 0) {
//			System.err.println("Performing 0 tasks");
//			return false;
//		}
//
//		boolean enableProgressBars = mode.usesProgressBars;
//
//		tasks.forEach(task -> {
//			task.setListener(new TaskListener() {
//				@Override
//				public void onStateChange(Task.State state) {
//					if(enableProgressBars) {
//						// do progress bar stuff
//					}
//				}
//
//				@Override
//				public void onLog(TaskLog log) {
//					log(log.getMessage(), log.getLevel());
//				}
//
//				@Override
//				public void onProgress(TaskProgress progress) {
//					if(enableProgressBars) {
//						// do progress bar stuff
//					}
//				}
//
//				@Override
//				public boolean onUpdateDialogue(String changelog, String version) {
//					return false;
//				}
//
//				@Override
//				public void onEvent(TaskEvent event) {
//
//				}
//			});
//		});
//
//		setActive(false);
//		triggerEvent(MCPEvent.STARTED_TASKS);
//
//		TaskExecutor executor = new TaskExecutor(2, tasks).side(side);
//
//		try {
//			executor.run();
//		} catch (Exception e) {
//			throw new RuntimeException(e);
//			return false;
//		}
//
//
//		ExecutorService pool = Executors.newFixedThreadPool(2);
//
//		AtomicInteger result1 = new AtomicInteger(Task.INFO);
//		AtomicReference<Throwable> e = new AtomicReference<>();
//
//		triggerEvent(MCPEvent.FINISHED_TASKS);
//
//		byte result = result1.byteValue();
//
//		List<String> msgs = new ArrayList<>();
//		for(Task task : performedTasks) {
//			msgs.addAll(task.getMessageList());
//			byte retresult = task.getResult();
//			if(retresult > result) {
//				result = retresult;
//			}
//		}
//		if(msgs.size() > 0) log("");
//		for(String msg : msgs) {
//			log(msg);
//		}
//		if(completionMsg) {
//			String[] msgs2 = {
//					TRANSLATOR.translateKey("tasks.success"),
//					TRANSLATOR.translateKey("tasks.warning"),
//					TRANSLATOR.translateKey("tasks.error")};
//			if(e.get() != null) {
//				showMessage(mode.getFullName(), msgs2[result], e.get());
//			} else {
//				showMessage(mode.getFullName(), msgs2[result], result);
//			}
//		}
//		setActive(true);
//		if(enableProgressBars) clearProgressBars();
//		System.gc();
//		return result != Task.ERROR;
//	}

	protected List<Task> getTasks(TaskMode mode) {
		return mode.getTasks(this);
	}

    protected boolean performTasks(List<Task> tasks, Task.Side side) {
        if (tasks.size() == 0) {
            System.err.println("Performing 0 tasks");
            return false;
        }

        TaskExecutor executor = new TaskExecutor(2, tasks).side(side);

        setActive(false);
        triggerEvent(MCPEvent.STARTED_TASKS);
        try {
            executor.run();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        triggerEvent(MCPEvent.FINISHED_TASKS);
        setActive(true);
        return tasks.stream().anyMatch(task -> task.getState() == Task.State.FAILED);
    }

    public boolean performTasks(TaskMode mode, Task.Side side) {
        return performTasks(getTasks(mode), side);
    }

    public Task.Side getSide() {
        return getOptions().side;
    }

    public void log(String msg) {
        log(msg, Level.INFO);
    }

    /**
     * Logs a message to console
     *
     * @param msg
     */
    public abstract void log(String msg, Level level);

    /**
     * @return Instance of options
     */
    public abstract Options getOptions();

    /**
     * @return Current version
     */
    public abstract Version getCurrentVersion();

    /**
     * Sets current version from parsed JSON data
     */
    public abstract void setCurrentVersion(Version version);

    /**
     * Marks MCP instance as busy on a task
     *
     * @param active
     */
    public abstract void setActive(boolean active);

    /**
     * Changes a parameter in current options and saves changes to disk
     *
     * @param param
     * @param value
     * @throws IllegalArgumentException
     */
    public void setParameter(TaskParameter param, Object value) throws IllegalArgumentException {
        getOptions().setParameter(param, value);
        getOptions().save();
    }

    /**
     * Changes a parameter in current options and saves changes to disk, shows an error message if fails
     *
     * @param param
     * @param value
     * @throws IllegalArgumentException
     */
    public boolean safeSetParameter(TaskParameter param, String value) {
        return getOptions().safeSetParameter(param, value);
    }

    /**
     * @param task
     * @see MCPPlugin#setTaskOverrides(TaskStaged)
     */
    public final void setPluginOverrides(TaskStaged task) {
        for (Map.Entry<String, MCPPlugin> entry : plugins.entrySet()) {
            entry.getValue().setTaskOverrides(task);
        }
    }

    /**
     * Triggers an MCPEvent for every plugin
     *
     * @param event
     */
    public final void triggerEvent(MCPEvent event) {
        for (Map.Entry<String, MCPPlugin> entry : plugins.entrySet()) {
            entry.getValue().onMCPEvent(event, this);
        }
    }

    /**
     * Triggers a TaskEvent for every plugin
     *
     * @param event
     */
    public final void triggerTaskEvent(TaskEvent event, Task task) {
        for (Map.Entry<String, MCPPlugin> entry : plugins.entrySet()) {
            entry.getValue().onTaskEvent(event, task);
        }
    }

    /**
     * Notifies language change
     *
     * @param lang
     */
    public final void changeLanguage(Language lang) {
        TRANSLATOR.changeLang(lang);
        for (Map.Entry<String, MCPPlugin> entry : plugins.entrySet()) {
            TRANSLATOR.readTranslation(entry.getValue().getClass());
        }
    }

    public final void changeTheme(Theme theme) {
        try {
            UIManager.setLookAndFeel(theme.themeClass);
            // If you dare call this on CLI, I will steal your kneecaps
            JFrame frame = ((MainGUI) this).frame;
            if (frame != null) {
                SwingUtilities.updateComponentTreeUI(frame);
            }
            THEME = theme;
        } catch (UnsupportedLookAndFeelException | ClassNotFoundException | InstantiationException |
                 IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public List<Path> getLibraries() {
        return DownloadData.getLibraries(MCPPaths.get(this, MCPPaths.LIB), getCurrentVersion());
    }

    public List<Path> getNatives() {
        return DownloadData.getNatives(MCPPaths.get(this, MCPPaths.LIB), getCurrentVersion());
    }
}

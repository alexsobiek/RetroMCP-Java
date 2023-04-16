package org.mcphackers.mcp.tools.fernflower;

import org.mcphackers.mcp.MCP;
import org.mcphackers.mcp.tasks.helper.TaskProgressProducer;

import de.fernflower.main.extern.IFernflowerLogger;

public class DecompileLogger extends IFernflowerLogger {

	private final TaskProgressProducer listener;
	private int total;

	public DecompileLogger(TaskProgressProducer listener) {
		this.listener = listener;
	}

	@Override
	public void writeMessage(String message, Severity severity) {
		if(severity.ordinal() >= Severity.WARN.ordinal()) {
//			System.out.println(message);
		}
	}

	@Override
	public void writeMessage(String message, Throwable t) {
//		System.out.println(message);
//		t.printStackTrace();
	}

	@Override
	public void startReadingClass(String className) {
		listener.updateProgress(MCP.TRANSLATOR.translateKey("task.stage.decompile") + " " + className);
	}

	@Override
	public void startSave(int total) {
		this.total = total;
	}

	@Override
	public void updateSave(int current) {
		listener.updateProgress((int)((double)current/(double)total * 100));
	}

}

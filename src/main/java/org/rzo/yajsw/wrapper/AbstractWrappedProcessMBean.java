package org.rzo.yajsw.wrapper;

import java.util.Date;

public interface AbstractWrappedProcessMBean
{
	/**
	 * Start.
	 */
	public void start();

	/**
	 * Stop.
	 */
	public void stop();

	/**
	 * Restart.
	 */
	public void restart();

	/**
	 * Gets the pid.
	 * 
	 * @return the pid
	 */
	public int getAppPid();

	/**
	 * Gets the exit code.
	 * 
	 * @return the exit code
	 */
	public int getExitCode();

	public String getStringState();

	public void threadDump();

	public void wrapperThreadDump();

	public String getType();

	public String getName();

	public void waitFor();

	public void stopTimerCondition();

	public boolean isTimerActive();

	public boolean isConditionActive();

	public int getTotalRestartCount();

	public int getRestartCount();

	public Date getAppStarted();

	public Date getAppStopped();

	public int getWrapperPid();

	public Date getWrapperStarted();

	public int getAppCpu();

	public int getAppHandles();

	public long getAppMemory();

	public int getAppThreads();

	public void startDrain();

	public String readDrainLine();

	public void stopDrain();

	public int getState();

	public String[][] getTrayIconMessages();

	public void stopWrapper();

	public boolean hasOutput();

	public void writeOutput(String txt);

	public void setInquireResponse(String s);

	public String getInquireMessage();

	public void init();

	public void setProperty(String key, String value);

	public void resetCache();

}

package org.rzo.yajsw.wrapper;

import java.util.ArrayList;

public class WrappedProcessList extends ArrayList<WrappedProcess>
{
	public void startAll()
	{
		for (WrappedProcess p : this)
		{
			p.start();
		}
	}

	public void stopAll()
	{
		for (WrappedProcess p : this)
		{
			p.stop();
		}
	}

	public void initAll()
	{
		for (WrappedProcess p : this)
		{
			p.init();
		}
	}

	public void restartAll()
	{
		for (WrappedProcess p : this)
		{
			p.restart();
		}
	}

	public void removeStateChangeListener(int state)
	{
		for (WrappedProcess p : this)
		{
			p.removeStateChangeListener(state);
		}
	}

	public void shutdown()
	{
		for (WrappedProcess p : this)
		{
			p.shutdown();
		}
	}

}

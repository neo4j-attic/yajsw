package org.rzo.yajsw.action;

import java.io.IOException;
import java.io.PrintStream;
import java.util.Iterator;
import java.util.Map;

import org.jboss.netty.channel.Channel;
import org.rzo.yajsw.controller.Message;

public class ThreadDumpImpl5 implements Action
{
	public void execute(Message msg, Channel session, PrintStream out) throws IOException
	{
		Map allThreads = Thread.getAllStackTraces();
		Iterator iterator = allThreads.keySet().iterator();
		StringBuffer stringBuffer = new StringBuffer();
		while (iterator.hasNext())
		{
			Thread key = (Thread) iterator.next();
			StackTraceElement[] trace = (StackTraceElement[]) allThreads.get(key);
			stringBuffer.append(key + "\r\n");
			for (int i = 0; i < trace.length; i++)
			{
				stringBuffer.append("  " + trace[i] + "\r\n");
			}
			stringBuffer.append("\r\n");
		}
		out.println(stringBuffer.toString());
		out.flush();
	}

	public static void main(String[] args) throws IOException
	{
		Action a = (Action) new ThreadDumpImpl5();
		a.execute(null, null, System.out);
	}

}

package org.rzo.netty.mcast.discovery;

import static org.jboss.netty.channel.Channels.pipeline;

import java.net.InetAddress;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.rzo.netty.ahessian.Constants;
import org.rzo.netty.mcast.MulticastEndpoint;

public class DiscoveryClient extends MulticastEndpoint
{

private String	name;
private Set<String>	hosts = Collections.synchronizedSet(new HashSet<String>());
private volatile boolean stop = false;
private Set<DiscoveryListener>	listeners = Collections.synchronizedSet(new HashSet<DiscoveryListener>());

private static Executor executor = Executors.newCachedThreadPool();


public void init() throws Exception
{
	ChannelPipelineFactory factory = new ChannelPipelineFactory()
	{
		public ChannelPipeline getPipeline() throws Exception
		{
			ChannelPipeline pipeline = pipeline();
			pipeline.addLast("discoveryServer", new SimpleChannelUpstreamHandler()
			{

				@Override
				public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception
				{
					try
					{
					String response = getStringMessage(e);
					if (response == null)
						return;
					String[] resp = response.split(":");
					if (resp.length == 2)
					{
						String host = resp[0];
						InetAddress.getByName(host);
						int port = Integer.parseInt(resp[1]);
						if (!hosts.contains(response))
						{
						hosts.add(response);
						for (DiscoveryListener listener : listeners)
						{
							listener.newHost(name, response);
						}
						}
						
					}
					}
					catch (Exception ex)
					{
						Constants.ahessianLogger.warn("", ex);
					}
				}
			});
			return pipeline;
		}

	};
	super.init(factory);
	
}

public void start() throws Exception
{
	stop = false;
	discoverServices();
}



private void discoverServices() throws Exception
{
	executor.execute(new Runnable()
	{
		public void run()
		{
			while (!stop)
			{
				try
				{
					send(ChannelBuffers.wrappedBuffer((name).getBytes()));
				}
				catch (Exception e)
				{
					Constants.ahessianLogger.warn("", e);
				}
				try
				{
					Thread.sleep(1000);
				}
				catch (InterruptedException e)
				{
					Constants.ahessianLogger.warn("", e);
				}
			}
		}
	});
}



public String getName()
{
	return name;
}


public void setName(String name)
{
	this.name = name;
}

public void stop()
{
	stop = true;
}

public void addListener(DiscoveryListener listener)
{
	listeners.add(listener);
}

public void removeHost(String host)
{
	hosts.remove(host);
}

}


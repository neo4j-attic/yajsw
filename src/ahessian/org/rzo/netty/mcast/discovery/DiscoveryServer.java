package org.rzo.netty.mcast.discovery;

import static org.jboss.netty.channel.Channels.pipeline;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;

import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.rzo.netty.ahessian.Constants;
import org.rzo.netty.mcast.MulticastEndpoint;

public class DiscoveryServer extends MulticastEndpoint
{
	private String	name;
	private String	host;
	private int		port;

	public void init() throws Exception
	{
		if (host == null)
			host = whatIsMyIp();

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
						String request = getStringMessage(e);
						if (request == null)
							return;
						if (name != null && name.equals(request) && host != null && port > 0)
						{
							send(ChannelBuffers.wrappedBuffer((host + ":" + port).getBytes()));
						}
					}
				});
				return pipeline;
			}

		};
		super.init(factory);
	}

	public String getName()
	{
		return name;
	}

	public String getHost()
	{
		return host;
	}

	public int getPort()
	{
		return port;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public void setHost(String host)
	{
		this.host = host;
	}

	public void setPort(int port)
	{
		this.port = port;
	}
	
	private String whatIsMyIp()
	{
		String result = null;
        try
        {
          Enumeration<NetworkInterface> e = NetworkInterface.getNetworkInterfaces();

          while (e.hasMoreElements())
          {
            NetworkInterface ne = (NetworkInterface) e.nextElement();
            Enumeration<InetAddress> e2 = ne.getInetAddresses();

            while (e2.hasMoreElements())
            {
              InetAddress ia = (InetAddress) e2.nextElement();

              if (!ia.isAnyLocalAddress() && !ia.isLinkLocalAddress()
                  && !ia.isLoopbackAddress() && !ia.isMulticastAddress())
              if (result == null || !ia.isSiteLocalAddress())
              {
                result = ia.getHostAddress();
              }
            }
          }
        } catch (Exception ex)
        {
			Constants.ahessianLogger.warn("", ex);
        }
        return result;

	}


}

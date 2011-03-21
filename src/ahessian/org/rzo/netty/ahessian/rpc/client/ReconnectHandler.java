package org.rzo.netty.ahessian.rpc.client;

import java.net.ConnectException;
import java.util.Timer;
import java.util.TimerTask;

import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.rzo.netty.ahessian.Constants;

public class ReconnectHandler extends SimpleChannelUpstreamHandler
{
	private static Timer timer = new Timer();
	private  long RECONNECT_DELAY = 5000;
	private BootstrapProvider _bootstrap;
	
	public ReconnectHandler(BootstrapProvider bootstrap, long reconnectDelay)
	{
		RECONNECT_DELAY = reconnectDelay;
		_bootstrap = bootstrap;
	}
	
	public ReconnectHandler(BootstrapProvider bootstrap)
	{
		_bootstrap = bootstrap;
	}
	
		@Override
	    public void channelClosed(ChannelHandlerContext ctx, ChannelStateEvent e) {
			ctx.sendUpstream(e);
			Constants.ahessianLogger.warn("channel closed wait to reconnect ...");
	        timer.schedule(new TimerTask() {
	            public void run() {
	            	Constants.ahessianLogger.warn("reconnecting...");
		              ChannelFuture f = _bootstrap.getBootstrap().connect();
		              try
				{
					f.awaitUninterruptibly();
				}
				catch (Exception e)
				{
					// TODO Auto-generated catch block
					Constants.ahessianLogger.warn("", e);
						}
	              if (f.isSuccess())
	            	  Constants.ahessianLogger.warn("connected");
	              else
	              {
	            	  Constants.ahessianLogger.warn("not connected");
	              }
	               
	            }
	        }, RECONNECT_DELAY);
	    }
		
		@Override
	    public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) {
	        Throwable cause = e.getCause();
	        cause.printStackTrace();
	        if (cause instanceof ConnectException) 
	        {
	        	Constants.ahessianLogger.warn("conection lost");
	        }
	        try
	        {
	        ctx.getChannel().close();
	        }
	        catch (Exception ex)
	        {
	        	
	        }
	    }
    
	
}

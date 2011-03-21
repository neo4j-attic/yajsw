package org.rzo.netty.ahessian.timeout;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.SimpleChannelHandler;
import org.rzo.netty.ahessian.Constants;

abstract class AbstractHeartBeatHandler extends SimpleChannelHandler
{
	final ScheduledExecutorService _executor;
	final long _timeout;
	volatile long _lastCalled;
	volatile boolean _connected = false;
	volatile ChannelHandlerContext _ctx;
	volatile ScheduledFuture _future;

	public AbstractHeartBeatHandler(ScheduledExecutorService executor, long timeout)
	{
		_executor = executor;
		_timeout = timeout;
	}
	
	abstract void timedOut(ChannelHandlerContext ctx);
	
    long getLastCalled()
    {
    	return _lastCalled;
    }
    
    boolean isConnected()
    {
    	return _connected;
    }
    
    public void channelDisconnected(
            ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
    _connected = false;
    if (_future != null)
    	_future.cancel(true);
    _future = null;
    }
    
    protected void ping()
    {
		_lastCalled = System.currentTimeMillis();    	
    }

    
    public void channelConnected(
            ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
		_lastCalled = System.currentTimeMillis();
		_ctx = ctx;
		_connected = true;
		
		Runnable command = new Runnable()
		{

			public void run()
			{
				if (isConnected())
				{
					if (getLastCalled() + _timeout < System.currentTimeMillis())
						try
						{
							timedOut(_ctx);
						}
						catch (Exception e)
						{
							Constants.ahessianLogger.warn("", e);
						}
				}
			}
			
		};
		_future = _executor.scheduleWithFixedDelay(command, _timeout, _timeout, TimeUnit.MILLISECONDS);

		ctx.sendUpstream(e);
    }



	
	

}

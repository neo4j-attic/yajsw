package org.rzo.netty.ahessian.timeout;

import java.util.concurrent.ScheduledExecutorService;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.handler.timeout.ReadTimeoutException;

public class ServerHeartBeatHandler extends AbstractHeartBeatHandler
{
    static final ReadTimeoutException EXCEPTION = new ReadTimeoutException();


	public ServerHeartBeatHandler(ScheduledExecutorService executor, long timeout)
	{
		super(executor, timeout);
	}

	@Override
	void timedOut(ChannelHandlerContext ctx)
	{		
		Channels.fireExceptionCaught(ctx, EXCEPTION);
	    ctx.getChannel().close();
	 }
	
    public void messageReceived(
            ChannelHandlerContext ctx, MessageEvent e) throws Exception {
    	ping();
        ctx.sendUpstream(e);
    }



}

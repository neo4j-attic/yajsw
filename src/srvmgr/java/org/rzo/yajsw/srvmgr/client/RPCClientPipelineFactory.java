package org.rzo.yajsw.srvmgr.client;

import static org.jboss.netty.channel.Channels.pipeline;

import java.util.concurrent.Executor;

import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.rzo.netty.ahessian.io.InputStreamDecoder;
import org.rzo.netty.ahessian.io.OutputStreamEncoder;
import org.rzo.netty.ahessian.io.PullInputStreamConsumer;
import org.rzo.netty.ahessian.rpc.client.HessianProxyFactory;
import org.rzo.netty.ahessian.rpc.message.HessianRPCCallEncoder;
import org.rzo.netty.ahessian.rpc.message.HessianRPCReplyDecoder;
import org.rzo.netty.ahessian.rpc.message.OutputProducer;



public class RPCClientPipelineFactory  implements ChannelPipelineFactory
{
	
	private HessianProxyFactory	_factory;
	private Executor	_executor;
	private Host _host;

	public RPCClientPipelineFactory (Executor executor, HessianProxyFactory factory)
	{
		_factory = factory;
		_executor = executor;
	}
	
	public ChannelPipeline getPipeline() throws Exception
	{	
	    ChannelPipeline pipeline = pipeline(); // Note the static import.
        // InputStreamDecoder returns an input stream and calls the next handler in a separate thread

        pipeline.addLast("inputStream", new InputStreamDecoder());

        //pipeline.addLast("logger2",new OutLogger("2"));
        pipeline.addLast("outputStream", new OutputStreamEncoder());
        
        pipeline.addLast("hessianReplyDecoder", new PullInputStreamConsumer(new HessianRPCReplyDecoder(_factory), _executor));
        pipeline.addLast("hessianCallEncoder", new HessianRPCCallEncoder());
        pipeline.addLast("outputProducer", new OutputProducer(_executor));
        //pipeline.addLast("logger3",new OutLogger("3"));
        pipeline.addLast("hessianHandler", _factory);

    return pipeline;
	}


}

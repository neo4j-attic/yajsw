/* This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * <p/>
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.  
 */
package org.rzo.yajsw.controller.jvm;

import java.net.InetSocketAddress;
import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.socket.oio.OioServerSocketChannelFactory;
import org.rzo.yajsw.Constants;
import org.rzo.yajsw.controller.AbstractController;
import org.rzo.yajsw.controller.Message;
import org.rzo.yajsw.util.Cycler;
import org.rzo.yajsw.util.DaemonThreadFactory;
import org.rzo.yajsw.wrapper.WrappedJavaProcess;
import org.rzo.yajsw.wrapper.WrappedProcess;

// TODO: Auto-generated Javadoc
/**
 * The Class Controller.
 */
public class JVMController extends AbstractController
{

	/** The Constant STATE_UNKNOWN. */
	static final int								STATE_UNKNOWN			= 0;

	/** The Constant STATE_WAITING. */
	static final int								STATE_WAITING			= 1;

	/** The Constant STATE_ESTABLISHED. */
	static final int								STATE_ESTABLISHED		= 2;

	/** The Constant STATE_LOGGED_ON. */
	static final int								STATE_LOGGED_ON			= 3;

	/** The Constant STATE_STARTUP_TIMEOUT. */
	public static final int							STATE_STARTUP_TIMEOUT	= 4;

	/** The Constant STATE_WAITING_CLOSED. */
	public static final int							STATE_WAITING_CLOSED	= 5;

	/** The Constant STATE_USER_STOP. */
	public static final int							STATE_USER_STOP			= 6;

	public static final int							STATE_PING_TIMEOUT		= 7;

	public static final int							STATE_PROCESS_KILLED	= 8;

	/** The _port. */
	int												_port					= DEFAULT_PORT;

	int												_minPort				= DEFAULT_PORT;

	int												_maxPort				= 65535;

	/** The _startup timeout. */
	int												_startupTimeout			= DEFAULT_STARTUP_TIMEOUT * 1000;

	/** The _key. */
	String											_key;

	/** The _ping timeout. */
	int												_pingTimeout			= 10;

	boolean											_pingOK					= false;

	static Executor									_pingExecutor			= Executors.newCachedThreadPool(new DaemonThreadFactory("pinger"));

	/** The _session. */
	// IoSession _session;
	Channel											_channel;

	/** The Constant pool. */
	// static final SimpleIoProcessorPool pool = new
	// SimpleIoProcessorPool(NioProcessor.class);
	/**
	 * To avoid tcp handle leak: Destroy the acceptor at stop and create new one
	 * on each start.
	 * 
	 * The _acceptor.
	 */
	// NioSocketAcceptor _acceptor = null;
	ServerBootstrap									_acceptor				= null;
	Channel											_parentChannel;

	/** The _init. */
	boolean											_init					= false;

	/** The Constant _usedPorts. */
	static final Set								_usedPorts				= Collections.synchronizedSet(new TreeSet());

	/** The Constant _scheduler. */
	static private final ScheduledExecutorService	_scheduler				= Executors.newScheduledThreadPool(1, new DaemonThreadFactory(
																					"controller.scheduler"));

	/** The _timeout handle. */
	volatile ScheduledFuture<?>						_timeoutHandle;

	Cycler											_pingCheck;
	ExecutorService									workerExecutor			= Executors.newCachedThreadPool(new DaemonThreadFactory(
																					"controller-worker"));

	Runnable										_serviceStartupListener;

	/**
	 * Instantiates a new controller.
	 * 
	 * @param wrappedJavaProcess
	 *            the wrapped java process
	 */
	public JVMController(WrappedProcess wrappedJavaProcess)
	{
		super(wrappedJavaProcess);
	}

	public void init()
	{
		if (_pingCheck == null)
			_pingCheck = new Cycler(_pingTimeout, _pingTimeout, _pingExecutor, new Runnable()
			{
				public void run()
				{
					if (!_pingOK)
					{
						getLog().info("Missing wrapper ping within timeout of " + _pingTimeout);
						// stop the process in a separate thread, otherwise
						// conflict
						executor.execute(new Runnable()
						{
							public void run()
							{
								stop(STATE_PING_TIMEOUT);
							}
						});
					}
					else
						_pingOK = false;
				}
			});
	}

	/**
	 * Inits the.
	 */
	private void initInternal()
	{
		_acceptor = null;
		_acceptor = new ServerBootstrap(new OioServerSocketChannelFactory(executor, executor));

		// ???do not allow multiple servers to bind on the same port
		_acceptor.setOption("reuseAddress", false);
		_acceptor.setOption("tcpNoDelay", true);
		_acceptor.setPipelineFactory(new ControllerPipelineFactory(this, _debug));

		_init = true;

	}

	/**
	 * Start.
	 */
	public boolean start()
	{
		int myPort = -1;

		// in case of wrapper chaining: if we already have opened a port to our
		// wrapper: do not use this port for a sub-process
		try
		{
			myPort = Integer.parseInt((String) System.getProperties().get("wrapper.port"));
		}
		catch (Exception e)
		{
		}
		if (myPort != -1)
			_usedPorts.add(myPort);

		try
		{
			initInternal();
			setState(STATE_UNKNOWN);
			// if we have kept the channel
			if (_parentChannel != null && _parentChannel.isBound())
			{
				setState(STATE_WAITING);
				beginWaitForStartup();
				if (isDebug())
					getLog().info("binding successfull");
				return true;
			}
			_port = _minPort;
			while (getState() < STATE_WAITING && _port <= _maxPort)
			{
				if (_usedPorts.contains(_port))
					_port++;
				else
					try
					{
						_usedPorts.add(_port);
						if (isDebug())
							getLog().info("binding to port " + _port);
						_parentChannel = _acceptor.bind(new InetSocketAddress(_port));
						setState(STATE_WAITING);
						beginWaitForStartup();
						if (isDebug())
							getLog().info("binding successfull");
						return true;
					}
					catch (Exception ex)
					{
						getLog().info("binding error: " + ex.getMessage() + " -> retry with another port");

						_usedPorts.remove(_port);
						try
						{
							Thread.sleep(500);
						}
						catch (InterruptedException e)
						{
							e.printStackTrace();
							getLog().info("sleep interrupted in JVMcontroller start");
							Thread.currentThread().interrupt();
							return false;
						}
						_port++;
					}
			}
			getLog().severe("could not find a free port in the range " + _minPort + "..." + _maxPort);
			return false;
		}
		catch (Exception ex)
		{
			getLog().severe("JVMController start " + ex);
			return false;
		}
	}

	/**
	 * Begin wait for startup.
	 */
	void beginWaitForStartup()
	{
		if (_startupTimeout <= 0)
			return;
		final Runnable timeOutAction = new Runnable()
		{
			public void run()
			{
				if (isDebug())
					getLog().severe("WrapperManger did not log on within timeout of " + _startupTimeout);
				stop(STATE_STARTUP_TIMEOUT);
			}
		};
		_timeoutHandle = _scheduler.schedule(timeOutAction, _startupTimeout, TimeUnit.MILLISECONDS);
	}

	void schedulePingCheck()
	{
		_pingOK = false;
		_pingCheck.start();
	}

	void stopPingCheck()
	{
		if (_pingCheck != null)
			_pingCheck.stop();
	}

	void pingReceived()
	{
		_pingOK = true;
	}

	void serviceStartup()
	{
		if (_serviceStartupListener != null)
			_serviceStartupListener.run();
		else
			getLog().info("cannot report service startup: listener is null");
	}

	/**
	 * Stop.
	 * 
	 * @param state
	 *            the state
	 */
	public void stop(int state)
	{
		stopPingCheck();
		if (_timeoutHandle != null)
			_timeoutHandle.cancel(false);
		if (_parentChannel != null)
		{
			int i = 0;
			while (_channel != null && _channel.isConnected() && i < 3)
			{
				i++;
				getLog().info("controller sending a stop command");
				if (_channel != null)
					_channel.write(new Message(Constants.WRAPPER_MSG_STOP, null));
				try
				{
					Thread.sleep(200);
				}
				catch (Exception ex)
				{
				}
			}
			if (_channel != null && _channel.isOpen())
				try
				{
					ChannelFuture cf = _channel.close();
					getLog().info("controller close session");
					cf.await(1000);
				}
				catch (InterruptedException e)
				{
					e.printStackTrace();
					getLog().info("session close wait interrupted in JVMController");
				}

			// undind and dispose all channels and ports.
			// keep the same port until we shut down
			/*
			 * log.info("unbind session"); if (_parentChannel != null &&
			 * _parentChannel.isBound()) { try {
			 * _parentChannel.unbind().await(1000); } catch
			 * (InterruptedException e) { e.printStackTrace(); } _parentChannel
			 * = null; //_acceptor.releaseExternalResources(); }
			 * _usedPorts.remove(_port);
			 */
		}
		setState(state);
	}

	/**
	 * Startup ok.
	 */
	void startupOK()
	{
		if (_timeoutHandle == null)
			return;
		_timeoutHandle.cancel(false);
		schedulePingCheck();
		_timeoutHandle = null;
	}

	// test
	/**
	 * The main method.
	 * 
	 * @param args
	 *            the arguments
	 */
	public static void main(String[] args)
	{
		JVMController c = new JVMController(null);
		c.setDebug(true);
		c.setKey("123");
		c.start();
		c.stop(0);
		JVMController c1 = new JVMController(null);
		c1.setDebug(true);
		c1.setKey("123");
		c1.start();

	}

	/**
	 * Gets the port.
	 * 
	 * @return the port
	 */
	public int getPort()
	{
		return _port;
	}

	/**
	 * Sets the port.
	 * 
	 * @param port
	 *            the new port
	 */
	public void setMinPort(int port)
	{
		if (port > 0 && port < 65536)
			_minPort = port;
		else
			getLog().info("port out of range " + port);
	}

	public void setMaxPort(int port)
	{
		if (port > 0 && port < 65536 && port >= _minPort)
			_maxPort = port;
		else
			getLog().info("port out of range " + port);
	}

	public void setPort(int port)
	{
		_port = port;
	}

	/**
	 * Checks if is debug.
	 * 
	 * @return true, if is debug
	 */
	boolean isDebug()
	{
		return _debug;
	}

	/**
	 * Sets the debug.
	 * 
	 * @param debug
	 *            the new debug
	 */
	public void setDebug(boolean debug)
	{
		_debug = debug;
	}

	/**
	 * Gets the key.
	 * 
	 * @return the key
	 */
	public String getKey()
	{
		return _key;
	}

	/**
	 * Sets the key.
	 * 
	 * @param key
	 *            the new key
	 */
	public void setKey(String key)
	{
		_key = key;
	}

	/**
	 * Gets the startup timeout.
	 * 
	 * @return the startup timeout
	 */
	int getStartupTimeout()
	{
		return _startupTimeout;
	}

	/**
	 * Sets the startup timeout.
	 * 
	 * @param startupTimeout
	 *            the new startup timeout
	 */
	public void setStartupTimeout(int startupTimeout)
	{
		_startupTimeout = startupTimeout;
	}

	/**
	 * Gets the ping timeout.
	 * 
	 * @return the ping timeout
	 */
	int getPingTimeout()
	{
		return _pingTimeout;
	}

	/**
	 * Sets the ping timeout.
	 * 
	 * @param pingTimeout
	 *            the new ping timeout
	 */
	public void setPingTimeout(int pingTimeout)
	{
		_pingTimeout = pingTimeout;
	}

	/**
	 * Wait for.
	 * 
	 * @return true, if successful
	 */
	public boolean waitFor(long timeout)
	{
		long end = System.currentTimeMillis() + 10000;
		while (true)
		{
			if (_state == STATE_LOGGED_ON)
				return true;
			else if (_state == STATE_STARTUP_TIMEOUT)
				return false;
			else if (System.currentTimeMillis() > end)
				return false;
			try
			{
				Thread.sleep(250);
			}
			catch (InterruptedException e)
			{
				e.printStackTrace();
				getLog().info("sleep interrupted in JVMController.waitfor");
				return false;
			}
		}
	}

	/**
	 * Request thread dump.
	 */
	public void requestThreadDump()
	{
		if (_channel != null)
			_channel.write(new Message(Constants.WRAPPER_MSG_THREAD_DUMP, null));
	}

	public void reset()
	{
		stop(JVMController.STATE_UNKNOWN);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#finalize()
	 */
	@Override
	public void finalize() throws Throwable
	{
		try
		{
			reset();
		}
		finally
		{
			super.finalize();
		}
	}

	public void processStarted()
	{
		executor.execute(new Runnable()
		{
			public void run()
			{
				org.rzo.yajsw.os.Process osProcess = ((WrappedJavaProcess) _wrappedProcess)._osProcess;
				if (osProcess != null)
					osProcess.waitFor();
				_wrappedProcess.osProcessTerminated();
				if (_state == STATE_LOGGED_ON || _state == STATE_WAITING_CLOSED || osProcess == null || osProcess.isTerminated())
				{
					stopPingCheck();
					setState(STATE_PROCESS_KILLED);
				}
			}
		});
	}

	public String stateAsStr(int state)
	{
		switch (state)
		{
		case STATE_UNKNOWN:
			return "UNKNOWN";
		case STATE_WAITING:
			return "WAITING";
		case STATE_ESTABLISHED:
			return "ESTABLISHED";
		case STATE_LOGGED_ON:
			return "LOGGED_ON";
		case STATE_STARTUP_TIMEOUT:
			return "STARTUP_TIMEOUT";
		case STATE_WAITING_CLOSED:
			return "WAITING_CLOSED";
		case STATE_USER_STOP:
			return "USER_STOP";
		case STATE_PING_TIMEOUT:
			return "PING_TIMEOUT";
		case STATE_PROCESS_KILLED:
			return "PROCESS_KILLED";

		default:
			return "?";

		}
	}

	public void logStateChange(int state)
	{
		if (state == STATE_STARTUP_TIMEOUT)
			getLog().warning("startup of java application timed out. if this is due to server overload consider increasing wrapper.startup.timeout");
		else if (state == STATE_PING_TIMEOUT)
			getLog()
					.warning(
							"ping between java application and wrapper timed out. if this this is due to server overload consider increasing wrapper.ping.timeout");

	}

	public void processFailed()
	{
		stop(STATE_PROCESS_KILLED);
	}

	public void setServiceStartupListener(Runnable serviceStartupListener)
	{
		_serviceStartupListener = serviceStartupListener;
	}

}

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
package org.rzo.yajsw.tray;

import java.awt.AWTException;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;

import org.rzo.yajsw.util.DaemonThreadFactory;
import org.rzo.yajsw.wrapper.AbstractWrappedProcessMBean;
import org.rzo.yajsw.wrapper.WrappedProcess;
import org.rzo.yajsw.wrapper.WrappedService;

// TODO: Auto-generated Javadoc
/**
 * The Class WrapperTrayIconImpl.
 */
public class WrapperTrayIconImpl implements WrapperTrayIcon
{

	/** The icon running. */
	Image									iconRunning;

	/** The icon idle. */
	Image									iconIdle;

	/** The icon else. */
	Image									iconElse;

	/** The icon offline. */
	Image									iconOffline;

	/** The ti. */
	TrayIcon								ti;

	/** The current image. */
	Image									currentImage			= iconIdle;

	/** The tool tip prefix. */
	String									toolTipPrefix;

	/** The current tool tip. */
	String									currentToolTip;

	/** The tray. */
	final SystemTray						tray					= SystemTray.getSystemTray();

	/** The init. */
	boolean									init					= false;

	/** The _console. */
	Console									_console				= null;

	/** The _process. */
	volatile AbstractWrappedProcessMBean	_process;
	protected static final Executor			executor				= Executors.newCachedThreadPool(new DaemonThreadFactory("console"));

	/** The stop. */
	volatile boolean						stop					= false;

	/** The _current state. */
	int										_currentState			= WrappedProcess.STATE_IDLE;

	/** The _stop item. */
	JMenuItem								_stopItem				= new JMenuItem();

	/** The _close item. */
	JMenuItem								_closeItem				= new JMenuItem();

	/** The _start item. */
	JMenuItem								_startItem				= new JMenuItem();

	/** The _restart item. */
	JMenuItem								_restartItem			= new JMenuItem();

	/** The _console item. */
	JMenuItem								_consoleItem			= new JMenuItem();

	/** The _stop timer item. */
	JMenuItem								_stopTimerItem			= new JMenuItem();

	/** The _thread dump item. */
	JMenuItem								_threadDumpItem			= new JMenuItem();

	/** The _exit item. */
	JMenuItem								_exitItem				= new JMenuItem();

	/** The _exit wrapper item. */
	JMenuItem								_exitWrapperItem		= new JMenuItem();

	/** The _thread dump wrapper item. */
	JMenuItem								_threadDumpWrapperItem	= new JMenuItem();

	/** The _close console item. */
	JMenuItem								_closeConsoleItem		= new JMenuItem();

	/** The _start service item. */
	JMenuItem								_startServiceItem		= new JMenuItem();

	/** The _response item. */
	JMenuItem								_responseItem			= new JMenuItem();

	/** The _inquire message. */
	String									_inquireMessage			= null;

	/**
	 * Instantiates a new wrapper tray icon impl.
	 * 
	 * @param name
	 *            the name
	 * @param icon
	 *            the icon
	 */
	public WrapperTrayIconImpl(String name, String icon)
	{
		if (!SystemTray.isSupported())
		{
			System.out.println("SystemTray not supported on this platform");
			return;
		}

		toolTipPrefix = name + " - ";

		InputStream f = null;
		try
		{
			f = getImage(icon);
			ti = new TrayIcon(createColorImage(f, null, null));
			ti.setImageAutoSize(true);

			Dimension d = ti.getSize();
			f = getImage(icon);
			iconRunning = createColorImage(f, Color.GREEN, d);
			f = getImage(icon);
			iconIdle = createColorImage(f, Color.RED, d);
			f = getImage(icon);
			iconElse = createColorImage(f, Color.ORANGE, d);
			f = getImage(icon);
			iconOffline = createColorImage(f, Color.BLACK, d);
		}
		catch (Exception ex)
		{
			System.out.println("System Tray: file type not supported -> abort");
			return;
		}

		ti = new TrayIcon(iconIdle);
		/*
		 * process.addStateChangeListener(new StateChangeListener() { public
		 * void stateChange(int newState, int oldState) { if (newState ==
		 * WrappedProcess.STATE_SHUTDOWN) { synchronized (tray) {
		 * tray.remove(ti); }
		 * 
		 * if (!_process.getType().endsWith("Service"))
		 * Runtime.getRuntime().halt(0); return; } showState(newState); } });
		 */
		ti.setImageAutoSize(true);

		final JPopupMenu popup = new JPopupMenu();
		_exitItem.setAction(new AbstractAction("Exit", createImageIcon("/resources/exit.png"))
		{
			public void actionPerformed(ActionEvent e)
			{
				popup.setVisible(false);
				stop = true;
				synchronized (tray)
				{
					tray.remove(ti);
				}
			}

		});
		_stopItem.setAction(new AbstractAction("Stop", createImageIcon("/resources/stop.png"))
		{
			public void actionPerformed(ActionEvent e)
			{
				executor.execute(new Runnable()
				{

					public void run()
					{
						if (_process != null)
							try
							{
								_process.stop();
							}
							catch (Throwable ex)
							{
								ex.printStackTrace();
							}
					}

				});
				popup.setVisible(false);
			}

		});
		_closeItem.setAction(new AbstractAction("Close Popup", createImageIcon("/resources/close.png"))
		{
			public void actionPerformed(ActionEvent e)
			{
				popup.setVisible(false);
			}
		});

		_startItem.setAction(new AbstractAction("Start", createImageIcon("/resources/start.png"))
		{
			public void actionPerformed(ActionEvent e)
			{
				if (_process != null)
					try
					{
						_process.start();
					}
					catch (Throwable ex)
					{
						ex.printStackTrace();
					}
				popup.setVisible(false);
			}
		});
		_restartItem.setAction(new AbstractAction("Restart", createImageIcon("/resources/restart.png"))
		{
			public void actionPerformed(ActionEvent e)
			{
				if (_process != null)
					try
					{
						_process.restart();
					}
					catch (Throwable ex)
					{
						ex.printStackTrace();
					}

				popup.setVisible(false);
			}

		});
		_consoleItem.setAction(new AbstractAction("Console", createImageIcon("/resources/console.png"))
		{
			public void actionPerformed(ActionEvent e)
			{
				if (_process != null)
					openConsole();
				popup.setVisible(false);
			}

		});
		_threadDumpItem.setAction(new AbstractAction("Thread Dump", createImageIcon("/resources/lightning.png"))
		{
			public void actionPerformed(ActionEvent e)
			{
				if (_process != null)
					try
					{
						_process.threadDump();
					}
					catch (Throwable ex)
					{
						ex.printStackTrace();
					}

				popup.setVisible(false);
			}
		});

		_stopTimerItem.setAction(new AbstractAction("Stop Timer/Condition", createImageIcon("/resources/clock_stop.png"))
		{
			public void actionPerformed(ActionEvent e)
			{
				if (_process != null)
				{
					try
					{
						_process.stopTimerCondition();
						if (_console != null)
						{
							_console.setTimer(_process.isTimerActive());
							_console.setCondition(_process.isConditionActive());
						}
					}
					catch (Throwable ex)
					{
						ex.printStackTrace();
					}

				}
				popup.setVisible(false);
			}
		});
		_exitWrapperItem.setAction(new AbstractAction("Stop Wrapper", createImageIcon("/resources/exitWrapper.png"))
		{
			public void actionPerformed(ActionEvent e)
			{
				executor.execute(new Runnable()
				{
					public void run()
					{
						if (_process != null)
							try
							{
								_process.stopWrapper();
							}
							catch (Throwable ex)
							{
								ex.printStackTrace();
							}

					}
				});
				popup.setVisible(false);
			}

		});

		_threadDumpWrapperItem.setAction(new AbstractAction("TDump Wrapper", createImageIcon("/resources/lightning.png"))
		{
			public void actionPerformed(ActionEvent e)
			{
				if (_process != null)
					try
					{
						_process.wrapperThreadDump();
					}
					catch (Throwable ex)
					{
						ex.printStackTrace();
					}

				popup.setVisible(false);
			}

		});

		_closeConsoleItem.setAction(new AbstractAction("Close Console")
		{
			public void actionPerformed(ActionEvent e)
			{
				closeConsole();
				popup.setVisible(false);
			}

		});

		_startServiceItem.setAction(new AbstractAction("Start Service", createImageIcon("/resources/startService.png"))
		{
			public void actionPerformed(ActionEvent e)
			{
				if (_process == null)
				{
					try
					{
						WrappedService w = new WrappedService();
						w.init();
						w.start();
					}
					catch (Throwable ex)
					{
						ex.printStackTrace();
					}

				}
				popup.setVisible(false);
			}

		});

		_responseItem.setAction(new AbstractAction("Response", createImageIcon("/resources/Help16.gif"))
		{
			public void actionPerformed(ActionEvent e)
			{
				if (_process != null && _inquireMessage != null)
				{
					String message = _inquireMessage;
					String s = (String) JOptionPane.showInputDialog(message, "");
					if (s != null && _process != null)
					{
						try
						{
							_process.setInquireResponse(s);
							_inquireMessage = null;
						}
						catch (Throwable ex)
						{
							ex.printStackTrace();
						}

					}

				}
				popup.setVisible(false);
			}

		});

		popup.add(_closeItem);
		popup.add(_startItem);
		popup.add(_stopItem);
		popup.add(_restartItem);
		popup.add(_consoleItem);
		popup.add(_responseItem);
		// popup.add(_threadDumpWrapperItem);
		popup.add(_exitWrapperItem);
		popup.add(_startServiceItem);
		popup.add(_exitItem);
		popup.validate();

		ti.addMouseListener(new MouseListener()
		{

			public void mouseClicked(MouseEvent e)
			{
				popup.show(e.getComponent(), e.getX() - popup.getWidth(), e.getY() - popup.getHeight());

			}

			public void mouseEntered(MouseEvent e)
			{
				// TODO Auto-generated method stub

			}

			public void mouseExited(MouseEvent e)
			{
				// TODO Auto-generated method stub

			}

			public void mousePressed(MouseEvent e)
			{
				popup.show(e.getComponent(), e.getX() - popup.getWidth(), e.getY() - popup.getHeight());

			}

			public void mouseReleased(MouseEvent e)
			{
				// TODO Auto-generated method stub

			}

		});

		Runtime.getRuntime().addShutdownHook(new Thread()
		{
			public void run()
			{
				stop = true;
				synchronized (tray)
				{
					tray.remove(ti);
				}
			}
		});

		try
		{
			tray.add(ti);
		}
		catch (AWTException e1)
		{
			// TODO Auto-generated catch block
			e1.printStackTrace();
			System.exit(0);
		}

		init = true;

	}

	private InputStream getImage(String icon)
	{
		InputStream f = null;
		if (icon == null)
			f = findFile("/resources/console.png");
		else
		{
			f = findFile(icon);
			if (f == null)
			{
				try
				{
					System.out.println("System Tray: " + new File(icon).getCanonicalPath() + " not found -> default icon");
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}
				f = findFile("/resources/console.png");
			}
		}
		if (f == null)
		{
			System.out.println("System Tray: no icon found -> abort");
			return null;
		}
		return f;
	}

	/**
	 * Gets the state image.
	 * 
	 * @param state
	 *            the state
	 * 
	 * @return the state image
	 */
	public Image getStateImage(int state)
	{
		switch (state)
		{
		case WrappedProcess.STATE_RUNNING:
			return iconRunning;
		case WrappedProcess.STATE_IDLE:
			return iconIdle;
		default:
			return iconElse;
		}
	}

	/**
	 * Gets the state tool tip.
	 * 
	 * @param state
	 *            the state
	 * 
	 * @return the state tool tip
	 */
	public String getStateToolTip(int state)
	{
		switch (state)
		{
		case WrappedProcess.STATE_RUNNING:
			return "Running";
		case WrappedProcess.STATE_IDLE:
			return "Idle";
		case WrappedProcess.STATE_RESTART:
		case WrappedProcess.STATE_RESTART_START:
		case WrappedProcess.STATE_RESTART_STOP:
		case WrappedProcess.STATE_RESTART_WAIT:
			return "Restarting";
		case WrappedProcess.STATE_STARTING:
			return "Starting";
		case WrappedProcess.STATE_USER_STOP:
		case WrappedProcess.STATE_STOP:
			return "Stopping";
		default:
			return "Other";
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.rzo.yajsw.tray.WrapperTrayIcon#showState(int)
	 */
	synchronized public void showState(int state)
	{
		int oldState = _currentState;
		_currentState = state;
		String strState = getStateToolTip(state);
		if (oldState != _currentState)
			this.message("STATE CHANGED", getStateToolTip(oldState) + " -> " + getStateToolTip(_currentState));
		if (_console != null && _process != null)
		{
			_console.setState(strState);
			_console.setAppRestartCount(_process.getTotalRestartCount(), _process.getRestartCount());
			_console.setAppPid(_process.getAppPid());
			_console.setAppStarted(_process.getAppStarted());
			_console.setAppStopped(_process.getAppStopped());
			_console.setExitCode(_process.getExitCode());
			_console.setTimer(_process.isTimerActive());
			_console.setCondition(_process.isConditionActive());
		}

		Image image = getStateImage(state);
		if (image != currentImage)
		{
			ti.setImage(image);
			currentImage = image;
			currentToolTip = toolTipPrefix + strState;
			ti.setToolTip(currentToolTip);
		}
	}

	/**
	 * Returns an ImageIcon, or null if the path was invalid.
	 * 
	 * @param path
	 *            the path
	 * 
	 * @return the image icon
	 */
	static ImageIcon createImageIcon(String path)
	{
		Image image = createImage(path);
		if (image == null)
			return null;
		return new ImageIcon(image);
	}

	static Image createImage(String path)
	{
		java.net.URL imgURL = WrapperTrayIconImpl.class.getResource(path);
		if (imgURL != null)
		{
			return Toolkit.getDefaultToolkit().getImage(imgURL);
		}
		else
		{
			if (new File(path).exists())
				return Toolkit.getDefaultToolkit().getImage(path);
			return null;
		}
	}

	private InputStream findFile(String path)
	{
		InputStream result = null;
		try
		{
			result = getClass().getResourceAsStream(path);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		if (result != null)
			return result;
		File f = null;
		if (result == null)
			f = new File(path);
		if (f.exists())
			try
			{
				result = new FileInputStream(f);
				return result;
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		return null;

	}

	private Image createColorImage(InputStream imageFile, Color color, Dimension d) throws Exception
	{
		BufferedImage image = ImageIO.read(imageFile);
		imageFile.close();

		if (d != null)
		{
			BufferedImage bufferedResizedImage = new BufferedImage(d.width, d.height, BufferedImage.TYPE_INT_RGB);
			Graphics2D g2d = bufferedResizedImage.createGraphics();
			g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
			g2d.drawImage(image, 0, 0, d.width, d.height, null);
			g2d.dispose();
			image = bufferedResizedImage;

		}
		if (color != null)
		{
			Graphics g = image.getGraphics();
			int w = image.getWidth(null);
			int h = image.getHeight(null);
			int rw = w / 2;
			int rh = h / 2;
			Color c = new Color(color.getRed(), color.getGreen(), color.getBlue(), 200);
			g.setColor(c);
			g.fillRoundRect(0, h - rh, rw, rh, rw, rh);
		}

		return image;
	}

	/**
	 * The main method.
	 * 
	 * @param args
	 *            the arguments
	 * 
	 * @throws InterruptedException
	 *             the interrupted exception
	 */
	public static void main(String[] args) throws InterruptedException
	{
		WrapperTrayIconImpl t = new WrapperTrayIconImpl("test", null);// "tomcat.gif");
		while (true)
		{
			Thread.sleep(2000);
			t.showState(WrappedProcess.STATE_RUNNING);
			Thread.sleep(2000);
			t.showState(WrappedProcess.STATE_IDLE);
			Thread.sleep(2000);
			t.showState(WrappedProcess.STATE_RESTART);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.rzo.yajsw.tray.WrapperTrayIcon#isInit()
	 */
	public boolean isInit()
	{
		return init;
	}

	/**
	 * Open console.
	 */
	public void openConsole()
	{
		if (_console != null)
			return;
		_console = new Console(this);
		this.showState(_currentState);
		_console.setWrapperPid(_process.getWrapperPid());
		_console.setWrapperStarted(_process.getWrapperStarted());
		_console.setWrapperType(_process.getType());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.rzo.yajsw.tray.WrapperTrayIcon#closeConsole()
	 */
	public void closeConsole()
	{
		if (_console == null)
			return;
		_console.close();
		_console = null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.rzo.yajsw.tray.WrapperTrayIcon#error(java.lang.String,
	 * java.lang.String)
	 */
	public void error(String caption, String message)
	{
		ti.displayMessage(toolTipPrefix + caption, message, TrayIcon.MessageType.ERROR);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.rzo.yajsw.tray.WrapperTrayIcon#info(java.lang.String,
	 * java.lang.String)
	 */
	public void info(String caption, String message)
	{
		ti.displayMessage(toolTipPrefix + caption, message, TrayIcon.MessageType.INFO);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.rzo.yajsw.tray.WrapperTrayIcon#message(java.lang.String,
	 * java.lang.String)
	 */
	public void message(String caption, String message)
	{
		ti.displayMessage(toolTipPrefix + caption, message, TrayIcon.MessageType.NONE);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.rzo.yajsw.tray.WrapperTrayIcon#warning(java.lang.String,
	 * java.lang.String)
	 */
	public void warning(String caption, String message)
	{
		ti.displayMessage(toolTipPrefix + caption, message, TrayIcon.MessageType.WARNING);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.rzo.yajsw.tray.WrapperTrayIcon#setProcess(org.rzo.yajsw.wrapper.
	 * AbstractWrappedProcessMBean)
	 */
	public void setProcess(AbstractWrappedProcessMBean proxy)
	{
		_process = proxy;
		if (_process == null)
		{
			Image image = iconOffline;
			if (image != currentImage)
			{
				ti.setImage(image);
				currentImage = image;
				currentToolTip = toolTipPrefix + "OFFLINE";
				ti.setToolTip(currentToolTip);
			}

		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.rzo.yajsw.tray.WrapperTrayIcon#isStop()
	 */
	public boolean isStop()
	{
		return stop;
	}

}

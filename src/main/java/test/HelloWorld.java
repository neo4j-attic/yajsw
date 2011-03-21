package test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Vector;

import org.rzo.yajsw.app.WrapperJVMMain;

public class HelloWorld
{
	static class MyWriter implements Runnable
	{
		public void run()
		{
			int i = 0;
			while (i < 10)
			{
				System.out.println(i++);
				try
				{
					Thread.sleep(100);
				}
				catch (InterruptedException e)
				{
					e.printStackTrace();
				}
			}
		}

	}

	// test for application main.
	public static void main(String[] args) throws Exception
	{
		if (args.length >= 1 && "crash".equals(args[0]))
		{
			Thread.sleep(5000);
			Runtime.getRuntime().halt(99);
		}
		if (args.length >= 1 && "outofmem".equals(args[0]))
		{
			Thread.sleep(5000);
			throw new OutOfMemoryError();
		}
		System.out.println("myenv "+System.getProperty("myenv"));
		/*
		 * try { Process p = Runtime.getRuntime().exec("../set.bat");
		 * BufferedReader in1 = new BufferedReader(new
		 * InputStreamReader(p.getInputStream())); String line; while ((line =
		 * in1.readLine()) != null) System.out.println(line); } catch (Exception
		 * ex) { ex.printStackTrace(); } DocumentBuilderFactory factory =
		 * DocumentBuilderFactory.newInstance();
		 * System.out.println(factory.getClass());
		 */
		// try
		// {
		// Configuration config = new BaseConfiguration();
		// }
		// catch (Throwable ex)
		// {
		// System.out.println("all ok we cannot access commons configuration");
		// ex.printStackTrace();
		// }
		System.out.println("args:");
		for (int i = 0; i < args.length; i++)
			System.out.println(args[i]);
		final Vector v = new Vector();
		new File("test.txt").delete();
		final FileWriter fw = new FileWriter("test.txt");
		final BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
		new Thread(new Runnable()
		{
			public void run()
			{
				try
				{
					int i = 0;
					byte[] buf = new byte[256];
					while (true)
					{
						i++;
						String line = in.readLine();
						System.out.println("in > " + line);
						if (line.contains("exit 0"))
						{
							System.out.println("exiting 0");
							System.exit(0);
						}
						if (line.contains("exit 1"))
						{
							System.out.println("exiting 1");
							System.exit(1);
						}
						if (line.contains("exit 257"))
						{
							System.out.println("exiting 1");
							System.exit(257);
						}
					}
				}
				catch (Exception ex)
				{
					ex.printStackTrace();
				}
				System.out.println("terminated");
			}
		}).start();

		Runtime.getRuntime().addShutdownHook(new Thread()
		{

			public void run()
			{
				System.out.println("Exception 1");
				System.out.println("Exception 2");
				System.out.println("Exception 3");

				int i = 1;
				// while (i>0)
				// System.out.println("asdfasd");
				// Runtime.getRuntime().halt(0);
				System.out.println("You wanna quit, hey?");
				try
				{
					fw.close();
					System.out.println("+ sleeping");
					// Thread.sleep(15000);
					System.out.println("- sleeping");
					// Runtime.getRuntime().halt(0);
				}
				catch (Exception e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				// while(true);
			}

		});

		ArrayList list = new ArrayList();

		// System.out.println(Scheduler.class.getClassLoader());
		// System.out.println(Configuration.class.getClassLoader());
		// System.out.flush();
		int i = 0;
		// org.rzo.yajsw.WrapperMain.WRAPPER_MANAGER.threadDump();
		try
		{
			// Thread.sleep(10000);
		}
		catch (Exception e2)
		{
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		new Thread(new MyWriter()).start();
		new Thread(new MyWriter()).start();
		new Thread(new MyWriter()).start();
		// System.out.println(new BufferedReader(new
		// InputStreamReader(System.in)).readLine());
		// for (; i < 10;)
		if (args.length > 0 && "reportStartup".equals(args[0]))
			WrapperJVMMain.WRAPPER_MANAGER.reportServiceStartup();
		while (true)
		{
			i++;
			System.out.println("a" + i);
			System.out.flush();
			// simulate jvm crash
			// while (i>3)
			// list.add("asfdasffsadfdsdfsaadfsasdasf");

			// if (i ==20)
			// org.rzo.yajsw.app.WrapperJVMMain.WRAPPER_MANAGER.restart();

			if (fw != null)
				try
				{
					// v.add(new byte[1000]);
					// fw.write("" + i + "\n");
					// fw.flush();
				}
				catch (Throwable e1)
				{
					// TODO Auto-generated catch block
					e1.printStackTrace();
					System.exit(0);
				}
			if (i % 2 == 0)
				try
				{
					// WrapperJVMMain.WRAPPER_MANAGER.stop();
					Thread.sleep(500);
					// System.out.println("Exception");
					// System.out.flush();
					// Runtime.getRuntime().halt(0);
				}
				catch (Exception e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}

		/*
		 * WrapperManager.instance.restart(); try { Thread.sleep(10000); } catch
		 * (InterruptedException e) { // TODO Auto-generated catch block
		 * e.printStackTrace(); }
		 */
		// System.exit(0);
		// System.out.println("hello world. short test");
	}

}

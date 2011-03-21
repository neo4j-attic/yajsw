package org.rzo.yajsw.boot;

import java.lang.reflect.Method;
import java.net.URLClassLoader;

public class WrapperServiceBooter
{
	public static void main(String[] args)
	{
		URLClassLoader cl = WrapperLoader.getWrapperClassLoader();
		Thread.currentThread().setContextClassLoader(cl);
		String osName = System.getProperty("os.name");
		String clazz = null;
		if (osName.toLowerCase().startsWith("windows"))
			clazz = "org.rzo.yajsw.app.WrapperMainServiceWin";
		else
			clazz = "org.rzo.yajsw.app.WrapperMainServiceUnix";
		try
		{
			Class cls = Class.forName(clazz, true, cl);
			Method mainMethod = cls.getDeclaredMethod("main", new Class[]
			{ String[].class });
			mainMethod.invoke(null, new Object[]
			{ args });
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

}

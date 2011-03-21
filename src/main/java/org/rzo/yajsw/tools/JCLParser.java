package org.rzo.yajsw.tools;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JCLParser
{
	List<String>	_classpath	= new ArrayList<String>();
	List<String>	_vmOptions	= new ArrayList<String>();
	List<String>	_args		= new ArrayList<String>();
	String			_java		= null;
	String			_mainClass	= null;
	String			_jar		= null;

	private JCLParser(String commandLine)
	{
		parseInternal(commandLine);
	}

	public static JCLParser parse(String commandLine)
	{
		JCLParser result = null;
		result = new JCLParser(commandLine);
		return result;
	}

	// TODO this should cover most cases but is not complete
	private void parseInternal(String commandLine)
	{
		Matcher mr;
		Pattern p;
		// last position of _java in commandLine
		int posJ = 0;
		// last position of _classpath in commandLine
		int posCp = 0;
		// last position of __vmOptions in commandLine
		int posOpts = 0;
		// last position of _mainClass
		int posclp = 0;
		// last position of _jar
		int posJar = 0;

		// parse java
		p = Pattern.compile("\\A(\"[^\"]+\")|(\\S+) ");
		mr = p.matcher(commandLine);
		if (mr.find())
		{
			_java = mr.group();
			_java = _java.replaceAll("\"", "");
			_java = _java.trim();
			posJ = mr.end() - 1;
		}
		else
			throw new RuntimeException("could not parse command line " + commandLine);

		// parse jar
		p = Pattern.compile(" -jar +((\"[^\"]+\")|(\\S+)) ");
		mr = p.matcher(commandLine);
		if (mr.find(posJ))
		{
			_jar = mr.group(1);
			_jar = _jar.replaceAll("\"", "");
			_jar = _jar.trim();
			posJar = mr.end() - 1;
		}

		// parse classpath
		p = Pattern.compile("(( -cp)|( -classpath)|( \"-classpath\")) +((\"[^\"]+\")|(\\S+)) ");
		mr = p.matcher(commandLine);
		if (mr.find(posJ))
		{
			String cp = mr.group().trim();
			posCp = mr.end() - 1;
			cp = cp.substring(cp.indexOf(' '));
			String[] cpArr = cp.split(File.pathSeparator);
			for (String cc : cpArr)
			{
				cc = cc.replaceAll("\"", "");
				_classpath.add(cc.trim());
			}
		}

		// parse main class
		if (_jar == null)
		{
			p = Pattern.compile(" ([^- ])+( |$)");
			mr = p.matcher(commandLine);
			int max = Math.max(posJ, posCp);
			if (mr.find(max))
			{
				_mainClass = mr.group();
				_mainClass = _mainClass.replaceAll("\"", "");
				_mainClass = _mainClass.trim();
				posclp = mr.end() - 1;
			}
		}

		// parse JVM options
		p = Pattern.compile("(( -\\S+)|( -\"[^\"]+\")|( \"-[^\"]+\")) ");
		mr = p.matcher(commandLine);
		int max = Math.max(posJar, posclp);
		while (mr.find())
		{
			String opt = mr.group().trim();
			opt = opt.replaceAll("\"", "");
			if (!opt.startsWith("-jar") && !opt.startsWith("-cp") && !opt.startsWith("-classpath") && mr.end() < max)
			{
				_vmOptions.add(opt);
				posOpts = mr.end();
			}
		}

		// parse args
		p = Pattern.compile(" ((\"[^\"]+\")|(\\S+))( |$)");
		mr = p.matcher(commandLine);
		max = Math.max(posclp, posJar);
		max = Math.max(max, posOpts);
		if (mr.find(max))
		{
			String arg = mr.group();
			arg = arg.replaceAll("\"", "");
			_args.add(arg.trim());
			max = mr.end() - 1;
			while (mr.find(max))
			{
				arg = mr.group();
				arg = arg.replaceAll("\"", "");
				_args.add(arg.trim());
				max = mr.end() - 1;
			}
		}

		if (_java == null || "".equals(_java) || ((_mainClass == null || "".equals(_mainClass)) && ((_jar == null || "".equals(_jar)))))
			throw new RuntimeException("error parsing java command line ");

	}

	public List<String> getClasspath()
	{
		return _classpath;
	}

	public List<String> getVmOptions()
	{
		return _vmOptions;
	}

	public List<String> getArgs()
	{
		return _args;
	}

	public String getJava()
	{
		return _java;
	}

	public String getMainClass()
	{
		return _mainClass;
	}

	public String getJar()
	{
		return _jar;
	}

	public static void main(String[] args)
	{
		String cmd = "\"java\" -cp \"C:\\Program Files\\yajsw-alpha-9.5\\bat\\/../wrapper.jar\" test.HelloWorld";
		cmd = "java -Xrs -jar \"Z:\\dev\\yajsw\\bat\\/..\\wrapper.jar\" -c conf/wrapper.conf       ";
		cmd = "java -cp wrapper.jar -Xrs x.Test -c conf/wrapper.conf       ";
		cmd = "\"java\" -cp \"C:\\Program Files\\yajsw-alpha-9.5\\bat\\/../wrapper.jar\" test.HelloWorld";
		cmd = "\"java\"  test.HelloWorld";
		JCLParser p = JCLParser.parse(cmd);
		System.out.println(p.getJar());
		System.out.println(p.getJava());
		System.out.println(p.getMainClass());
		System.out.println(p.getArgs());
		System.out.println(p.getClasspath());
		System.out.println(p.getVmOptions());
	}

}

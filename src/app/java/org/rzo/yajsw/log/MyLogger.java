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
package org.rzo.yajsw.log;

import java.util.logging.Level;
import java.util.logging.Logger;

// TODO: Auto-generated Javadoc
/**
 * The Class MyLogger.
 */
public class MyLogger extends Logger
{

	/** The _name. */
	String	_pid;
	String _name;

	/**
	 * Instantiates a new my logger.
	 */
	public MyLogger()
	{
		super(null, null);
	}

	/**
	 * Sets the name.
	 * 
	 * @param name
	 *            the new name
	 */
	public void setPID(String name)
	{
		_pid = name;
	}
	
	public void setName(String name)
	{
		_name = name;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.logging.Logger#log(java.util.logging.Level,
	 * java.lang.String)
	 */
	@Override
	public void log(Level level, String msg)
	{
		super.log(level, msg, new String[]{_pid,_name});
	}

}

package org.rzo.yajsw.os.posix;

import org.rzo.yajsw.os.OperatingSystem;

public abstract class OperatingSystemPosix extends OperatingSystem
{
	@Override
	public boolean setWorkingDir(String name)
	{
		return new PosixProcess().setWorkingDirectory(name);
	}

}

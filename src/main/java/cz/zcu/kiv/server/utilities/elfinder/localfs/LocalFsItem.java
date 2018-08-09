package cz.zcu.kiv.server.utilities.elfinder.localfs;

import cz.zcu.kiv.server.utilities.elfinder.service.FsItem;
import cz.zcu.kiv.server.utilities.elfinder.service.FsVolume;

import java.io.File;

public class LocalFsItem implements FsItem
{
	File file;

	FsVolume volume;

	public LocalFsItem(LocalFsVolume volume, File file)
	{
		super();
		this.volume = volume;
		this.file = file;
	}

	public File getFile()
	{
		return file;
	}

	public FsVolume getVolume()
	{
		return volume;
	}

	public void setFile(File file)
	{
		this.file = file;
	}

	public void setVolume(FsVolume volume)
	{
		this.volume = volume;
	}

	@Override
	public String toString()
	{
		return "LocalFsVolume [" + file + "]";
	}
}

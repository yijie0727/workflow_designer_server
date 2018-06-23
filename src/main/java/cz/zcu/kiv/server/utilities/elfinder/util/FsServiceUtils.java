package cz.zcu.kiv.server.utilities.elfinder.util;

import cz.zcu.kiv.server.utilities.elfinder.controller.executor.FsItemEx;
import cz.zcu.kiv.server.utilities.elfinder.service.FsItem;
import cz.zcu.kiv.server.utilities.elfinder.service.FsService;

import java.io.IOException;

public abstract class FsServiceUtils
{
	public static FsItemEx findItem(FsService fsService, String hash)
			throws IOException
	{
		FsItem fsi = fsService.fromHash(hash);
		if (fsi == null)
		{
			return null;
		}

		return new FsItemEx(fsi, fsService);
	}
}

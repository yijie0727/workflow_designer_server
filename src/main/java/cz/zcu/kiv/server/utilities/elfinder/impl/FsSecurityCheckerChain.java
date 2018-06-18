package cz.zcu.kiv.server.utilities.elfinder.impl;

import cz.zcu.kiv.server.utilities.elfinder.service.FsItem;
import cz.zcu.kiv.server.utilities.elfinder.service.FsSecurityChecker;
import cz.zcu.kiv.server.utilities.elfinder.service.FsService;

import java.io.IOException;
import java.util.List;

public class FsSecurityCheckerChain implements FsSecurityChecker
{
	private static final FsSecurityChecker DEFAULT_SECURITY_CHECKER = new FsSecurityCheckForAll();

	List<FsSecurityCheckFilterMapping> _filterMappings;

	private FsSecurityChecker getChecker(FsService fsService, FsItem fsi)
			throws IOException
	{
		String hash = fsService.getHash(fsi);
		for (FsSecurityCheckFilterMapping mapping : _filterMappings)
		{
			if (mapping.matches(hash))
			{
				return mapping.getChecker();
			}
		}

		return DEFAULT_SECURITY_CHECKER;
	}

	public List<FsSecurityCheckFilterMapping> getFilterMappings()
	{
		return _filterMappings;
	}

	@Override
	public boolean isLocked(FsService fsService, FsItem fsi) throws IOException
	{
		return getChecker(fsService, fsi).isLocked(fsService, fsi);
	}

	@Override
	public boolean isReadable(FsService fsService, FsItem fsi)
			throws IOException
	{
		return getChecker(fsService, fsi).isReadable(fsService, fsi);
	}

	@Override
	public boolean isWritable(FsService fsService, FsItem fsi)
			throws IOException
	{
		return getChecker(fsService, fsi).isWritable(fsService, fsi);
	}

	public void setFilterMappings(
			List<FsSecurityCheckFilterMapping> filterMappings)
	{
		_filterMappings = filterMappings;
	}
}

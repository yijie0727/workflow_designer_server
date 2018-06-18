package cz.zcu.kiv.server.utilities.elfinder.controller.executors;

import cz.zcu.kiv.server.utilities.elfinder.controller.executor.AbstractJsonCommandExecutor;
import cz.zcu.kiv.server.utilities.elfinder.controller.executor.CommandExecutor;
import cz.zcu.kiv.server.utilities.elfinder.controller.executor.FsItemEx;
import cz.zcu.kiv.server.utilities.elfinder.service.FsService;
import org.json.JSONObject;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

/**
 * This calculates the total size of all the supplied targets and returns the
 * size in bytes.
 */
public class SizeCommandExecutor extends AbstractJsonCommandExecutor implements
        CommandExecutor
{
	@Override
	protected void execute(FsService fsService, HttpServletRequest request,
			ServletContext servletContext, JSONObject json) throws Exception
	{
		String[] targets = request.getParameterValues("targets[]");
		long size = 0;
		for (String target : targets)
		{
			FsItemEx item = findItem(fsService, target);
			size += item.getSize();
		}
		json.put("size", size);
	}
}

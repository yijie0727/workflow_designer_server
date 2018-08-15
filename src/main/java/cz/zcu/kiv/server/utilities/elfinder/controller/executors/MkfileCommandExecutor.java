package cz.zcu.kiv.server.utilities.elfinder.controller.executors;

import cz.zcu.kiv.server.utilities.elfinder.controller.executor.AbstractJsonCommandExecutor;
import cz.zcu.kiv.server.utilities.elfinder.controller.executor.CommandExecutor;
import cz.zcu.kiv.server.utilities.elfinder.controller.executor.FsItemEx;
import cz.zcu.kiv.server.utilities.elfinder.service.FsItemFilter;
import cz.zcu.kiv.server.utilities.elfinder.service.FsService;
import org.json.JSONObject;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

public class MkfileCommandExecutor extends AbstractJsonCommandExecutor
		implements CommandExecutor
{
	@Override
	public void execute(FsService fsService, HttpServletRequest request,
                        ServletContext servletContext, JSONObject json) throws Exception
	{
		String target = request.getParameter("target");
		String name = request.getParameter("name");

		FsItemEx fsi = super.findItem(fsService, target);
		if(fsi.getVolumnName().equals("MyFiles") && fsi.getName().equals("")){
			throw new Exception("Permission Denied");
		}

		FsItemEx dir = new FsItemEx(fsi, name);
		dir.createFile();

		// if the new file is allowed to be display?
		FsItemFilter filter = getRequestedFilter(request);
		json.put(
				"added",
				filter.accepts(dir) ? new Object[] { getFsItemInfo(request, dir) }
						: new Object[0]);
	}
}

package cz.zcu.kiv.server.utilities.elfinder.controller.executors;

import cz.zcu.kiv.server.utilities.elfinder.controller.executor.AbstractJsonCommandExecutor;
import cz.zcu.kiv.server.utilities.elfinder.controller.executor.CommandExecutor;
import cz.zcu.kiv.server.utilities.elfinder.controller.executor.FsItemEx;
import cz.zcu.kiv.server.utilities.elfinder.service.FsService;
import org.json.JSONObject;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

public class MkdirCommandExecutor extends AbstractJsonCommandExecutor implements
        CommandExecutor
{
	@Override
	public void execute(FsService fsService, HttpServletRequest request,
                        ServletContext servletContext, JSONObject json) throws Exception
	{
		String target = request.getParameter("target");
		String name = request.getParameter("name");

		FsItemEx fsi = super.findItem(fsService, target);
		FsItemEx dir = new FsItemEx(fsi, name);
		dir.createFolder();

		json.put("added", new Object[] { getFsItemInfo(request, dir) });
	}
}

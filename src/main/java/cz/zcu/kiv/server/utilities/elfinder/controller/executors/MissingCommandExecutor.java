package cz.zcu.kiv.server.utilities.elfinder.controller.executors;

import cz.zcu.kiv.server.utilities.elfinder.controller.ErrorException;
import cz.zcu.kiv.server.utilities.elfinder.controller.executor.AbstractJsonCommandExecutor;
import cz.zcu.kiv.server.utilities.elfinder.controller.executor.CommandExecutor;
import cz.zcu.kiv.server.utilities.elfinder.service.FsService;
import org.json.JSONObject;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

/**
 * This is a command that should be executed when a matching command can't be
 * found.
 */
public class MissingCommandExecutor extends AbstractJsonCommandExecutor
		implements CommandExecutor
{
	@Override
	protected void execute(FsService fsService, HttpServletRequest request,
                           ServletContext servletContext, JSONObject json) throws Exception
	{
		String cmd = request.getParameter("cmd");
		throw new ErrorException("errUnknownCmd", cmd);
	}
}

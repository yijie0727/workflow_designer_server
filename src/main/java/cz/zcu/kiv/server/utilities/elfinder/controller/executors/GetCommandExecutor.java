package cz.zcu.kiv.server.utilities.elfinder.controller.executors;

import cz.zcu.kiv.server.utilities.elfinder.controller.executor.AbstractJsonCommandExecutor;
import cz.zcu.kiv.server.utilities.elfinder.controller.executor.CommandExecutor;
import cz.zcu.kiv.server.utilities.elfinder.controller.executor.FsItemEx;
import cz.zcu.kiv.server.utilities.elfinder.service.FsService;
import org.apache.commons.io.IOUtils;
import org.json.JSONObject;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import java.io.InputStream;

public class GetCommandExecutor extends AbstractJsonCommandExecutor implements
        CommandExecutor
{
	@Override
	public void execute(FsService fsService, HttpServletRequest request,
                        ServletContext servletContext, JSONObject json) throws Exception
	{
		String target = request.getParameter("target");

		FsItemEx fsi = super.findItem(fsService, target);
		InputStream is = fsi.openInputStream();
		String content = IOUtils.toString(is, "utf-8");
		is.close();
		json.put("content", content);
	}
}

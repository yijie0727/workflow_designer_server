package cz.zcu.kiv.server.utilities.elfinder.controller.executors;

import cz.zcu.kiv.server.utilities.elfinder.controller.executor.AbstractJsonCommandExecutor;
import cz.zcu.kiv.server.utilities.elfinder.controller.executor.CommandExecutor;
import cz.zcu.kiv.server.utilities.elfinder.service.FsService;
import cz.zcu.kiv.server.utilities.elfinder.util.FsItemFilterUtils;
import org.json.JSONObject;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

public class SearchCommandExecutor extends AbstractJsonCommandExecutor
		implements CommandExecutor
{
	@Override
	public void execute(FsService fsService, HttpServletRequest request,
                        ServletContext servletContext, JSONObject json) throws Exception
	{
		json.put(
				"files",
				files2JsonArray(request, FsItemFilterUtils.filterFiles(
						fsService.find(FsItemFilterUtils
								.createFileNameKeywordFilter(request
										.getParameter("q"))), super
								.getRequestedFilter(request))));
	}
}

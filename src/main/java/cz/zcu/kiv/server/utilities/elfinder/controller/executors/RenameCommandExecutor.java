package cz.zcu.kiv.server.utilities.elfinder.controller.executors;

import cz.zcu.kiv.server.utilities.elfinder.controller.executor.AbstractJsonCommandExecutor;
import cz.zcu.kiv.server.utilities.elfinder.controller.executor.CommandExecutor;
import cz.zcu.kiv.server.utilities.elfinder.controller.executor.FsItemEx;
import cz.zcu.kiv.server.utilities.elfinder.service.FsService;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.json.JSONObject;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

public class RenameCommandExecutor extends AbstractJsonCommandExecutor
		implements CommandExecutor
{
	@Override
	public void execute(FsService fsService, HttpServletRequest request,
                        ServletContext servletContext, JSONObject json) throws Exception
	{
		String target = request.getParameter("target");
		String current = request.getParameter("current");


		String name = request.getParameter("name");

		FsItemEx fsi = super.findItem(fsService, target);
		if(request.getHeader("email")!=null){
			if(fsi.getVolumnName().equals("MyFiles")&& fsi.getName().equals("user_dir_"+request.getHeader("email"))){

				throw new Exception("Permission Denied");
			}

		}
		FsItemEx dst = new FsItemEx(fsi.getParent(), name);
		fsi.renameTo(dst);

		json.put("added", new Object[] { getFsItemInfo(request, dst) });
		json.put("removed", new String[] { target });
	}
}

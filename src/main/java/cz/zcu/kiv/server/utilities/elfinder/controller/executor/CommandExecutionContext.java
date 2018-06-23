package cz.zcu.kiv.server.utilities.elfinder.controller.executor;

import cz.zcu.kiv.server.utilities.elfinder.service.FsServiceFactory;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface CommandExecutionContext
{
	FsServiceFactory getFsServiceFactory();

	HttpServletRequest getRequest();

	HttpServletResponse getResponse();

	ServletContext getServletContext();
}

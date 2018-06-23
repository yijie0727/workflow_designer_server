package cz.zcu.kiv.server.utilities.elfinder.servlet;

import cz.zcu.kiv.server.utilities.elfinder.controller.ConnectorController;
import cz.zcu.kiv.server.utilities.elfinder.controller.executor.CommandExecutorFactory;
import cz.zcu.kiv.server.utilities.elfinder.controller.executor.DefaultCommandExecutorFactory;
import cz.zcu.kiv.server.utilities.elfinder.controller.executors.MissingCommandExecutor;
import cz.zcu.kiv.server.utilities.elfinder.impl.DefaultFsService;
import cz.zcu.kiv.server.utilities.elfinder.impl.DefaultFsServiceConfig;
import cz.zcu.kiv.server.utilities.elfinder.impl.FsSecurityCheckForAll;
import cz.zcu.kiv.server.utilities.elfinder.impl.StaticFsServiceFactory;
import cz.zcu.kiv.server.utilities.elfinder.localfs.LocalFsVolume;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.File;
import java.io.IOException;

import static cz.zcu.kiv.server.Workflow.WORK_FOLDER;

/**
 * ConnectorServlet is an example servlet
 * it creates a ConnectorController on init() and use it to handle requests on doGet()/doPost()
 * 
 * users should extend from this servlet and customize required protected methods
 * 
 * @author bluejoe
 *
 */
public class ConnectorServlet extends HttpServlet
{
	// core member of this Servlet
	public ConnectorController _connectorController;

	/**
	 * create a command executor factory
	 * 
	 * @param config
	 * @return
	 */
	protected CommandExecutorFactory createCommandExecutorFactory(
			ServletConfig config)
	{
		DefaultCommandExecutorFactory defaultCommandExecutorFactory = new DefaultCommandExecutorFactory();
		defaultCommandExecutorFactory
				.setClassNamePattern("cz.zcu.kiv.server.utilities.elfinder.controller.executors.%sCommandExecutor");
		defaultCommandExecutorFactory
				.setFallbackCommand(new MissingCommandExecutor());
		return defaultCommandExecutorFactory;
	}

	/**
	 * create a connector controller
	 * 
	 * @param config
	 * @return
	 */
	protected ConnectorController createConnectorController(ServletConfig config)
	{
		ConnectorController connectorController = new ConnectorController();

		connectorController
				.setCommandExecutorFactory(createCommandExecutorFactory(config));
		connectorController.setFsServiceFactory(createServiceFactory(config));

		return connectorController;
	}

	protected DefaultFsService createFsService()
	{
		DefaultFsService fsService = new DefaultFsService();
		fsService.setSecurityChecker(new FsSecurityCheckForAll());

		DefaultFsServiceConfig serviceConfig = new DefaultFsServiceConfig();
		serviceConfig.setTmbWidth(80);

		fsService.setServiceConfig(serviceConfig);

		fsService.addVolume("MyFiles",
				createLocalFsVolume("My Files", new File(WORK_FOLDER+"MyFiles")));
		fsService.addVolume("Shared",
				createLocalFsVolume("Shared", new File(WORK_FOLDER+"Shared")));


		return fsService;
	}

	private LocalFsVolume createLocalFsVolume(String name, File rootDir)
	{
		LocalFsVolume localFsVolume = new LocalFsVolume();
		localFsVolume.setName(name);
		localFsVolume.setRootDir(rootDir);
		return localFsVolume;
	}

	/**
	 * create a service factory
	 * 
	 * @param config
	 * @return
	 */
	protected StaticFsServiceFactory createServiceFactory(ServletConfig config)
	{
		StaticFsServiceFactory staticFsServiceFactory = new StaticFsServiceFactory();
		DefaultFsService fsService = createFsService();

		staticFsServiceFactory.setFsService(fsService);
		return staticFsServiceFactory;
	}

	@Override
	public void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException
	{
		_connectorController.connector(req, resp);
	}

	@Override
	public void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException
	{
		_connectorController.connector(req, resp);
	}

	@Override
	public void init(ServletConfig config) throws ServletException
	{
		_connectorController = createConnectorController(config);
	}
}

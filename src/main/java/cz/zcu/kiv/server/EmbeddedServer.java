package cz.zcu.kiv.server;

import java.net.URI;
import java.net.URL;
import java.security.ProtectionDomain;

import javax.ws.rs.core.UriBuilder;

import cz.zcu.kiv.server.scheduler.Manager;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.DefaultHandler;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.util.component.LifeCycle;
import org.eclipse.jetty.util.component.LifeCycle.Listener;
import org.eclipse.jetty.util.log.Slf4jLog;
import org.glassfish.jersey.jetty.JettyHttpContainerFactory;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.ServerProperties;

/***********************************************************************************************************************
 *
 * This file is part of the Workflow Designer project

 * ==========================================
 *
 * Copyright (C) 2018 by University of West Bohemia (http://www.zcu.cz/en/)
 *
 ***********************************************************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 *
 ***********************************************************************************************************************
 *
 * EmbeddedServer, 2018/22/05 10:02 Joey Pinto
 *
 * This file hosts the embedded Jetty Web server to run the project.
 **********************************************************************************************************************/

public class EmbeddedServer{
	public static Manager manager;
    private static Log logger = LogFactory.getLog(EmbeddedServer.class);

	public static final int SERVER_PORT = 8680;

	private Server server;
	public EmbeddedServer() {
	}

    public static void main(String[] args){
		try {
			new EmbeddedServer().startServer();
		} catch (Exception e) {
			logger.error(e);
		}
	}

    public void onServerStarted(){
    }

    public void startServer() throws Exception {

		URI baseUri = UriBuilder.fromUri("http://localhost").port(SERVER_PORT)
				.build();

		ResourceConfig config = new ResourceConfig();
		config.register(Workflow.class);
		config.register(UserAccounts.class);
		config.register(Slf4jLog.class);
		config.register(MultiPartFeature.class);

		ServletContextHandler servletContextHandler = new ServletContextHandler(server, "/elfinder",true, false);
		servletContextHandler.addServlet(Servlet.class, "/connector");
		servletContextHandler.setAllowNullPathInfo(true);
		server = JettyHttpContainerFactory.createServer(baseUri, config,
				false);


		ContextHandler contextHandler = new ContextHandler("/api");
		contextHandler.setHandler(server.getHandler());
		contextHandler.setInitParameter(ServerProperties.PROVIDER_CLASSNAMES, MultiPartFeature.class.getCanonicalName());
		ProtectionDomain protectionDomain = EmbeddedServer.class
				.getProtectionDomain();
		final URL location = protectionDomain.getCodeSource().getLocation();

		ResourceHandler resourceHandler = new ResourceHandler();
		resourceHandler.setWelcomeFiles(new String[] { "index.html" });
		resourceHandler.setResourceBase(location.toExternalForm());
		HandlerCollection handlerCollection = new HandlerCollection();
		handlerCollection.setHandlers(new Handler[] { servletContextHandler,resourceHandler,
				contextHandler, new DefaultHandler() });
		server.setHandler(handlerCollection);
		server.addLifeCycleListener(new Listener() {
            @Override
            public void lifeCycleStarting(LifeCycle lifeCycle) {
                logger.info("Server is starting..");
            }

            @Override
            public void lifeCycleStarted(LifeCycle lifeCycle) {
                onServerStarted();
                logger.info("Server Started");
            }

			@Override
			public void lifeCycleFailure(LifeCycle lifeCycle, Throwable throwable) {
                logger.info("Server failed to start");
			}
			@Override
			public void lifeCycleStopping(LifeCycle lifeCycle) {
                logger.info("Server is stopping..");
			}

			@Override
			public void lifeCycleStopped(LifeCycle lifeCycle) {
            	logger.info("Server stopped");
			}

		});
        manager=new Manager();
        manager.start();
		server.start();
		server.join();


	}

    public void stopServer() throws Exception {
	    server.stop();
    }
}

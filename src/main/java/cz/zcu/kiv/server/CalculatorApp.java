package cz.zcu.kiv.server;

import org.glassfish.jersey.server.ResourceConfig;

public class CalculatorApp extends ResourceConfig {

	public CalculatorApp() {
		packages("cz.zcu.kiv");
	}
}

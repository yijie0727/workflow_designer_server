package cz.zcu.kiv.server;

import org.glassfish.jersey.server.ResourceConfig;

public class AppConfig extends ResourceConfig {
    public AppConfig(){
        packages("cz.zcu.kiv");
    }
}

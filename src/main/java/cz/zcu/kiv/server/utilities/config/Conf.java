package cz.zcu.kiv.server.utilities.config;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.util.Properties;

public class Conf {

    private Properties properties;
    Log logger = LogFactory.getLog(Conf.class);
    public static Conf conf;

    private Conf(){
        properties = new Properties();
        try {
            properties.load(Conf.class.getResourceAsStream("/config.properties"));
        } catch (IOException e) {
            logger.error("Could not find config.properties file");
            logger.fatal(e);
            try {
                properties.load(Conf.class.getResourceAsStream("/config.properties.template"));
            } catch (IOException e1) {
                logger.error(e1);
            }
        }
    }
    public static Conf getConf() {
        if(conf ==null){
            conf =new Conf();
        }
        return conf;
    }

    public String getFromEmailAddress(){
        return properties.getProperty("fromEmailAddress");
    }

    public boolean getAuthEnabled(){
        return properties.getProperty("AUTHEnabled").equalsIgnoreCase("true");
    }

    public boolean getHDFSEnabled(){
        return properties.getProperty("HDFSenabled").equalsIgnoreCase("true");
    }

    public String getPort(){
        return properties.getProperty("mail.smtp.port");
    }

    public String getHost(){
        return properties.getProperty("mail.smtp.host");
    }

    public String getSocketClass(){
        return properties.getProperty("mail.smtp.socketFactory.class");
    }

    public String getEmailUsername(){
        return properties.getProperty("emailUsername");
    }

    public String getEmailPassword(){
        return properties.getProperty("emailPassword");
    }

    public Properties getProperties() {
        return properties;
    }

    public void setProperties(Properties properties) {
        this.properties = properties;
    }

    public String getHDFsURI() {
        return properties.getProperty("hdfs.uri");
    }

    public String getHDFsUsername() {
        return properties.getProperty("hdfs.username");
    }

    public int getServerPort() {
        return Integer.valueOf(properties.getProperty("server.port"));
    }

    public int getServerMaxThreads() {
        return Integer.valueOf(properties.getProperty("server.maxthreads"));
    }
}

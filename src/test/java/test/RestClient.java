package test;


import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RestClient extends Thread{

	private static final Logger LOG = LoggerFactory.getLogger(RestClient.class);

	private WebTarget target;

	public RestClient() {
		Client client = ClientBuilder.newClient();
		target = client.target("http://localhost:8680").path("rest").path("workflow");
	}

	public String test() {
        Response response = target.path("test")
                .request(MediaType.APPLICATION_JSON_TYPE).get();
        return response.readEntity(String.class);
    }


}
package test;


import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.glassfish.jersey.media.multipart.MultiPart;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.media.multipart.file.FileDataBodyPart;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.File;

public class RestClient extends Thread{

	private static final Logger LOG = LoggerFactory.getLogger(RestClient.class);

    /**
     *  Test if server is running
     * @return
     */
    public static String test() {
		Client client = ClientBuilder.newClient();
		WebTarget target = client.target("http://localhost:8680").path("rest").path("workflow");
        Response response = target.path("test")
                .request(MediaType.APPLICATION_JSON_TYPE).get();
        return response.readEntity(String.class);
    }

    /**
     *  Upload a JAR and return response
     * @param file
     * @return
     */
	public String uploadJar(File file)  {
		Client client = ClientBuilder.newClient();
        client.register(MultiPartFeature.class);
		WebTarget target = client.target("http://localhost:8680").path("rest").path("workflow");

       MultiPart multiPart = new MultiPart();

        FileDataBodyPart jarFile = new FileDataBodyPart("file", file,
                MediaType.APPLICATION_OCTET_STREAM_TYPE);
        FormDataBodyPart package_name = new FormDataBodyPart("package","data");


        // Add body part
        multiPart.bodyPart(jarFile);
        multiPart.bodyPart(package_name);


        Response response = target.path("uploadJar").request(MediaType.APPLICATION_JSON_TYPE)
                .post(Entity.entity(multiPart, MediaType.MULTIPART_FORM_DATA));

        System.out.println(response.getStatus());
		return response.readEntity(String.class);

	}

    public String executeJar(File file, JSONObject workflow_object)  {
        Client client = ClientBuilder.newClient();
        client.register(MultiPartFeature.class);
        WebTarget target = client.target("http://localhost:8680").path("rest").path("workflow");

        MultiPart multiPart = new MultiPart();

        FileDataBodyPart jarFile = new FileDataBodyPart("file", file,
                MediaType.APPLICATION_OCTET_STREAM_TYPE);
        FormDataBodyPart package_name = new FormDataBodyPart("package","data");
        FormDataBodyPart workflow = new FormDataBodyPart("workflow",workflow_object.toString());


        // Add body part
        multiPart.bodyPart(jarFile);
        multiPart.bodyPart(package_name);
        multiPart.bodyPart(workflow);


        Response response = target.path("executeJar").request(MediaType.APPLICATION_JSON_TYPE)
                .post(Entity.entity(multiPart, MediaType.MULTIPART_FORM_DATA));
        return response.readEntity(String.class);

    }
}
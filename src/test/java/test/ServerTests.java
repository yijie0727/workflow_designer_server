package test;

import cz.zcu.kiv.server.EmbeddedServer;
import cz.zcu.kiv.server.Workflow;
import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;
import java.io.File;
import java.nio.charset.Charset;

public class ServerTests {
    /**
     *  Test if server is running
     * @throws Exception
     */

    private static Log logger = LogFactory.getLog(ServerTests.class);

    @Test
    public void testServer() throws Exception {
        final EmbeddedServer server = new EmbeddedServer(){
            EmbeddedServer server=this;

            @Override
            public void onServerStarted() {
                super.onServerStarted();
                new Runnable() {
                    @Override
                    public void run() {
                        String testResponse = new RestClient().test();
                        assert testResponse.equals("works");
                        try {
                            server.stopServer();
                        }
                        catch (Exception e){
                            logger.error(e);
                            assert false;
                        }
                    }
                }.run();


            }
        };
        server.startServer();

    }

    /**
     *  upload a Jar file and check if blocks are returned
     * @throws Exception
     */
    @Test
    public void testJarToBlocks() throws Exception {
        final EmbeddedServer server = new EmbeddedServer(){
            EmbeddedServer server=this;

            @Override
            public void onServerStarted() {
                super.onServerStarted();
                new Runnable() {
                    @Override
                    public void run() {
                        File file =new File("test_data"+File.separator+"test.jar");
                        try {
                            String testResponse = new RestClient().upload(file,"data");
                            JSONArray jsonArray = new JSONArray(testResponse);
                            assert jsonArray.length() > 0;
                            server.stopServer();
                        }
                        catch (Exception e){
                            logger.error(e);
                            assert false;
                        }
                    }
                }.run();


            }
        };
        server.startServer();

    }

    /**
     *  upload a Jar file and check if blocks are returned
     * @throws Exception
     */
    @Test
    public void testWorkflowToOutput() throws Exception {
        final EmbeddedServer server = new EmbeddedServer(){
            EmbeddedServer server=this;

            @Override
            public void onServerStarted() {
                super.onServerStarted();
                new Runnable() {
                    @Override
                    public void run() {
                        File workflow = new File("test_data" + File.separator + "test.json");
                        try {
                            String testResponse = new RestClient().execute(
                                    new JSONObject(FileUtils.readFileToString(workflow,Charset.defaultCharset())));
                            JSONArray jsonArray = new JSONArray(testResponse);
                            assert jsonArray.length() > 0;
                            server.stopServer();
                        }
                        catch (Exception e){
                            logger.error(e);
                            assert false;
                        }
                    }
                }.run();

            }
        };
        server.startServer();

    }

}

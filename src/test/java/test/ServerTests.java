package test;

import cz.zcu.kiv.server.EmbeddedServer;
import org.junit.Test;

public class ServerTests {

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
                                e.printStackTrace();
                                assert false;
                            }
                        }
                    }.run();


            }
        };
        server.startServer();

    }
}

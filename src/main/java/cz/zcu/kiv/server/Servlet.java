package cz.zcu.kiv.server;

import cz.zcu.kiv.server.utilities.elfinder.servlet.ConnectorServlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class Servlet extends HttpServlet {
    static ConnectorServlet connectorServlet;
    public Servlet(){
        connectorServlet = new ConnectorServlet();
        try {
            connectorServlet.init(this.getServletConfig());
        } catch (ServletException e) {
            e.printStackTrace();
        }
    }
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        connectorServlet.doPost(request,response);
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        connectorServlet.doGet(request,response);
    }
}

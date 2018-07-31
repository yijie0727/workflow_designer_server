package cz.zcu.kiv.server;

import cz.zcu.kiv.server.sqlite.Model.User;
import cz.zcu.kiv.server.sqlite.Users;
import org.glassfish.jersey.media.multipart.FormDataParam;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import static cz.zcu.kiv.server.Workflow.DATA_FOLDER;
import static cz.zcu.kiv.server.Workflow.WORK_FOLDER;
import static cz.zcu.kiv.server.Workflow.createFolderIfNotExists;

@Path("/users")
public class UserAccounts {
    public static final String SQLITE_DB = DATA_FOLDER+"/sqlite.db";

    @POST
    @Path("/register")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    public Response register(@FormDataParam("email")String email,@FormDataParam("password")String password,@FormDataParam("username")String username) {

        User user = new User();
        user.setEmail(email);
        user.setPassword(password);
        user.setUsername(username);
        user = new Users(SQLITE_DB).addUser(user);
        if(user!=null){
            createFolderIfNotExists(WORK_FOLDER+"user_dir_"+user.getEmail());
            return Response.status(200).entity(user.toJSON().toString(4)).build();
        }
        else{
            return Response.status(403)
                    .entity("Unauthorized!").build();
        }

    }

    @POST
    @Path("/login")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    public Response login(@FormDataParam("email")String email, @FormDataParam("password")String password) {

        User user = new Users(SQLITE_DB).getUserByEmail(email);
        if(user!=null&&user.getPassword().equals(password)){
            return Response.status(200).entity(user.toJSON().toString(4)).build();
        }
        else{
            return Response.status(403)
                    .entity("Unauthorized!").build();
        }

    }
}

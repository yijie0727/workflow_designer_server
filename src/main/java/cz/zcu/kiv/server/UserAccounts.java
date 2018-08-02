package cz.zcu.kiv.server;

import cz.zcu.kiv.server.sqlite.Model.User;
import cz.zcu.kiv.server.sqlite.UserAlreadyExistsException;
import cz.zcu.kiv.server.sqlite.UserDoesNotExistException;
import cz.zcu.kiv.server.sqlite.Users;
import cz.zcu.kiv.server.utilties.email.Email;
import cz.zcu.kiv.server.utilties.email.Templates;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.glassfish.jersey.media.multipart.FormDataParam;

import javax.mail.MessagingException;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import java.io.File;
import java.sql.SQLException;

import static cz.zcu.kiv.server.Workflow.WORK_FOLDER;
import static cz.zcu.kiv.server.Workflow.createFolderIfNotExists;

@Path("/users")
public class UserAccounts {
    private static Log logger = LogFactory.getLog(UserAccounts.class);

    @POST
    @Path("/register")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    public Response register(@FormDataParam("email")String email,@FormDataParam("username")String username) {

        User user = new User();
        user.setEmail(email);
        user.setUsername(username);
        String generatedPassword = RandomStringUtils.randomAlphanumeric(6);
        user.setPassword(generatedPassword);
        String generatedToken = RandomStringUtils.randomAlphanumeric(6);
        user.setToken(generatedToken);

        try {
            user = Users.addUser(user);
            Email.sendMail(email,"New Account has been created",
                    Templates.getNewAccountPasswordEmail(user.getUsername(),user.getEmail(),generatedPassword));
            createFolderIfNotExists(WORK_FOLDER+"MyFiles"+File.separator+"user_dir_"+user.getEmail());
            return Response.status(200)
                    .entity(user.toJSON().toString(4)).build();
        } catch (SQLException e) {
            logger.error(e);
            return Response.status(500)
                    .entity("Database Error").build();
        } catch (UserAlreadyExistsException e) {
            return Response.status(403)
                    .entity("User already exists!").build();
        } catch (MessagingException e) {
            logger.error(e);
            return Response.status(500)
                    .entity("Error sending email to user").build();
        }

    }

    @POST
    @Path("/login")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    public Response login(@FormDataParam("email")String email, @FormDataParam("password")String password) {

        try {
            User user = Users.getUserByEmail(email);
            if(user.getPassword().equals(password)){
                String generatedToken = RandomStringUtils.randomAlphanumeric(6);
                user.setToken(generatedToken);
                Users.updateUser(user);
                return Response.status(200).entity(user.toJSON().toString(4)).build();
            }
            else{
                return Response.status(403)
                        .entity("Unauthorized!").build();
            }
        }  catch (UserDoesNotExistException e) {
            return Response.status(403)
                    .entity("Unauthorized!").build();
        }  catch (SQLException e) {
            logger.error(e);
            return Response.status(500)
                    .entity("Database Error").build();
        }
    }

    @POST
    @Path("/forgot")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.TEXT_PLAIN)
    public Response forgot(@FormDataParam("email")String email, @Context HttpHeaders headers) {

        try {
            User user = Users.getUserByEmail(email);
            String referrer=headers.getHeaderString("referer");
            String link = referrer+(referrer.endsWith("/")?"":"/")
                    +"api/users/forgotReset/"+user.getEmail()+"/"+user.getToken();
            Email.sendMail(email,"Password reset link",
                    Templates.getResetPasswordEmail(user.getUsername(),user.getEmail(),link));

            return Response.status(200).entity("Reset link sent").build();

        } catch (SQLException e) {
            logger.error(e);
            return Response.status(500)
                    .entity("Database Error").build();
        } catch (MessagingException e) {
            logger.error(e);
            return Response.status(500)
                    .entity("Error sending email").build();
        } catch (UserDoesNotExistException e) {
            return Response.status(403)
                    .entity("User does not exist!").build();
        }
    }

    @GET
    @Path("/forgotReset/{email}/{token}")
    @Produces(MediaType.TEXT_HTML)
    public Response resetRequest(@PathParam("email")String email,@PathParam("token")String token, @Context HttpHeaders headers) {

        try {
            User user = Users.getUserByEmail(email);
            if(token.equals(user.getToken())){
                String generatedPassword = RandomStringUtils.randomAlphanumeric(6);
                user.setPassword(generatedPassword);
                String generatedToken = RandomStringUtils.randomAlphanumeric(6);
                user.setToken(generatedToken);
                Users.updateUser(user);
                Email.sendMail(email,"Password reset link",
                        Templates.getResetAccountPasswordEmail(user.getUsername(),user.getEmail(),generatedPassword));

                return Response.status(200).entity(Templates.getResetAccountPasswordEmail(user.getUsername(),user.getEmail(),generatedPassword)).build();
            }
            else
                return Response.status(403).entity("Unauthorized").build();


        } catch (SQLException e) {
            logger.error(e);
            return Response.status(500)
                    .entity("Database Error").build();
        } catch (MessagingException e) {
            logger.error(e);
            return Response.status(500)
                    .entity("Error sending email").build();
        } catch (UserDoesNotExistException e) {
            return Response.status(403)
                    .entity("User does not exist!").build();
        }
    }

    @POST
    @Path("/reset")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    public Response reset(@FormDataParam("currentPassword")String currentPassword,@FormDataParam("newPassword")String newPassword, @Context HttpHeaders httpHeaders){
        String email = httpHeaders.getHeaderString("email");
        String token = httpHeaders.getHeaderString("token");
        try {
            if(email==null||email.equals("undefined")
                    ||token==null||token.equals("undefined")
                    ||!Users.checkAuthorized(email,token))
                return Response.status(403).entity("Unauthorized!").build();
        } catch (SQLException e) {
            logger.error(e);
        }

        try {
            User user = Users.getUserByEmail(email);
            if(user.getPassword().equals(currentPassword)){
                user.setPassword(newPassword);
                Users.updateUser(user);
                return Response.status(200).entity(user.toJSON().toString(4)).build();
            }
            else{
                return Response.status(403)
                        .entity("Unauthorized!").build();
            }
        }  catch (UserDoesNotExistException e) {
            return Response.status(403)
                    .entity("Unauthorized!").build();
        }  catch (SQLException e) {
            logger.error(e);
            return Response.status(500)
                    .entity("Database Error").build();
        }
    }
}

package cz.zcu.kiv.server;


import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.io.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

@Path("/workflow")
public class Workflow {

    /** The path to the folder where we want to store the uploaded files */
    private static final String UPLOAD_FOLDER = "uploadedFiles/";
    public Workflow() {
    }
    @Context
    private UriInfo context;
    @GET
    @Path("/test")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String test() {

        return "works";
    }
    /**
     * Returns text response to caller containing Blocks representation of parsed classes
     *
     * @return error response in case of missing parameters an internal
     *         exception or success response if file has been stored
     *         successfully
     */
    @POST
    @Path("/uploadJar")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response uploadJar(
            @FormDataParam("file") File file,
            @FormDataParam("file") FormDataContentDisposition fileDetail,
            @FormDataParam("package") String package_name) {
        // check if all form parameters are provided
        if (file == null || package_name == null )
            return Response.status(400).entity("Invalid form data").build();
        // create our destination folder, if it not exists
        try {
            createFolderIfNotExists(UPLOAD_FOLDER);
        } catch (SecurityException se) {
            return Response.status(500)
                    .entity("Can not create destination folder on server")
                    .build();
        }
        String uploadedFileLocation = UPLOAD_FOLDER + fileDetail.getFileName();
        File outputFile;
        try {
            FileInputStream fos=new FileInputStream(file);
            outputFile = saveToFile(fos, uploadedFileLocation);
        } catch (IOException e) {
            e.printStackTrace();
            return Response.status(500).entity("Can not save file").build();
        }
        JSONArray result = null;
        try{
            URLClassLoader child = new URLClassLoader(new URL[]{outputFile.toURL()}, this.getClass().getClassLoader());

            JarFile jarFile = new JarFile(outputFile);
            Enumeration e = jarFile.entries();


            while (e.hasMoreElements()) {
                JarEntry je = (JarEntry) e.nextElement();
                if(je.isDirectory() || !je.getName().endsWith(".class")){
                    continue;
                }
                String className = je.getName().substring(0,je.getName().length()-6);
                className = className.replace('/', '.');
                if(className.startsWith(package_name)){
                     child.loadClass(className);
                }

            }


            Class classToLoad = Class.forName("cz.zcu.kiv.WorkflowDesigner.Workflow", true, child);
            Constructor<?> ctor=classToLoad.getConstructor(String.class,ClassLoader.class);

            Method method = classToLoad.getDeclaredMethod("initializeBlocks");
            Object instance = ctor.newInstance(package_name,child);
             result = (JSONArray)method.invoke(instance);
        }
        catch(Exception e){
            e.printStackTrace();
            Response.status(200)
                    .entity("Execution failed with " + e.getMessage()).build();
        }
        return Response.status(200)
                .entity(result.toString(4)).build();
    }

    /**
     * Returns text response to indicate execution success
     *
     * @return error response in case of missing parameters an internal
     *         exception or success response if file has been stored
     *         successfully
     */
    @POST
    @Path("/executeJar")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response executeJar(
            @FormDataParam("file") File file,
            @FormDataParam("file") FormDataContentDisposition fileDetail,
            @FormDataParam("package") String package_name,
            @FormDataParam("workflow") String workflow) {


        JSONObject workflow_object = null;
        // check if all form parameters are provided
        if (file == null || package_name == null || workflow == null)
            return Response.status(400).entity("Invalid form data").build();

        try {
             workflow_object = new JSONObject(workflow);

        }
        catch (JSONException e){
            e.printStackTrace();
            return Response.status(400).entity("Invalid workflow JSON").build();

        }
        // create our destination folder, if it not exists
        try {
            createFolderIfNotExists(UPLOAD_FOLDER);
        } catch (SecurityException se) {
            return Response.status(500)
                    .entity("Can not create destination folder on server")
                    .build();
        }
        String uploadedFileLocation = UPLOAD_FOLDER + fileDetail.getFileName();
        File outputFile;
        try {
            FileInputStream fos=new FileInputStream(file);
            outputFile = saveToFile(fos, uploadedFileLocation);
        } catch (IOException e) {
            e.printStackTrace();
            return Response.status(500).entity("Can not save file").build();
        }
        String result = null;
        try{
            URLClassLoader child = new URLClassLoader(new URL[]{outputFile.toURL()}, this.getClass().getClassLoader());

            JarFile jarFile = new JarFile(outputFile);
            
            Enumeration e = jarFile.entries();


            while (e.hasMoreElements()) {
                JarEntry je = (JarEntry) e.nextElement();
                if(je.isDirectory() || !je.getName().endsWith(".class")){
                    continue;
                }
                String className = je.getName().substring(0,je.getName().length()-6);
                className = className.replace('/', '.');
                try {
                    child.loadClass(className);
                }
                catch (RuntimeException e1){
                    e1.printStackTrace();
                }

            }


            Class classToLoad = Class.forName("cz.zcu.kiv.WorkflowDesigner.Workflow", true, child);
            Constructor<?> ctor=classToLoad.getConstructor(String.class,ClassLoader.class);

            Method method = classToLoad.getDeclaredMethod("execute",JSONObject.class);
            Object instance = ctor.newInstance(package_name,child);
            method.invoke(instance,workflow_object);
            result ="success";
        }
        catch(Exception e){
            e.printStackTrace();
            Response.status(500)
                    .entity("Execution failed with " + e.getMessage() ).build();
        }
        return Response.status(200)
                .entity(result).build();
    }

    /**
     * Utility method to save InputStream data to target location/file
     *  @param inStream
     *            - InputStream to be saved
     * @param target
     */
    private File saveToFile(InputStream inStream, String target)
            throws IOException {
        OutputStream out = null;
        int read = 0;
        byte[] bytes = new byte[1024];
        File file = new File(target);
        if(!file.exists()) {
            out = new FileOutputStream(file);
            while ((read = inStream.read(bytes)) != -1) {
                out.write(bytes, 0, read);
            }
            out.flush();
            out.close();
        }
        return file;
    }

    /**
     * Creates a folder to desired location if it not already exists
     *
     * @param dirName
     *            - full path to the folder
     * @throws SecurityException
     *             - in case you don't have permission to create the folder
     */
    private void createFolderIfNotExists(String dirName)
            throws SecurityException {
        File theDir = new File(dirName);
        if (!theDir.exists()) {
            theDir.mkdir();
        }
    }
    
}

package cz.zcu.kiv.server;


import cz.zcu.kiv.server.scheduler.Job;
import cz.zcu.kiv.server.scheduler.Manager;
import cz.zcu.kiv.server.sqlite.Model.Module;
import cz.zcu.kiv.server.sqlite.Modules;
import cz.zcu.kiv.server.sqlite.Users;
import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.glassfish.jersey.media.multipart.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.io.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.Charset;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import static cz.zcu.kiv.server.UserAccounts.SQLITE_DB;
import static cz.zcu.kiv.server.scheduler.Manager.getJob;
import static cz.zcu.kiv.server.scheduler.Manager.getJobs;
import static cz.zcu.kiv.server.scheduler.Manager.jobs;

/***********************************************************************************************************************
 *
 * This file is part of the Workflow Designer project

 * ==========================================
 *
 * Copyright (C) 2018 by University of West Bohemia (http://www.zcu.cz/en/)
 *
 ***********************************************************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 *
 ***********************************************************************************************************************
 *
 * Workflow, 2018/22/05 10:02 Joey Pinto
 *
 * This file hosts the embedded primary APIs for the Workflow Designer Server.
 **********************************************************************************************************************/


@Path("/workflow")
public class Workflow {
    private static Log logger = LogFactory.getLog(Workflow.class);

    /** The path to the folder where we want to store the uploaded files */
    //private static final String DATA_FOLDER = new File(Workflow.class.getClassLoader().getResource("").getFile()).getParentFile().getParentFile().getAbsolutePath();
    public static final String DATA_FOLDER = System.getProperty("user.home")+"/.workflow_designer_files";
    public static final String UPLOAD_FOLDER = DATA_FOLDER+"/uploadedFiles/";
    public static final String GENERATED_FILES_FOLDER = DATA_FOLDER+"/generatedFiles/";
    public static final String WORK_FOLDER = DATA_FOLDER+"/workFiles/";
    public static final String TEMP_FOLDER = DATA_FOLDER+"/tmp/";
    public static final String WORKING_DIRECTORY = DATA_FOLDER+"/workingDirectory/";

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


    @GET
    @Path("/file/{filename}")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response test(@PathParam("filename")String filename) {

        File file = new File(GENERATED_FILES_FOLDER+filename);
        if(file.exists()){
            return Response.status(200).entity(file).build();
        }
        else{
            return Response.status(404)
                    .entity("File not found!").build();
        }

    }

    /**
     * Initializes blocks from already uploaded jars
     *
     * @return error response in case of failure to load a Jar
     */
    @POST
    @Path("/initialize")
    @Produces(MediaType.TEXT_PLAIN)
    public Response initializeAtom(@Context HttpHeaders httpHeaders)  {
        if (EmbeddedServer.manager==null){
            EmbeddedServer.manager=new Manager();
            EmbeddedServer.manager.start();
        }
        try {
            createFolderIfNotExists(UPLOAD_FOLDER);
            createFolderIfNotExists(GENERATED_FILES_FOLDER);
            createFolderIfNotExists(WORK_FOLDER);
            createFolderIfNotExists(TEMP_FOLDER);
            createFolderIfNotExists(WORKING_DIRECTORY);
        } catch (SecurityException se) {
            logger.error("Error saving folder on server ",se);
            return Response.status(500)
                    .entity("Can not create destination folder on server")
                    .build();
        }

        String email = httpHeaders.getHeaderString("email");
        if(email==null||email.equals("undefined")||new Users(SQLITE_DB).getUserByEmail(email)==null)
            return Response.status(403).entity("Unauthorized").build();

        ClassLoader child;
        JSONArray result=new JSONArray();
        try {
            for(String module: getModulesList(email)){
                String jarFileName=module.split(":")[0];
                String packageName=module.split(":")[1];
                File jarFile=new File(UPLOAD_FOLDER+File.separator+jarFileName);
                child = initializeJarClassLoader(packageName,jarFile);


                try{
                    Class classToLoad = Class.forName("cz.zcu.kiv.WorkflowDesigner.Workflow", true, child);
                    Constructor<?> ctor = classToLoad.getConstructor(ClassLoader.class,String.class,String.class,String.class);
                    Method method = classToLoad.getDeclaredMethod("initializeBlocks");
                    method.setAccessible(true);
                    Object instance = ctor.newInstance(child,module,UPLOAD_FOLDER,WORK_FOLDER);
                    JSONArray blocks = (JSONArray)method.invoke(instance);
                    for(int i=0;i<blocks.length();i++){
                        result.put(blocks.getJSONObject(i));
                    }

                }
                catch(Exception e){
                    logger.error("Execution failed",e);
                    Response.status(200)
                            .entity("Execution failed with " + e.getMessage()).build();
                }
            }


        } catch (IOException e) {
            logger.error("Error reading folder",e);
            return Response.status(500)
                    .entity("Can not read destination folder on server")
                    .build();
        }

        return Response.status(200)
                .entity(result.toString(4)).build();
    }


    /**
     * Returns text response to caller containing Blocks representation of parsed classes
     *
     * @return error response in case of missing parameters an internal
     *         exception or success response if file has been stored
     *         successfully
     */
    @POST
    @Path("/upload")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.TEXT_PLAIN)
    public Response uploadJar(
            final FormDataMultiPart formData,
            @FormDataParam("package") String packageName,
            @FormDataParam("public") Boolean publicModule,
            @Context HttpHeaders httpHeaders)  {
        // check if all form parameters are provided
        if (formData == null || packageName == null)
            return Response.status(400).entity("Invalid form data").build();

        if(publicModule==null)publicModule=false;
        else publicModule=true;

        String email = httpHeaders.getHeaderString("email");
        if(email==null||email.equals("undefined")||new Users(SQLITE_DB).getUserByEmail(email)==null)
            return Response.status(403).entity("Unauthorized").build();

        ClassLoader child;
        String jarName;
        String moduleName;
        Module newModule;
        try {


            File jarFile=getSingleJarFile(formData);
            jarName=jarFile.getName();
            moduleName=jarFile.getName()+":"+packageName;
            child = initializeJarClassLoader(packageName,jarFile);
            newModule=new Modules(SQLITE_DB).getModuleByName(jarFile.getName(),packageName);

        } catch (IOException e) {
            logger.error("Cannot read folder on server",e);
            return Response.status(500)
                    .entity("Can not read destination folder on server")
                    .build();
        }



        JSONArray result=new JSONArray();

        try{
            Class classToLoad = Class.forName("cz.zcu.kiv.WorkflowDesigner.Workflow", true, child);
            Constructor<?> ctor=classToLoad.getConstructor(ClassLoader.class,String.class,String.class,String.class);
            Method method = classToLoad.getDeclaredMethod("initializeBlocks");
            method.setAccessible(true);
            Object instance = ctor.newInstance(child,moduleName,UPLOAD_FOLDER,WORK_FOLDER);
            result = (JSONArray)method.invoke(instance);

            if(newModule==null){
                newModule = new Module();
                newModule.setAuthor(email);
                newModule.setPublicJar(publicModule);
                newModule.setJarName(jarName);
                newModule.setPackageName(packageName);
                new Modules(SQLITE_DB).addModule(newModule);
            }
            else{
                newModule.setAuthor(email);
                newModule.setPublicJar(publicModule);
                new Modules(SQLITE_DB).updateModule(newModule);
            }
        }
        catch(Exception e){
            logger.error("Initializing blocks failed",e);
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
    @Path("/execute")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.TEXT_PLAIN)
    public Response execute(
            @FormDataParam("workflow") String workflow)  {

        if (workflow == null)
            return Response.status(400).entity("Invalid form data").build();

        JSONObject workflowObject = null;
        // check if all form parameters are provided

        Set<String>modules=new HashSet<>();
        try {
            workflowObject = new JSONObject(workflow);
            JSONArray blocksArray = workflowObject.getJSONArray("blocks");
            for(int i=0;i<blocksArray.length();i++){
                modules.add(blocksArray.getJSONObject(i).getString("module"));
            }

        }
        catch (JSONException e){
            logger.error("Error parsing workflow JSON",e);
            return Response.status(400).entity("Invalid workflow JSON").build();

        }

        ClassLoader child;
        Map<Class,String>moduleSource = new HashMap<>();

        try {
            child = initializeClassLoader(modules,moduleSource);
        } catch (IOException e) {
            logger.error("Cannot read jar from server",e);
            return Response.status(500)
                    .entity("Can not read destination folder on server")
                    .build();
        }

        JSONArray result = null;

        try{
            result = executeJar(child,workflowObject,moduleSource,null);
        }
        catch(Exception e1){
            logger.error("Executing jar failed",e1);
            return Response.status(500)
                    .entity("Execution failed with " + e1.getMessage() ).build();
        }
        if(result!=null){
            return Response.status(200)
                    .entity(result.toString(4)).build();
        }
        else{
            logger.error("No result was generated");
            return Response.status(400).entity("No output").build();
        }
    }

    /**
     * Returns text response to indicate job scheduling success
     *
     * @return job ID
     */
    @POST
    @Path("/schedule")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.TEXT_PLAIN)
    public Response schedule(
            @FormDataParam("workflow") String workflow)  {

        if (workflow == null)
            return Response.status(400).entity("Invalid form data").build();

        JSONObject workflowObject = new JSONObject(workflow);
        // check if all form parameters are provided

        Job job=new Job(workflowObject);
        long jobId = Manager.addJob(job);
        return Response.status(200)
                .entity(jobId).build();
    }

    /**
     * Returns text response to indicate job scheduling success
     *
     * @return job ID
     */
    @GET
    @Path("/schedule")
    @Produces(MediaType.TEXT_PLAIN)
    public Response schedule(@Context HttpHeaders httpHeaders)  {
        String email = httpHeaders.getHeaderString("email");
        if(email==null||email.equals("undefined")||new Users(SQLITE_DB).getUserByEmail(email)==null)
            return Response.status(403).entity("Unauthorized").build();
        JSONArray jobs = getJobs();
        return Response.status(200)
                .entity(jobs.toString(4)).build();
    }

    /**
     * Returns text response to indicate job scheduling success
     *
     * @return job ID
     */
    @GET
    @Path("/jobs/{jobId}")
    @Produces(MediaType.TEXT_PLAIN)
    public Response getJobById(@PathParam("jobId")long jobId)  {

        JSONObject job = getJob(jobId);
        return Response.status(200)
                .entity(job.toString(4)).build();
    }

    private File getSingleJarFile(FormDataMultiPart multiPart) throws IOException {
        Map<String, List<FormDataBodyPart>> map = multiPart.getFields();

        FormDataBodyPart filePart = null;
        for (Map.Entry<String, List<FormDataBodyPart>> entry : map.entrySet()) {

            for (FormDataBodyPart part : entry.getValue()) {
                if(part.getName().equals("file")){
                    filePart = part;
                    break;
                }

            }
        }
        BodyPart part=filePart;
        InputStream is = part.getEntityAs(InputStream.class);
        ContentDisposition meta = part.getContentDisposition();

        String uploadedFileLocation = UPLOAD_FOLDER + meta.getFileName();
        File outputFile = saveToFile(is, uploadedFileLocation);
        return outputFile;
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
        file.delete();
            out = new FileOutputStream(file);
            while ((read = inStream.read(bytes)) != -1) {
                out.write(bytes, 0, read);
            }
            out.flush();
            out.close();
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
            theDir.mkdirs();
            logger.info(theDir.getAbsolutePath()+ "Created");
        }
        else
            logger.info(theDir.getAbsolutePath()+ "Exists");
    }


    public static ClassLoader initializeClassLoader(Set<String>modules, Map<Class,String>moduleSource) throws IOException {


        int files = modules.size();
        URL[]urls=new URL[files];
        File[]outputFiles=new File[files];

        Iterator<String> iterator = modules.iterator();
        for(int i=0; i<files;i++){
            String module=iterator.next();
            String uploadedFileLocation = UPLOAD_FOLDER + module.split(":")[0];
                outputFiles[i] = new File(uploadedFileLocation);
                urls[i]=outputFiles[i].toURL();
        }
        URLClassLoader child = new URLClassLoader(urls, Workflow.class.getClassLoader());

        iterator = modules.iterator();


        for(int i=0;i<files;i++){
            String module=iterator.next();
            String packageName = module.split(":")[1];
            JarFile jarFile = new JarFile(outputFiles[i]);
            Enumeration e = jarFile.entries();

            while (e.hasMoreElements()) {
                JarEntry je = (JarEntry) e.nextElement();
                if(je.isDirectory() || !je.getName().endsWith(".class")){
                    continue;
                }
                if(packageName!=null&&!je.getName().startsWith(packageName.replace('.','/')))
                    continue;
                String className = je.getName().substring(0,je.getName().length()-6);
                className = className.replace('/', '.');
                try {
                    Class loadedClass = child.loadClass(className);
                    moduleSource.put(loadedClass,module);
                }
                catch (Exception|Error e1){
                    logger.error(e1.getMessage());
                }
            }

        }
        Thread.currentThread().setContextClassLoader(child);
        return child;
    }

    public static JSONArray executeJar(ClassLoader child,JSONObject workflow, Map<Class,String>moduleSource, String workflowOutputFile)throws Exception{
        Class classToLoad = Class.forName("cz.zcu.kiv.WorkflowDesigner.Workflow", true, child);
        Thread.currentThread().setContextClassLoader(child);

        Constructor<?> ctor=classToLoad.getConstructor(ClassLoader.class,Map.class,String.class,String.class);
        Method method = classToLoad.getDeclaredMethod("execute",JSONObject.class,String.class,String.class);
        Object instance = ctor.newInstance(child,moduleSource,UPLOAD_FOLDER,WORK_FOLDER);
        JSONArray result = (JSONArray)method.invoke(instance,workflow,GENERATED_FILES_FOLDER,workflowOutputFile);
        return result;
    }
    private ClassLoader initializeJarClassLoader(String packageName,File outputFile) throws IOException {



            URL url = outputFile.toURL();

            URLClassLoader child = new URLClassLoader(new URL[]{url}, this.getClass().getClassLoader());

            addJarToClassLoader(outputFile,packageName,child);

            Thread.currentThread().setContextClassLoader(child);
            return child;

    }

    public static void addJarToClassLoader(File outputFile, String packageName, ClassLoader child) throws IOException {
        JarFile jarFile = new JarFile(outputFile);
        Enumeration e = jarFile.entries();

        while (e.hasMoreElements()) {
            JarEntry je = (JarEntry) e.nextElement();
            if(je.isDirectory() || !je.getName().endsWith(".class")||(packageName!=null&&!je.getName().startsWith(packageName.replace('.','/')))){
                continue;
            }
            String className = je.getName().substring(0,je.getName().length()-6);
            className = className.replace('/', '.');
            try {
                child.loadClass(className);
            }
            catch (Exception|Error e1){
                logger.error(e1.getMessage());
            }
        }
    }

    public static List<String> getModulesList(String userEmail){
        List<Module> modules=new Modules(SQLITE_DB).getModulesForUser(userEmail);
        List<String>list=new ArrayList<>();
        if(modules!=null){
            for(Module module:modules){
                list.add(module.getJarName()+":"+module.getPackageName());
            }
        }
        return list;


    }
}

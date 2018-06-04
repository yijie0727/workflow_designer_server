package cz.zcu.kiv.server;


import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.glassfish.jersey.media.multipart.*;
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
import java.nio.charset.Charset;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

@Path("/workflow")
public class Workflow {
    private static Log logger = LogFactory.getLog(Workflow.class);

    /** The path to the folder where we want to store the uploaded files */
    private static final String UPLOAD_FOLDER = "uploadedFiles/";
    private static final String GENERATED_FILES_FOLDER = "generatedFiles/";

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
    @Consumes()
    @Produces(MediaType.TEXT_PLAIN)
    public Response initializeAtom()  {

        ClassLoader child;
        JSONArray result=new JSONArray();
        try {
            for(String module: getModules()){
                String jarFileName=module.split(":")[0];
                String packageName=module.split(":")[1];
                File jarFile=new File(UPLOAD_FOLDER+File.separator+jarFileName);
                child = initializeJarClassLoader(packageName,jarFile);


                try{
                    Class classToLoad = Class.forName("cz.zcu.kiv.WorkflowDesigner.Workflow", true, child);
                    Constructor<?> ctor = classToLoad.getConstructor(ClassLoader.class,String.class,String.class);
                    Method method = classToLoad.getDeclaredMethod("initializeBlocks");
                    method.setAccessible(true);
                    Object instance = ctor.newInstance(child,module,UPLOAD_FOLDER);
                    JSONArray blocks = (JSONArray)method.invoke(instance);
                    for(int i=0;i<blocks.length();i++){
                        result.put(blocks.getJSONObject(i));
                    }

                }
                catch(Exception e){
                    e.printStackTrace();
                    Response.status(200)
                            .entity("Execution failed with " + e.getMessage()).build();
                }
            }


        } catch (IOException e) {
            e.printStackTrace();
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
            @FormDataParam("package") String packageName)  {
        // check if all form parameters are provided
        if (formData == null || packageName == null )
            return Response.status(400).entity("Invalid form data").build();
        // create our destination folder, if it not exists
        try {
            createFolderIfNotExists(UPLOAD_FOLDER);
        } catch (SecurityException se) {
            se.printStackTrace();
            return Response.status(500)
                    .entity("Can not create destination folder on server")
                    .build();
        }


        ClassLoader child;
        String module;
        try {
            File jarFile=getSingleJarFile(formData);
            module=jarFile.getName()+":"+packageName;
            child = initializeJarClassLoader(packageName,jarFile);
            putModule(module);

        } catch (IOException e) {
            e.printStackTrace();
            return Response.status(500)
                    .entity("Can not read destination folder on server")
                    .build();
        }


        JSONArray result=new JSONArray();

        try{
            Class classToLoad = Class.forName("cz.zcu.kiv.WorkflowDesigner.Workflow", true, child);
            Constructor<?> ctor=classToLoad.getConstructor(ClassLoader.class,String.class,String.class);

            Method method = classToLoad.getDeclaredMethod("initializeBlocks");
            method.setAccessible(true);
            Object instance = ctor.newInstance(child,module,UPLOAD_FOLDER);
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
            e.printStackTrace();
            return Response.status(400).entity("Invalid workflow JSON").build();

        }
        // create our destination folder, if it not exists
        try {
            createFolderIfNotExists(GENERATED_FILES_FOLDER);
        } catch (SecurityException se) {
            return Response.status(500)
                    .entity("Can not create destination folder on server")
                    .build();
        }

        ClassLoader child;
        Map<Class,String>moduleSource = new HashMap<>();

        try {
            child = initializeClassLoader(modules,moduleSource);
        } catch (IOException e) {
            return Response.status(500)
                    .entity("Can not read destination folder on server")
                    .build();
        }

        JSONArray result = null;

        try{
            Class classToLoad = Class.forName("cz.zcu.kiv.WorkflowDesigner.Workflow", true, child);
            Thread.currentThread().setContextClassLoader(child);

            Constructor<?> ctor=classToLoad.getConstructor(ClassLoader.class,Map.class,String.class);
            Method method = classToLoad.getDeclaredMethod("execute",JSONObject.class,String.class);
            Object instance = ctor.newInstance(child,moduleSource,UPLOAD_FOLDER);
            result = (JSONArray)method.invoke(instance,workflowObject,GENERATED_FILES_FOLDER);
        }
        catch(Exception e1){
            e1.printStackTrace();
            Response.status(500)
                    .entity("Execution failed with " + e1.getMessage() ).build();
        }
        if(result!=null)
        return Response.status(200)
                .entity(result.toString(4)).build();
        else{
            return Response.status(400).entity("No output").build();
        }
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
            theDir.mkdir();
        }
    }


    private ClassLoader initializeClassLoader(Set<String>modules, Map<Class,String>moduleSource) throws IOException {


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
        URLClassLoader child = new URLClassLoader(urls, this.getClass().getClassLoader());

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

    public static void putModule(String module) throws IOException {
        List<String>modules=getModules();
        if(!modules.contains(module)){
            modules.add(module);
        }
        saveModules(modules);
    }

    public static List<String> getModules() throws IOException {
        List<String>modules=new ArrayList<>();
        File modulesFile = new File(UPLOAD_FOLDER+File.separator+"modules.json");
        if(modulesFile.exists()){
            JSONArray jsonArray =new JSONArray(FileUtils.readFileToString(modulesFile,Charset.defaultCharset()));
            for(int i=0;i<jsonArray.length();i++){
                modules.add(jsonArray.getString(i));
            }
        }
        return modules;
    }

    public static void saveModules(List<String>modules) throws IOException {
        File modulesFile = new File(UPLOAD_FOLDER+File.separator+"modules.json");
        JSONArray jsonArray =new JSONArray();
        for(int i=0;i<modules.size();i++){
            jsonArray.put(modules.get(i));
        }
        FileUtils.writeStringToFile(modulesFile,jsonArray.toString(4),Charset.defaultCharset());

    }
}

package cz.zcu.kiv.server.scheduler;

import cz.zcu.kiv.server.sqlite.Jobs;
import cz.zcu.kiv.server.utilities.config.Conf;
import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;


public class Manager {
    private static Log logger = LogFactory.getLog(Manager.class);
    private static Manager manager;
    private static final int MAX_THREADS = Conf.getConf().getServerMaxThreads();
    private static int CURRENT_THREAD_COUNT=0;

    private Manager(){

    }

    public static Manager getInstance(){
        if(manager==null){
            manager=new Manager();
            try {
                Jobs.terminateRunningJobs();
            } catch (SQLException e) {
                logger.error(e);
            }
        }

        return manager;
    }

    //New Job arrives
    public long addJob(Job job) throws SQLException {
        //Add job to database as a pending job
        job = Jobs.addJob(job);

        //Check for thread allocation
        if(CURRENT_THREAD_COUNT<MAX_THREADS){
            startJob(job);
        }

        return job.getId();
    }

    //Thread is available, start execution
    public synchronized void startJob(Job job){
        CURRENT_THREAD_COUNT++;
        JobThread jobThread=new JobThread(job){
            @Override
            public void onJobCompleted(){
                //Job execution is complete
                super.onJobCompleted();
                //Indicate thread is available
                endJob();
            }
        };
        jobThread.start();

    }

    //Indicate thread is available and schedule pending job
    public void endJob() {

        CURRENT_THREAD_COUNT--;
        try {
            //Get list of pending jobs from database
            List<Job> pendingJobs = Jobs.getJobsByStatus(Status.WAITING.name());
            if(!pendingJobs.isEmpty()){

                //check if thread is available and schedule job
                startJob(pendingJobs.get(0));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    public JSONArray getJobs(String email) throws SQLException {
        JSONArray jsonArray=new JSONArray();
        List<Job> jobsList = Jobs.getJobsByEmail(email);
        for(Job job:jobsList) {
            if(job.getOwner().equals(email)){
                JSONObject jobObject=job.toJSON(false);
                jsonArray.put(jobObject);
            }

        }
        return jsonArray;
    }

    public JSONObject getJob(long jobId) throws SQLException {
        return Jobs.getJob(jobId).toJSON(true);
    }


    public void clearJobs(String email) throws SQLException {
        List<Job> jobs = Jobs.getNotRunningOrWaitingJobs(email);
        Jobs.clearJobs(email);
        for(Job item : jobs) {
            FileUtils.deleteQuietly(new java.io.File(item.getWorkflowOutputFile()));
        }
    }



    /**
     * save Template or WorkFlow to MyTemplates Or MyWorkFlow Folder
     * @param obj is either the template jsonString or the workflow jodID
     * @param name is either the templateName or the WorkFlowName
     * @param table is either "MyTemplates" or "MyWorkFlows"
     * @param folderName is the whole path either where we store the templates or the workFlows
     */
    public void saveTemplateOrWorkFlow(String obj, String name, String table, String folderName) throws SQLException{

        Date date = new Date();
        String currentTime = date.toString();

        JSONObject tempJSON = null;
        if(table.equals("MyTemplates")){
            tempJSON = new JSONObject(obj);
        } else if(table.equals("MyWorkFlows")){
            long jobID = Long.parseLong(obj);
            tempJSON = getJob(jobID);
        }

        String path = folderName + File.separator+ currentTime + "_" + name + ".json";
        File templateFile=new File(path);

        try{
            boolean flag = templateFile.createNewFile();
            FileUtils.writeStringToFile(templateFile, tempJSON.toString(4), Charset.defaultCharset());

        } catch(IOException e){
            logger.error(e);
        }
    }


    /**
     * return Templates Table(no data)  or  WorkFlows Table(with results)
     *  which contains index, name, createdTime in JSONArray
     */
    public JSONArray showTable(String myFolder){

        String pathStr = myFolder;
        File templatesFiles = new File(pathStr);
        String[] tempNames= templatesFiles.list(new MyFilter());
        JSONArray templates = new JSONArray();

        if(tempNames == null)
            return templates;

        int index = 1;
        for(String fileStr: tempNames){

            String[] temps = fileStr.split("/");
            String fileName = temps[temps.length - 1]; //Sun May 26 20:35:48 CST 2019_Add.json
            fileName = fileName.split("\\.")[0]; //Sun May 26 20:35:48 CST 2019_Add

            int i = fileName.indexOf("_");
            //int j = fileName.lastIndexOf("_");

            String createdTime = fileName.substring(0, i);      //Sun May 26 20:35:48 CST 2019
            String showedFileName = fileName.substring(i+1);    //Add

            JSONObject tempObj = new JSONObject();
            tempObj.put("index", index);
            tempObj.put("name", showedFileName);
            tempObj.put("time", createdTime);

            templates.put(tempObj);
            index++;
        }
        return templates;
    }

    /**
     * return particular Template/WorkFlow in JSONObject
     */
    public JSONObject loadTemplateWorkFlow(String myFolder, int templateIndex){

        //logger.info("templateIndex = "+templateIndex);

        String pathStr1 = myFolder;
        File templatesFiles = new File(pathStr1);
        String[] tempNames= templatesFiles.list(new MyFilter());
        JSONObject template =  new JSONObject();

        if(tempNames == null) return template;

        String fileName = tempNames[templateIndex-1];

        String[] temps = fileName.split("/");
        fileName = temps[temps.length - 1];
        //fileName = temps[1000];
        String pathStr2 = myFolder+File.separator+fileName;
        File jsonFile = new File(pathStr2);

        String jsonString = null;
        try{
            jsonString = FileUtils.readFileToString(jsonFile, "UTF-8");

        } catch(IOException e){
            logger.error(e);
        }
        template = new JSONObject(jsonString);

        return template;
    }


    //inner class for FileFilter
    class MyFilter implements FilenameFilter {
        @Override
        public boolean accept(File dir, String name) {
            return name.endsWith(".json");
        }
    }



}

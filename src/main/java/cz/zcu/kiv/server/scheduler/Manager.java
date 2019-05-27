package cz.zcu.kiv.server.scheduler;

import cz.zcu.kiv.server.sqlite.Jobs;
import cz.zcu.kiv.server.utilities.config.Conf;
import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

import static cz.zcu.kiv.server.Workflow.WORK_FOLDER;
import static cz.zcu.kiv.server.Workflow.createFolderIfNotExists;

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
    public void startJob(Job job){
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
        Jobs.clearJobs(email);
    }


    /**
     * add WorkFlow to MyWorkFlows Folder
     */
    public void addWorkFlowToMyWorkFlows(long jobID, String workFlowName) throws SQLException{
        createFolderIfNotExists(WORK_FOLDER+"MyWorkFlows");

        Date date = new Date();
        String currentTime = date.toString();

        JSONObject work = getJob(jobID);

        try{
            File workFlowOutputFile = File.createTempFile(currentTime+"_"+workFlowName+"_",".json",
                    new File(WORK_FOLDER+File.separator+"MyWorkFlows"));
            File workFlowOutput=new File(workFlowOutputFile.getAbsolutePath());
            FileUtils.writeStringToFile(workFlowOutput, work.toString(4), Charset.defaultCharset());

        } catch(IOException e){
            logger.error(e);
        }
    }

    /**
     * add Template to MyTemplates Folder
     */
    public void addTemplateToMyTemplates(String template, String templateName){
        createFolderIfNotExists(WORK_FOLDER+"MyTemplates");
        Date date = new Date();
        String currentTime = date.toString();
        JSONObject temp = new JSONObject(template);
        try{

            File templateOutputFile = File.createTempFile(currentTime+"_"+templateName+"_",".json",
                    new File(WORK_FOLDER+File.separator+"MyTemplates"));
            File templateOutput=new File(templateOutputFile.getAbsolutePath());
            FileUtils.writeStringToFile(templateOutput, temp.toString(4), Charset.defaultCharset());

        } catch(IOException e){
            logger.error(e);
        }
    }

    /**
     * return Templates Table(no data)  or  WorkFlows Table(with results)
     *  which contains index, name, createdTime in JSONArray
     */
    public JSONArray showTable(String tableDestinationName){


        String pathStr = WORK_FOLDER+tableDestinationName;
        File templatesFiles = new File(pathStr);
        String[] tempNames= templatesFiles.list();
        JSONArray templates = new JSONArray();

        if(tempNames == null)
            return templates;

        int index = 1;
        for(String fileStr: tempNames){
            if(fileStr.startsWith(".")) continue;//avoid ".DS_Store" in Folder

            String[] temps = fileStr.split("/");
            String fileName = temps[temps.length - 1]; //Sun May 26 20:35:48 CST 2019_Add_12345.json
            fileName = fileName.split("\\.")[0]; //Sun May 26 20:35:48 CST 2019_Add_12345

            int i = fileName.indexOf("_");
            int j = fileName.lastIndexOf("_");

            String createdTime = fileName.substring(0, i);      //Sun May 26 20:35:48 CST 2019
            String showedFileName = fileName.substring(i+1, j); //Add
            String uniqueID = fileName.substring(j+1);          //_12345

            JSONObject tempObj = new JSONObject();
            tempObj.put("index", index);
            tempObj.put("name", showedFileName);
            tempObj.put("time", createdTime);
            tempObj.put("uniqueID", uniqueID);

            templates.put(tempObj);
            index++;
        }
        return templates;
    }

    /**
     * return particular Template/WorkFlow in JSONObject
     */
    public JSONObject loadTemplateWorkFlow(String tableDestinationName, int templateIndex){

        //logger.info("templateIndex = "+templateIndex);

        String pathStr1 = WORK_FOLDER+tableDestinationName;
        File templatesFiles = new File(pathStr1);
        String[] tempNames= templatesFiles.list();
        JSONObject template =  new JSONObject();

        if(tempNames == null) return template;

        //avoid ".DS_Store" in Folder
        int fileIndex = templateIndex - 1;
        int dot = 0;
        for(int i = 0; i<tempNames.length; i++){
            if(tempNames[i].startsWith(".DS")){
                dot = i; break;
            }
        }
        String fileName = fileIndex >= dot? tempNames[templateIndex]:tempNames[templateIndex-1];

        String[] temps = fileName.split("/");
        fileName = temps[temps.length - 1];
        String pathStr2 = WORK_FOLDER+tableDestinationName+File.separator+fileName;
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



}

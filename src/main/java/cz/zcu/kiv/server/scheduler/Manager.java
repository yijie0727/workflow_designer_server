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

import static cz.zcu.kiv.server.Workflow.GENERATED_FILES_FOLDER;


public class Manager {
    private static Log logger = LogFactory.getLog(Manager.class);
    private static Manager manager;
    private static final int MAX_THREADS = Conf.getConf().getServerMaxThreads();
    private static int CURRENT_THREAD_COUNT = 0;

    private Manager() {

    }

    public static Manager getInstance() {
        if (manager == null) {
            manager = new Manager();
            try {
                Jobs.terminateRunningJobs();
            } catch (SQLException e) {
                logger.error(e);
            }
        }

        return manager;
    }

    //New Job arrives
    public synchronized long addJob(Job job) throws SQLException {
        //Add job to database as a pending job
        job = Jobs.addJob(job);
        //Check for thread allocation
        if (CURRENT_THREAD_COUNT < MAX_THREADS) {
            startJob(job);
        } else {
            job.setStatus(Status.WAITING);
            Jobs.updateJob(job);
        }


        return job.getId();
    }

    //Thread is available, start execution
    public void startJob(Job job) throws SQLException {
        CURRENT_THREAD_COUNT++;
        
        job.setStatus(Status.RUNNING);
        Jobs.updateJob(job);
        JobThread jobThread = new JobThread(job) {
            @Override
            public void onJobCompleted() {
                //Job execution is complete
                super.onJobCompleted();
                //Indicate thread is available
                endJob();
            }
        };
        jobThread.start();

    }

    //Indicate thread is available and schedule pending job
    public synchronized void endJob() {

        CURRENT_THREAD_COUNT--;
        try {
            //Get list of pending jobs from database
            List<Job> pendingJobs = Jobs.getJobsByStatus(Status.WAITING.name());
            if (!pendingJobs.isEmpty()) {

                // check if thread is available and schedule job
                Job job = pendingJobs.get(0);
                job.setStatus(Status.RUNNING);
                Jobs.updateJob(job);
                startJob(job);
            }
        } catch (SQLException e) {
            logger.error(e);
        }

    }

    public JSONArray getJobs(String email) throws SQLException {
        JSONArray jsonArray = new JSONArray();
        List<Job> jobsList = Jobs.getJobsByEmail(email);
        for (Job job : jobsList) {
            if (job.getOwner().equals(email)) {
                JSONObject jobObject = job.toJSON(false);
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

        if (jobs == null || jobs.size() == 0) return;

        // all the jobs belongs to the owner = this email
        String outputFolder = GENERATED_FILES_FOLDER + email;
        for(Job item : jobs) {
            // delete all the jsonFiles(in WORKING_DIRECTORY ) the owner has
            String file = item.getWorkflowOutputFile(); 
            if (item != null && file != null && !file.equals("")) {
                // delete json file
                FileUtils.deleteQuietly(new java.io.File(item.getWorkflowOutputFile()));

                // delete generated files(FILE, TABLE, GRAPH)
                JSONObject workflow = item.getWorkflow();

                JSONArray blocksArray = workflow.getJSONArray("blocks");
                for(int i = 0; i<blocksArray.length(); i++){
                    JSONObject block = blocksArray.getJSONObject(i);

                    if(!block.has("output")) continue;

                    JSONObject JSONOutput = block.getJSONObject("output");
                    String type = JSONOutput.getString("type");

                    if("FILE".equals(type) || "TABLE".equals(type) || "GRAPH".equals(type)) {
                        JSONObject value = JSONOutput.getJSONObject("value");
                        String fileName = value.getString("filename");

                        FileUtils.deleteQuietly(new File( GENERATED_FILES_FOLDER + fileName));
                    }
                }
            }
            // delete all the generatedFiles(in GENERATED_FILES_FOLDER ) of this job the owner has
            List<String> files = item.getGeneratedFilesList();
            if ( files == null || files.size() == 0) continue;
            for(String generatedFile: files){
                logger.info("delete " + email + "'s " + generatedFile);
                FileUtils.deleteQuietly(new File(outputFolder + File.separator + generatedFile ));
            }
        }

    }


    /**
     * save Template or WorkFlow to MyTemplates Or MyWorkFlow Folder
     *
     * @param obj        is either the template jsonString or the workflow jodID
     * @param name       is either the templateName or the WorkFlowName
     * @param table      is either "MyTemplates" or "MyWorkFlows"
     * @param folderName is the whole path either where we store the templates or the workFlows
     */
    public void saveTemplateOrWorkFlow(String obj, String name, String table, String folderName) throws SQLException, IOException {

        Date date = new Date();
        String currentTime = date.toString();

        JSONObject tempJSON = null;
        if (table.equals("MyTemplates")) {
            tempJSON = new JSONObject(obj);
        } else if (table.equals("MyWorkFlows")) {
            long jobID = Long.parseLong(obj);
            tempJSON = getJob(jobID);
        }

        String path = folderName + File.separator + currentTime + "_" + name + ".json";
        File templateFile = new File(path);

        templateFile.createNewFile();
        FileUtils.writeStringToFile(templateFile, tempJSON.toString(4), Charset.defaultCharset());
    }


    /**
     * return Templates Table(no data)  or  WorkFlows Table(with results)
     * which contains index, name, createdTime in JSONArray
     */
    public JSONArray showTable(String myFolder) {

        String pathStr = myFolder;
        File templatesFiles = new File(pathStr);
        String[] tempNames = templatesFiles.list(new JSONFilter());
        JSONArray templates = new JSONArray();

        if (tempNames == null)
            return templates;

        int index = 1;
        for (String fileStr : tempNames) {

            String[] temps = fileStr.split("/");
            String fileName = temps[temps.length - 1]; //Sun May 26 20:35:48 CST 2019_Add.json
            fileName = fileName.substring(0, fileName.length()-5);


            int i = fileName.indexOf("_");
            //int j = fileName.lastIndexOf("_");

            String createdTime = fileName.substring(0, i);      //Sun May 26 20:35:48 CST 2019
            String showedFileName = fileName.substring(i + 1);    //Add

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
    public JSONObject loadTemplateWorkFlow(String myFolder, int templateIndex) throws IOException {

        //logger.info("templateIndex = "+templateIndex);

        String pathStr1 = myFolder;
        File templatesFiles = new File(pathStr1);
        String[] tempNames = templatesFiles.list(new JSONFilter());
        JSONObject template = new JSONObject();

        if (tempNames == null) return template;

        String fileName = tempNames[templateIndex - 1];

        String[] temps = fileName.split("/");
        fileName = temps[temps.length - 1];
        //fileName = temps[1000];
        String pathStr2 = myFolder + File.separator + fileName;
        File jsonFile = new File(pathStr2);

        String jsonString = FileUtils.readFileToString(jsonFile, "UTF-8");
        template = new JSONObject(jsonString);

        return template;
    }


    //Filter the JSON files in MyTemplates and MyWorkFlows Folder
    class JSONFilter implements FilenameFilter {
        @Override
        public boolean accept(File dir, String name) {
            return name.endsWith(".json");
        }
    }


}

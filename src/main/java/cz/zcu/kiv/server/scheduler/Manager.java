package cz.zcu.kiv.server.scheduler;

import cz.zcu.kiv.server.sqlite.Jobs;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONObject;

import java.sql.SQLException;
import java.util.List;

public class Manager {
    private static Log logger = LogFactory.getLog(Manager.class);
    private static Manager manager;
    private static final int MAX_THREADS = 2;
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



}

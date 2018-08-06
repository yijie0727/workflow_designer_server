package cz.zcu.kiv.server.scheduler;

import cz.zcu.kiv.server.EmbeddedServer;
import cz.zcu.kiv.server.sqlite.Jobs;
import cz.zcu.kiv.server.sqlite.UserDoesNotExistException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONObject;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class Manager extends Thread{
    private static Log logger = LogFactory.getLog(Manager.class);
    private static boolean running = false;
    private static Manager manager;

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
            manager.start();
        }

        return manager;
    }

    public long addJob(Job job) throws SQLException {
        job = Jobs.addJob(job);
        if(!running){
            manager=new Manager();
            manager.start();
        }
        return job.getId();
    }

    @Override
    public void run() {

            running=true;
            System.out.println("Starting thread");
            try {

                List<Job> pendingJobs = Jobs.getJobsByStatus(Status.WAITING.name());
                while(!pendingJobs.isEmpty()) {

                    for (Job job : pendingJobs) {
                        logger.info("Starting Job " + job.getId());
                        job.execute();
                        logger.info("Job " + job.getId() + " Completed");
                    }
                    pendingJobs = Jobs.getJobsByStatus(Status.WAITING.name());
                }
            } catch (SQLException e) {
                logger.fatal(e);
            }
            running=false;
            System.out.println("Ending thread");


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

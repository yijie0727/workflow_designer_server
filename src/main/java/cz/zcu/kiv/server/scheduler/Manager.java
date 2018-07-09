package cz.zcu.kiv.server.scheduler;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class Manager extends Thread{
    private static Log logger = LogFactory.getLog(Manager.class);

    public static long newJobId=1;
    public static List<Job> jobs=new ArrayList();

    public static long addJob(Job job){
        long jobId=newJobId++;
        job.setId(jobId);
        jobs.add(job);
        return jobId;
    }

    @Override
    public void run() {
        while(true){
            Job[] pendingJobs=getPendingJobs();
            for(Job job:pendingJobs){
                logger.info("Starting Job "+job.getId());
                job.execute();
                logger.info("Job "+job.getId()+" Completed");
            }
        }
    }

    public static JSONArray getJobs(){
        JSONArray jsonArray=new JSONArray();
        for(Job job:jobs) {
            JSONObject jobObject=job.toJSON(false);
            jsonArray.put(jobObject);
        }
        return jsonArray;
    }

    public static JSONObject getJob(long jobId){
        for(Job job:jobs) {
            if(job.getId()==jobId)
                return job.toJSON(true);
        }
        return null;
    }

    private static Job[] getPendingJobs(){
        List<Job>pending=new ArrayList<>();
        for(int i=0;i<jobs.size();i++){
            Job job = jobs.get(i);
            if(job.getStatus()==Status.WAITING)
                pending.add(job);
        }
        Job[]jobs=new Job[pending.size()];
        return pending.toArray(jobs);
    }

}

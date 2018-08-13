package cz.zcu.kiv.server.scheduler;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.sql.SQLException;

public class JobThread extends Thread {

    Log logger = LogFactory.getLog(JobThread.class);
    Job job;

    public JobThread(Job job){
        this.job=job;
    }
    @Override
    public void run(){
        try {
            logger.info("Starting Job " + job.getId());
            job.execute();

        } catch (SQLException e) {
            logger.error(e);
        }
        onJobCompleted();
    }

    public void onJobCompleted(){
        logger.info("Job " + job.getId() + " Completed");
    }
}

package cz.zcu.kiv.server.scheduler;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.*;

import static cz.zcu.kiv.server.Workflow.*;

public class Job {
    private static Log logger = LogFactory.getLog(Job.class);

    private long id;
    private JSONObject workflow;
    private Date startTime;
    private Date endTime;
    private Status status;

    public Job(JSONObject workflowObject) {
        this.workflow=workflowObject;
        this.startTime=new Date();
        this.status=Status.WAITING;

    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public JSONObject getWorkflow() {
        return workflow;
    }

    public void setWorkflow(JSONObject workflow) {
        this.workflow = workflow;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public void execute(){
        setStatus(Status.RUNNING);
        Set<String> modules=new HashSet<>();
        try {

            JSONArray blocksArray = workflow.getJSONArray("blocks");
            for(int i=0;i<blocksArray.length();i++){
                modules.add(blocksArray.getJSONObject(i).getString("module"));
            }

        }
        catch (JSONException e){
            logger.error("Error parsing workflow JSON",e);
            setStatus(Status.FAILED);
            return;
        }

        ClassLoader child;
        Map<Class,String> moduleSource = new HashMap<>();

        try {
            child = initializeClassLoader(modules,moduleSource);
        } catch (IOException e) {
            logger.error("Cannot read jar from server",e);
            setStatus(Status.FAILED);
            return;
        }

        JSONArray result = null;

        try{
            result=executeJar(child,workflow,moduleSource);
        }
        catch(Exception e1){
            logger.error("Executing jar failed",e1);
            setStatus(Status.FAILED);
            return;
        }
        if(result!=null){
            setStatus(Status.COMPLETED);
        }
        else{
            logger.error("No result was generated");
            setStatus(Status.FAILED);
        }
        setEndTime(new Date());
    }

    public JSONObject toJSON() {
        JSONObject jsonObject=new JSONObject();
        jsonObject.put("id",getId());
        jsonObject.put("startTime",getStartTime().getTime());
        jsonObject.put("endTime",getEndTime()!=null?getEndTime().getTime():"");
        jsonObject.put("status",getStatus().name());
        return jsonObject;
    }
}

package cz.zcu.kiv.server.sqlite;


import cz.zcu.kiv.server.scheduler.Job;
import cz.zcu.kiv.server.scheduler.Status;
import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONObject;

import java.io.File;
import java.sql.*;
import java.util.ArrayList;

public class Jobs {
   public static Log logger= LogFactory.getLog(Jobs.class);


    public static Job addJob(Job job) throws SQLException {

        Connection connection = null;
        PreparedStatement preparedStatement = null;
        try {
            connection = SQLiteDB.getInstance().connect();
            preparedStatement =
                    connection.prepareStatement(
                            "INSERT into jobs (owner, status, startTime, endTime, workflow, workflowOutputFile) " +
                                    "VALUES (?,?,?,?,?,?);",
                            Statement.RETURN_GENERATED_KEYS);

            preparedStatement.setString(1, job.getOwner());
            preparedStatement.setString(2,job.getStatus().name());
            if(job.getStartTime()!=null)
                preparedStatement.setTimestamp(3,new Timestamp(job.getStartTime().getTime()));
            else
                preparedStatement.setString(3,"");
            if(job.getEndTime()!=null)
                preparedStatement.setTimestamp(4,new Timestamp(job.getEndTime().getTime()));
            else
                preparedStatement.setString(4,"");
            preparedStatement.setString(5,job.getWorkflow().toString());
            preparedStatement.setString(6,job.getWorkflowOutputFile());

            preparedStatement.executeUpdate();

            ResultSet tableKeys = preparedStatement.getGeneratedKeys();
            tableKeys.next();
            long autoGeneratedID = tableKeys.getLong(1);
            job.setId(autoGeneratedID);
            return job;
        } finally {
            if (preparedStatement != null) {
                try {
                    preparedStatement.close();
                } catch (SQLException e1) {
                    logger.error(e1);
                }
            }
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e1) {
                    logger.error(e1);
                }
            }

        }
    }

    public static ArrayList<Job> getJobsByEmail(String email) throws SQLException{
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        try {
            connection = SQLiteDB.getInstance().connect();
            preparedStatement =
                    connection.prepareStatement("SELECT * FROM jobs WHERE owner=? order by startTime DESC;" );

            preparedStatement.setString(1, email);

            ResultSet resultSet = preparedStatement.executeQuery();
            ArrayList<Job>jobs=new ArrayList<>();
            while(resultSet.next()){
                Job job=new Job();
                job.setId(resultSet.getLong("id"));
                job.setOwner(resultSet.getString("owner"));
                job.setStatus(Status.valueOf(resultSet.getString("status")));
                if(resultSet.getString("startTime")!=null)
                    job.setStartTime(new Date(resultSet.getLong("startTime")));
                if(resultSet.getString("endTime")!=null)
                    job.setEndTime(new Date(resultSet.getLong("endTime")));
                job.setWorkflow(new JSONObject(resultSet.getString("workflow")));
                job.setWorkflowOutputFile(resultSet.getString("workflowOutputFile"));

                jobs.add(job);
            }
            return jobs;

        }
        finally {
            if(preparedStatement!=null) {
                try {
                    preparedStatement.close();
                } catch (SQLException e1) {
                    logger.error(e1);
                }
            }
            if(connection!=null){
                try {
                    connection.close();
                } catch (SQLException e1) {
                    logger.error(e1);
                }
            }

        }
    }

    public static ArrayList<Job> getJobsByStatus(String status) throws SQLException{
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        try {
            connection = SQLiteDB.getInstance().connect();
            preparedStatement =
                    connection.prepareStatement("SELECT * FROM jobs WHERE status=?;" );

            preparedStatement.setString(1, status);

            ResultSet resultSet = preparedStatement.executeQuery();
            ArrayList<Job>jobs=new ArrayList<>();
            while(resultSet.next()){
                Job job=new Job();
                job.setId(resultSet.getLong("id"));
                job.setOwner(resultSet.getString("owner"));
                job.setStatus(Status.valueOf(resultSet.getString("status")));
                if(resultSet.getLong("startTime")!=0)
                    job.setStartTime(new Date(resultSet.getLong("startTime")));
                if(resultSet.getLong("endTime")!=0)
                    job.setEndTime(new Date(resultSet.getLong("endTime")));
                job.setWorkflow(new JSONObject(resultSet.getString("workflow")));
                job.setWorkflowOutputFile(resultSet.getString("workflowOutputFile"));

                jobs.add(job);
            }
            return jobs;

        }
        finally {
            if(preparedStatement!=null) {
                try {
                    preparedStatement.close();
                } catch (SQLException e1) {
                    logger.error(e1);
                }
            }
            if(connection!=null){
                try {
                    connection.close();
                } catch (SQLException e1) {
                    logger.error(e1);
                }
            }

        }
    }

    public static Job getJob(Long id) throws SQLException{
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        try {
            connection = SQLiteDB.getInstance().connect();
            preparedStatement =
                    connection.prepareStatement("SELECT * FROM jobs WHERE id=?;" );

            preparedStatement.setLong(1, id);

            ResultSet resultSet = preparedStatement.executeQuery();
            ArrayList<Job>jobs=new ArrayList<>();

            if(resultSet.next()){
                Job job=new Job();
                job.setId(resultSet.getLong("id"));
                job.setOwner(resultSet.getString("owner"));
                job.setStatus(Status.valueOf(resultSet.getString("status")));
                if(resultSet.getLong("startTime")!=0)
                    job.setStartTime(new Date(resultSet.getLong("startTime")));
                if(resultSet.getLong("endTime")!=0)
                    job.setEndTime(new Date(resultSet.getLong("endTime")));
                job.setWorkflow(new JSONObject(resultSet.getString("workflow")));
                job.setWorkflowOutputFile(resultSet.getString("workflowOutputFile"));
                return job;
            }
            throw new SQLException("Job id not found");

        }
        finally {
            if(preparedStatement!=null) {
                try {
                    preparedStatement.close();
                } catch (SQLException e1) {
                    logger.error(e1);
                }
            }
            if(connection!=null){
                try {
                    connection.close();
                } catch (SQLException e1) {
                    logger.error(e1);
                }
            }

        }
    }

    public static Job updateJob(Job job) throws SQLException {

        Connection connection = null;
        PreparedStatement preparedStatement = null;
        try {
            connection = SQLiteDB.getInstance().connect();
            preparedStatement =
                    connection.prepareStatement(
                            "UPDATE jobs set owner=?, status=?, startTime=?, endTime=?, workflow=?, workflowOutputFile=? " +
                                    " WHERE id=?",
                            Statement.RETURN_GENERATED_KEYS);

            preparedStatement.setString(1, job.getOwner());
            preparedStatement.setString(2,job.getStatus().name());
            if(job.getStartTime()!=null)
                preparedStatement.setTimestamp(3,new Timestamp(job.getStartTime().getTime()));
            else
                preparedStatement.setString(3,null);
            if(job.getEndTime()!=null)
                preparedStatement.setTimestamp(4,new Timestamp(job.getEndTime().getTime()));
            else
                preparedStatement.setString(4,null);
            preparedStatement.setString(5,job.getWorkflow().toString());
            preparedStatement.setString(6,job.getWorkflowOutputFile());

            preparedStatement.setLong(7,job.getId());


            preparedStatement.executeUpdate();
            return job;
        } finally {
            if (preparedStatement != null) {
                try {
                    preparedStatement.close();
                } catch (SQLException e1) {
                    logger.error(e1);
                }
            }
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e1) {
                    logger.error(e1);
                }
            }

        }
    }


    public static void terminateRunningJobs() throws SQLException {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        try {
            connection = SQLiteDB.getInstance().connect();
            preparedStatement =
                    connection.prepareStatement("UPDATE jobs set status='FAILED' WHERE status='RUNNING' OR status='WAITING';" );

            preparedStatement.executeUpdate();
        }
        finally {
            if(preparedStatement!=null) {
                try {
                    preparedStatement.close();
                } catch (SQLException e1) {
                    logger.error(e1);
                }
            }
            if(connection!=null){
                try {
                    connection.close();
                } catch (SQLException e1) {
                    logger.error(e1);
                }
            }

        }
    }

    public static void clearJobs(String email) throws SQLException {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        String query = "FROM jobs WHERE owner=? AND status!='RUNNING' AND status !='WAITING'";

        try {
            connection = SQLiteDB.getInstance().connect();
            preparedStatement =
                    connection.prepareStatement("SELECT workflowOutputFile " + query);

            preparedStatement.setString(1, email);

            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                String file = resultSet.getString("workflowOutputFile");
                if(file != null) {
                    FileUtils.deleteQuietly(new File(file));
                }
            }

        } finally {
            if(preparedStatement != null) {
                try {
                    preparedStatement.close();
                } catch (Exception e) {
                    logger.error(e);
                }

            }
        }

        try {
            connection = SQLiteDB.getInstance().connect();
            preparedStatement =
                    connection.prepareStatement("DELETE " + query);

            preparedStatement.setString(1, email);

            preparedStatement.executeUpdate();

        }
        finally {
            if(preparedStatement!=null) {
                try {
                    preparedStatement.close();
                } catch (SQLException e1) {
                    logger.error(e1);
                }
            }
            if(connection!=null){
                try {
                    connection.close();
                } catch (SQLException e1) {
                    logger.error(e1);
                }
            }

        }
    }
}

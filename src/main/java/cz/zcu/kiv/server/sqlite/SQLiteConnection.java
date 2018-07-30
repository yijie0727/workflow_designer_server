package cz.zcu.kiv.server.sqlite;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class SQLiteConnection {
    private static Log logger = LogFactory.getLog(SQLiteConnection.class);
    private static boolean initialized = false;

    private String database;
    public SQLiteConnection(String database){
        this.database=database;
    }
    /**
     * Connect to a database
     */
    Connection connect() {
        Connection conn = null;
        Statement stmt = null;
        try {

            String url = "jdbc:sqlite:"+database;
            // create a connection to the database
            conn = DriverManager.getConnection(url);
            if(!initialized){
                String createUsersTableSQL = "CREATE TABLE IF NOT EXISTS users ("
                        + "	id integer PRIMARY KEY AUTOINCREMENT,"
                        + "	email text NOT NULL,"
                        + "	password text NOT NULL,"
                        + "	username text NOT NULL"
                        + ");";

                String createModulesTableSQL = "CREATE TABLE IF NOT EXISTS modules ("
                        + "	id integer PRIMARY KEY AUTOINCREMENT,"
                        + "	jarName text NOT NULL,"
                        + "	packageName text NOT NULL,"
                        + "	publicJar boolean NOT NULL,"
                        + "	author text NOT NULL,"
                        + "	lastUpdate text NOT NULL"
                        + ");";

                stmt = conn.createStatement();
                // create a new table
                stmt.execute(createUsersTableSQL);
                stmt.execute(createModulesTableSQL);

                stmt.close();
                initialized=true;
            }

            return conn;


        } catch (SQLException e) {
            logger.error(e);
        } finally {
            try {
                if(stmt !=null) {
                    stmt.close();
                }
            } catch (SQLException ex) {
                logger.error(ex);
            }
        }
        return null;
    }
}

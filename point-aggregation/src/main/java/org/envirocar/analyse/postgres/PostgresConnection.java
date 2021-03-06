/**
 * Copyright 2016 52°North Initiative for Geospatial Open Source
 * Software GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
 /**
  * Copyright 2016 52°North Initiative for Geospatial Open Source
  * Software GmbH
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *    http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
package org.envirocar.analyse.postgres;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import org.envirocar.analyse.properties.GlobalProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PostgresConnection {
    
    private static final Logger logger = LoggerFactory.getLogger(PostgresConnection.class);
    
    private String connectionURL = null;
    private String databaseName;
    private String databasePath;
    
    private String username;
    private String password;
    
    private Connection connection;
    
    public PostgresConnection(String databaseName) {
        this.databaseName = databaseName;
        try {
            createConnection();
        } catch (ClassNotFoundException e) {
            logger.warn(e.getMessage(), e);
        }
    }
    
    private boolean createConnection() throws ClassNotFoundException {
        Class.forName("org.postgresql.Driver");
        connectionURL = "jdbc:postgresql:" + getDatabasePath() + "/"
                + getDatabaseName();
        
        java.util.Properties props = new java.util.Properties();
        
        props.setProperty("create", "true");
        props.setProperty("user", getDatabaseUsername());
        props.setProperty("password", getDatabasePassword());
        this.connection = null;
        try {
            connection = DriverManager.getConnection(connectionURL, props);
            connection.setAutoCommit(false);
            logger.info("Connected to measurement database.");
        } catch (SQLException e) {
            logger.error("Could not connect to or create the database.", e);
            return false;
        }
        
        return true;
    }
    
    private String getDatabaseName() {
        return databaseName;
    }
    
    private String getDatabasePath() {
        
        if(databasePath == null || databasePath.equals("")){
            databasePath = GlobalProperties.getProperty("databasePath").toString();
        }
        
        return databasePath;
    }
    
    private String getDatabaseUsername() {
        
        if(username == null || username.equals("")){
            username = GlobalProperties.getProperty("username").toString();
        }
        
        return username;
    }
    
    private String getDatabasePassword() {
        
        if(password == null || password.equals("")){
            this.password = GlobalProperties.getProperty("password").toString();
        }
        
        return password;
    }
    
    public boolean executeStatement(String statement) {
        Statement st = null;
        try {
            st = connection.createStatement();
            st.execute(statement);
            
        } catch (SQLException e) {
            logger.error("Execution of the following statement failed: "
                    + statement + " cause: " + e.getMessage());
            return false;
        } finally {
            try {
                connection.commit();
            } catch (SQLException e) {
                logger.warn(e.getMessage(), e);
            }
        }
        return true;
    }
    
    public ResultSet executeQueryStatement(String statement) {
        Statement st = null;
        try {
            st = connection.createStatement();
            ResultSet resultSet = st.executeQuery(statement);
            
            return resultSet;
            
        } catch (SQLException e) {
            logger.error("Execution of the following statement failed: "
                    + statement + " cause: " + e.getMessage());
            return null;
        } finally {
            try {
                connection.commit();
            } catch (SQLException e) {
                logger.warn(e.getMessage(), e);
            }
        }
    }
    
    public boolean executeUpdateStatement(String statement) {
        Statement st = null;
        try {
            st = connection.createStatement();
            st.executeUpdate(statement);
            
        } catch (SQLException e) {
            logger.error("Execution of the following statement failed: "
                    + statement + " cause: " + e.getMessage());
            return false;
        } finally {
            try {
                connection.commit();
                st.close();
            } catch (SQLException e) {
                logger.warn(e.getMessage(), e);
            }
        }
        return true;
    }
    
    public DatabaseMetaData getDatabasMetaData() throws SQLException {
        return this.connection.getMetaData();
    }
    
    public PreparedStatement createPreparedStatement(String statement,
            int autoGeneratedKeys, List<Object> values) throws SQLException {
        PreparedStatement result = this.connection.prepareStatement(statement, autoGeneratedKeys);
        
        int index = 1;
        for (Object object : values) {
            if (object instanceof String) {
                result.setString(index++, object.toString());
            }
            else if (object instanceof Integer) {
                result.setInt(index++, (int) object);
            }
            else if (object instanceof Double) {
                result.setDouble(index++, (double) object);
            }
        }
        
        return result;
    }
    
    public void shutdown() throws SQLException {
        this.connection.close();
    }
    
    
}

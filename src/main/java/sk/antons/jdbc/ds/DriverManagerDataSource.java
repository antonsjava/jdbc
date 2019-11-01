/*
 * Copyright 2019 Anton Straka
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package sk.antons.jdbc.ds;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.logging.Logger;
import javax.sql.DataSource;

/**
 * Simple DataSource implementation. It uses DriverManager 
 * to create new connections. It makes no pooling just create 
 * new connection every time when getConnection is called.
 * 
 * Only getConnection methods are implemented. Rest contains only 
 * dummy implementation.
 * @author antons
 */
public class DriverManagerDataSource implements DataSource {
    
    private String driverClassName;
    private String url;
    private String user;
    private String password;

    /**
     * New DataSource instance 
     * @param driverClassName fqdn class name of driver
     * @param url jdbc url for connection creation
     * @param user database user name
     * @param password database user password
     */
    public DriverManagerDataSource(String driverClassName, String url, String user, String password) {
        this.driverClassName = driverClassName;
        this.url = url;
        this.user = user;
        this.password = password;
    }
 

    /**
     * Factory method for new instancies
     * @param driverClassName
     * @param url
     * @param user
     * @param password
     * @return 
     */
    public static DriverManagerDataSource instance(String driverClassName, String url, String user, String password) {
        DriverManagerDataSource ds = new DriverManagerDataSource(driverClassName, url, user, password); 
        return ds;
    }

    private static boolean driverRegistered = false;
    private static void registerDriver(String clazz) throws SQLException {
        if(driverRegistered) return;
        try {
            if(clazz != null) Class.forName(clazz);
        } catch(Exception e) {
            throw new SQLException("Unable to register driver " + clazz);
        }
    }

    @Override
    public Connection getConnection() throws SQLException {
        registerDriver(driverClassName);
        Connection conn = DriverManager.getConnection(url, user, password);
        conn.setAutoCommit(false);
        return conn;
    }

    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        registerDriver(driverClassName);
        Connection conn = DriverManager.getConnection(url, username, password);
        conn.setAutoCommit(false);
        return conn;
    }

    @Override
    public PrintWriter getLogWriter() throws SQLException {
        return null;
    }

    @Override
    public void setLogWriter(PrintWriter out) throws SQLException {
    }

    @Override
    public void setLoginTimeout(int seconds) throws SQLException {
    }

    @Override
    public int getLoginTimeout() throws SQLException {
        return 0;
    }

    @Override
    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        return null;
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return false;
    }
    



}

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
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.logging.Logger;
import javax.sql.DataSource;
import sk.antons.jdbc.log.LogConfig;
import sk.antons.jdbc.log.invocation.JdbcWrapper;

/**
 * Simple wrapper for DataSource instancies. It provides 
 * simplified functionality for logging jdbc statements.
 * 
 * @author antons
 */
public class LogDataSource implements DataSource {
    
    private DataSource ds;
    private LogConfig config;

    /**
     * New instance of DataSource wrapper
     * @param ds DataSource to be wrapped
     * @param config configuration of logging
     */
    public LogDataSource(DataSource ds, LogConfig config) {
        this.ds = ds;
        this.config = config;
    }
    
    /**
     * Factory method for wrapped DataSource.
     * 
     * @param ds DataSource to be wrapped
     * @param config log configuration
     * @return wrapped DataSource
     */
    public static LogDataSource wrap(DataSource ds, LogConfig config) {
        return new LogDataSource(ds, config);
    }

    @Override
    public Connection getConnection() throws SQLException {
        Connection conn = ds.getConnection();
        return JdbcWrapper.wrap(conn, config);
    }

    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        Connection conn = ds.getConnection(username, password);
        return JdbcWrapper.wrap(conn, config);
    }

    @Override
    public PrintWriter getLogWriter() throws SQLException {
        return ds.getLogWriter();
    }

    @Override
    public void setLogWriter(PrintWriter out) throws SQLException {
        ds.setLogWriter(out);
    }

    @Override
    public void setLoginTimeout(int seconds) throws SQLException {
        ds.setLoginTimeout(seconds);
    }

    @Override
    public int getLoginTimeout() throws SQLException {
        return ds.getLoginTimeout();
    }

    @Override
    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        return ds.getParentLogger();
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

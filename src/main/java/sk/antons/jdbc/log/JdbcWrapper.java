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
package sk.antons.jdbc.log;

import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import sk.antons.jdbc.log.LogConfig;
import sk.antons.jdbc.log.invocation.ConnectionHandler;
import sk.antons.jdbc.log.invocation.PreparedStatementHandler;
import sk.antons.jdbc.log.invocation.ResultSetHandler;
import sk.antons.jdbc.log.invocation.StatementHandler;

/**
 * Simple factory for wrapping jdbc instancies.
 * @author antons
 */
public class JdbcWrapper {
    
    /**
     * Wraps ResultSet instance with logging capabilities.
     * @param rs instance to be wrapped
     * @param config log configuration
     * @param connidentity identity of connection
     * @param stmidentity identity of statement
     * @return wrapped instance
     */
    public static ResultSet wrap(ResultSet rs, LogConfig config, int connidentity, int stmidentity) {
        ResultSet proxy = (ResultSet) Proxy.newProxyInstance(JdbcWrapper.class.getClassLoader(), 
            new Class[] { ResultSet.class }, 
            new ResultSetHandler(rs, config, connidentity, stmidentity));
        return proxy;
    }
    
    /**
     * Wraps Statement instance with logging capabilities.
     * @param stm instance to be wrapped
     * @param config log configuration
     * @param connidentity identity of connection
     * @return wrapped instance
     */
    public static Statement wrap(Statement stm, LogConfig config, int connidentity) {
        Statement proxy = (Statement) Proxy.newProxyInstance(JdbcWrapper.class.getClassLoader(), 
            new Class[] { Statement.class }, 
            new StatementHandler(stm, config, connidentity));
        return proxy;
    }
    
    /**
     * Wraps PreparedStatement instance with logging capabilities.
     * @param stm instance to be wrapped
     * @param statement text of prepared statement
     * @param config log configuration
     * @param connidentity identity of connection
     * @return wrapped instance
     */
    public static PreparedStatement wrap(PreparedStatement stm, String statement, LogConfig config, int connidentity) {
        PreparedStatement proxy = (PreparedStatement) Proxy.newProxyInstance(JdbcWrapper.class.getClassLoader(), 
            new Class[] { PreparedStatement.class }, 
            new PreparedStatementHandler(stm, statement, config, connidentity));
        return proxy;
    }

    /**
     * Wraps Connection instance with logging capabilities.
     * @param conn instance to be wrapped
     * @param config log configuration
     * @return wrapped instance
     */
    public static Connection wrap(Connection conn, LogConfig config) {
        Connection proxy = (Connection) Proxy.newProxyInstance(JdbcWrapper.class.getClassLoader(), 
            new Class[] { Connection.class }, 
            new ConnectionHandler(conn, config));
        return proxy;
    }
}

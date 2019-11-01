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
package sk.antons.jdbc.log.invocation;

import sk.antons.jdbc.log.JdbcWrapper;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;
import sk.antons.jdbc.log.LogConfig;

/**
 * Implementation class
 * @author antons
 */
public class ConnectionHandler extends CommonHandler {

    Connection conn = null;
    
    public ConnectionHandler(Connection conn, LogConfig config) {
        super(conn, config, Identities.nextConnection(), 0);
        this.conn = conn;
    }

    public static ConnectionHandler instance(Connection conn, LogConfig config) {
        return new ConnectionHandler(conn, config);
    }
    

    @Override
    protected void preInvoke(Method method, Object[] args) {
    }
    
    @Override
    protected Object postInvoke(Method method, Object[] args, Object o) {
        if(config.consumerStatus().isConsumerOn()) {
            String name = method.getName();
            if("createStatement".equals(name)) {
                if(config.logStatement()) {
                    o = JdbcWrapper.wrap((Statement)o, config, connidentity);
                }
            } else if("prepareStatement".equals(name)) {
                if(config.logStatement()) {
                    o = JdbcWrapper.wrap((PreparedStatement)o, (String)args[0], config, connidentity);
                }
            } else if("commit".equals(name) || "rollback".equals(name)){
                StringBuilder sb = new StringBuilder();
                consumerPrefix(sb);
                sb.append(" ").append(name);
                consumerPostfix(sb);
                config.consumer().consume(sb.toString());
            }
        }
        return o;
    }
    
    
}

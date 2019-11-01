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
import java.sql.ResultSet;
import java.sql.Statement;
import sk.antons.jdbc.log.LogConfig;

/**
 * Implementation class
 * @author antons
 */
public class StatementHandler extends CommonHandler {

    Statement stm = null;
    
    public StatementHandler(Statement stm, LogConfig config, int connidentity) {
        super(stm, config, connidentity, Identities.nextStatement());
        this.stm = stm;
    }

    public static StatementHandler instance(Statement stm, LogConfig config, int connidentity) {
        return new StatementHandler(stm, config, connidentity);
    }
    

    @Override
    protected void preInvoke(Method method, Object[] args) {
    }
    
    @Override
    protected Object postInvoke(Method method, Object[] args, Object o) {
        if(config.consumerStatus().isConsumerOn()) {
            String name = method.getName();
            if("executeQuery".equals(name)) {
                StringBuilder sb = new StringBuilder();
                consumerPrefix(sb);
                sb.append(" statement: ").append(args[0]);
                consumerPostfix(sb);
                config.consumer().consume(sb.toString());
                if(config.logResult() && (o != null)) {
                    o = JdbcWrapper.wrap((ResultSet)o, config, connidentity, stmidentity);
                }
            } else if("executeUpdate".equals(name)){
                StringBuilder sb = new StringBuilder();
                consumerPrefix(sb);
                sb.append(" statement: ").append(args[0]);
                sb.append(" result: ").append(o);
                consumerPostfix(sb);
                config.consumer().consume(sb.toString());
            }
        }
        return o;
    }
    
    
}

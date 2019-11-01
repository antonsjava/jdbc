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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import sk.antons.jdbc.log.LogConfig;

/**
 * Implementation class
 * @author antons
 */
public class PreparedStatementHandler extends CommonHandler {

    PreparedStatement stm = null;
    String statement = null;
    Map<Integer, String> params = new HashMap<>();
    
    public PreparedStatementHandler(PreparedStatement stm, String statement, LogConfig config, int connidentity) {
        super(stm, config, connidentity, Identities.nextStatement());
        this.stm = stm;
        this.statement = statement;
    }

    public static PreparedStatementHandler instance(PreparedStatement stm, String statement, LogConfig config, int connidentity) {
        return new PreparedStatementHandler(stm, statement, config, connidentity);
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
                sb.append(" statement: ").append(statement);
                sb.append(" params: ").append(params());
                consumerPostfix(sb);
                config.consumer().consume(sb.toString());
                if(config.logResult() && (o != null)) {
                    o = JdbcWrapper.wrap((ResultSet)o, config, connidentity, stmidentity);
                }
                params.clear();
            } else if("executeUpdate".equals(name)){
                StringBuilder sb = new StringBuilder();
                consumerPrefix(sb);
                sb.append(" statement: ").append(statement);
                sb.append(" params: ").append(params());
                sb.append(" result: ").append(o);
                consumerPostfix(sb);
                config.consumer().consume(sb.toString());
                params.clear();
            } else if(name.startsWith("set")){
                addParam(method, args);
            }
        }
        return o;
    }

    private void addParam(Method method, Object[] args) {
        if(method.getParameterTypes().length < 2) return;
        if(method.getParameterTypes().length > 3) return;
        Class c = method.getParameterTypes()[0];
        if(! int.class.isAssignableFrom(c)) return;
        Object value = args[1];
        String svalue = "" + value;;
        int key = (int)args[0];
        params.put(key, svalue);
    }

    private String params() {
        List<Integer> keys = new ArrayList<>();
        keys.addAll(params.keySet());
        Collections.sort(keys);
        StringBuilder sb = new StringBuilder();
        for(Integer key : keys) {
            String value = params.get(key);
            sb.append(" ?").append(key).append(':').append(value);
        }
        return sb.toString();
    }
    
    
}

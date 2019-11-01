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

import java.lang.reflect.Method;
import java.sql.ResultSet;
import sk.antons.jdbc.log.LogConfig;

/**
 * Implementation class
 * @author antons
 */
public class ResultSetHandler extends CommonHandler {

    ResultSet rs = null;

    private long nextstartTime;
    private long nextfirstTime;
    private long nextlastTime;
    private int count = -1;
    
    public ResultSetHandler(ResultSet rs, LogConfig config, int connidentity, int stmidentity) {
        super(rs, config, connidentity, stmidentity);
        this.rs = rs;
    }

    public static ResultSetHandler instance(ResultSet rs, LogConfig config, int connidentity, int stmidentity) {
        return new ResultSetHandler(rs, config, connidentity, stmidentity);
    }
    

    @Override
    protected void preInvoke(Method method, Object[] args) {
        if(config.consumerStatus().isConsumerOn()) {
            if("next".equals(method.getName())) {
                if(nextstartTime == 0) nextstartTime = System.currentTimeMillis();
            }
        }
    }
    
    @Override
    protected Object postInvoke(Method method, Object[] args, Object o) {
        if(config.consumerStatus().isConsumerOn()) {
            String name = method.getName();
            if("next".equals(name)) {
                if(nextfirstTime == 0) nextfirstTime = System.currentTimeMillis();
                count++;
            } else if("close".equals(name)){
                nextlastTime = System.currentTimeMillis();
                StringBuilder sb = new StringBuilder();
                consumerPrefix(sb);
                sb.append(" resultset row count: ").append(count);
                sb.append(" first row time: ").append(nextfirstTime-nextstartTime);
                sb.append(" all rows time: ").append(nextlastTime-nextstartTime);
                sb.append(" close");
                consumerPostfix(sb);
                config.consumer().consume(sb.toString());
            }
        }
        return o;
    }
    
    
}

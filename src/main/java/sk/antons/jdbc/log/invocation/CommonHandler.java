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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import sk.antons.jdbc.log.LogConfig;

/**
 * Implementation class
 * @author antons
 */
public abstract class CommonHandler implements InvocationHandler {

    protected Object wrapped = null;
    protected LogConfig config = null;
    protected int connidentity;
    protected int stmidentity;
    protected long invocationTime;
    protected Throwable error;
    
    public CommonHandler(Object wrapped, LogConfig config
            , int connidentity, int stmidentity) {
        this.wrapped = wrapped;
        this.config = config;
        this.connidentity = connidentity;
        this.stmidentity = stmidentity;
    }
    
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        preInvoke(method, args);
        Object o = null;
        error = null;
        long starttime = System.currentTimeMillis();
        try {
            o = method.invoke(wrapped, args);
        } catch(Throwable e) {
            error = e;
        }
        long endtime = System.currentTimeMillis();
        invocationTime = endtime - starttime;
        o = postInvoke(method, args, o);
        if(error != null) throw error;
        return o;
    }

    protected abstract void preInvoke(Method method, Object[] args);
    protected abstract Object postInvoke(Method method, Object[] args, Object o);

    protected void consumerPrefix(StringBuilder sb) {
        sb.append("jdbc");
        if((connidentity > 0) && (stmidentity > 0)) sb.append(" [").append(connidentity).append("][").append(stmidentity).append(']');
        else if(connidentity > 0) sb.append(" [").append(connidentity).append(']');
    }
    protected void consumerPostfix(StringBuilder sb) {
        sb.append(" time: ").append(invocationTime);
        if(error != null) sb.append(" error: ").append(toError(error));
    }

    protected boolean isFail() { return error != null; }
    
    protected String toError(Throwable t) {
        if(t == null) return "";
        while(t instanceof InvocationTargetException) {
            t = t.getCause();
            if(t == null) break;
        }
        if(t == null) return "InvocationTargetException";
        return t.toString();
    }
}

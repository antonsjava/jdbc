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


/**
 * Log configuration. Cumulates all settings to be used 
 * for logging jdbc statements.
 * @author antons
 */
public class LogConfig {

    private Consumer consumer;
    private ConsumerStatus consumerStatus;

    private boolean logStatement = true;
    private boolean logResultSet = true;
    private boolean logTransaction = true;
    
    /**
     * New instance 
     * @param consumerStatus function to check logging on/off status
     * @param consumer function to consume log messages
     */
    public LogConfig(ConsumerStatus consumerStatus, Consumer consumer) {
        this.consumer = consumer;
        this.consumerStatus = consumerStatus;
    }

    /**
     * Factory method for new instances
     * @param consumerStatus
     * @param consumer
     * @return 
     */
    public static LogConfig instance(ConsumerStatus consumerStatus, Consumer consumer) {
        return new LogConfig(consumerStatus, consumer);
    }

    /**
     * true if statements should be logged. (default true)
     * @param log
     * @return this
     */
    public LogConfig statement(boolean log) {
        logStatement = log;
        return this;
    }

    /**
     * true if resultset info should be logged. (default true)
     * @param log
     * @return this
     */
    public LogConfig resultSet(boolean log) {
        logResultSet = log;
        return this;
    }
    
    /**
     * true if transaction boundaries should be logged. (default true)
     * @param log
     * @return this
     */
    public LogConfig transaction(boolean log) {
        logTransaction = log;
        return this;
    }


    public Consumer consumer() { return consumer; }
    public ConsumerStatus consumerStatus() { return consumerStatus; }
    public boolean logStatement() { return logStatement; }
    public boolean logTransaction() { return logTransaction; }
    public boolean logResult() { return logResultSet; }
}

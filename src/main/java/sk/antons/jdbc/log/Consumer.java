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
 * Generic log message consumer. Jdbc API sends messages to consumer.
 * This mechanism allow to be independent to concrete logging library.
 * Simple implementation is 
 * <pre>
 *   (message) -> { log.debug(message); } 
 * </pre>
 * @author antons
 */
public interface Consumer {

    /**
     * Process log message. (ussually simple log message to logging system of client)
     * @param message message to be logged
     */
    void consume(String message);    
}

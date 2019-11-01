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
 * Generic log message consumer status. Jdbc API checks consumer status by this method.
 * This mechanism allow to be independent to concrete logging library.
 * Simple implementation is 
 * <pre>
 *   () -> { log.iaDebugEnabled(); } 
 * </pre>
 * @author antons
 */
public interface ConsumerStatus {
    
    /**
     * Checks if consumer can process messages.
     * @return true if consumer is ready for processing 
     */
    boolean isConsumerOn();    
}

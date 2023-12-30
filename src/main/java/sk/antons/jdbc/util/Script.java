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
package sk.antons.jdbc.util;

import java.io.FileReader;
import java.io.Reader;
import java.io.StringReader;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Sql script parser. Split script to individual statements.
 *
 * Statements are separated by ';'. And single line comments
 * started by '--' are ignored.
 *
 * @author antons
 */
public class Script {
    Reader reader;

    public Script(Reader reader) {
        this.reader = reader;
        readOne();
    }
    public static Script instance(Reader reader) { return new Script(reader); }
    public static Script instance(String script) { return new Script(new StringReader(script)); }

    private int head = 0;
    private int head1 = 0;

    private void readOne() {
        try {
            if(head == -1) return;
            head = head1;
            head1 = reader.read();
        } catch(Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private String next() {

        boolean incomment = false;
        boolean inliteral = false;
        StringBuilder sb = new StringBuilder();
        while(head != -1) {
            if(inliteral) {
                if(head == '\'') {
                    inliteral = false;
                }
            } else if(incomment) {
                if(head == '\n') {
                    incomment = false;
                } else {
                    readOne();
                    continue;
                }
            } else {
                if(head == ';') {
                    readOne();
                    break;
                } if(head == '\'') {
                    inliteral = true;
                } else if((head == '-') && (head1 == '-')) {
                    incomment = true;
                    continue;
                }
            }
            sb.append((char)head);
            readOne();
        }

        String s = sb.toString().trim();
        if(!s.isEmpty()) return s;
        if(head != -1) return next();
        return null;
    }

    private boolean firstuse = true;
    public Iterator<String> iterator() {
        if(firstuse) firstuse = false;
        else throw new IllegalStateException("Script can be used only once");
        return new Iterator<String>() {
            String n = Script.this.next();
            @Override
            public boolean hasNext() {
                return n != null;
            }
            @Override
            public String next() {
                String rv = n;
                n = Script.this.next();
                return rv;
            }
        };

    }

    public Stream<String> stream() {

        return StreamSupport.stream(
            Spliterators.spliteratorUnknownSize(iterator()
            , Spliterator.IMMUTABLE), false);

    }

    private int commitAfter = 0;
    public Script commitAfter(int value) { this.commitAfter = value; return this; }

    public void execute(final Connection conn) throws SQLException {
        Iterator<String> iter = iterator();
        Statement st = conn.createStatement();
        int counter = 0;
        while(iter.hasNext()) {
            String statement = iter.next();
            st.executeUpdate(statement);
            counter++;
            if((commitAfter>0) && (counter > 0) && ((counter % commitAfter) == 0)) conn.commit();
        }
        conn.commit();

    }



    public static void main(String[] argv) throws Exception {
        Script s = Script.instance("/home/antons/sic.sql;' -- poznamka';a toto --realna poznamka\n;dalsi ");
        String stm = s.next();
        while(stm != null) {
            System.out.println(" --------\n"+stm);
            stm = s.next();
        }
        s = Script.instance(new FileReader("/tmp/init2.sql"));
        s.stream().forEach(m -> System.out.println("--------\n"+m));

    }
}

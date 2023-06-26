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
import java.sql.Connection;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

/**
 * Creates html table from resultset
 * @author antons
 */
public class SqlToHtml {

    private Connection conn;
    private String sql;
    private List<String> header;
    private List<String> fieldsHeader;
    private List<String> fields;
    private int page = 0;
    private int pagelen = 0;
    private StringBuilder sb = new StringBuilder();

    public SqlToHtml(Connection conn) { this.conn = conn; }
    public static SqlToHtml instance(Connection conn) { return new SqlToHtml(conn); }

    public SqlToHtml sql(String value) { this.sql = value; return this; }
    public SqlToHtml sqlHeader(List<String> value) { this.header = value; return this; }
    public SqlToHtml fieldsHeader(List<String> value) { this.fieldsHeader = value; return this; }
    public SqlToHtml fields(List<String> value) { this.fields = value; return this; }
    public SqlToHtml page(int value) { this.page = value; return this; }
    public SqlToHtml pagelen(int value) { this.pagelen = value; return this; }


    public Table build() {
        Table table = new Table();
        //log.debug("sql2html {}, page: {} pagelen: {}", sql, page, pagelen);
        if(sql == null) {
            table.error = "no sql";
            return table;
        }
        sql = clearSql(sql);
        if(pagelen < 1) page = 0;

		if(sql.toLowerCase().trim().startsWith("select")) {
            sb.append("<table class=\"report\">\n");
			ResultSet rs = null;
			Statement stmt = null;
			try {
				stmt = conn.createStatement();
				rs = stmt.executeQuery(sql);
				ResultSetMetaData md = rs.getMetaData();
				int columnCount = md.getColumnCount();
                List<String> labels = new ArrayList<>();
                for(int i = 0; i < columnCount; i++) {
                    String label = md.getColumnLabel(i+1);
                    if(label == null) label = "";
                    labels.add(label.toLowerCase());
                }
                if((header == null) || header.isEmpty()) header = labels;
                rowStart("header");
                if((header != null) && (!header.isEmpty())) for(String string : header) { coll(string); }
                if((fieldsHeader != null) && (!fieldsHeader.isEmpty())) for(String string : fieldsHeader) { coll(string); }
                rowEnd();

				SimpleDateFormat dFromat = new SimpleDateFormat("yyyy.MM.dd");
				SimpleDateFormat tsFromat = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");
                boolean paggig = (page > -1) && (pagelen > 0);
                int startindex = page * pagelen;
                int endindex = startindex + pagelen;
                int index = -1;
                int size = 0;
				while(rs.next()) {
                    index++;
                    if(paggig) {
                        if(index < startindex) continue;
                        if(index >= endindex) break;
                    }
                    size++;

                    Map<String, String> row = new HashMap<>();

                    rowStart();
  						for(int i = 0; i < columnCount; i++) {
							String value = null;
							String name = labels.size() <= i ? null : labels.get(i);
							int type = md.getColumnType(i+1);
							if(type == Types.TIMESTAMP) {
								value = formatDate(tsFromat, rs.getTimestamp(i+1));
							} else if(type == Types.DATE) {
								value = formatDate(dFromat, rs.getDate(i+1));
							} else { value = rs.getString(i+1); }

                            String style = null;
							if(type == Types.TIMESTAMP) {
                                style = "time";
							} else if(type == Types.DATE) {
                                style = "date";
							} else if(isNumberType(type)) {
                                style = "number";
							}
                            coll(value, style);
                            row.put(name, value);

						}
                    if((header != null) && (!header.isEmpty())) {
                        for(String field : fields) {
                            String value = field;
                            if(field != null) {
                                for(Map.Entry<String, String> entry : row.entrySet()) {
                                    String key = entry.getKey();
                                    String va = entry.getValue();
                                    if(va == null) va = "";
                                    value = value.replace("${"+key+"}", va);
                                }
                            }
                            coll(value);
                        }
                    }
                    rowEnd();
				}
                sb.append("</table>\n");
                table.size = size;
                table.html = sb.toString();
				return table;
			} catch(Exception e) {
                table.error = e.toString();
			} finally {
				try {
					if(rs != null) rs.close();
					if(stmt != null) stmt.close();
				} catch(SQLException e) { }
			}
		} else {
            table.error = "no selext present";
        }
        return table;
    }

    private String clearSql(String sql) {
        StringBuilder sb = new StringBuilder();
        StringTokenizer st = new StringTokenizer(sql, "\n\r");
        while(st.hasMoreTokens()) {
            String line = st.nextToken();
            if(line == null) continue;
            line = line.trim();
            if(line.startsWith("--")) continue;
            sb.append(' ').append(line).append(' ');
        }
        return sb.toString().trim();
    }

    private static boolean isNumberType(int type) {
        if(type == Types.BIGINT) return true;
        if(type == Types.DOUBLE) return true;
        if(type == Types.FLOAT) return true;
        if(type == Types.INTEGER) return true;
        if(type == Types.NUMERIC) return true;
        if(type == Types.REAL) return true;
        if(type == Types.SMALLINT) return true;
        if(type == Types.TINYINT) return true;
        return false;
    }

    private static String formatDate(SimpleDateFormat df, Date value) {
        if(value == null) return null;
        if(df == null) return value.toString();
        return df.format(value);
    }

    private static String formatDate(SimpleDateFormat df, Timestamp value) {
        if(value == null) return null;
        if(df == null) return value.toString();
        return df.format(value);
    }

    public static class Table {
        String html;
        String error;
        int size;

        public String getError() { return error; }
        public String getHtml() { return html; }
        public int getSize() { return size; }

    }

    private void rowEnd() {
        sb.append("</tr>\n");
    }
    private void rowStart() { rowStart(null); }
    private void rowStart(String classes) {
        if(classes == null) sb.append(" <tr>\n");
        else sb.append(" <tr class=\"").append(classes).append("\">\n");
    }
    private void coll(String value) { coll(value, null); }
    private void coll(String value, String classes) {
        if(classes == null) sb.append("  <td>");
        else sb.append("  <td class=\"").append(classes).append("\">");
        if(value != null) sb.append(value);
        sb.append("</td>\n");
    }

}

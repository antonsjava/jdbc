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
import java.util.List;
import java.util.StringTokenizer;
import java.util.function.Function;

/**
 * Creates html table from resultset
 * @author antons
 */
public class SqlToHtml {

    private Connection conn;
    private String sql;
    private List<Column> columns = new ArrayList<>();
    private int page = 0;
    private int pagelen = 0;
    private String dateFormat = "yyyy.MM.dd";
    private SimpleDateFormat dateFormatter = null;
    private String datetimeFormat = "yyyy.MM.dd HH:mm:ss";
    private SimpleDateFormat datetimeFormatter = null;
    private Function<Row, String> rowStyleResolver;
    private Function<Row, String> rowClassResolver;

    private StringBuilder sb = new StringBuilder();

    public SqlToHtml(Connection conn) { this.conn = conn; }
    public static SqlToHtml instance(Connection conn) { return new SqlToHtml(conn); }

    public SqlToHtml sql(String value) { this.sql = value; return this; }
    public Column addColumn(String value) { Column col = Column.instance(this).header(value); this.columns.add(col); return col; }
    public SqlToHtml page(int value) { this.page = value; return this; }
    public SqlToHtml pagelen(int value) { this.pagelen = value; return this; }
    public SqlToHtml rowStyleResolver(Function<Row, String> value) { this.rowStyleResolver = value; return this; }
    public SqlToHtml rowClassResolver(Function<Row, String> value) { this.rowClassResolver = value; return this; }
    private SimpleDateFormat dateFormatter() {
        if(dateFormatter == null) dateFormatter = new SimpleDateFormat(dateFormat);
        return dateFormatter;
    }
    public SqlToHtml dateFormat(String value) { this.dateFormat = value; return this; }
    private SimpleDateFormat datetimeFormatter() {
        if(datetimeFormatter == null) datetimeFormatter = new SimpleDateFormat(datetimeFormat);
        return datetimeFormatter;
    }
    public SqlToHtml datetimeFormat(String value) { this.datetimeFormat = value; return this; }


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
            sb.append("<table class=\"sqlreport\">\n");
			ResultSet rs = null;
			Statement stmt = null;
			try {
				stmt = conn.createStatement();
				rs = stmt.executeQuery(sql);
				ResultSetMetaData md = rs.getMetaData();
				int columnCount = md.getColumnCount();
                if(columns.isEmpty()) {
                    for(int i = 0; i < columnCount; i++) {
                        String label = md.getColumnLabel(i+1);
                        final int rownum = i;
                        if(label == null) label = "";
                        columns.add(Column.instance(this)
                            .header(label)
                            .valueResolver(row -> row.column(rownum))
                        );
                    }
                }
                rowStart(null, "sqlheader");
                for(Column col : columns) { coll(col, null, col.header()); }
                rowEnd();

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
                    Row row = Row.instance(index);

                    for(int i = 0; i < columnCount; i++) {
                        String value = null;
                        int type = md.getColumnType(i+1);
                        if(type == Types.TIMESTAMP) {
                            value = formatDate(datetimeFormatter(), rs.getTimestamp(i+1));
                        } else if(type == Types.DATE) {
                            value = formatDate(dateFormatter(), rs.getDate(i+1));
                        } else {
                            value = rs.getString(i+1);
                        }

                        row.add(value);
                    }

                    rowStart(row);
                    for(Column col : columns) { coll(col, row, col.value(row)); }
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
            table.error = "no select present";
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

    public static class Row {
        private int row;
        private int size;
        private List<String> columns = new ArrayList<>();

        private Row(int row) { this.row = row; }
        private static Row instance(int row) { return new Row(row); }

        private void add(String value) {
            row = columns.size();
            size++;
            columns.add(value == null ? "" : value);
        }

        public int row() { return row; }
        public int size() { return size; }
        public String column(int i) {
            if(i < 0) return "";
            if(i >= size) return "";
            return columns.get(i);
        }

    }

    public static class Column {

        private SqlToHtml parent;
        private String header = "";
        private Function<Row, String> valueResolver;
        private Function<Row, String> styleResolver;
        private Function<Row, String> classResolver;

        private Column(SqlToHtml parent) { this.parent = parent; }
        private static Column instance(SqlToHtml parent) { return new Column(parent); }
        private String header() { return this.header; }
        public Column header(String value) { this.header = value; return this; }
        public Column valueResolver(Function<Row, String> value) { this.valueResolver = value; return this; }
        public Column styleResolver(Function<Row, String> value) { this.styleResolver = value; return this; }
        public Column classResolver(Function<Row, String> value) { this.classResolver = value; return this; }
        private String value(Row row) {
            if(valueResolver == null) return "";
            return valueResolver.apply(row);
        }


        public SqlToHtml columnDone() { return parent; }
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
    private void rowStart(Row row) { rowStart(row, null); }
    private void rowStart(Row row, String classes) {
        sb.append(" <tr");
        classes = combineClasses(classes, rowClassResolver == null || row == null ? null : rowClassResolver.apply(row));
        if(classes != null) sb.append(" class=\"").append(classes).append("\"");
        String style = rowStyleResolver == null || row == null ? null : rowStyleResolver.apply(row);
        if(style != null) sb.append(" style=\"").append(style).append("\"");
        sb.append(">\n");
    }
    private void coll(Column col, Row row, String value) { coll(col, row, value, null); }
    private void coll(Column col, Row row, String value, String classes) {
        sb.append("  <td");
        classes = combineClasses(classes, col.classResolver == null || row == null ? null : col.classResolver.apply(row));
        if(classes != null) sb.append(" class=\"").append(classes).append("\"");
        String style = col.styleResolver == null || row == null ? null : col.styleResolver.apply(row);
        if(style != null) sb.append(" style=\"").append(style).append("\"");
        sb.append(">");
        if(value != null) sb.append(value);
        sb.append("</td>\n");
    }

    private static String combineClasses(String cl1, String cl2) {
        StringBuilder sb = new StringBuilder();
        String cl = cl1;
        if(cl != null) {
            cl = cl.trim();
            if(cl.length() > 0) {
                sb.append(" ").append(cl);
            }
        }
        cl = cl2;
        if(cl != null) {
            cl = cl.trim();
            if(cl.length() > 0) {
                sb.append(" ").append(cl);
            }
        }
        return (sb.isEmpty() ? null : sb.toString().trim());
    }

}

/*
 *
 */
package sk.antons.jdbc.util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import javax.sql.DataSource;
/**
 * Simple wrapper around Connection, Statement and ResultSet.
 * @author antons
 */
public class Db implements AutoCloseable {

    private DataSource ds = null;
    private Connection conn = null;
    private Statement st = null;
    private PreparedStatement ps = null;
    private ResultSet rs = null;
    private String laststm = null;


    public Db(DataSource ds) { this.ds = ds; }
    public static Db instance(DataSource ds) { return new Db(ds); }

    /**
     * Closes wrapped jdbc objects.
     * @throws Exception
     */
    @Override
    public void close() throws Exception {
        closeResultSet();
        closePreparedStatement();
        closeStatement();
        closeConnection();
    }

    /**
     * Input DataSource.
     * @return DataSource
     */
    public DataSource ds() { return ds; }
    /**
     * Last created PreparedStatement
     * @return PreparedStatement
     */
    public PreparedStatement ps() { return ps; }
    /**
     * Last created Statement
     * @return Statement
     */
    public Statement st() { return st; }
    /**
     * Last created ResultSet.
     * @return ResultSet
     */
    public ResultSet rs() { return rs; }

    /**
     * Last created Connection. New connection is created from provided
     * DataSource during first call.
     * @return Connection
     * @throws SQLException
     */
    public Connection conn() throws SQLException {
        if(conn == null) {
            conn = ds.getConnection();
            conn.setAutoCommit(false);
            conn.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
        }
        return conn;
    }

    private Statement st_() throws SQLException {
        //closeStatement();
        if(st == null) st = conn().createStatement();
        return st;
    }

    /**
     * Create Statement (if it was not created before) and execute update.
     * @param statement to be executed
     * @return number of processed rows
     * @throws SQLException
     */
    public int executeUpdate(String statement) throws SQLException {
        laststm = statement;
        try {
            return st_().executeUpdate(statement);
        } catch (SQLException e) {
            if(laststm != null) throw new SQLException("last statement: " +laststm, e);
            throw e;
        }
    }

    /**
     * Execute query and returns ResultSet.
     * @param statement to be executed
     * @return ResultSet
     * @throws SQLException
     */
    public ResultSet executeQuery(String statement) throws SQLException {
        laststm = statement;
        try {
            closeResultSet();
            rs = st_().executeQuery(statement);
            return rs;
        } catch (SQLException e) {
            if(laststm != null) throw new SQLException("last statement: " +laststm, e);
            throw e;
        }
    }

    /**
     * Close previous PreparedStatement and creates newone.
     * @param statement to be prepared
     * @throws SQLException
     */
    public void prepareStatement(String statement) throws SQLException {
        laststm = statement;
        try {
            closePreparedStatement();
            ps = conn().prepareStatement(statement);
        } catch (SQLException e) {
            if(laststm != null) throw new SQLException("last statement: " +laststm, e);
            throw e;
        }
    }

    /**
     * Execute update for previously created PreparedStatement.
     * @return number of affected rows
     * @throws SQLException
     */
    public int executeUpdate() throws SQLException {
        try {
            return ps.executeUpdate();
        } catch (SQLException e) {
            if(laststm != null) throw new SQLException("last statement: " +laststm, e);
            throw e;
        }
    }


    /**
     * Execute query for previously created PreparedStatement.
     * @return ResultSet
     * @throws SQLException
     */
    public ResultSet executeQuery() throws SQLException {
        try {
            closeResultSet();
            rs = ps.executeQuery();
            return rs;
        } catch (SQLException e) {
            if(laststm != null) throw new SQLException("last statement: " +laststm, e);
            throw e;
        }
    }

    /**
     * Close last ResultSet.
     */
    public void closeResultSet() {
        try {
            if((rs != null) && (!rs.isClosed())) rs.close();
            rs = null;
            laststm = null;
        } catch(Exception e) {
            //log.warn("Unable to close result set {}", e.toString());
        }
    }

    /**
     * Close last Statement
     */
    public void closeStatement() {
        try {
            if((st != null) && (!st.isClosed())) st.close();
            st = null;
            laststm = null;
        } catch(Exception e) {
            //log.warn("Unable to close statement {}", e.toString());
        }
    }

    /**
     * Close last PreparedStatement
     */
    public void closePreparedStatement() {
        try {
            if((ps != null) && (!ps.isClosed())) ps.close();
            ps = null;
            laststm = null;
        } catch(Exception e) {
            //log.warn("Unable to close prepared sattement {}", e.toString());
        }
    }

    /**
     * Close Connection. Connection is rollbacked before
     */
    public void closeConnection() {
        try {
            if((conn != null)) conn.rollback();
            if((conn != null) && (!conn.isClosed())) conn.close();
            conn = null;
            laststm = null;
        } catch(Exception e) {
            //log.warn("Unable to close connection sattement {}", e.toString());
        }
    }
}

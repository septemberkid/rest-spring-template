package leaf;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import leaf.exception.LeafException;
import org.apache.commons.lang3.StringUtils;

import java.sql.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LeafDb {
    public static Map<String,HikariDataSource> datasources = new HashMap<>();
    private Connection connect;
    private Map<String, String> config = new HashMap<>();
    private String instanceName;
    public static String defaultInstanceName = "primary";

    private LeafDb(String instanceName) {
        this.instanceName = instanceName;
        Map<String, String> config = new HashMap<>();
        config.put("class", LeafProperties.get("db." + instanceName + ".class"));
        config.put("dsn", LeafProperties.get("db." + instanceName + ".dsn"));
        config.put("username", LeafProperties.get("db." + instanceName + ".username"));
        config.put("password", LeafProperties.get("db." + instanceName + ".password"));
        config.put("minimumIdleSize", LeafProperties.get("db." + instanceName + ".minimumIdleSize"));
        config.put("maximumPoolSize", LeafProperties.get("db." + instanceName + ".maximumPoolSize"));
        this.config = config;
    }

    public static LeafDb instance() {
        return LeafDb.instance(LeafDb.defaultInstanceName);
    }

    public static LeafDb instance(String instanceName) {
        return new LeafDb(instanceName);
    }

    public static void shutdown() {
        for (Map.Entry<String,HikariDataSource> entry : datasources.entrySet()) {
            entry.getValue().close();
        }
    }

    private void connect() throws Exception {
        String tag = LeafDb.class+"->connect()";
        try {
            if (this.connect == null || !this.connect.isValid(1)) {
                if (LeafDb.datasources.get(this.instanceName) == null) {
                    // Connection Pooling - using Hikari CP
                    HikariConfig config = new HikariConfig();
                    config.setJdbcUrl(this.config.get("dsn"));
                    config.setUsername(this.config.get("username"));
                    config.setPassword(this.config.get("password"));
                    config.setDriverClassName(this.config.get("class"));
                    if (!this.config.get("minimumIdleSize").equals("")) config.setMinimumIdle(Integer.parseInt(this.config.get("minimumIdleSize")));
                    if (!this.config.get("maximumPoolSize").equals("")) config.setMaximumPoolSize(Integer.parseInt(this.config.get("maximumPoolSize")));
                    config.addDataSourceProperty( "cachePrepStmts" , "true" );
                    config.addDataSourceProperty( "prepStmtCacheSize" , "250" );
                    config.addDataSourceProperty( "prepStmtCacheSqlLimit" , "2048" );
                    LeafDb.datasources.put(this.instanceName,new HikariDataSource(config));

                    LeafLog.log(tag,"Connection Pool created for "+this.config.get("dsn"));
                }
                connect = LeafDb.datasources.get(this.instanceName).getConnection();
                //LeafLog.log(tag,"Connected on "+this.config.get("dsn"));
            }
        } catch (Exception e) {
            LeafLog.error(tag,e.getMessage());
            throw new LeafException("Can't connect to database");
        }
    }

    public void testConfiguration() {
        String tag = LeafDb.class+"->testConfiguration()";
        try {
            this.connect();
            this.close();
        }
        catch (Exception e) {
            LeafLog.error(tag,e.getMessage());
        }
    }
    public void begin() {
        String tag = LeafDb.class+"->begin()";
        this.begin(tag);
    }

    public void begin(String tag) {
        try {
            this.connect();
            this.connect.setAutoCommit(false);

            LeafLog.log(tag,"Beginning Transaction [AUTOCOMMIT: OFF]");
        } catch (Exception e) {
            LeafLog.error(tag,e.getMessage());
        }
    }

    public void commit() {
        String tag = LeafDb.class+"->commit()";
        this.commit(tag);
    }

    public void commit(String tag) {
        try {
            if (this.connect != null) {
                this.connect.commit();
                this.connect.setAutoCommit(true);

                LeafLog.log(tag,"Comitting Transaction [AUTOCOMMIT: ON]");
            }
        } catch (Exception e) {
            LeafLog.error(tag,e.getMessage());
        }
        finally {
            // close connection, on transaction = OFF, then close it
            try { if (this.connect != null && this.connect.getAutoCommit()) this.close(); } catch (Exception ignored) {}
        }
    }

    public void rollback() {
        String tag = LeafDb.class+"->rollback()";
        this.rollback(tag);
    }

    public void rollback(String tag) {
        try {
            if (this.connect != null) {
                this.connect.rollback();
                this.connect.setAutoCommit(true);

                LeafLog.log(tag,"Rolling-back Transaction [AUTOCOMMIT: ON]");
            }
        } catch (Exception e) {
            LeafLog.error(tag,e.getMessage());
        }
        finally {
            // close connection, on transaction = OFF, then close it
            try { if (this.connect != null && this.connect.getAutoCommit()) this.close(); } catch (Exception ignored) {}
        }
    }

    public LeafDbRows query(String sql) throws Exception {
        String tag = LeafDb.class+"->query()";
        Statement statement = null;
        ResultSet rs = null;
        LeafDbRows res = null;

        try {
            LeafLog.log(tag,sql);
            // connect
            this.connect();
            // create the statement object
            statement = this.connect.createStatement();
            // execute query
            rs = statement.executeQuery(this.transformQuery(sql));
            // parse result
            res = this.parseResult(rs);
        } catch (Exception e) {
            LeafLog.error(tag,e.getMessage());
            throw new LeafException(e.getMessage());
        } finally {
            // close connection, on transaction = OFF, then close it
            try { if (this.connect.getAutoCommit()) this.close(); } catch (Exception ignored) {}
            // close result-set
            try { if (rs != null) rs.close(); } catch (Exception ignored) {}
            // close statement
            try { if (statement != null) statement.close(); } catch (Exception ignored) {}
        }
        return res == null ? new LeafDbRows() : res;
    }

    public void executeBatch(List<String> sql){
        String tag = LeafDb.class+"->executeBatch()";
        Statement statement = null;
        try {
            // connect
            this.connect();
            // create the statement object
            statement = this.connect.createStatement();

            for (String s : sql){
                statement.addBatch(s);
            }

            statement.executeBatch();
        }catch (Exception e) {
            LeafLog.error(tag,e.getMessage());
            throw new LeafException(e.getMessage());
        } finally {
            // close connection, on transaction = OFF, then close it
            try { if (this.connect.getAutoCommit()) this.close(); } catch (Exception ignored) {}
            // close statement
            try { if (statement != null) statement.close(); } catch (Exception ignored) {}
        }
    }

    public int execute(String sql) throws Exception {
        return this.execute(sql, true);
    }

    /**
     * Execute alter query to database, return affected rows
     * @param sql
     * @param generateKey
     * @return
     * @throws Exception
     */
    public int execute(String sql, boolean generateKey) throws Exception {
        String tag = LeafDb.class+"->execute()";
        Statement statement = null;
        int res = 0;

        try {
            LeafLog.log(tag,sql);
            // connect
            this.connect();
            // create the statement object
            statement = this.connect.createStatement();
            // check if insert
            String[] arrsql = sql.split("[ ]");
            if (arrsql[0].equalsIgnoreCase("insert") && generateKey) {
                res = statement.executeUpdate(sql, Statement.RETURN_GENERATED_KEYS);
                // run this, only on 'insert' query
                switch (this.config.get("class")) {
                    case "org.postgresql.Driver":
                        ResultSet generatedKeys = statement.getGeneratedKeys();
                        if (generatedKeys.next())
                            res = generatedKeys.getInt(1);
                        break;
                }
            } else {
                // execute query
                res = statement.executeUpdate(sql);
            }
        } catch (Exception e) {
            LeafLog.error(tag,e.getMessage());
            throw new LeafException(e.getMessage());
        } finally {
            // close connection, on transaction = OFF, then close it
            try { if (this.connect.getAutoCommit()) this.close(); } catch (Exception ignored) {}
            // close statement
            try { if (statement != null) statement.close(); } catch (Exception ignored) {}
        }
        return res;
    }

    public int execute(String sql,boolean generateKey,ArrayList<Object> items) throws Exception {
        String tag = LeafDb.class+"->execute()";
        PreparedStatement ps = null;
        int res = 0;
        if (items.size() == 0) throw new LeafException("No data fields supplied");

        try {
            this.connect();
            if (generateKey) ps = this.connect.prepareStatement(sql,PreparedStatement.RETURN_GENERATED_KEYS);
            else ps = this.connect.prepareStatement(sql);
            int i = 1;
            for (Object item : items) {
                if (item == null)
                    ps.setNull(i, Types.NULL);
                else if (item.getClass().getName().equals("java.lang.Integer"))
                    ps.setInt(i, (Integer) item);
                else if (item.getClass().getName().equals("java.lang.Double"))
                    ps.setDouble(i, (Double) item);
                else if (item.getClass().getName().equals("java.lang.Float"))
                    ps.setFloat(i, (Float) item);
                else if (item.getClass().getName().equals("java.lang.Long"))
                    ps.setLong(i, (Long) item);
                else if (item.getClass().getName().equals("java.sql.Date"))
                    ps.setDate(i, (Date) item);
                else if (item.getClass().getName().equals("java.sql.Timestamp"))
                    ps.setTimestamp(i,(Timestamp) item);
                else
                    ps.setString(i, String.valueOf(item));
                i++;
            }
            // check if insert
            String[] arrsql = sql.split("[ ]");
            if (arrsql[0].equalsIgnoreCase("insert") && generateKey) {
                res = ps.executeUpdate();
                // run this, only on 'insert' query
                switch (this.config.get("class")) {
                    case "org.postgresql.Driver":
                        ResultSet generatedKeys = ps.getGeneratedKeys();
                        if (generatedKeys.next())res = generatedKeys.getInt(1);
                        break;
                }
            } else {
                // execute query
                res = ps.executeUpdate();
            }
        } catch (Exception e) {
            LeafLog.error(tag,e.getMessage());
            throw new LeafException(e.getMessage());
        } finally {
            // close connection, on transaction = OFF, then close it
            try { if (this.connect.getAutoCommit()) this.close(); } catch (Exception ignored) {}
            // close statement
            try { if (ps != null) ps.close(); } catch (Exception ignored) {}
        }
        return res;
    }

    public void close() {
        String tag = LeafDb.class+"->close()";
        try {
            if (this.connect != null && !this.connect.isClosed()) {
                connect.close();
                connect = null;
                //LeafLog.log(tag,"Disconnect from "+this.config.get("dsn"));
            }
        } catch (Exception e) {
            LeafLog.error(tag,e.getMessage());
        }
    }

    private LeafDbRows parseResult(ResultSet rs) {
        LeafDbRows res = new LeafDbRows();
        try {
            ResultSetMetaData meta = rs.getMetaData();
            while (rs.next()) {
                LeafDbRow row = new LeafDbRow();
                for (int i = 1; i <= meta.getColumnCount(); i++) {
                    row.put(meta.getColumnName(i).toLowerCase(), rs.getString(i));
                }
                res.add(row);
            }
        } catch (Exception e) {
            LeafLog.error(LeafDb.class+"->parseResult()",e.getMessage());
        }

        return res;
    }

    private String transformQuery(String query) {
        String sql = "";

        switch (this.config.get("class")) {
            case "oracle.jdbc.driver.OracleDriver":
                query = query.replaceAll("LIMIT", "limit");
                query = query.replaceAll("OFFSET", "offset");

                String[] tmp = query.split(" limit ");
                if (tmp.length <= 1)
                    return query;

                int limit = 0;
                int offset = 0;

                String q = tmp[0].trim();
                String[] tmp2 = tmp[1].split(" offset ");
                if (tmp2.length == 1) {
                    limit = Integer.valueOf(tmp2[1].trim());
                } else {
                    limit = Integer.valueOf(tmp2[0].trim());
                    offset = Integer.valueOf(tmp2[1].trim());
                }

                sql = "select * from ( select a.*, ROWNUM rnum from ( " + q + " ) a where ROWNUM <= " + (limit + offset) + " ) where rnum  > " + offset;
                break;

            default:
                sql = query;
                break;
        }

        return sql;
    }

    public String quote(Object o) {
        return this.quote(String.valueOf(o));
    }

    public String quote(int s) {
        return this.quote(Integer.toString(s));
    }

    public String quote(double s) {
        return this.quote(Double.toString(s));
    }

    public String quote(long s) {
        return this.quote(Long.toString(s));
    }

    public String quote(float s) {
        return this.quote(Float.toString(s));
    }

    public String quote(String s) {
        return this.quote(s, true);
    }

    public String quote(String s, boolean isquote) {
        return s == null || s.equalsIgnoreCase("null") ? "null" : ((isquote ? "'" : "") + this.escapeSql(s.trim()) + (isquote ? "'" : ""));
    }

    private String escapeSql(String str) {
        if (str == null) {
            return null;
        }
        return StringUtils.replace(StringUtils.replace(str, "'", "''"),"\\","\\\\");
    }

    public Date toSqlDate(String inputDate,String format) throws Exception {
        DateFormat formatter = new SimpleDateFormat(format);
        java.util.Date date = formatter.parse(inputDate);
        return new Date(date.getTime());
    }

    public Date toSqlDate(String inputDate) throws Exception {
        return this.toSqlDate(inputDate,"yyyy-MM-dd HH:mm:ss");
    }

    public Timestamp toSqlTimestamp(String inputTimestamp,String format) throws Exception {
        DateFormat formatter = new SimpleDateFormat(format);
        java.util.Date date = formatter.parse(inputTimestamp);
        return new Timestamp(date.getTime());
    }

    public Timestamp toSqlTimestamp(String inputTimestamp) throws Exception {
        return this.toSqlTimestamp(inputTimestamp,"yyyy-MM-dd HH:mm:ss");
    }

}

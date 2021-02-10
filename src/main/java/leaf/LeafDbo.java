package leaf;

import leaf.exception.LeafException;
import org.apache.commons.lang3.StringUtils;

import java.sql.Date;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class LeafDbo {

    protected LeafDb db;

    protected Map<String, Object> fields = new HashMap<String, Object>();
    protected ArrayList<String> selects = new ArrayList<String>();
    protected ArrayList<String> wheres = new ArrayList<String>();
    protected ArrayList<String> groups = new ArrayList<String>();
    protected ArrayList<String> orders = new ArrayList<String>();

    protected boolean autoinc = true;
    protected Integer limit = null;
    protected Integer offset = null;
    protected String pkey;
    protected String tableName;
    protected boolean usePreparedStatement = true;


    /**
     * Create DBO and make db instance
     */
    public LeafDbo() {
        this.db = LeafDb.instance();
    }

    /**
     * craete dbo based on supplied db
     * @param db
     */
    public LeafDbo(LeafDb db) { this.db = db; }

    /**
     * Get data from database object
     * @return
     * @throws Exception
     */
    public LeafDbRows get() throws Exception {
        return this.db.query(this.getSelectSQL());
    }

    /**
     * Get only one row data
     * @return
     * @throws Exception
     */
    public LeafDbRow getOne() throws Exception {
        LeafDbRows rows = this.get();
        if (rows.size() > 0) return rows.get(0);
        else return new LeafDbRow();
    }

    /**
     * Get Total Row from database object
     * @return
     * @throws Exception
     */
    public int count() throws Exception {
        LeafDbRows rows = this.db.query("SELECT count(*) as numofrows FROM "+this.tableName+ this.getWhere());
        return rows.get(0).getInteger("numofrows");
    }

    /**
     * retrieve exactly one row based on primary key
     * @param pkeyid
     * @return
     * @throws Exception
     */
    public LeafDbRow retrieve(String pkeyid) throws Exception {
        LeafDbRows rows = this.db.query("SELECT "+this.getSelect()+" FROM "+this.tableName+ " where "+this.pkey+" = "+this.quote(pkeyid));
        return rows.size() > 0? rows.get(0) : new LeafDbRow();
    }

    /**
     * retrieve exactly one row based on primary key
     * @param pkeyid
     * @return
     * @throws Exception
     */
    public LeafDbRow retrieve(int pkeyid) throws Exception {
        return this.retrieve(String.valueOf(pkeyid));
    }

    /**
     * Get SQL
     * @return
     * @throws Exception
     */
    public String getSql() throws Exception {
        return this.getSelectSQL();
    }


    protected String getSelectSQL() { return "SELECT "+this.getSelect()+" FROM "+this.tableName +this.getWhere()+this.getGroupBy()+this.getOrder()+this.getLimitOffset(); }

    protected String getLimitOffset() {
        String lo = "";
        if (this.limit != null && this.offset != null)
            lo = " LIMIT "+this.limit+ " OFFSET "+this.offset;
        else if (this.limit != null)
            lo= " LIMIT "+this.limit;

        return lo;
    }

    protected String getSelect()    { return this.selects.size() > 0? String.join(",",this.selects) : String.join(",",new ArrayList<>(this.fields.keySet())); }

    protected String getGroupBy()   { return this.groups.size() > 0? " GROUP BY "+String.join(",",this.groups) : ""; }

    protected String getOrder()     { return this.orders.size() > 0? " ORDER BY "+String.join(",",this.orders) : ""; }

    protected String getWhere()     { return this.wheres.size() > 0? " WHERE "+String.join(" AND ",this.wheres) : ""; }

    public LeafDbo select(String... selects) {
        for (String s : selects) {
            this.selects.add(s);
        }
        return this;
    }

    public LeafDbo where(String w) {
        this.wheres.add(w);
        return this;
    }

    public LeafDbo groupBy(String g) {
        this.groups.add(g);
        return this;
    }

    public LeafDbo orderBy(String field,String sort) {
        if (this.fields.containsKey(field) && (sort.equalsIgnoreCase("ASC") || sort.equalsIgnoreCase("DESC") || sort.equalsIgnoreCase("")))
            this.orders.add(field + " " + sort);

        return this;
    }

    public LeafDbo orderBy(String field) {
        if (this.fields.containsKey(field))
            this.orders.add(field);
        return this;
    }

    public LeafDbo limit(int limit) {
        if (limit > 0) this.limit = limit;
        return this;
    }

    public LeafDbo offset(int offset) {
        if (offset > 0) this.offset = offset;
        return this;
    }


    public int insert() throws Exception{
        return this.insert(new HashMap<>());
    }

    /**
     * Insert data into table, return ID (if auto inc)
     * @param param
     * @return
     * @throws Exception
     */
    public int insert(Map<String, Object> param) throws Exception {
        // SQL base
        String sql = "insert into " + this.tableName + " (";
        // set fields and values to insert
        ArrayList<String> fnames = new ArrayList<>();
        ArrayList<Object> values = new ArrayList<>();
        ArrayList<String> qvalues = new ArrayList<>();
        ArrayList<String> qmarks = new ArrayList<>();
        for (Map.Entry<String, Object> entry : this.fields.entrySet())
            if (!entry.getKey().equals(this.pkey) || !this.autoinc) {
                // field name to be insert
                fnames.add(entry.getKey());
                // field values
                if (this.usePreparedStatement)
                    values.add(param.containsKey(entry.getKey()) ? param.get(entry.getKey()) : entry.getValue());
                else
                    qvalues.add(this.quote(param.containsKey(entry.getKey()) ? param.get(entry.getKey()) : entry.getValue()));
                // marks
                qmarks.add("?");
            }
        // list sql field name
        sql += fnames.size() > 0? String.join(",",fnames) : "";
        // then values
        sql += ") values (";
        // list sql var
        sql +=  this.usePreparedStatement?
                (values.size() > 0 ? String.join(",", qmarks) : "") :
                (qvalues.size() > 0? String.join(",",qvalues) : "");
        // close parenthesis
        sql += ")";
        // execute
        return this.usePreparedStatement? this.db.execute(sql, this.autoinc,values) : this.db.execute(sql, this.autoinc);
    }

    /**
     * Update table data, require pkey or where clause to be set
     * @param param
     * @return
     * @throws Exception
     */
    public int update(Map<String, Object> param) throws Exception {
        // SQL base
        String sql = "update " + this.tableName + " set ";
        // set fields and values to update
        int i = 0;
        ArrayList<Object> values = new ArrayList<>();
        for (Map.Entry<String, Object> entry : param.entrySet()) {
            if (this.fields.containsKey(entry.getKey()) && !entry.getKey().equals(this.pkey)) {
                values.add(param.get(entry.getKey()));
                sql += (i != 0 ? "," : "") + entry.getKey() + (this.usePreparedStatement? " = ?" : " = "+this.quote(param.get(entry.getKey())));
                i++;
            }
        }
        // check if anything to update
        if (i == 0) return 0;

        // append sql
        if (this.getWhere().equals("")) {
            // update using pkey
            if (param.containsKey(this.pkey) && !this.isNullOrEmpty(String.valueOf(param.get(this.pkey))))
                sql += " where "+this.pkey+" = "+this.quote(param.get(this.pkey));
            else throw new LeafException("Invalid update, pkey not set in parameters");
        }
        else {
            // update using where clause
            sql += this.getWhere();
        }

        // execute
        return this.usePreparedStatement? this.db.execute(sql,false,values) : this.db.execute(sql,false);
    }

    /**
     * Delete by ID
     * @param pkeyid
     * @return
     * @throws Exception
     */
    public int delete(String pkeyid) throws Exception {
        // SQL base
        String sql = "delete from " + this.tableName + " where "+this.pkey+" = "+this.quote(StringUtils.substring(pkeyid,0,36));
        // execute
        return this.db.execute(sql);
    }

    /**
     * Delete by ID
     * @param pkeyid
     * @return
     * @throws Exception
     */
    public int delete(int pkeyid) throws Exception {
        return this.delete(String.valueOf(pkeyid));
    }

    /**
     * Delete by where clause
     * @return
     * @throws Exception
     */
    public int delete() throws Exception {
        // check where clause
        if (this.getWhere().equals("")) throw new LeafException("No where clause is set");
        // SQL base
        String sql = "delete from " + this.tableName + this.getWhere();
        // execute
        return this.db.execute(sql);
    }

    /**
     * Get key values, default by primary key
     * @return
     * @throws Exception
     */
    public Map<String,LeafDbRow> keyvalues() throws Exception {
        return this.keyvalues(this.pkey);
    }

    /**
     * Get key values, by identifier
     * @param key
     * @return
     * @throws Exception
     */
    public Map<String,LeafDbRow> keyvalues(String key) throws Exception {
        LeafDbRows rows = this.get();
        Map<String,LeafDbRow> res = new HashMap<>();
        for (LeafDbRow row : rows) {
            res.put(row.get(key),row);
        }
        return res;
    }

    public Map<String,String> keyvalues(String key, String valuekey) throws Exception {
        LeafDbRows rows = this.get();
        Map<String,String> res = new HashMap<>();
        for (LeafDbRow row : rows) {
            res.put(row.get(key),row.getString(valuekey));
        }
        return res;
    }

    public boolean isNullOrEmpty(String s) {
        return StringUtils.isEmpty(s);
    }

    public String quote(Object o) {
        return this.db.quote(String.valueOf(o));
    }

    public String quote(int s) {
        return this.db.quote(Integer.toString(s));
    }

    public String quote(double s) {
        return this.db.quote(Double.toString(s));
    }

    public String quote(long s) {
        return this.db.quote(Long.toString(s));
    }

    public String quote(float s) {
        return this.db.quote(Float.toString(s));
    }

    public String quote(String s) {
        return this.db.quote(s, true);
    }

    public Date toSqlDate(String inputDate,String format) throws Exception {
        return this.db.toSqlDate(inputDate,format);
    }
    public Date toSqlDate(String inputDate) throws Exception {
        return this.db.toSqlDate(inputDate);
    }

    public Timestamp toSqlTimestamp(String inputTimestamp, String format) throws Exception {
        return this.db.toSqlTimestamp(inputTimestamp,format);
    }
    public Timestamp toSqlTimestamp(String inputTimestamp) throws Exception {
        return this.db.toSqlTimestamp(inputTimestamp);
    }

}

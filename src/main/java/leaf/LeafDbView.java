package leaf;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class LeafDbView {

    protected LeafDb db;

    protected Map<String, Object> fields = new HashMap<String, Object>();
    protected ArrayList<String> selects = new ArrayList<String>();
    protected ArrayList<String> wheres = new ArrayList<String>();
    protected ArrayList<String> groups = new ArrayList<String>();
    protected ArrayList<String> orders = new ArrayList<String>();

    protected Integer limit = null;
    protected Integer offset = null;
    protected String pkey;
    protected String viewName;

    /**
     * Create DBO and make db instance
     */
    public LeafDbView() {
        this.db = LeafDb.instance();
    }

    /**
     * craete dbo based on supplied db
     * @param db
     */
    public LeafDbView(LeafDb db) { this.db = db; }

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
        LeafDbRows rows = this.db.query("SELECT count(*) as numofrows FROM "+this.viewName + this.getWhere());
        return rows.get(0).getInteger("numofrows");
    }

    /**
     * retrieve exactly one row based on primary key
     * @param pkeyid
     * @return
     * @throws Exception
     */
    public LeafDbRow retrieve(String pkeyid) throws Exception {
        LeafDbRows rows = this.db.query("SELECT "+this.getSelect()+" FROM "+this.viewName + " where "+this.pkey+" = "+this.quote(pkeyid));
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

    protected String getSelectSQL() { return "SELECT "+this.getSelect()+" FROM "+this.viewName +this.getWhere()+this.getGroupBy()+this.getOrder()+this.getLimitOffset(); }

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

    public LeafDbView select(String... selects) {
        for (String s : selects) {
            this.selects.add(s);
        }
        return this;
    }

    public LeafDbView where(String w) {
        this.wheres.add(w);
        return this;
    }

    public LeafDbView groupBy(String g) {
        this.groups.add(g);
        return this;
    }

    public LeafDbView orderBy(String field, String sort) {
        if (this.fields.containsKey(field) && (sort.equalsIgnoreCase("ASC") || sort.equalsIgnoreCase("DESC") || sort.equalsIgnoreCase("")))
            this.orders.add(field + " " + sort);

        return this;
    }

    public LeafDbView orderBy(String field) {
        if (this.fields.containsKey(field))
            this.orders.add(field);

        return this;
    }

    public LeafDbView limit(int limit) {
        if (limit > 0) this.limit = limit;
        return this;
    }

    public LeafDbView offset(int offset) {
        if (offset > 0) this.offset = offset;
        return this;
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
}

package leaf;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class LeafDbRow extends HashMap<String, Object>{
    public Map<String, String> map(){
        Map<String, String> raw = new HashMap<>();

        for(Entry<String, Object> entry : this.entrySet()){
            raw.put(entry.getKey(), this.getString(entry.getKey()));
        }

        return raw;
    }

    public String get(String key){
        return this.getString(key);
    }

    public String getString(String key){
        String res = String.valueOf(super.get(key));
        return res;
    }

    public Integer getInteger(String key){
        return Integer.valueOf(this.getString(key).equalsIgnoreCase("") ? "0" : this.getString(key));
    }

    public Long getLong(String key){
        return Long.valueOf(this.getString(key).equalsIgnoreCase("") ? "0" : this.getString(key));
    }

    public Double getDouble(String key){
        return Double.valueOf(this.getString(key).equalsIgnoreCase("") ? "0" : this.getString(key));
    }

    public Float getFloat(String key){
        return Float.valueOf(this.getString(key).equalsIgnoreCase("") ? "0" : this.getString(key));
    }

    public Date getDate(String key){
        return this.getDate(key, "YYYY-MM-DD");
    }
    public Date getDate(String key, String inputFormat){
        String input = this.get(key);
        SimpleDateFormat df = new SimpleDateFormat(inputFormat);
        Date date = new Date();
        try {
            date = df.parse(input);
        }
        catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }
}

package leaf;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.text.SimpleDateFormat;
import java.util.Date;

public class LeafLog {
    public static final int LEVEL_STDLOG = 5;
    public static final int LEVEL_DEBUG = 4;
    public static final int LEVEL_INFO  = 3;
    public static final int LEVEL_WARN  = 2;
    public static final int LEVEL_ERROR = 1;
    public static int   activeLogLevel = LeafLog.LEVEL_STDLOG;
    public static String dateFormat = "yyyy-MM-dd HH:mm:ss.SSS Z";

    private static ObjectMapper mapper  = new ObjectMapper();

    public static void write(int level, String tag, Object data){
        String type     = "[";
        String color    = "\u001B[";
        Date date = new Date();
        String time     = new SimpleDateFormat(LeafLog.dateFormat).format(date);
        switch(level){
            case LeafLog.LEVEL_STDLOG:
                type += "LOG  ";
                color += "37";
                break;
            case LeafLog.LEVEL_DEBUG:
                type += "DEBUG";
                color += "34";
                break;
            case LeafLog.LEVEL_INFO:
                type += "INFO ";
                color += "32";
                break;
            case LeafLog.LEVEL_WARN:
                type += "WARN ";
                color += "33";
                break;
            case LeafLog.LEVEL_ERROR:
                type += "ERROR";
                color += "31";
                break;
            default:
                type += "";
                color += "0";
                break;
        }
        type += "]";
        color += "m";

        try{
            if(LeafLog.activeLogLevel >= level)
                System.out.println(color +time +" " +type +" " +tag + " :: " + mapper.writeValueAsString(data != null ? data : "") + "\u001B[0m");

        }catch (Exception e){
            System.out.println(color +time +" " +type +" " +tag + " :: " + (data != null ? data.toString() : "") + "\u001B[0m");
        }
    }

    public static void log(String tag, Object data){
        LeafLog.write(LeafLog.LEVEL_STDLOG, tag, data);
    }
    public static void log(Class<?> clazz, Object data){
        LeafLog.write(LeafLog.LEVEL_STDLOG, clazz.getName(), data);
    }


    public static void debug(String tag, Object data){
        LeafLog.write(LeafLog.LEVEL_DEBUG, tag, data);
    }
    public static void debug(Class<?> clazz, Object data){
        LeafLog.write(LeafLog.LEVEL_DEBUG, clazz.getName(), data);
    }


    public static void info(String tag, Object data){
        LeafLog.write(LeafLog.LEVEL_INFO, tag, data);
    }
    public static void info(Class<?> clazz, Object data){
        LeafLog.write(LeafLog.LEVEL_INFO, clazz.getName(), data);
    }


    public static void warn(String tag, Object data){
        LeafLog.write(LeafLog.LEVEL_WARN, tag, data);
    }
    public static void warn(Class<?> clazz, Object data){
        LeafLog.write(LeafLog.LEVEL_WARN, clazz.getName(), data);
    }


    public static void error(String tag, Object data){
        LeafLog.write(LeafLog.LEVEL_ERROR, tag, data);
    }
    public static void error(Class<?> clazz, Object data){
        LeafLog.write(LeafLog.LEVEL_ERROR, clazz.getName(), data);
    }
}

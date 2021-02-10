package leaf;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Properties;

public class LeafProperties {
    private static Properties properties;

    public static void load(){
        if(properties == null) {
            InputStream input = null;
            properties = new Properties();

            try {
                File file = null;
                if (new File("config/leaf.properties").exists())
                    file = new File("config/leaf.properties");
                else if (new File("conf/leaf.properties").exists())
                    file = new File("conf/leaf.properties");
                else if (new File("leaf.properties").exists())
                    file = new File("leaf.properties");

                if (file != null) {
                    input = new FileInputStream(file);
                }
                else {
                    ClassLoader classloader = Thread.currentThread().getContextClassLoader();
                    input = classloader.getResourceAsStream("application.properties");
                }

                properties.load(input);

                // load properties by profile
                if (!get("spring.profiles.active").equals("")){
                    loadPropertiesByProfiles(get("spring.profiles.active").toLowerCase());
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            } finally {
                if (input != null) {
                    try {
                        input.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    public static String get(String key){
        if(properties == null) LeafProperties.load();

        return LeafProperties.properties.getProperty(key) != null ? LeafProperties.properties.getProperty(key) : "";
    }

    public static String getOrDefault(String key, String def){
        if(properties == null) LeafProperties.load();

        return LeafProperties.properties.getProperty(key) != null ? LeafProperties.properties.getProperty(key) : def;
    }

    private static void loadPropertiesByProfiles(String profile){
        if (Arrays.asList("develop","staging","prod").contains(profile)){
            InputStream is = null;
            try {
                ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
                is = classLoader.getResourceAsStream("application-"+profile+".properties");

                properties.load(is);
            }catch (IOException e){
                e.printStackTrace();
            }finally {
                if (is != null){
                    try {
                        is.close();
                    }catch (IOException ex){
                        ex.printStackTrace();
                    }
                }
            }
        }
    }
}

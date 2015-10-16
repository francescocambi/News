package it.fcambi.news;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * Created by Francesco on 09/10/15.
 */
public class PropertyConfig {

    private Properties props;

    public PropertyConfig() throws IOException {
        this.props = new Properties();
        FileInputStream in = new FileInputStream("src/main/resources/config.properties");
        this.props.load(in);
        in.close();
    }

    public String getProp(String key) {
        return props.getProperty(key);
    }

}

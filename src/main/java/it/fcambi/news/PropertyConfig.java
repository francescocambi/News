package it.fcambi.news;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Created by Francesco on 09/10/15.
 */
public class PropertyConfig {

    private Properties props;

    public PropertyConfig() throws IOException {
        this.props = new Properties();
//        FileInputStream in = new FileInputStream("classpath:config.properties");
        InputStream in = getClass().getResourceAsStream("/config.properties");
        this.props.load(in);
        in.close();
    }

    public String getProp(String key) {
        return props.getProperty(key);
    }

}

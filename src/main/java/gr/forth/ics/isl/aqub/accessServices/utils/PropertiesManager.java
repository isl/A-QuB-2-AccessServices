package gr.forth.ics.isl.aqub.accessServices.utils;

import java.io.*;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PropertiesManager {

    private static PropertiesManager propManager = null;
    private static final String initFilePath = "config.properties";
    private static Properties prop;

    public static PropertiesManager getPropertiesManager() {
        if (propManager == null) {
            try {
                ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
                InputStream input = classLoader.getResourceAsStream(initFilePath);
                prop = new Properties();
                if (input != null) {
                    prop.load(input);
                }
                propManager = new PropertiesManager();
            } catch (IOException ex) {
                Logger.getLogger(PropertiesManager.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return propManager;
    }

    public String getPropertyValue(String key) {
        return prop.getProperty(key);
    }

    public Properties getProperties() {
        return prop;
    }

    public String getTripleStoreUrl() {
        return prop.getProperty("virtuoso.rest.url");
    }

    public String getTripleStoreNamespace() {
        return prop.getProperty("triplestore.namespace");
    }
}

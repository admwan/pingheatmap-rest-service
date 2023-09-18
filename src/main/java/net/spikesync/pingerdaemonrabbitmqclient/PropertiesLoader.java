package net.spikesync.pingerdaemonrabbitmqclient;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class PropertiesLoader {
	
	private static String FILENAME_PROPERTIES = "nospringboot.application.properties";
	
    public static Properties loadProperties() { //throws IOException {
        Properties configuration = new Properties();
        InputStream inputStream = PropertiesLoader.class
          .getClassLoader()
          .getResourceAsStream(FILENAME_PROPERTIES);
        try {
        	if(inputStream!=null)
        		configuration.load(inputStream);
        	else throw (new IOException("------------- The properties file with name: " + FILENAME_PROPERTIES + " CAN NOT BE FOUND!!!!!!\n"
        			+ "Properties for this application will not be read from file!! "));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        try {
        	if(inputStream != null)
        		inputStream.close();	
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        return configuration;
    }
}

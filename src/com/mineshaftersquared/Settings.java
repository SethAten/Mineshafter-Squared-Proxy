package com.mineshaftersquared;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import mineshafter.util.Resources;

public class Settings 
{
	private Properties properties  = new Properties();
	
	public Settings(File rootFolder)
	{
		String file = rootFolder + "\\" + "mineshaftersquared.properties";
		
		try {
			Logger.log(file);
			properties.load(new FileInputStream(file));
			Logger.log("Properties found, adding new settings if needed");
		} catch (IOException e) {
			Logger.log("No properties file: creating with defaults");
			purgeFiles(file);
		}
		
		updateWithDefaults(file);
	}
	
	private void updateWithDefaults(String filePath)
	{
		try {
			// defaults
			boolean updated = false;
			
			// AUTH
			if(!properties.containsKey("auth"))
			{
				properties.setProperty("auth", Resources.loadString("auth").trim());
				updated = true;
			}
			
			// LOG-FILE
			if(!properties.containsKey("log-file"))
			{
				properties.setProperty("log-file", "none");
				updated = true;
			}
			
			// write out
			if(updated)
			{
				properties.store(new FileOutputStream(filePath), null);
			}
		} catch (FileNotFoundException ex) {
			Logger.log("Error creating properties file" + ex);
		} catch (IOException ex) {
			Logger.log("Error writing properties file" + ex);
		}
	}
	
	private void purgeFiles(String file)
	{
		File ms2 = new File(file + "/minecraft.jar");
		File ms2_modified = new File(file + "/minecraft_modified.jar");
		
		ms2.delete();
		ms2_modified.delete();
	}
	
	public String get(String key)
	{
		return properties.getProperty(key);
	}
}

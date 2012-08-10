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
	public Properties properties  = new Properties();
	private String fileName = "mineshaftersquared.properties";
	private File workingDirectory;
	
	public Settings()
	{
		this(new File("."));
	}
	
	public Settings(File file)
	{
		Logger.logln("Loading settings...");
		
		workingDirectory = file;
		
		try {
			
			properties.load(new FileInputStream(fileName));
			
		} catch (IOException e) {
			
			Logger.logln("No properties file: creating with defaults");
			purgeFiles();
			createWithDefaults();
		}
	}
	
	private void createWithDefaults()
	{
			properties.setProperty("auth", Resources.loadString("auth").trim());
			save();
	}
	
	private void purgeFiles()
	{
		File ms2 = new File(workingDirectory + "/minecraft.jar");
		File ms2_modified = new File(workingDirectory + "/minecraft_modified.jar");
		
		ms2.delete();
		ms2_modified.delete();
	}
	
	public void save()
	{
		try 
		{
			properties.store(new FileOutputStream(workingDirectory + "/" + fileName), null);	
		} 
		catch (FileNotFoundException e1)
		{
			Logger.logln("Error creating properties file" + e1);
		} 
		catch (IOException e1) 
		{
			Logger.logln("Error creating properties file" + e1);
		}
		
	}
	
	public String get(String key)
	{
		return properties.getProperty(key);
	}
	
	public void set(String key, String value)
	{
		properties.setProperty(key, value);
	}
}

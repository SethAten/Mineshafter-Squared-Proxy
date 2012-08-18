package com.mineshaftersquared;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import net.minecraft.Util;
import mineshafter.util.Resources;

public class Settings 
{
	public Properties properties  = new Properties();
	private String fileName = "mineshaftersquared.properties";
	private File workingDirectory;
	private MS2Logger logger;
	public static enum Mode { CLIENT, SERVER };
	private static Mode programType;
	
	public Settings()
	{	
		File file = getMS2Directory();
		
		try 
		{
			workingDirectory = file;
			File propertiesFile = new File(workingDirectory + "/" + fileName);
			logger = new MS2Logger(workingDirectory);
			properties.load(new FileInputStream(propertiesFile));
		} 
		catch (IOException e) 
		{
			logger.info("No properties file: creating with defaults");
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
		catch (FileNotFoundException e)
		{
			logger.severe("Error creating properties file" + e.getMessage());
		} 
		catch (IOException e) 
		{
			logger.severe("Error creating properties file" + e.getMessage());
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
	
	private File getMS2Directory()
	{
		File workingDir;
		switch(programType)
		{
			case SERVER:
				workingDir = new File(".");
				break;
				
			default:
			case CLIENT:
				// get working directory
				File gamePath = Util.getWorkingDirectory(); // test
				workingDir = new File(gamePath.toString().replace("minecraft", "mineshaftersquared"));
				
				// ensure working directory exists
				if(!workingDir.exists())
					workingDir.mkdir();
				break;
		}
		
		
		return workingDir;
	}
	
	public File getWorkingDirectory()
	{
		return workingDirectory;
	}
	
	public static void setProgramType(Mode mode)
	{
		programType = mode;
	}
}

package com.mineshaftersquared;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class Logger {
	private static String logName 			= "[Mineshafter Squared]";
	private static BufferedWriter logFile 	= null; 
	
	public static void log(String output)
	{
		String message = logName + " " + output;
		
		System.out.println(message);
		
		if(logFile != null)
		{
			write(message);
		}
	}
	
	private static void write(String output)
	{
		try {
			logFile.append(output);
			logFile.newLine();
			logFile.flush();
		} catch (IOException ex) {
			log(ex.getLocalizedMessage());
		}
	}
	
	public static void setFile(String filePath)
	{
		if(filePath != null && !filePath.trim().toLowerCase().equals("none")) {
			try {
				FileWriter fstream = new FileWriter(filePath);
				logFile = new BufferedWriter(fstream);
			} catch (IOException ex) {
				log(ex.getLocalizedMessage());
			}
			
			Logger.log("Log file set to: " + filePath);
		}
	}
}

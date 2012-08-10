package com.mineshaftersquared;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class Logger {
	
	private static String logName = "[Mineshafter Squared]";
	private static String logFile = "mineshaftersquared.log";
	
	public static void logln(String output, boolean toScreen)
	{
		log(output + "\n", toScreen);
	}
	
	public static void logln(String output)
	{
		log(output + "\n");
	}
	
	public static void log(String output)
	{
		log(output, false);
	}
	
	public static void log(String output, boolean toScreen)
	{
		String printOut = logName + " " + output;
		writeToFile(printOut);
		
		if(toScreen)
			System.out.print(printOut);
	}
	
	private static void writeToFile(String output)
	{
		try 
		{
			FileWriter fstream = new FileWriter(logFile);
			BufferedWriter out = new BufferedWriter(fstream);
			
			out.write(output);
			
			out.close();
			fstream.close();
		}
		catch (IOException ex)
		{
			Logger.logln(ex.toString());
		}
	}
}

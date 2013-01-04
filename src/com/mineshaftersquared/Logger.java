package com.mineshaftersquared;

public class Logger {
	
	private static String logName = "[Mineshafter Squared]";
	
	
	public static void logln(String output)
	{
		log(output + "\n");
	}
	
	public static void log(String output)
	{
		System.out.print(logName + " " + output);
	}
}

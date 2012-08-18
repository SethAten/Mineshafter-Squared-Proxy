package com.mineshaftersquared;

import java.io.File;
import java.io.IOException;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MS2Logger
{
	private static Logger logger;
	private static String fileName = "mineshaftersquared.log";
	
	public MS2Logger()
	{
		this(".");
	}
	
	public MS2Logger(File file)
	{
		this(file.toString());
	}
	
	public MS2Logger(String location)
	{
		try 
		{
			// get the logger
			logger = Logger.getLogger("Mineshafter Squared");		
			logger.setUseParentHandlers(false);

			// remove and handlers that will be replaced
			Handler[] handlers = logger.getHandlers();
			for(Handler handler : handlers)
			{
			    if(handler.getClass() == ConsoleHandler.class)
			        logger.removeHandler(handler);
			}
			
			// setup the file
			File file = new File(location + "/" + fileName);
			
			// file handler
			FileHandler fh = new FileHandler(file.toString(), true);
			fh.setFormatter(new MS2Formatter());
			logger.addHandler(fh);
			
			// console handler
			ConsoleHandler ch = new ConsoleHandler();
			ch.setFormatter(new MS2Formatter());
			logger.addHandler(ch);
			
			// remove and handlers that will be replaced
			logger.setLevel(Level.INFO);
		} 
		catch (SecurityException e) 
		{
			e.printStackTrace();
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}
	}
	
	public void setLevel(Level level)
	{
		logger.setLevel(level);
	}
	
	public void info(String message)
	{
		logger.info(message);
	}
	
	public void severe(String message)
	{
		logger.severe(message);
	}
	
	public void fine(String message)
	{
		logger.fine(message);
	}
	
	public void finer(String message)
	{
		logger.finer(message);
	}
	
	public void finest(String message)
	{
		logger.finest(message);
	}
	
	public void config(String message)
	{
		logger.config(message);
	}
}

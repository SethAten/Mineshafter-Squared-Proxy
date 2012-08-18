package com.mineshaftersquared;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import org.json.JSONException;
import org.json.JSONObject;

import mineshafter.programs.MineServer;
import mineshafter.proxy.MineProxyHandler;

public class ConsoleStream extends Thread {
	// tbd
	private String url = "http://alpha.mineshaftersquared.com/process/sendLog";
	private String logFile = "server.log";
	private String version = "0.1.0";
	
	private MS2Logger logger = new MS2Logger();
	private BufferedReader reader;
	private boolean enabled = true;
	private int sleepTime = 60000;
	private boolean online = false;
	
	public ConsoleStream()
	{
		logger.info("Console Stream Version: " + version);
		
		try
		{
			// get file
			reader = new BufferedReader(new FileReader(logFile));
			// get to end
			while(reader.readLine() != null);
		} 
		catch (FileNotFoundException ex) 
		{
			logger.severe(ex.toString());
		} 
		catch (IOException ex) 
		{
			logger.severe(ex.toString());
		}
	}
	
	public void run() 
	{
        while(enabled) 
        {	
        	String content = new String();
        	String serverId = MineServer.getId();
        	
			if(online)
			{
				// gather data
				String log = readConsoleLog();
				String hardware = getHardwareLoad().toString();
				// build post data
				content = "content=" + log + "&server=" + serverId + "&hardware=" + hardware;
			}
			else
			{
				// build post data
				content = "server=" + serverId;
			}
			
			// send Request and Process
			processResponse(sendRequest(content));
			
			sleep();
        }
        
        destruct();
    }
	
	private void processResponse(JSONObject json)
	{
		try
		{
			// only change what is available (cuts down on bandwidth)
			if(json.has("enabled"))
			{
				enabled = Boolean.parseBoolean(json.getString("enabled"));
			}
			
			if(json.has("sleepTime"))
			{
				sleepTime = Integer.parseInt(json.getString("sleepTime"));
			}
			
			if(json.has("url"))
			{
				url = json.getString("url");
			}
			
			if(json.has("online"))
			{
				boolean newStatus = Boolean.parseBoolean(json.getString("online"));
				
				if(newStatus != online)
				{
					if(newStatus)
						logger.info("Console Online");
					else
						logger.info("Console Offline");
				}
				
				online = newStatus;
			}
		}
		catch(JSONException ex)
		{
			logger.severe(ex.toString());
		}
	}
	
	private String readConsoleLog()
	{
		String line = new String();
		String buffer = new String();
		
		try 
		{
			while((line = reader.readLine()) != null)
			{
				buffer += line + "\n";
			}
		}
		catch(IOException ex)
		{
			logger.severe(ex.toString());
		}
		
		return buffer;
	}
	
	private JSONObject getHardwareLoad()
	{
		Runtime rt = Runtime.getRuntime();
		JSONObject hardware = new JSONObject();
		JSONObject memory = new JSONObject();
		
		// memory
		try
		{
			memory.put("free", rt.freeMemory());
			memory.put("total", rt.totalMemory());
			memory.put("max", rt.maxMemory());
			hardware.put("memory", memory);
		}
		catch(JSONException ex)
		{
			logger.severe(ex.toString());
		}
		
		return hardware;
	}
	
	private String getData(byte[] responseData)
	{
		String response = new String();
		if(responseData != null)
		{
			response = new String(responseData);
		}
		
		return response;
	}
	
	private JSONObject sendRequest(String content)
	{
		String response = getData(MineProxyHandler.postRequest(url, content, "application/x-www-form-urlencoded"));
		
		try 
		{
			return new JSONObject(response);
		} 
		catch (JSONException e) 
		{
			return new JSONObject();
		}
	}
	
	private void sleep()
	{
		try 
		{
			Thread.sleep(sleepTime);
		} 
		catch (InterruptedException ex) 
		{
			logger.severe(ex.toString());
		} 
	}
	
	private void destruct()
	{
		try 
        {
			reader.close();
		} 
        catch (IOException ex) 
        {
			logger.severe(ex.toString());
		}
	}
}
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
	private String version = "0.0.10";
	
	private BufferedReader reader;
	private String line = null;
	private String buffer = null;
	private boolean enabled = true;
	private int sleepTime = 5000;
	private boolean online = false; // this is the reason
	
	public ConsoleStream()
	{
		Logger.logln("Console Stream Version " + version);
		
		try {
			reader = new BufferedReader(new FileReader("server.log"));
			while((line = reader.readLine()) != null);
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		buffer = new String();
	}
	
	public void run() {
		try{
			// enter loop
	        while(enabled) 
	        {
				line = reader.readLine();
				
	        	if(line == null)
	        	{
	        		if(!buffer.isEmpty() && buffer != null)
	        		{
	        			// create request
	        			String serverId = MineServer.getId();
	        			String hardware = getHardwareLoad().toString();
	        			
		        		String content = "content=" + buffer + 
		        						 "&server=" + serverId + 
		        						 "&hardware=" + hardware;
		        		
		        		// send request
		        		Logger.logln("Sending...");
						String response = getData(MineProxyHandler.postRequest(url, content, "application/x-www-form-urlencoded"));
						Logger.logln("Response: " + response);
						buffer = new String(); // clear buffer
						
						// deal with response
						JSONObject json = new JSONObject(response);
						
						// only change what is available (cuts down on bandwidth)
						if(json.has("enabled"))
							enabled = Boolean.parseBoolean(json.getString("enabled"));
						if(json.has("sleepTime"))
							sleepTime = Integer.parseInt(json.getString("sleepTime"));
						if(json.has("url"))
							url = json.getString("url");
	        		}
	        		
					Thread.sleep(sleepTime); 
	        	}
	        	else if(online)
	        	{
	        		// gather
	        		buffer += line + "\n";	      
	        	}
	        }
	        
	        // cleanup
	        reader.close();
	        
		} catch(IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		}
    }
	
	private JSONObject getHardwareLoad() throws JSONException
	{
		Runtime rt = Runtime.getRuntime();
		JSONObject hardware = new JSONObject();
		JSONObject memory = new JSONObject();
		
		// memory
		memory.put("free", rt.freeMemory());
		memory.put("total", rt.totalMemory());
		memory.put("max", rt.maxMemory());
		
		hardware.put("memory", memory);
		
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
	
}
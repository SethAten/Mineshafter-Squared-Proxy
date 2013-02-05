package mineshafter.proxy;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.Socket;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.mineshaftersquared.Logger;

import mineshafter.util.Streams;

public class MineProxyHandler extends Thread {
	private DataInputStream fromClient;
	private DataOutputStream toClient;
	private Socket connection;
	
	private MineProxy proxy;
	
	private static String[] BLACKLISTED_HEADERS = new String[]{"Connection", "Proxy-Connection", "Transfer-Encoding"};
	public static Pattern HELP_TICKET_LOGIN_REGEX = Pattern.compile("\\b[0-9]{13}\\b:\\b\\w+\\b:\\S+:\\b[0-9|a-z]+\\b:\\b[0-9|a-z]+\\b");
	
	public MineProxyHandler(MineProxy proxy, Socket conn) throws IOException {
		setName("MineProxyHandler Thread");
		
		this.proxy = proxy;
		
		connection = conn;
		fromClient = new DataInputStream(conn.getInputStream());
		toClient = new DataOutputStream(conn.getOutputStream());
	}
	
	public void run() {
		HashMap<String, String> headers = new HashMap<String, String>();
		
		// Read the incoming request
	    String[] requestLine = readUntil(fromClient, '\n').split(" ");
	    String method = requestLine[0].trim().toUpperCase();
	    String url = requestLine[1].trim();
	    
	    Logger.log("Request: " + method + " " + url);
	 	
		// Read the incoming headers
	 	String header;
		do {
			header = readUntil(fromClient, '\n').trim();
			
			int splitPoint = header.indexOf(':');
			if (splitPoint != -1) {
				headers.put(header.substring(0, splitPoint).toLowerCase()
						.trim(), header.substring(splitPoint + 1).trim());
			}

		} while (header.length() > 0);
		
		// run matchers
		Matcher skinMatcher = MineProxy.SKIN_URL.matcher(url);
		Matcher cloakMatcher = MineProxy.CLOAK_URL.matcher(url);
		Matcher getversionMatcher = MineProxy.GETVERSION_URL.matcher(url);
		Matcher joinserverMatcher = MineProxy.JOINSERVER_URL.matcher(url);
		Matcher checkserverMatcher = MineProxy.CHECKSERVER_URL.matcher(url);
		Matcher audiofix_url = MineProxy.AUDIOFIX_URL.matcher(url);
		Matcher dl_bukkit = MineProxy.DL_BUKKIT.matcher(url);
		Matcher client_snoop = MineProxy.CLIENT_SNOOP.matcher(url);
		Matcher server_snoop = MineProxy.SERVER_SNOOP.matcher(url);
		
		
		byte[] data = null;
		String contentType = null;
		String params;
		
		// If Skin Request
		if(skinMatcher.matches()) {
			Logger.log("Skin");
			
			String username = skinMatcher.group(1);
			if(proxy.skinCache.containsKey(username)) { // Is the skin in the cache?
				Logger.log("Skin from cache");
				
				data = proxy.skinCache.get(username);  // Then get it from there
			} else {
				url = "http://" + MineProxy.authServer + "/game/get_skin/" + username;
				
				Logger.log("To: " + url);
				
				data = getRequest(url); // Then get it...
				Logger.log("Response length: " + data.length);
				
				proxy.skinCache.put(username, data); // And put it in there
			}
			
		} 
		// If Cloak Request
		else if(cloakMatcher.matches()) {
			Logger.log("Cloak");
			
			String username = cloakMatcher.group(1);
			if(proxy.cloakCache.containsKey(username)) {
				Logger.log("Cloak from cache");
				data = proxy.cloakCache.get(username);
			} else {
				url = "http://" + MineProxy.authServer + "/game/get_cloak/" + username;
				
				Logger.log("To: " + url);
				
				data = getRequest(url);
				Logger.log("Response length: " + data.length);
				
				proxy.cloakCache.put(username, data);
			}
			
		} 
		// If Version Request
		else if(getversionMatcher.matches()) {
			Logger.log("GetVersion");
			
			url = "http://" + MineProxy.authServer + "/game/get_version/";
			Logger.log("To: " + url);
			
			try {
				int postlen = Integer.parseInt(headers.get("content-length"));
				char[] postdata = new char[postlen];
				InputStreamReader reader = new InputStreamReader(fromClient);
				reader.read(postdata);

				String postString = new String();
				for(char c : postdata) {
					postString += c;
				}
				
				Logger.log(postString);

				data = postRequest(url, new String(postdata), "application/x-www-form-urlencoded");
				
				String response = new String(data);
				
				if(HELP_TICKET_LOGIN_REGEX.matcher(response).matches()) {
					// TODO: Flip Help Toggle
				}
			} catch(IOException ex) {
				Logger.log("Unable to read POST data from getversion request: " + ex.getLocalizedMessage());
			}
		} 
		// If JoinServer Request
		else if(joinserverMatcher.matches()) {
			Logger.log("JoinServer");
			
			params = joinserverMatcher.group(1);
			url = "http://" + MineProxy.authServer + "/game/join_server" + params;
			Logger.log("To: " + url);
			data = getRequest(url);
			contentType = "text/plain";
			// TODO There may be a bug here, keeps causing a hang in the MC thread that tries to read the data from it
		}
		// If Check Server Request
		else if(checkserverMatcher.matches()) {
			Logger.log("CheckServer");
			
			params = checkserverMatcher.group(1);
			url = "http://" + MineProxy.authServer + "/game/check_server" + params;
			Logger.log("To: " + url);
			data = getRequest(url);
			
		} else if(audiofix_url.matches()) {  // this is to fix the audio problems
			Logger.log("Audio Fix");
			url = "http://s3.amazonaws.com/MinecraftResources/";
			Logger.log("To: " + url);
			data = getRequest(url);
		} else if(dl_bukkit.matches()) {
			Logger.log("Bukkit Fix");
			data = getRequest(url);
		} else if(client_snoop.matches()) // tmp for now since else does not seem to handle these dont have time to look into it
		{
			params = client_snoop.group(1);
			url = "http://snoop\\.minecraft\\.net/client"+params;
			
			Logger.log("To: " + url);
			
			try {
				int postlen = Integer.parseInt(headers.get("content-length"));
				char[] postdata = new char[postlen];
				InputStreamReader reader = new InputStreamReader(fromClient);
				reader.read(postdata);
				
				data = postRequest(url, new String(postdata), "application/x-www-form-urlencoded");
				
			} catch(IOException ex) {
				Logger.log("Unable to read POST data from getversion request: " + ex.getLocalizedMessage());
			}
		} 
		else if(server_snoop.matches())
		{
			params = server_snoop.group(1);
			url = "http://snoop\\.minecraft\\.net/server"+params;
			
			Logger.log("To: " + url);
			
			try 
			{
				int postlen = Integer.parseInt(headers.get("content-length"));
				char[] postdata = new char[postlen];
				InputStreamReader reader = new InputStreamReader(fromClient);
				reader.read(postdata);
				
				data = postRequest(url, new String(postdata), "application/x-www-form-urlencoded");
				
			} 
			catch(IOException ex) 
			{
				Logger.log("Unable to read POST data from getversion request: " + ex.getLocalizedMessage());
			}
		}
		// If Any other network request
		else {
			Logger.log("No handler. Piping.");
			
			try {
				if(!url.startsWith("http://") && !url.startsWith("https://")) {
					url = "http://" + url;
				}
				URL u = new URL(url);
				if(method.equals("CONNECT")) {
					int port = u.getPort();
					if(port == -1) port = 80;
					Socket sock = new Socket(u.getHost(), port);
					
					Streams.pipeStreamsActive(sock.getInputStream(), toClient);
					Streams.pipeStreamsActive(connection.getInputStream(), sock.getOutputStream());
					// TODO Maybe put POST here instead, less to do, but would it work?
					
					// to avoid a resource leak
					sock.close();
					
				} else if(method.equals("GET")  || method.equals("POST")) {
					HttpURLConnection c = (HttpURLConnection) u.openConnection(Proxy.NO_PROXY);
					c.setRequestMethod(method);
					boolean post = method.equals("POST");
					
					for(String k : headers.keySet()) {
						c.setRequestProperty(k, headers.get(k)); // TODO Might need to blacklist these as well later
					}
					
					if (post) {
						c.setDoInput(true);
						c.setDoOutput(true);
						c.setUseCaches(false);
						c.connect();
						int postlen = Integer.parseInt(headers.get("content-length"));
						byte[] postdata = new byte[postlen];
						fromClient.read(postdata);
						DataOutputStream os = new DataOutputStream(c.getOutputStream());
						os.write(postdata);
					}
					
					int responseCode = c.getResponseCode();
					String res = "HTTP/1.0 " + responseCode + " " + c.getResponseMessage() + "\r\n";
					res += "Connection: close\r\nProxy-Connection: close\r\n";
					
					java.util.Map<String, java.util.List<String>> h = c.getHeaderFields();
					headerloop:
					for(String k : h.keySet()) {
						if(k == null) continue;
						
						k = k.trim();
						
						for(String forbiddenHeader : BLACKLISTED_HEADERS)
						{
							if(k.equalsIgnoreCase(forbiddenHeader)) 
								continue headerloop;
						}
						
						java.util.List<String> vals = h.get(k);
						for(String v : vals) 
						{
							res += k + ": " + v + "\r\n";
						}
					}
					res += "\r\n";
					
					int size = 0;
					if (responseCode / 100 != 5) {
						toClient.writeBytes(res);
						size = Streams.pipeStreams(c.getInputStream(), toClient);
					}
					
					toClient.close();
					connection.close();
					
					Logger.log("Piping finished, data size: " + size);
					
				} else if (method.equals("HEAD")) {
					HttpURLConnection c = (HttpURLConnection) u.openConnection(Proxy.NO_PROXY);
					c.setRequestMethod("HEAD");
					
					for (String k : headers.keySet()){
						c.setRequestProperty(k, headers.get(k));
					}
					
					String res = "HTTP/1.0 " + c.getResponseCode() + " " + c.getResponseMessage() + "\r\n";
					res += "Proxy-Connection: close\r\n";
					
					java.util.Map<String, java.util.List<String>> h = c.getHeaderFields();
					for (String k : h.keySet()) {
						if(k == null) continue;
						java.util.List<String> vals = h.get(k);
						for(String v : vals) {
							res += k + ": " + v + "\r\n";
						}
					}
					res += "\r\n";
					
					toClient.writeBytes(res); // TODO Occasional exception socket write error
					toClient.close();
					connection.close();
				} else {
					Logger.log("UNEXPECTED REQUEST TYPE: " + method);
				}
				
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			return;
		}
		
		try {
			if (data != null) {
				this.toClient.writeBytes("HTTP/1.0 200 OK\r\nConnection: close\r\nProxy-Connection: close\r\nContent-Length: " + data.length + "\r\n");
				
				if (contentType != null) {
					toClient.writeBytes("Content-Type: " + contentType + "\r\n");
				}

				toClient.writeBytes("\r\n");
				toClient.write(data);
				toClient.flush();
			}
			
			this.toClient.close();
			this.connection.close();
			fromClient.close();
			toClient.close();
			connection.close();
		} 
		catch (IOException ex) 
		{
			Logger.log("Error: " + ex.getLocalizedMessage());
		}
	}
	
	public static byte[] getRequest(String url) {
		try {
			HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection(Proxy.NO_PROXY);
			conn.setInstanceFollowRedirects(false);
			Map<String, List<String>> requestHeaders = conn.getRequestProperties();
			int code = conn.getResponseCode();
			
			if(code == 301 || code == 302 || code == 303) {
				Logger.log("Java didn't redirect automatically, going manual: " + Integer.toString(code));
				String l = conn.getHeaderField("location").trim();
				Logger.log("Manual redirection to: " + l);
				return getRequest(l);
			}
			
			Logger.log("Response: " + code);
			
			if(code == 403) {
				String s = "403 from req to " + url + "\nRequest headers:\n";
				
				for (String k : requestHeaders.keySet()) {
					if(k == null) continue;
					java.util.List<String> vals = requestHeaders.get(k);
					for(String v : vals) {
						s += k + ": " + v + "\n";
					}
				}
				
				s += "Response headers:\n";
				
				Map<String, List<String>> responseHeaders = conn.getHeaderFields();
				for (String k : responseHeaders.keySet()) {
					if(k == null) continue;
					java.util.List<String> vals = responseHeaders.get(k);
					for(String v : vals) {
						s += k + ": " + v + "\n";
					}
				}
				
				Logger.log(s);
				Logger.log("Contents:\n" + new String(grabData(conn.getErrorStream())));
			}
			
			if(code / 100 == 4) {
				return new byte[0];
			}
			
			BufferedInputStream in = new BufferedInputStream(conn.getInputStream());
			
			return grabData(in);
			
		} catch (MalformedURLException ex) {
			Logger.log("Bad URL in getRequest: " + ex.getLocalizedMessage());
		} catch (IOException ex) {
			Logger.log("IO error during a getRequest: " + ex.getLocalizedMessage());
		}
		
		return new byte[0];
	}
	
	public static byte[] postRequest(String url, String postdata, String contentType) {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		OutputStreamWriter writer = new OutputStreamWriter(out);
		
		try {
			writer.write(postdata);
			writer.flush();
		} catch(IOException e) {
			e.printStackTrace();
		}
		
		byte[] rd = postRequest(url, out.toByteArray(), contentType);
		
		return rd;
	}
	
	public static byte[] postRequest(String url, byte[] postdata, String contentType) {
		try {
			URL u = new URL(url);
			
			HttpURLConnection c = (HttpURLConnection) new URL(url).openConnection(Proxy.NO_PROXY);
			c.setDoOutput(true);
			c.setRequestMethod("POST");
			
			c.setRequestProperty("Host", u.getHost());
			c.setRequestProperty("Content-Length", Integer.toString(postdata.length));
			c.setRequestProperty("Content-Type", contentType);
			
			BufferedOutputStream out = new BufferedOutputStream(c.getOutputStream());
			out.write(postdata);
			out.flush();
			out.close();
			
			byte[] data = grabData(new BufferedInputStream(c.getInputStream()));
			return data;
			
		} catch(java.net.UnknownHostException ex) {
			Logger.log("Unable to resolve remote host, returning null: " + ex.getLocalizedMessage());
		} catch (MalformedURLException ex) {
			Logger.log("Bad URL when doing postRequest: " + ex.getLocalizedMessage());
		} catch (IOException ex) {
			Logger.log("Error: " + ex.getLocalizedMessage());
		}
		
		return null;
	}
	
	public static byte[] grabData(InputStream in) {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		byte[] buffer = new byte[1024];
		
		while(true) {
			int len;
			try {
				len = in.read(buffer);
				if(len == -1) break;
			} catch(IOException e) {
				break;
			}
			out.write(buffer, 0, len);
		}
		
		return out.toByteArray();
	}
	

	public static String readUntil(DataInputStream is, String endSequence) {
		return readUntil(is, endSequence.getBytes());
	}

	public static String readUntil(DataInputStream is, char endSequence) {
		return readUntil(is, new byte[] { (byte) endSequence });
	}

	public static String readUntil(DataInputStream is, byte endSequence) {
		return readUntil(is, new byte[] { endSequence });
	}

	public static String readUntil(DataInputStream is, byte[] endSequence) { // If there is an edge case, make sure we can see it
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		String r = null;

		try {
			int i = 0;
			
			while (true) {
				boolean end = false;
				byte b = is.readByte(); // Read a byte
				if (b == endSequence[i]) { // If equal to current byte of endSequence
					if (i == endSequence.length - 1) {
						end = true; // If we hit the end of endSequence, we're done
					}
					
					i++; // Increment for next round
				} else {
					i = 0; // Reset
				}

				out.write(b);
				if (end)
					break;
			}
		} catch (IOException ex) {
			Logger.log("readUntil unable to read from InputStream, endSeq: " + new String(endSequence));
			Logger.log("Error: " + ex.getLocalizedMessage());
		}

		try {
			r = out.toString("UTF-8");
		} catch (java.io.UnsupportedEncodingException ex) {
			Logger.log("readUntil unable to encode data: " + out.toString());
			Logger.log("Error: " + ex.getLocalizedMessage());
		}
		
		return r;
	}
}

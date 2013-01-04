package mineshafter.proxy;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Hashtable;
import java.util.regex.Pattern;
import java.net.BindException;

import com.mineshaftersquared.Logger;
import com.mineshaftersquared.Version;


public class MineProxy extends Thread {
	public static String authServer;
	public Version version;
	private int port = -1;
	
	// Patterns
	public static Pattern SKIN_URL = Pattern.compile("http://skins\\.minecraft\\.net/MinecraftSkins/(.+?)\\.png");
 	public static Pattern CLOAK_URL = Pattern.compile("http://skins\\.minecraft\\.net/MinecraftCloaks/(.+?)\\.png");
	public static Pattern GETVERSION_URL = Pattern.compile("http://session\\.minecraft\\.net/game/getversion\\.jsp");
	public static Pattern JOINSERVER_URL = Pattern.compile("http://session\\.minecraft\\.net/game/joinserver\\.jsp(.*)");
	public static Pattern CHECKSERVER_URL = Pattern.compile("http://session\\.minecraft\\.net/game/checkserver\\.jsp(.*)");
	public static Pattern AUDIOFIX_URL = Pattern.compile("http://s3\\.amazonaws\\.com/MinecraftResources/");
	public static Pattern CLIENT_SNOOP = Pattern.compile("http://snoop\\.minecraft\\.net/client(.*)");
	public static Pattern SERVER_SNOOP = Pattern.compile("http://snoop\\.minecraft\\.net/server(.*)");
	public static Pattern DL_BUKKIT = Pattern.compile("http://dl.bukkit.org/(.+?)");
	
	/* NTS: See if this is still needed */
	public Hashtable<String, byte[]> skinCache;
	public Hashtable<String, byte[]> cloakCache;
	
	public MineProxy(Version version, String currentAuthServer) {
		setName("MineProxy Thread");
		
		MineProxy.authServer = currentAuthServer; // TODO maybe change this leave it for now 
		
		this.version = version;
		
		skinCache = new Hashtable<String, byte[]>();
		cloakCache = new Hashtable<String, byte[]>();
	}
	
	@SuppressWarnings("resource")
	public void run() {
		try {
			ServerSocket server = null;
			int port = 9010; // A lot of other applications use the 80xx range,
								// let's try for some less crowded real-estate
			while (port < 12000) { // That should be enough
				try {
					Logger.logln("Trying to proxy on port " + port);
					byte[] loopback = {127, 0, 0, 1};
					server = new ServerSocket(port, 16, InetAddress.getByAddress(loopback));
					this.port = port;
					Logger.logln("Proxying successful");
					break;
				} catch (BindException ex) {
					port++;
				}

			}
			
			while(true) {
				Socket connection = server.accept();
				
				MineProxyHandler handler = new MineProxyHandler(this, connection);
				handler.start();
			}
		} catch(IOException ex) {
			Logger.logln("Error in server accept loop: " + ex.getLocalizedMessage());
		}
	}
	

	public int getPort() {
		while (port < 0) {
			try {
				sleep(50);
			} catch (InterruptedException ex) {
				Logger.logln("Interrupted while waiting for port: " + ex.getLocalizedMessage());
			}
		}
		
		return port;
	}
}

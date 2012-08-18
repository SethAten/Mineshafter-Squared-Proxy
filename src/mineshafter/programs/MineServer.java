package mineshafter.programs;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import sun.applet.Main;
import java.net.URLClassLoader;
import java.util.jar.JarFile;
import java.lang.reflect.Method;
import java.util.jar.Attributes;
import com.mineshaftersquared.ConsoleStream;
import com.mineshaftersquared.MS2Logger;
import com.mineshaftersquared.Settings;

import mineshafter.proxy.MineProxy;
import mineshafter.util.SimpleRequest;

@SuppressWarnings("restriction")
public class MineServer {
	protected static String VERSION = "3.8.0";

	protected static String authServer = new String();
	protected static File mineshaftersquaredPath;
	protected static String gamePath;
	protected static String versionPath;
	protected static Settings settings;
	protected static MS2Logger logger = new MS2Logger();
	protected static ConsoleStream consoleStream = new ConsoleStream();

	public static void main(String[] args) throws FileNotFoundException, IOException {
		Settings.setProgramType(Settings.Mode.SERVER);
		settings = new Settings();
		
		// Get Update Info
		authServer = settings.get("auth");
		
		// check for updates
		if(MS2Update())
		{
			logger.info("An update for Mineshafter Squared is available, please go to " + authServer + " and redownload the proxy client.");
			System.exit(0);
		}
		
		startConsoleStream();
		
		launchProxyAndServer(args);
	}
	
	private static void launchProxyAndServer(String[] args)
	{
		try {
			// Create MineProxy
			MineProxy proxy = new MineProxy(VERSION, authServer);
			proxy.start(); // start Proxy
			int proxyPort = proxy.getPort();
			
			System.setProperty("http.proxyHost", "127.0.0.1");
			System.setProperty("http.proxyPort", Integer.toString(proxyPort));
			
			// Try to load provided jar from console. If none is present fall
			// back to default server jar.
			String load;
			try {
				load = args[0];
			} catch (ArrayIndexOutOfBoundsException e) {
				load = "minecraft_server.jar";
			}

			@SuppressWarnings("resource")
			Attributes attributes = new JarFile(load).getManifest().getMainAttributes();
			String name = attributes.getValue("Main-Class");
			
			URLClassLoader cl = null;
			Class<?> cls = null;
			Method main = null;
			try {
				cl = new URLClassLoader(new URL[] { new File(load).toURI().toURL() }, Main.class.getClassLoader());
				cls = cl.loadClass(name);
				main = cls.getDeclaredMethod("main", new Class[] { String[].class });
			} catch (Exception e) {
				System.out.println("Error loading class " + name + " from jar " + load + ":");
				e.printStackTrace();
				System.exit(1);
			}
			
			String[] nargs;
			try {
				nargs = new String[args.length - 1];
				System.arraycopy(args, 1, nargs, 0, nargs.length);
			} catch (Exception e) {
				nargs = new String[0];
			}
			
			main.invoke(cls, new Object[] { nargs });
		} 
		catch (Exception e) 
		{
			logger.severe("Something bad happened: " + e.getMessage());
			e.printStackTrace();
			System.exit(1);
		}
	}
	
	private static boolean MS2Update()
	{
			// old "http://" + authServer + "/update.php?name=client&build=" + buildNumber
			String updateInfo = new String(SimpleRequest.get("http://" + authServer + "/update/server/new"));
			
			// Print Proxy Version Numbers to Console
			logger.info("Current proxy version: " + VERSION);
			logger.info("Gotten proxy version: " + updateInfo);
			
			// tell user to update if not at latest version
			if(updateInfo.equals(VERSION))
				return false;
			else
				return true;
	}
	
	public static ConsoleStream getConsoleStream()
	{
		return consoleStream;
	}
	
	public static void startConsoleStream()
	{
		consoleStream.start();
	}
	
	public static String getId()
	{
		String serverId = settings.get("server");
		
		if(serverId == null)
		{
			serverId = "0";
			settings.set("server", serverId);
			settings.save();
		}
		
		return serverId;
	}
}
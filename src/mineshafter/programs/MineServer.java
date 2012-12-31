package mineshafter.programs;

import java.io.File;
import java.net.URL;
import sun.applet.Main;
import java.net.URLClassLoader;
import java.util.jar.JarFile;
import java.lang.reflect.Method;
import java.util.jar.Attributes;

import com.mineshaftersquared.Logger;
import com.mineshaftersquared.Settings;
import com.mineshaftersquared.Version;

import mineshafter.proxy.MineProxy;
import mineshafter.util.SimpleRequest;

@SuppressWarnings("restriction")
public class MineServer {
	protected static final Version VERSION = new Version(3, 8, 1);

	protected static String authServer = new String();
	protected static File mineshaftersquaredPath;
	protected static String gamePath;
	protected static String versionPath;
	protected static Settings settings;

	public static void main(String[] args) {
		// Get Update Info
		settings = new Settings(new File("."));
		authServer = settings.get("auth");
		
		// check for updates
		if(MS2Update())
		{
			Logger.logln("An update for Mineshafter Squared is available, please go to " + authServer + " and redownload the proxy client.");
			System.exit(0);
		}
		
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

			JarFile jar 			= new JarFile(load);
			Attributes attributes 	= jar.getManifest().getMainAttributes();
			String name 			= attributes.getValue("Main-Class");
			
			// close the jar to avoid a resource leak
			jar.close();
			
			URLClassLoader cl 	= null;
			Class<?> cls 		= null;
			Method main 		= null;
			try {
				cl 		= new URLClassLoader(new URL[] { new File(load).toURI().toURL() }, Main.class.getClassLoader());
				cls 	= cl.loadClass(name);
				main 	= cls.getDeclaredMethod("main", new Class[] { String[].class });
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
		} catch (Exception e) {
			Logger.logln("Something bad happened:");
			e.printStackTrace();
			System.exit(1);
		}
	}
	
	private static boolean MS2Update()
	{
		String updateInfo = new String(SimpleRequest.get("http://" + authServer + "/update/client/"));
		
		// Print Proxy Version Numbers to Console
		System.out.println("Current proxy version: " + VERSION);
		System.out.println("Gotten proxy version: " + updateInfo);
		
		// create version object out of latest version
		Version latestVersion = new Version(updateInfo);
		
		// tell user to update if not at latest version
		if(VERSION.updateTo(latestVersion))
			return true;
		else
			return false;
	}
}
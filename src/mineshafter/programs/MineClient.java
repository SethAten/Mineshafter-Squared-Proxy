package mineshafter.programs;

import java.applet.Applet;
import java.awt.Frame;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import javax.swing.JOptionPane;

import mineshafter.proxy.MineProxy;
import mineshafter.util.Resources;
import mineshafter.util.SimpleRequest;
import mineshafter.util.Streams;
import net.minecraft.Util;
import sun.applet.Main;

import com.mineshaftersquared.Logger;
import com.mineshaftersquared.Settings;
import com.mineshaftersquared.Version;

@SuppressWarnings("restriction")
public class MineClient extends Applet {
	private static final long serialVersionUID = 1L;
	protected static final Version VERSION = new Version(3, 9, 0);
	
	protected static String launcherDownloadURL = "https://s3.amazonaws.com/MinecraftDownload/launcher/minecraft.jar";
	protected static String normalLauncherFilename = "minecraft.jar";
	protected static String hackedLauncherFilename = "minecraft_modified.jar";
	protected static String MANIFEST_TEXT = "Manifest-Version: 1.2\nCreated-By: 1.6.0_22 (Sun Microsystems Inc.)\nMain-Class: net.minecraft.MinecraftLauncher\n";
	
	/* Added For MineshafterSquared */
	protected static String authServer = new String();
	protected static File mineshaftersquaredPath;
	protected static String gamePath;
	protected static String versionPath;
	protected static Settings settings;
	
	public void init() {
		MineClient.main(new String[0]);
	}
	
	public static void main(String[] args) 
	{
		// Get Update Info
		File gamePath = Util.getWorkingDirectory(); // test
		mineshaftersquaredPath = new File(gamePath.toString().replace("minecraft", "mineshaftersquared"));
		
		if(!mineshaftersquaredPath.exists())
			mineshaftersquaredPath.mkdir();
		
		settings = new Settings(mineshaftersquaredPath);
		
		authServer = settings.get("auth");
		Logger.setFile(mineshaftersquaredPath + "/" + settings.get("log-file"));
		
		// check for MS2 updates
		if(MS2Update())
		{
			JOptionPane.showMessageDialog(null, "An update for Mineshafter Squared is available, please go to " + authServer + " and redownload the proxy client.", "Update Available", JOptionPane.PLAIN_MESSAGE);
			System.exit(0);
		}
		
		// get everything going
		if(startProxy())
			startLauncher(args);
	}
	
	private static boolean MS2Update()
	{
		String updateInfo = new String();
		try {
			updateInfo = new String(SimpleRequest.get("http://" + authServer + "/update/client/"));
		} catch(Exception ex) {
			Logger.log("Problem connecting to auth server");
			JOptionPane.showMessageDialog(null, "Could not connect to auth server. Please make sure you are online and pointing to a valid auth server.", "Critical Error", JOptionPane.ERROR_MESSAGE);
			System.exit(1);
		}
		
		// Print Proxy Version Numbers to Console
		Logger.log("Current proxy version: " + VERSION);
		Logger.log("Gotten proxy version: " + updateInfo);
		
		// create version object out of latest version
		Version latestVersion = new Version(updateInfo);
		
		// tell user to update if not at latest version
		if(VERSION.updateTo(latestVersion))
			return true;
		else
			return false;
	}
	
	private static boolean startProxy()
	{
		try {
			// set minecraft downloads to Mineshafter Squared dir
			MineClient.normalLauncherFilename = mineshaftersquaredPath + "/" + MineClient.normalLauncherFilename;
			MineClient.hackedLauncherFilename = mineshaftersquaredPath + "/" + MineClient.hackedLauncherFilename;
						
			MineProxy proxy = new MineProxy(VERSION, authServer); // create proxy
			proxy.start(); // launch proxy
			int proxyPort = proxy.getPort();
			
			System.setProperty("http.proxyHost", "127.0.0.1");
			System.setProperty("http.proxyPort", Integer.toString(proxyPort));
			System.setProperty("java.net.preferIPv4Stack", "true");
			
			// Make sure we have a fresh launcher every time
			File hackedFile = new File(hackedLauncherFilename);
			if(hackedFile.exists()){ 
				hackedFile.delete();
			}
			// everything's peachy
			return true;
		} catch(Exception e) {
			Logger.log("Something bad happened:" + e);
			System.exit(1);
			// oops
			return false;
		}
	}
	
	private static void startLauncher(String[] args) {
		try {
			// if hacked game exists
			if(new File(hackedLauncherFilename).exists()) {
				URL u = new File(hackedLauncherFilename).toURI().toURL();
				@SuppressWarnings("resource")
				URLClassLoader cl = new URLClassLoader(new URL[]{u}, Main.class.getClassLoader());
				
				@SuppressWarnings("unchecked")
				Class<Frame> launcherFrame = (Class<Frame>) cl.loadClass("net.minecraft.LauncherFrame");
				
				String[] nargs;
				try{
					nargs = new String[args.length - 1];
					System.arraycopy(args, 1, nargs, 0, nargs.length); // Transfer the arguments from the process call so that the launcher gets them
				} catch(Exception e){
					nargs = new String[0];
				}
				
				Method main = launcherFrame.getMethod("main", new Class[]{ String[].class });
				main.invoke(launcherFrame, new Object[]{ nargs });
			}
			// if the normal game exists
			else if(new File(normalLauncherFilename).exists()) {
				editLauncher();
				startLauncher(args);
			}
			// 
			else {
				try{
					byte[] data = SimpleRequest.get(launcherDownloadURL);
					OutputStream out = new FileOutputStream(normalLauncherFilename);
					out.write(data);
					out.flush();
					out.close();
					startLauncher(args);
					
				} catch(Exception ex) {
					Logger.log("Error downloading launcher: " + ex.getLocalizedMessage());
					return;
				}
			}
		} catch(Exception ex) {
			Logger.log("Error starting launcher: " + ex.getLocalizedMessage());
			return;
		}
	}
	
	private static void editLauncher() {
		try {
			ZipInputStream in = new ZipInputStream(new FileInputStream(normalLauncherFilename));
			ZipOutputStream out = new ZipOutputStream(new FileOutputStream(hackedLauncherFilename));
			ZipEntry entry;
			String n;
			InputStream dataSource;
			while((entry = in.getNextEntry()) != null) {
				n = entry.getName();
				if(n.contains(".svn") 
						|| n.equals("META-INF/MOJANG_C.SF") 
						|| n.equals("META-INF/MOJANG_C.DSA") 
						|| n.equals("net/minecraft/minecraft.key") 
						|| n.equals("net/minecraft/Util$OS.class")) continue;
				
				out.putNextEntry(entry);
				if(n.equals("META-INF/MANIFEST.MF")) dataSource = new ByteArrayInputStream(MANIFEST_TEXT.getBytes());
				else if(n.equals("net/minecraft/Util.class")) dataSource = Resources.load("net/minecraft/Util.class");
				else dataSource = in;
				Streams.pipeStreams(dataSource, out);
				out.flush();
			}
			in.close();
			out.close();
		} catch(Exception ex) {
			Logger.log("Editing launcher failed: " + ex.getLocalizedMessage());
		}
	}
}
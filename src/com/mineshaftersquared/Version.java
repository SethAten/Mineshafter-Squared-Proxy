package com.mineshaftersquared;

public class Version {
	private int major;
	private int minor;
	private int fix;
	
	public Version(String verString) {
		String[] verArray = verString.split("[.]");
		
		major 	= Integer.parseInt(verArray[0]);
		minor 	= Integer.parseInt(verArray[1]);
		fix		= Integer.parseInt(verArray[2]);
	}
	
	public Version(int major, int minor, int fix) {
		this.major = major;
		this.minor = minor;
		this.fix   = fix;
	}
	
	public boolean updateTo(Version newerVersion) {	
		if(newerVersion.minor > minor || newerVersion.major > major)
			return true;
		else
			return false;
	}
	
	public String toString() {
		return new String(major + "." + minor + "." + fix);
	}
}

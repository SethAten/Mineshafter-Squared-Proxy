package com.mineshaftersquared;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.Date;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

public class MS2Formatter extends Formatter 
{	
	private static final String LINE_SEPARATOR = System.getProperty("line.separator");
	
	@Override
	public String format(LogRecord record) 
	{
		StringBuilder logBuilder = new StringBuilder();
		
		// add date
		logBuilder.append(new Date(record.getMillis()));
		
		// add level
		logBuilder.append(" [");
		logBuilder.append(record.getLevel().getLocalizedName());
		logBuilder.append("] ");
		
		// add message
		logBuilder.append(formatMessage(record));
		logBuilder.append(LINE_SEPARATOR);
		
		if (record.getThrown() != null) 
		{
            try 
            {
                StringWriter stringWriter = new StringWriter();
                PrintWriter printWriter = new PrintWriter(stringWriter);
                
                record.getThrown().printStackTrace(printWriter);
                printWriter.close();
                
                logBuilder.append(stringWriter.toString());
            } catch (Exception ex) { }
        }
		
		return logBuilder.toString();
	}

}

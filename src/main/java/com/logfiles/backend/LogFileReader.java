package com.logfiles.backend;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Predicate;

import com.logfiles.api.LogFile;
import com.logfiles.api.LogFileBuffer;

/**
 * This class reads any file in the order specify.
 * @author alexdel
 */
public class LogFileReader {
	/**
	 * File to be read.
	 */
	private File file;
	/** 
	 * Last position in the file after the reading.
	 */
	private long lastPos;
	
	/** 
	 * Constructor
	 * @param file File to be read.
	 */
	public LogFileReader(File file) {
		this.file = file;
		lastPos   = file.length();
	}
	
	/**
	 * Get file details: Path, name, size, host ip
	 * @return LogFile POJO with file details.
	 */
	public LogFile getFileDetails()
	{
		String path = file.getAbsolutePath();
		String name = file.getName();		
		Long size = file.length();
		String ipStr;
		try
		{
			InetAddress ip = InetAddress.getLocalHost();
			ipStr = ip.toString();
		} catch (UnknownHostException ue)
		{
			ipStr = "unknown";
		}
		
		
		return new LogFile(ipStr, path + name, size);
	}
	
	/**
	 * Read the file in order specified.
	 * @param  ascdesc         Order to read the file. ASC, DESC.
	 * @param  p               Predicate to filter lines.
	 * @return LogFileBuffer   POJO LogFileBuffer which contains lines read and last position in the file after reading.
	 * @throws IOException
	 */
	public LogFileBuffer readLines(ReadOrder ascdesc, 			                       
			                       Predicate<String> p) throws IOException
	{	
		long iniValue;
		if (ascdesc == ReadOrder.ASC)
		{
			iniValue = 0; 
		}
		else
		{
			iniValue = file.length();
		}
		return readLines(iniValue, file.length(), ascdesc, p);
	}
	
	/**
	 * Read numLines lines in order specified. 
	 * @param  numLines        Number of lines to be read.    
	 * @param  ascdesc         Order to read the file. ASC, DESC.
	 * @param  p               Predicate to filter lines.
	 * @return LogFileBuffer   POJO LogFileBuffer which contains lines read and last position in the file after reading.
	 * @throws IOException
	 */
	public LogFileBuffer readLines(long      numLines, 
			                       ReadOrder ascdesc, 			                       
			                       Predicate<String> p) throws IOException
	{
		long iniValue;
		if (ascdesc == ReadOrder.ASC)
		{
			iniValue = 0; 
		}
		else
		{
			iniValue = file.length();
		}
		return readLines(iniValue, numLines, ascdesc, p);
	}
	
	/**
	 * Read numLines lines in order specified from position lastP in the file.
	 * @param  lastP           Position on the file where the reading will start.
	 * @param  numLines        Number of lines to be read.
	 * @param  ascdesc         Order to read the file. ASC, DESC.
	 * @param  p               Predicate to filter lines.
	 * @return LogFileBuffer   POJO LogFileBuffer which contains lines read and last position in the file after reading.
	 * @throws IOException
	 */
	public LogFileBuffer readLines(long      lastP, 
			                       long      numLines, 
			                       ReadOrder ascdesc, 			                       
			                       Predicate<String> p) throws IOException
	{
		LogFileBuffer fileBuffer;		
		List<String> lines = new LinkedList<>();
		LogInputStream inputStream;		
		
		/* @todo: For now I only implement desc order, but I will implement asc order */
		if (ascdesc == ReadOrder.ASC)
		{
			inputStream = new LogAscRandInputStream(file, lastP, numLines);
		}
		else
		{
			inputStream = new LogDescRandInputStream(file, lastP, numLines);
		}
		
		BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
		long currLine = 0;
		
		String line = br.readLine();
				
		while(line != null && currLine < numLines)
		{					
			if (p.test(line))
			{
				lines.add(line);			
				currLine++;
			}
			line = br.readLine();				
		} 
		
		lastPos = inputStream.getLastPos();
		
		inputStream.close();
		br.close();
				
		fileBuffer = new LogFileBuffer(lines, lastPos);
		
		return fileBuffer;
	}
	
	/**
	 * Get the position on where the cursor in the read file is after do the reading.
	 * @return Position on file.
	 */
	public long getLastPos()
	{
		return lastPos;
	}

}

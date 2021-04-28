package com.logfiles.backend;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.logfiles.api.LogFile;

/**
 * This class read all the files on a directory
 * @author alexdel
 *
 */
public class LogFileDirectory {
	
	private LogFileReader logFileReader;
	
	/**
	 * Constructor
	 */
	public LogFileDirectory()	
	{
		this.logFileReader = new LogFileReader();
	}
	
	/** 
	 * Constructor
	 * @param logFileReader log file controller backend used to read every particular 
	 *                      file in the log directory
	 */
	public LogFileDirectory(LogFileReader logFileReader)
	{
		this.logFileReader = logFileReader;
	}
	
	/**
	 * Read all the files from a directory considering path, ordering and keyword if were specified.
	 * with specified query params
	 * @param path      Directory path
	 * @param ascdesc   Order: ascendant (0) | descendant(1)
	 * @param p         Predicates to filter lines with a keyword
	 * @return
	 */
	public List<LogFile> getAllFiles(File              path,		
            ReadOrder         ascdesc, 			                       
            Predicate<String> p)
	{
		File files[] = path.listFiles();
		List<File> filesList = Arrays.asList(files);
		
		/* Executes reading file in parallel way for every file specified */
		List <LogFile> logFiles = filesList.parallelStream().map(file -> readFile(file, ascdesc, p)).collect(Collectors.toList());			
		return logFiles;
	}

	/**
	 * Read all the files from a directory considering numLines, path, ordering and keyword if were specified.
	 * @param path       Log directory path
	 * @param numLines   Number of lines to be query
	 * @param ascdesc    Ordering
	 * @param p          keyword predicate to filter lines read.
	 * @return
	 */
	public List<LogFile> getAllFiles(File              path,
									 long              numLines, 
			                         ReadOrder         ascdesc, 			                       
			                         Predicate<String> p)
	{
		File files[] = path.listFiles();
		List<File> filesList = Arrays.asList(files);		
		
        /* Executes reading file in parallel way for every file specified */
		List <LogFile> logFiles = filesList.parallelStream().map(file -> readFile(file, numLines, ascdesc, p)).collect(Collectors.toList());			
		return logFiles;
	}
	
	/**
	 * Read a particular file considering only ordering and keyword
	 * @param file       File to be read
	 * @param ascdesc    Ordering read: ascendant(0), descendant(1)
	 * @param p          Keyword predicate
	 * @return
	 */
	private LogFile readFile(File file, ReadOrder ascdesc, Predicate<String> p)
	{
		LogFile logFile = null;
		try 
		{
			logFile = logFileReader.readLines(file, ascdesc, p); 
		} catch(IOException ioe)
		{
		}
		return logFile;			
	}
	
	/**
	 * Read a particular file considering numLines, ordering, keyword
	 * @param file       File to be read.
	 * @param numLines   Number of lines to be read;
	 * @param ascdesc    Ordering reading: ascendant(0), descendant(1)
	 * @param p          Keyword predicate
	 * @return
	 */
	private LogFile readFile(File file, long numLines, ReadOrder ascdesc, Predicate<String> p)
	{
		LogFile logFile = null;
		try 
		{
			logFile = logFileReader.readLines(file, numLines, ascdesc, p); 
		} catch(IOException ioe)
		{
		}
		return logFile;			
	}
}

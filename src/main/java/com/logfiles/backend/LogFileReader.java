package com.logfiles.backend;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.logfiles.api.LogFile;
import com.logfiles.api.LogFileBuffer;

/**
 * This class reads any file in the order specify.
 * 
 * @author alexdel
 */
public class LogFileReader {
	/**
	 * Last position in the file after the reading.
	 */
	private long lastPos;
	private final int MAX_SIZE = 20;
	private Map<String, LogFileBuffer> cacheFiles;
	
	/**
    * Default Constructor
    */
	public LogFileReader()
	{
		/*
		 * Creates a hash map which will be modifying keeping in top recent used
		 * Removes the last entry if the linked hash map reach the maximum size.
		 */
		cacheFiles = Collections
				.synchronizedMap(new LinkedHashMap<String, LogFileBuffer>(MAX_SIZE, 1, true) {
					protected boolean removeEldestEntry(Map.Entry<String, LogFileBuffer> eldest)
					{
						return size() > MAX_SIZE;
					}
				});
	}

	/**
	 * Get file details: Path, name, size, host ip
	 * @param  file    File reference 
	 * @return LogFile POJO with file details.
	 */
	private LogFile getFileDetails(File file) {
		String path = file.getAbsolutePath();		
		Long size = file.length();
		String ipStr;
		try {
			InetAddress ip = InetAddress.getLocalHost();
			ipStr = ip.getHostAddress();
		} catch (UnknownHostException ue) {
			ipStr = "unknown";
		}

		return new LogFile(ipStr, path, size);
	}

	/**
	 * Read the file in order specified.
	 * 
	 * @param file    File reference to be looked
	 * @param ascdesc Order to read the file. ASC, DESC.
	 * @param p       Predicate to filter lines.
	 * @return LogFileBuffer POJO LogFileBuffer which contains lines read and
	 *         last position in the file after reading.
	 *         
	 * @throws IOException if file not found or forbidden
	 */
	public LogFile readLines(File file, ReadOrder ascdesc, Predicate<String> p)
			throws IOException {
		long iniValue;
		if (ascdesc == ReadOrder.ASC) {
			iniValue = 0;
		} else {
			iniValue = file.length();
		}
		return readLines(file, iniValue, file.length(), ascdesc, p);
	}

	/**
	 * Read numLines lines in order specified.
	 * 
	 * @param file     File reference to be looked
	 * @param numLines Number of lines to be read.
	 * @param ascdesc  Order to read the file. ASC, DESC.
	 * @param p        Predicate to filter lines.
	 * @return LogFileBuffer POJO LogFileBuffer which contains lines read and
	 *         last position in the file after reading.
	 * @throws IOException if file not found or forbidden
	 */
	public LogFile readLines(File file, long numLines, ReadOrder ascdesc,
			Predicate<String> p) throws IOException {
		long iniValue;
		if (ascdesc == ReadOrder.ASC) {
			iniValue = 0;
		} else {
			iniValue = file.length();
		}
		return readLines(file, iniValue, numLines, ascdesc, p);
	}

	/**
	 * Read numLines lines in order specified from position lastP in the file.
	 * 
	 * @param file     File reference to be looked
	 * @param lastP    Position on the file where the reading will start.
	 * @param numLines Number of lines to be read.
	 * @param ascdesc  Order to read the file. ASC, DESC.
	 * @param p        Predicate to filter lines.
	 * @return LogFileBuffer POJO LogFileBuffer which contains lines read and
	 *         last position in the file after reading.
	 * @throws IOException if file not found or forbidden
	 */
	public LogFile readLines(File file, long lastP, long numLines, ReadOrder ascdesc,
			Predicate<String> p) throws IOException {
		LogFile       logFile;
		LogFileBuffer fileBuffer;		
		List<String> lines = new LinkedList<>();
		LogInputStream inputStream;
		boolean linesToBeReadFromFile = true;
		int numLinesCached = 0;
		long lastPtemp = lastP;
		
		logFile = getFileDetails(file);

		/*
		 * For now, if the customer needs the reading file by DESC, and it is reading
		 * from the end of the file then let's look if some or all lines are cached, and 
		 * after read and cache the rest of lines.
		 */
		 /** @todo: 
		 * 1. Do a general cache idea to make it work with ASC and DESC order and 
		 * from any line not only from the end or start file.
		 * 2. Makes rules to have a better caching, maybe based on the
		 * number of lines for each file.
		 */
		if (ascdesc == ReadOrder.DESC && lastPtemp == file.length()) 
		{		
			LogFileBuffer fileCached = cacheFiles.getOrDefault(file.getName(),
					null);
			if (fileCached != null) 
			{
				List<String> linesCached = fileCached.getLines();
				numLinesCached = linesCached.size();
				if (numLines > numLinesCached) 
				{
					lines.addAll(linesCached);
					numLines -= numLinesCached;
					lastP = fileCached.getLastPosRead();
				} 
				else 
				{
					lines = linesCached.stream().limit(numLines)
							.collect(Collectors.toList());
					linesToBeReadFromFile = false;
					
					lastPos = lastP;
					for (String line : lines)
					{
						lastPos -= line.length();
					}		
					/* Less CR and LF */
					lastPos -= numLines * 2;							
				}
			}
		}

		/* If needs to read the file */
		if (linesToBeReadFromFile) 
		{
			/* Get input reader based on the order */
			if (ascdesc == ReadOrder.ASC) 
			{
				inputStream = new LogAscRandInputStream(file, lastP, numLines);
			} else 
			{
				inputStream = new LogDescRandInputStream(file, lastP, numLines);
			}

			BufferedReader br = new BufferedReader(
					new InputStreamReader(inputStream));
			long currLine = 0;

			String line = br.readLine();

			while (line != null && currLine < numLines) 
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
		}
		
		/* Are we filtering something from the cache? */
		int linesBefore = lines.size();
		lines = lines.stream().filter(p).collect(Collectors.toList());
		int linesAfter = lines.size();

		fileBuffer = new LogFileBuffer(lines, lastPos);
		logFile.setFileBuffered(fileBuffer);

		/* If it is a descendant ordering and
		 * read from disk  and
		 * reading started from the end and
		 * there wasn't filtering buffer cache.
		 */
		if (ascdesc == ReadOrder.DESC && linesToBeReadFromFile && lastPtemp == file.length() &&
			linesBefore == linesAfter) 
		{
			/* The file is already cached */
			if (numLinesCached > 0)
			{
				List<String> newLines = lines.subList(numLinesCached, lines.size());
				LogFileBuffer fileCached = cacheFiles.get(file.getName());
				/* Add the new lines and update last position */
				fileCached.getLines().addAll(newLines);
				fileCached.setLastPosRead(lastPos);
			}
			else
			{
				/* The files was not cached */
				/* If we already have the maximum number of files cached,
				 * then remove the last file cached (LRU) */
				cacheFiles.put(file.getName(), fileBuffer);
			}
		}
		

		return logFile;
	}

	/**
	 * Get the position on where the cursor in the read file is after do the
	 * reading.
	 * 
	 * @return Position on file.
	 */
	public long getLastPos() {
		return lastPos;
	}

}

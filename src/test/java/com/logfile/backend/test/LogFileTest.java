package com.logfile.backend.test;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.logfiles.api.LogFile;
import com.logfiles.backend.FilterKeyword;
import com.logfiles.backend.LogFileReader;
import com.logfiles.backend.ReadOrder;

import junitx.framework.FileAssert;

public class LogFileTest {

	private static LogFileReader 	logFileReader;	
	
	private static File             testLogFile;
	/* Generated log file test */
	@TempDir
	File                    genPath;	
	private File            genFile;
	
	/* Load test properties */
	static Properties   props;
	
	@BeforeAll
	public static void setUpForAllTest() 
	{
		String pathProps = LogFileTest.class.getClassLoader()
				      .getResource("config.properties").getFile();
		String pathTestF;
		String testFName;
		
		
		try (InputStream input = new FileInputStream(pathProps))
		{
			/* Load properties file test/resources/config.properties */
			props = new Properties();
			props.load(input);
			
			/* Read log file name on which the reading log file test will run */
			testFName = props.getProperty("logfile");
			pathTestF = LogFileTest.class.getClassLoader()
					    .getResource(testFName).getFile();
			testLogFile = new File(pathTestF);
			/* Instance of LogFile to be tested */
			logFileReader     = new LogFileReader();
		} catch (IOException ioe)
		{
			ioe.printStackTrace();		
		}
	}
		
	@BeforeEach
	public void setUpBeforeEachTest()
	{				
		/* Define temporary file to be created to save test execution and after
		 * compare this file with the expected master log file for each
		 * test.
		 */
		String tempF = props.getProperty("temp_testfile");
		genFile = new File(genPath, tempF);	
	}
	
	private void writeToFile(BufferedWriter bufWrf, List<String> lines) throws IOException
	{
		lines.stream().forEach((str) -> 
        {
     	   try{
     		   bufWrf.write(str); 
     		   bufWrf.newLine();
     	   } catch (IOException e) 
     	   {}
        });
		bufWrf.newLine();
	}
	
	@Test
	@DisplayName("Testing log file in descendant way ...")
	public void descTest()
	{		
		LogFile         logFile;
		List<String>    lines;
		long            lastPos;		
		String          masterLogF   = props.getProperty("logfiletest_masterdesc");
		File            expectedFile = new File (getClass().getClassLoader().
		                               getResource(masterLogF).getFile());
		FilterKeyword keywordFilter  = new FilterKeyword("");
		
		/* try-with-resource to open and close generated file */
		try(FileWriter generatedFile = new FileWriter(genFile);
			BufferedWriter bufWrf = new BufferedWriter(generatedFile))
		{
			/* Read complete log file in descendant way */
			logFile = logFileReader.readLines(testLogFile, ReadOrder.DESC, keywordFilter);
			lines      = logFile.getFileBuffered().getLines();
			writeToFile(bufWrf, lines);
						
			/* Read last 5 lines of log file */
			logFile = logFileReader.readLines(testLogFile, 5, ReadOrder.DESC, keywordFilter);
			lines      = logFile.getFileBuffered().getLines();
			writeToFile(bufWrf, lines);
			
			/* From last execution, get 3 more lines in descendant way */
			lastPos = logFileReader.getLastPos();			
			logFile = logFileReader.readLines(testLogFile, lastPos, 3, ReadOrder.DESC, keywordFilter);
			lines      = logFile.getFileBuffered().getLines();
			writeToFile(bufWrf, lines);							
		} 
		catch (IOException ioe)
		{
		}
		
		/* compare generated file with expected file output */
		FileAssert.assertEquals(expectedFile, genFile);
	}
	
	
	@Test
	@DisplayName("Testing log file in ascendant way ...")
	public void ascTest()
	{		
		LogFile         logFile;
		List<String>    lines;
		long            lastPos;
		String          masterLogF   = props.getProperty("logfiletest_masterasc");
		File            expectedFile = new File (getClass().getClassLoader().
		                               getResource(masterLogF).getFile());
		FilterKeyword keywordFilter  = new FilterKeyword("");
		
		/* try-with-resource to open and close generated file */
		try(FileWriter generatedFile = new FileWriter(genFile);
			BufferedWriter bufWrf = new BufferedWriter(generatedFile))
		{
			/* Read complete log file in ascendant way */
			logFile = logFileReader.readLines(testLogFile, ReadOrder.ASC, keywordFilter);
			lines      = logFile.getFileBuffered().getLines();
			writeToFile(bufWrf, lines);
			
			/* Read first 5 lines of log file */
			logFile = logFileReader.readLines(testLogFile, 5, ReadOrder.ASC, keywordFilter);
			lines      = logFile.getFileBuffered().getLines();
			writeToFile(bufWrf, lines);
			
			/* From last execution, read 3 more lines in ascendant way */
			lastPos = logFileReader.getLastPos();			
			logFile = logFileReader.readLines(testLogFile, lastPos, 3, ReadOrder.ASC, keywordFilter);
			lines      = logFile.getFileBuffered().getLines();
			writeToFile(bufWrf, lines);							
		} 
		catch (IOException ioe)
		{
		}
		/* compare generated file with expected file output */
		FileAssert.assertEquals(expectedFile, genFile);
	}
	
	
	@Test
	@DisplayName("Testing log file in descendant and ascendant way ...")
	public void ascDescTest()
	{		
		LogFile         logFile;
		List<String>    lines;
		long            lastPos;
		String          masterLogF   = props.getProperty("logfiletest_masterascdesc");
		File            expectedFile = new File (getClass().getClassLoader().
		                               getResource(masterLogF).getFile());
		FilterKeyword keywordFilter  = new FilterKeyword("");
		
		/* try-with-resource to open and close generated file */
		try(FileWriter generatedFile = new FileWriter(genFile);
			BufferedWriter bufWrf = new BufferedWriter(generatedFile))
		{
			/* Read complete log file in descendant way */
			logFile = logFileReader.readLines(testLogFile, ReadOrder.DESC, keywordFilter);
			lines      = logFile.getFileBuffered().getLines();
			writeToFile(bufWrf, lines);
			
			/* Read complete log file in ascendant way */
			logFile = logFileReader.readLines(testLogFile, ReadOrder.ASC, keywordFilter);
			lines      = logFile.getFileBuffered().getLines();
			writeToFile(bufWrf, lines);
			
			/* Read last 5 lines of log file */
			logFile = logFileReader.readLines(testLogFile, 5, ReadOrder.DESC, keywordFilter);
			lines      = logFile.getFileBuffered().getLines();
			writeToFile(bufWrf, lines);
			
			/* After last execution, read after 3 lines in ascendant way */
			lastPos = logFileReader.getLastPos();			
			logFile = logFileReader.readLines(testLogFile, lastPos, 3, ReadOrder.ASC, keywordFilter);
			lines      = logFile.getFileBuffered().getLines();
			writeToFile(bufWrf, lines);		
			
			/* After last execution, read before 2 lines in descendant way */
			lastPos = logFileReader.getLastPos();
			logFile = logFileReader.readLines(testLogFile, lastPos, 2, ReadOrder.DESC, keywordFilter);
			lines      = logFile.getFileBuffered().getLines();
			writeToFile(bufWrf, lines);					
		} 
		catch (IOException ioe)
		{
		}
		/* compare generated file with expected file output */
		FileAssert.assertEquals(expectedFile, genFile);
	}
	
}

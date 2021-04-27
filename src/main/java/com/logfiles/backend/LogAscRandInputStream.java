package com.logfiles.backend;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * Input stream to traverse a file with random access in ascendant way.
 * This input stream can read n lines in ascendant way from a particular position on the file.
 * @author alexdel
 */
public class LogAscRandInputStream extends LogInputStream {
	/**
	 * Random access file reference.
	 */
	private RandomAccessFile randFile;
	/**
	 * Last position of cursor after do the reading.
	 */
	private long lastBufPos;
	/**
	 * Current number of lines read.
	 */
	private long readLines;
	/**
	 * Number of lines to be read.
	 */
	private long numLines;
	/**
	 * File size.
	 */
	private long fileSize;
	/**
	 * Has input stream finished to read?
	 */
	private boolean finishRead;
	/**
	 * Position where current line finish.
	 */
	private long currLineEnd;
	/**
	 * Position where current line start.
	 */
	private long currLineStart;
	/**
	 * Buffer used to buffer current line avoiding the reading byte by byte
	 */
	private byte buffer[];
	/**
	 * Current position on buffer
	 */
	private int  posBuffer;
	/**
	 * Buffer size.
	 */
	private int  bufferSize;
	/**
	 * Is it the last line read on the file? 
	 */
	private boolean lastLine;
	
	/**
	 * Constructor
	 * @param file      File to be read.
	 * @param lastPos   Cursor position on where the reading will start.
	 * @param numLines  Number of lines to be read.
	 * @throws FileNotFoundException
	 */
	public LogAscRandInputStream(File file, long lastPos, long numLines) 
		   throws FileNotFoundException {
		super();		
		this.lastBufPos = lastPos;
		this.readLines  = 0;
		this.numLines   = numLines;
		this.finishRead = false;
		
		
		this.currLineStart = lastPos;
		this.currLineEnd   = lastPos;
		
		this.buffer = null;
		this.posBuffer = 0;
		this.bufferSize = 0;
		
		this.lastLine = false;
		
		randFile = new RandomAccessFile(file, "r");
		this.fileSize   = file.length();
	}
	
	/**
	 * Find the start and end of next line to be read and after, buffer the line to avoid
	 * reading byte by byte during read input stream operation.
	 * @throws IOException
	 */
	private void bufferNextLine() 
			throws IOException {
		long tempCurPos;
		int  readByte;				
		
		currLineStart = currLineEnd;			
		
		/* If reach the file EOF. No more lines to read */
		if (currLineStart == fileSize)
		{
			lastBufPos = -1;
			currLineEnd   = -1;
			currLineStart = -1;
			lastLine = true;
			return;
		}
		
		/* @todo:
		 * Find out a way to avoid read byte by byte to find out where start 
		 * the line. This is slow. Maybe we could buffer this reading and work
		 * with the buffer.
		 */
		tempCurPos = currLineStart;
		
		do
		{
			/* During first iteration. Avoid Line Feed(LF) byte,
			 * after skip one byte read.
			 */
			tempCurPos++;
			
			if (tempCurPos >= fileSize)
				break;
			
			randFile.seek(tempCurPos);
			readByte = randFile.readByte();
		} while(readByte != 0xA);
		
		/* if not EOF. Then reach the next byte to buffer the line,
		 * else then this position is ok, we will allocate one more byte 
		 * to save CR character */
		if (tempCurPos < fileSize)
			currLineEnd = tempCurPos + 1;
		else
			currLineEnd = tempCurPos;
	
		if (currLineEnd == fileSize)
		{			
			lastLine = true;
		}
		
		/* 1. Creates a buffer to avoid read byte by byte again with size of 
		 *    the current line if possible, if not then creates a buffer with
		 *    maximum size (Integer.MAX_VALUE).
		 * 2. Cache the line.
		 */
		allocateAndCacheBuffer(currLineStart, currLineEnd);			
	}
	
	/**
	 * Allocate the buffer for each line
	 * @param start  Position where the line start.
	 * @param end    Position where the line end.
	 * @throws IOException
	 */
	@Override
	protected void allocateAndCacheBuffer(long start, long end) 
		   throws IOException {		
		/* The buffer can be created from start to end size */
		if (end - start < Integer.MAX_VALUE)
		{
			/* update last position buffered */
			lastBufPos = end;
			/* Avoid buffer CR and LF */
			bufferSize = (int)(end - start);
			
			if (lastLine)
			{
				/* One byte more to save CR */
				bufferSize++;
			}
		}
		else
		{
			/* Update last position buffered */
			lastBufPos = start + Integer.MAX_VALUE;
			/* Buffer with maximum size allowed */
			bufferSize = Integer.MAX_VALUE;
		}
				
		/* Allocate buffer */
		buffer = new byte[bufferSize];
		posBuffer = 0;
	
		/* Cache line or buffer with maximum size to avoid repeat */
		randFile.seek(start);
		randFile.read(buffer);
		
		if (lastLine)
		{
			buffer[bufferSize - 1] = '\n';
		}
	}
	
	/**
	 * Read byte from the buffer.
	 */
	@Override
	public int read() 
		   throws IOException {		
		int readByte = -1;
		
		if (finishRead)
			return readByte;
		
		/* If file beginning has not been reached */
		if (currLineEnd <= fileSize)
		{
			/* If new start line has been found */
			if (buffer != null)
			{
				/* Read line from start line to end line */
				readByte = buffer[posBuffer++];
				if (posBuffer >= bufferSize)
				{
					if (lastBufPos < currLineEnd)
					{
						/* This code is reached, if the complete line
						 * has not been possible to be cached in the buffer.
						 * Then again.
						 * 1. Create a buffer to cache the rest of line
						 *    if possible, if not then creates a buffer with
						 *    maximum size possible (Integer.MAX_VALUE).
						 * 2. Cache the rest of line.
						 */
						allocateAndCacheBuffer(lastBufPos, currLineEnd);
					}
					else
					{						
						readLines++;
						if (readLines == numLines)
						{
							finishRead = true;
						}
						
						/* If EOF has been reached, then we finished to read */
						if (currLineEnd == fileSize)
						{
							finishRead = true;
						}
						
						buffer = null;
						bufferSize = 0;
						posBuffer = 0;						
					}
				}
			}
			else
			{								
				/* Buffer next line and after read a byte from that buffer 
				 * In this way we can read line by line
				 */				
				bufferNextLine();
				readByte = read();
			}
		}
			
		return readByte;
	}
	
	/**
	 * Close RandomAccessFile file resource
	 */
	@Override
	public void close() 
		   throws IOException {
		if (randFile != null)
		{
			randFile.close();
			randFile = null;
		}
		super.close();
	}
	
	/**
	 * Get cursor position on the file after reading.
	 */
	public long getLastPos()
	{		
		return currLineEnd;
	}
}

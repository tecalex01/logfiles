package com.logfiles.backend;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * Input stream to traverse a file with random access in descendant way. 
 * This input stream can read n lines in descendant way from a particular position on the file.
 * @author alexdel
 */
public class LogDescRandInputStream extends LogInputStream {
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
	 * Buffer size
	 */
	private int  bufferSize;
	/**
	 * Is it the first line read on the file? It means, is it the last line? Because this
	 * an inverse reading file.
	 */
	boolean firstLine;
	
	/**
	 * Constructor 
	 * @param file      File to be read.
	 * @param lastPos   Cursor position on where the reading will start.
	 * @param numLines  Number of lines to be read.
	 * @throws FileNotFoundException
	 */
	public LogDescRandInputStream(File file, long lastPos, long numLines) 
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
		
		this.firstLine = false;
		
		randFile = new RandomAccessFile(file, "r");
	}
	
	/**
	 * Find the start and end of previous line to be read and after, buffer the line to avoid
	 * reading byte by byte during read input stream operation.
	 * @throws IOException
	 */
	private void bufferPrevLine() 
			throws IOException {
		long tempCurPos;
		int  readByte;				
		
		currLineEnd = currLineStart;			
		
		/* If reach the file beginning. No more lines to read */
		if (currLineEnd == 0)
		{
			lastBufPos = -1;
			currLineEnd   = -1;
			currLineStart = -1;
			return;
		}
		
		/* @todo:
		 * Find out a way to avoid read byte by byte to find out where start 
		 * the line. This could be slow
		 */
		
		tempCurPos = currLineEnd - 1;
		randFile.seek(tempCurPos);	
		
		/* Avoid Line Feed (LF) byte */
		if (randFile.readByte() != 0XA)
		{
			firstLine = true;		
		}		
		
		do
		{
			/* During first iteration. Avoid Carry Return (CR) byte,
			 * after skip one byte read.
			 */
			tempCurPos--;
			
			if (tempCurPos < 0)
				break;
			
			randFile.seek(tempCurPos);
			readByte = randFile.readByte();
		} while(readByte != 0xA);
		
		/* Return one byte after LF was found or beginning file has been 
		 * reached */
		currLineStart = tempCurPos + 1;
							
		/* 1. Creates a buffer to avoid read byte by byte again with size of 
		 *    the current line if possible, if not then creates a buffer with
		 *    maximum size (Integer.MAX_VALUE).
		 * 2. Cache the line.
		 */
		allocateAndCacheBuffer(currLineStart, currLineEnd);			
	}
	
	/**
	 * Allocate the buffer for each line
	 * @param start    Position where the line start.
	 * @param end	   Position where the line finish.
	 * @throws IOException
	 */
	protected void allocateAndCacheBuffer(long start, long end) 
		   throws IOException {		
		/* The buffer can be created from start to end size */
		if (end - start < Integer.MAX_VALUE)
		{
			/* update last position buffered */
			lastBufPos = end;
			/* Avoid buffer CR and LF */
			bufferSize = (int)(end - start);
			
			if (firstLine)
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
		
		if (firstLine)
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
		if (currLineStart >= 0)
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
						
						buffer = null;
						bufferSize = 0;
						posBuffer = 0;
						firstLine = false;
					}
				}
			}
			else
			{								
				/* Buffer previous line and after read a byte from that buffer 
				 * In this way we can read line by line but in reverse order
				 */				
				bufferPrevLine();
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
		return currLineStart;
	}
}

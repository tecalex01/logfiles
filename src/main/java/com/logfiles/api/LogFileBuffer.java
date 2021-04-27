package com.logfiles.api;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * POJO class
 * Saves the lines read.
 * Saves the file cursor position after read.
 * @author alexdel
 */
public class LogFileBuffer {
	private long lastPosRead;
	private List<String> lines;
	
	public LogFileBuffer(List<String> lines, long lastPos) {
		this.lines = lines;
		this.lastPosRead = lastPos;
	}
	
	/**
	 * Get the file cursor position after read.
	 * @return cursor position.
	 */
	@JsonProperty
	public long getLastPosRead() {
		return lastPosRead;
	}

	/**
	 * Set a file cursor position
	 * @param lastPosRead cursor position
	 */
	public void setLastPosRead(long lastPosRead) {
		this.lastPosRead = lastPosRead;
	}

	/**
	 * Get the lines cached from read.
	 * @return lines cached.
	 */
	@JsonProperty
	public List<String> getLines() {
		return lines;
	}
	
	/**
	 * Set lines cached from read.
	 * @param lines lines cached.
	 */
	public void setLines(List<String> lines) {
		this.lines = lines;
	}
	
	
}

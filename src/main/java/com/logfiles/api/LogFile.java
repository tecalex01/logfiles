package com.logfiles.api;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * POJO class
 * Represent a log file. Contains info like its lines, host, path and size.
 * @author alexdel
 *
 */
public class LogFile {

	/** Host where log file is */
	private String        host;
	/** Path where file was found */
	private String        path;
	/** File size */
	private Long       size;
	/** Lines read */
	private LogFileBuffer fileBuffered; 
	
	/**
	 * Constructor
	 * @param host  Host where the file is found.
	 * @param path  Directory path for the file.
	 * @param size  File size.
	 */
	public LogFile(String host, String path, Long size)
	{
		this.host = host;
		this.path = path;
		this.size = size;
	}

	/**
	 * Getter host
	 * @return host on which the file was found.
	 */
	@JsonProperty
	public String getHost() {
		return host;
	}

	/**
	 * Setter host
	 * @param host Host on which the file was found.
	 */
	public void setHost(String host) {
		this.host = host;
	}

	/**
	 * Getter path
	 * @return Directory path
	 */
	@JsonProperty
	public String getPath() {
		return path;
	}

	/**
	 * Setter path
	 * @param path Set directory path.
	 */
	public void setPath(String path) {
		this.path = path;
	}

	/**
	 * Getter size
	 * @return file size
	 */
	@JsonProperty
	public Long getSize() {
		return size;
	}

	/**
	 * Setter size
	 * @param size File size
	 */
	public void setSize(Long size) {
		this.size = size;
	}

	/**
	 * Getter lines buffered 
	 * @return LogFileBuffer which contains reference to current cursor file and  buffered lines.
	 */
	@JsonProperty
	public LogFileBuffer getFileBuffered() {
		return fileBuffered;
	}

	/**
	 * Setter lines buffered
	 * @param fileBuffered FileBuffer which contains reference to current cursor file and buffered lines.
	 */
	public void setFileBuffered(LogFileBuffer fileBuffered) {
		this.fileBuffered = fileBuffered;
	}	
}

package com.logfiles;

import javax.validation.constraints.NotEmpty;

import io.dropwizard.Configuration;

public class LogFilesConf extends Configuration {
	@NotEmpty
	private String defaultHost;
	@NotEmpty
	private String logDirectory;
	@NotEmpty
	private String startPos;
	@NotEmpty
	private String nEvents;
	@NotEmpty
	private String orderBy;
	
	public String getDefaultHost() {
		return defaultHost;
	}
	
	public void setDefaultHost(String defaultHost) {
		this.defaultHost = defaultHost;
	}
	
	public String getLogDirectory() {
		return logDirectory;
	}
	
	public void setLogDirectory(String logDirectory) {
		this.logDirectory = logDirectory;
	}
	
	public String getStartPos() {
		return startPos;
	}

	public void setStartPos(String startPos) {
		this.startPos = startPos;
	}

	public String getnEvents() {
		return nEvents;
	}

	public void setnEvents(String nEvents) {
		this.nEvents = nEvents;
	}

	public String getOrderBy() {
		return orderBy;
	}

	public void setOrderBy(String orderBy) {
		this.orderBy = orderBy;
	}		
}

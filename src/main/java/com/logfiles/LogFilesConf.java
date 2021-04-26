package com.logfiles;

import javax.validation.constraints.NotEmpty;

import io.dropwizard.Configuration;

public class LogFilesConf extends Configuration {
	@NotEmpty
	private String defaultHost;
	@NotEmpty
	private String logDirectory;
	
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
}

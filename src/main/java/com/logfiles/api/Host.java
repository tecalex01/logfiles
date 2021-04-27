package com.logfiles.api;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Host {
	private String host;	
	private List<LogFile> Logfiles;
	private String code;
	private String message;
	
	@JsonProperty
	public String getHost() {
		return host;
	}
	
	public void setHost(String host) {
		this.host = host;
	}
	
	@JsonProperty
	public List<LogFile> getLogfiles() {
		return Logfiles;
	}
	
	public void setLogfiles(List<LogFile> logfiles) {
		Logfiles = logfiles;
	}
	
	@JsonProperty
	public String getCode() {
		return code;
	}
	
	
	public void setCode(String code) {
		this.code = code;
	}
	
	@JsonProperty
	public String getMessage() {
		return message;
	}
	
	public void setMessage(String message) {
		this.message = message;
	}		
}

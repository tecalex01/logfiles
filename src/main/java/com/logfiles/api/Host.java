package com.logfiles.api;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * POJO class
 * @author alexdel
 *
 */
public class Host {
	/** Host ip */
	private String host;
	/** List of logfiles found in host ip */
	private List<LogFile> Logfiles;
	/** Response code for REST-API request */
	private Integer code;
	/** Response message for REST-API request */
	private String message;
	
	/**
	 * Getter host
	 * @return
	 */
	@JsonProperty
	public String getHost() {
		return host;
	}
	
	/**
	 * Setter host
	 * @param host on which the file(s) is|are found
	 */
	public void setHost(String host) {
		this.host = host;
	}
	
	/**
	 * Getter log files
	 * @return
	 */
	@JsonProperty
	public List<LogFile> getLogfiles() {
		return Logfiles;
	}
	
	/**
	 * Setter log files
	 * @param logfiles List of log files on this host
	 */
	public void setLogfiles(List<LogFile> logfiles) {
		Logfiles = logfiles;
	}
	
	/**
	 * Getter code for REST-API request
	 * @return
	 */
	@JsonProperty
	public Integer getCode() {
		return code;
	}
	
	/**
	 * Setter code for REST-API request
	 * @param code Response http code
	 */
	public void setCode(Integer code) {
		this.code = code;
	}
	
	/**
	 * Getter message response for REST-API request
	 * @return
	 */
	@JsonProperty
	public String getMessage() {
		return message;
	}
	
	/**
	 * Setter message response for REST-API request
	 * @param message  Message
	 */
	public void setMessage(String message) {
		this.message = message;
	}		
}

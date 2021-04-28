package com.logfiles;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.dropwizard.Configuration;
import io.dropwizard.client.JerseyClientConfiguration;

/**
 * Dropwizard configuration.
 * Here is where the modules are load.
 * Here are defined:
 * 1. Default variables for LogFileResource
 * 2. Set jersey client
 * @author alexdel
 *
 */
public class LogFilesConf extends Configuration {
	/** Default host reference set on config.yml */
	@NotEmpty
	private String defaultHost;
	/** Default log directory set on config.yml */
	@NotEmpty
	private String logDirectory;
	/** start post file set on config.yml */
	@NotEmpty
	private String startPos;
	/** Number of lines set on config.yml */
	@NotEmpty
	private String nEvents;
	/** Order by. Default descendant set on config.yml */
	@NotEmpty
	private String orderBy;
	/** Jersey client to makes REST-API requests */
	@Valid	
	@NotNull
	private JerseyClientConfiguration jerseyClient = new JerseyClientConfiguration();
	
	/** 
	 * Getter default host
	 * @return default host
	 */
	public String getDefaultHost() {
		return defaultHost;
	}
	
	/**
	 * Setter default host
	 * @param defaultHost Default host to be query
	 */
	public void setDefaultHost(String defaultHost) {
		this.defaultHost = defaultHost;
	}
	
	/**
	 * Getter default log directory for this service REST-API.
	 * @return Default log directory
	 */
	public String getLogDirectory() {
		return logDirectory;
	}
	
	/**
	 * Setter default log directory for this service REST-API
	 * @param logDirectory Default log directory
	 */
	public void setLogDirectory(String logDirectory) {
		this.logDirectory = logDirectory;
	}
	
	/**
	 * Getter start pos to read a file. By default -1. 
	 * Means that start at 0 if ASC ordering or file.legnth DESC ordering
	 * @return start pos
	 */
	public String getStartPos() {
		return startPos;
	}

	/**
	 * Setter start pos
	 * @param startPos Start pos to read a file.
	 */
	public void setStartPos(String startPos) {
		this.startPos = startPos;
	}

	/**
	 * Getter default number of lines. By default -1. Means read all lines from file.
	 * @return
	 */
	public String getnEvents() {
		return nEvents;
	}

	/**
	 * Setter number of lines
	 * @param nEvents Number of lines
	 */
	public void setnEvents(String nEvents) {
		this.nEvents = nEvents;
	}

	/**
	 * Getter orderby. Ordering ASC (0), DESC(1). Default DESC (1).
	 * @return Ordering
	 */
	public String getOrderBy() {
		return orderBy;
	}

	/**
	 * Setter orderBy 
	 * @param orderBy Ordering
	 */
	public void setOrderBy(String orderBy) {
		this.orderBy = orderBy;
	}

	/**
	 * Get jersey client for REST-API requests.
	 * @return jersey client
	 */
	@JsonProperty("jerseyClient")
	public JerseyClientConfiguration getJerseyClient() {
		return jerseyClient;
	}

	/**
	 * Set jersey client.
	 * @param jerseyClient Jersey client
	 */
	@JsonProperty("jerseyClient")
	public void setJerseyClient(JerseyClientConfiguration jerseyClient) {
		this.jerseyClient = jerseyClient;
	}		
	
}

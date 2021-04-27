package com.logfiles;

import com.logfiles.entrypoint.LogFileResource;

import io.dropwizard.Application;
import io.dropwizard.setup.Environment;

/**
 * Main application run by dropwizard on Jetty http server.
 * @author alexdel
 *
 */
public class LogFilesApp extends Application<LogFilesConf> {
	
	public static void main(String []args) throws Exception
	{
		new LogFilesApp().run(new String[] {"server", "config.yml"});
	}

	/**
	 * Register resources, read configuration and environment variables.
	 * @param conf         Configuration file
	 * @param env          Environment variable
	 */
	@Override
	public void run(LogFilesConf conf, Environment env)
			throws Exception {

		final LogFileResource logFileRes = new LogFileResource(conf.getLogDirectory(),
															   Long.valueOf(conf.getStartPos()),
															   Integer.valueOf(conf.getnEvents()), 
				                                               Integer.valueOf(conf.getOrderBy()),
				                                               conf.getDefaultHost());
		/* Register log file resource.
		 * It will response on /logfiles/api/v1/files/
		 */
		env.jersey().register(logFileRes);
	}

	
}

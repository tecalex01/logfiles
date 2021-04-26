package com.logfiles;

import io.dropwizard.Application;
import io.dropwizard.setup.Environment;

public class LogFilesApp extends Application<LogFilesConf> {
	
	public static void main(String []args) throws Exception
	{
		new LogFilesApp().run(new String[] {"server", "config.yml"});
	}

	@Override
	public void run(LogFilesConf configuration, Environment environment)
			throws Exception {

		
	}

	
}

package com.telus.credit.config;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.opencensus.exporter.trace.stackdriver.StackdriverTraceConfiguration;
import io.opencensus.exporter.trace.stackdriver.StackdriverTraceExporter;

public class TracerConfig {

	private static final Logger LOGGER = LoggerFactory.getLogger(TracerConfig.class);

	public static void createAndRegisterWithGCP() throws IOException {
		StackdriverTraceExporter.createAndRegister(StackdriverTraceConfiguration.builder().build());
		LOGGER.info("Stackdriver exporter init done.");
	}

	//Access static way
	private TracerConfig() {
		
	}
}

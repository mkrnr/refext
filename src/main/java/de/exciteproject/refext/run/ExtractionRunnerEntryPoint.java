package de.exciteproject.refext.run;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

import py4j.GatewayServer;

public class ExtractionRunnerEntryPoint {

    public static void main(String[] args) {
	GatewayServer gatewayServer = new GatewayServer(new ExtractionRunnerEntryPoint(new File(args[0])));
	gatewayServer.start();

	// override logger settings
	GatewayServer.turnLoggingOn();
	Logger logger = Logger.getLogger("py4j");
	logger.setLevel(Level.SEVERE);

	System.out.println("Started gateway server");
    }

    private ExtractionRunner extractionRunner;

    public ExtractionRunnerEntryPoint(File crfModelFile) {
	this.extractionRunner = new ExtractionRunner(crfModelFile);
    }

    public ExtractionRunner getExtractionRunner() {
	return this.extractionRunner;
    }
}
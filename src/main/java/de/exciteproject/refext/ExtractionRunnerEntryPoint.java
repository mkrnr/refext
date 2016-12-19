package de.exciteproject.refext;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import pl.edu.icm.cermine.exception.AnalysisException;
import py4j.GatewayServer;

public class ExtractionRunnerEntryPoint {

    public static void main(String[] args) throws IOException, AnalysisException {
        GatewayServer gatewayServer = new GatewayServer(new ExtractionRunnerEntryPoint(new File(args[0])));
        gatewayServer.start();

        // override logger settings
        GatewayServer.turnLoggingOn();
        Logger logger = Logger.getLogger("py4j");
        logger.setLevel(Level.SEVERE);

        System.out.println("Started gateway server");
    }

    private ExtractionRunner extractionRunner;

    public ExtractionRunnerEntryPoint(File crfModelFile) throws IOException, AnalysisException {
        this.extractionRunner = new ExtractionRunner(crfModelFile);
    }

    public ExtractionRunner getExtractionRunner() {
        return this.extractionRunner;
    }
}

package de.exciteproject.refext;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import pl.edu.icm.cermine.exception.AnalysisException;
import py4j.GatewayServer;

/**
 * Class for generating a GatewayServer for access via py4j
 */
public class ReferenceExtractorEntryPoint {

    public static void main(String[] args) throws IOException, AnalysisException {
        GatewayServer gatewayServer = new GatewayServer(new ReferenceExtractorEntryPoint(new File(args[0])));
        gatewayServer.start();

        // override logger settings
        GatewayServer.turnLoggingOn();
        Logger logger = Logger.getLogger("py4j");
        logger.setLevel(Level.SEVERE);

        System.out.println("Started gateway server");
    }

    private ReferenceExtractor extractionRunner;

    public ReferenceExtractorEntryPoint(File crfModelFile) throws IOException, AnalysisException {
        this.extractionRunner = new ReferenceExtractor(crfModelFile);
    }

    public ReferenceExtractor getExtractionRunner() {
        return this.extractionRunner;
    }
}

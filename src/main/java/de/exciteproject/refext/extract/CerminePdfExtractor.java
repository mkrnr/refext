package de.exciteproject.refext.extract;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import pl.edu.icm.cermine.ComponentConfiguration;
import pl.edu.icm.cermine.ExtractionUtils;
import pl.edu.icm.cermine.configuration.ContentExtractorConfig;
import pl.edu.icm.cermine.configuration.ContentExtractorConfigLoader;
import pl.edu.icm.cermine.exception.AnalysisException;
import pl.edu.icm.cermine.structure.model.BxDocument;

/**
 * Class for manually executing the needed CERMINE extractors to improve the
 * runtime in comparison with calling ContentExtractor methods
 */
public class CerminePdfExtractor {

    private ComponentConfiguration componentConfig;

    public CerminePdfExtractor() throws AnalysisException {
	ContentExtractorConfig contentExtractorConfig = new ContentExtractorConfigLoader().loadConfiguration();
	this.componentConfig = new ComponentConfiguration(contentExtractorConfig);
    }

    /**
     * Executes character extraction, page segmentation, and reading order
     * resolving methods.
     *
     * @param pdfInputFile
     * @return bxDocument
     * @throws AnalysisException
     * @throws IOException
     */
    public BxDocument extractWithResolvedReadingOrder(File pdfInputFile) throws AnalysisException, IOException {
	InputStream inputStream = new FileInputStream(pdfInputFile);
	BxDocument bxDocument = ExtractionUtils.extractCharacters(this.componentConfig, inputStream);
	bxDocument = ExtractionUtils.segmentPages(this.componentConfig, bxDocument);
	bxDocument = ExtractionUtils.resolveReadingOrder(this.componentConfig, bxDocument);
	inputStream.close();
	return bxDocument;
    }

}

package de.exciteproject.refext;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import de.exciteproject.refext.extract.CermineLineLayoutExtractor;
import de.exciteproject.refext.extract.ReferenceLineAnnotator;
import de.exciteproject.refext.extract.ReferenceLineAnnotation;
import de.exciteproject.refext.extract.ReferenceLineMerger;
import pl.edu.icm.cermine.ComponentConfiguration;
import pl.edu.icm.cermine.exception.AnalysisException;

/**
 * Class for applying reference extraction on text files that include layout
 * information (see {@link CermineLineLayoutExtractor}).
 *
 */
public class ReferenceExtractor {

    private ReferenceLineAnnotator crfReferenceLineAnnotator;

    private CermineLineLayoutExtractor cermineLineLayoutExtractor;

    public ReferenceExtractor(File crfModelFile) throws AnalysisException {
        this.crfReferenceLineAnnotator = new ReferenceLineAnnotator(crfModelFile);
        ComponentConfiguration componentConfiguration = new ComponentConfiguration();

        this.cermineLineLayoutExtractor = new CermineLineLayoutExtractor(componentConfiguration);
    }

    public List<ReferenceLineAnnotation> annotateLinesFromLayoutFile(File layoutFile, Charset charset)
            throws IOException, AnalysisException {
        List<String> layoutLines = org.apache.commons.io.FileUtils.readLines(layoutFile, charset);
        return this.annotateLinesFromLayoutLines(layoutLines);

    }

    public List<ReferenceLineAnnotation> annotateLinesFromLayoutLines(List<String> layoutLines)
            throws IOException, AnalysisException {
        return this.crfReferenceLineAnnotator.annotate(layoutLines);

    }

    public List<ReferenceLineAnnotation> annotateLinesFromPdfFile(File pdfFile) throws IOException, AnalysisException {
        List<String> layoutLines = this.cermineLineLayoutExtractor.extract(pdfFile);
        return this.annotateLinesFromLayoutLines(layoutLines);

    }

    public List<String> extractReferencesFromLayoutFile(File layoutFile, Charset charset)
            throws IOException, AnalysisException {
        List<String> layoutLines = org.apache.commons.io.FileUtils.readLines(layoutFile, charset);
        return this.extractReferencesFromLayoutLines(layoutLines);
    }

    public List<String> extractReferencesFromLayoutLines(List<String> layoutLines)
            throws IOException, AnalysisException {
        List<ReferenceLineAnnotation> annotatedLines = this.annotateLinesFromLayoutLines(layoutLines);
        List<String> bestAnnotatedLines = new ArrayList<String>();
        for (ReferenceLineAnnotation referenceLineAnnotation : annotatedLines) {
            String bestAnnotatedLine = referenceLineAnnotation.getBestAnnotation();
            bestAnnotatedLine += "\t" + referenceLineAnnotation.getLine();
            bestAnnotatedLines.add(bestAnnotatedLine);
        }
        List<String> referenceStrings = ReferenceLineMerger.merge(bestAnnotatedLines);
        return referenceStrings;
    }

    public List<String> extractReferencesFromPdf(File pdfFile) throws IOException, AnalysisException {
        List<String> layoutLines = this.cermineLineLayoutExtractor.extract(pdfFile);
        return this.extractReferencesFromLayoutLines(layoutLines);
    }
}

package de.exciteproject.refext;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import de.exciteproject.refext.extract.CermineLineLayoutExtractor;
import de.exciteproject.refext.extract.CrfReferenceLineAnnotator;
import de.exciteproject.refext.extract.ReferenceLineMerger;
import pl.edu.icm.cermine.ComponentConfiguration;
import pl.edu.icm.cermine.bibref.BibReferenceParser;
import pl.edu.icm.cermine.bibref.CRFBibReferenceParser;
import pl.edu.icm.cermine.bibref.model.BibEntry;
import pl.edu.icm.cermine.exception.AnalysisException;

/**
 * Class for applying reference extraction on text files that include layout
 * information (see {@link CermineLineLayoutExtractor}).
 *
 */
public class ReferenceExtractor {

    private CrfReferenceLineAnnotator crfReferenceLineAnnotator;

    private CermineLineLayoutExtractor cermineLineLayoutExtractor;
    private BibReferenceParser<BibEntry> bibReferenceParser;

    public ReferenceExtractor(File crfModelFile) throws AnalysisException {
        this.crfReferenceLineAnnotator = new CrfReferenceLineAnnotator(crfModelFile);
        ComponentConfiguration componentConfiguration = new ComponentConfiguration();

        this.cermineLineLayoutExtractor = new CermineLineLayoutExtractor(componentConfiguration);
        this.bibReferenceParser = CRFBibReferenceParser.getInstance();
    }

    public List<BibEntry> extractBibEntriesFromReferenceFile(File referenceFile) throws IOException, AnalysisException {
        List<String> referenceStrings = org.apache.commons.io.FileUtils.readLines(referenceFile,
                Charset.defaultCharset());
        return this.extractBibEntriesFromReferences(referenceStrings);
    }

    public List<BibEntry> extractBibEntriesFromReferences(List<String> referenceStrings)
            throws IOException, AnalysisException {
        List<BibEntry> bibEntries = new ArrayList<BibEntry>();

        for (String referenceString : referenceStrings) {
            bibEntries.add(this.bibReferenceParser.parseBibReference(referenceString));
        }
        return bibEntries;
    }

    public List<BibEntry> extractBibtexFromPdf(File pdfFile) throws IOException, AnalysisException {
        List<String> referenceStrings = this.extractReferencesFromPdf(pdfFile);
        List<BibEntry> bibEntries = new ArrayList<BibEntry>();

        for (String referenceString : referenceStrings) {
            bibEntries.add(this.bibReferenceParser.parseBibReference(referenceString));
        }
        return bibEntries;
    }

    public List<String> extractReferencesFromLayoutFile(File layoutFile) throws IOException, AnalysisException {
        // TODO specify/pass Charset?
        List<String> layoutLines = org.apache.commons.io.FileUtils.readLines(layoutFile, Charset.defaultCharset());
        return this.extractReferencesFromLayoutLines(layoutLines);
    }

    public List<String> extractReferencesFromPdf(File pdfFile) throws IOException, AnalysisException {
        List<String> layoutLines = this.cermineLineLayoutExtractor.extract(pdfFile);
        return this.extractReferencesFromLayoutLines(layoutLines);
    }

    private List<String> extractReferencesFromLayoutLines(List<String> layoutLines)
            throws IOException, AnalysisException {
        List<String> annotatedLines = this.crfReferenceLineAnnotator.annotate(layoutLines);
        List<String> referenceStrings = ReferenceLineMerger.merge(annotatedLines);
        return referenceStrings;
    }

}

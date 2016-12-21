package de.exciteproject.refext;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FilenameUtils;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import com.beust.jcommander.converters.FileConverter;

import de.exciteproject.refext.extract.CermineLineLayoutExtractor;
import de.exciteproject.refext.extract.CrfReferenceLineAnnotator;
import de.exciteproject.refext.extract.ReferenceLineMerger;
import de.exciteproject.refext.util.FileUtils;
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

    public static void main(String[] args) throws IOException, AnalysisException {
        ReferenceExtractor referenceExtractor = new ReferenceExtractor();

        JCommander jCommander = null;
        try {
            jCommander = new JCommander(referenceExtractor, args);
        } catch (ParameterException e) {
            e.printStackTrace();
            return;
        }

        if (referenceExtractor.help) {
            jCommander.usage();
        } else {
            referenceExtractor.run();
        }
    }

    @Parameter(names = { "-h", "--help" }, description = "print information about available parameters", help = true)
    private boolean help;

    @Parameter(names = { "-bibtex", "--extract-bibtex-references" }, description = "will extract bibtex references")
    private boolean extractBibtex = false;

    @Parameter(names = { "-refs", "--extract-reference-strings" }, description = "will extract reference strings")
    private boolean extractReferenceStrings = false;

    @Parameter(names = { "-skipExist",
            "--skip-existing-ouput-files" }, description = "will skip files for which there is already an output file")
    private boolean skipIfOutputExists = false;

    @Parameter(names = { "-sizeLimit", "--pdf-file-size-limit" }, description = "limit in byte for pdf files")
    private long pdfFileSizeLimit = 10000000;

    @Parameter(names = { "-crfModel",
            "--crf-model-path" }, description = "File containing a CRF model (see SupervisedCrfTrainer)", required = true, converter = FileConverter.class)
    private File crfModelFile;

    @Parameter(names = { "-pdf",
            "--input-pdf-path" }, description = "File or directory containing PDFs", converter = FileConverter.class)
    private File pdfFile;

    @Parameter(names = { "-layout",
            "--layout-path" }, description = "File or directory where files contain lines and layout information (see CermineLineLayoutExtractor)", converter = FileConverter.class)
    private File layoutFile;

    @Parameter(names = { "-outputDir",
            "--output-directory" }, description = "Directory to store the output", required = true, converter = FileConverter.class)
    private File outputDir;

    private CrfReferenceLineAnnotator crfReferenceLineAnnotator;

    private CermineLineLayoutExtractor cermineLineLayoutExtractor;
    private BibReferenceParser<BibEntry> bibReferenceParser;

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

    public void init(File crfModelFile) throws AnalysisException {
        this.crfReferenceLineAnnotator = new CrfReferenceLineAnnotator(crfModelFile);
        this.cermineLineLayoutExtractor = new CermineLineLayoutExtractor();
        this.bibReferenceParser = CRFBibReferenceParser.getInstance();

    }

    private List<String> extractReferencesFromLayoutLines(List<String> layoutLines)
            throws IOException, AnalysisException {
        List<String> annotatedLines = this.crfReferenceLineAnnotator.annotate(layoutLines);
        List<String> referenceStrings = ReferenceLineMerger.merge(annotatedLines);
        return referenceStrings;
    }

    private File getOutputFile(File inputFile, String inputDirectoryPath) {

        String subDirectories = inputFile.getParentFile().getAbsolutePath().replaceFirst(inputDirectoryPath, "");
        File currentOutputDirectory = new File(this.outputDir.getAbsolutePath() + File.separator + subDirectories);

        String outputFileName = FilenameUtils.removeExtension(inputFile.getName());
        if (this.extractBibtex) {
            outputFileName += ".bib";
        } else {
            if (this.extractReferenceStrings) {
                outputFileName += ".txt";
            }
        }

        File outputFile = new File(currentOutputDirectory.getAbsolutePath() + File.separator + outputFileName);
        return outputFile;
    }

    private void run() throws AnalysisException, IOException {
        if (((this.pdfFile == null) && (this.layoutFile == null))
                || ((this.pdfFile != null) && (this.layoutFile != null))) {
            throw new IllegalArgumentException("specify either -pdf or -layout");
        }

        if (this.extractReferenceStrings == this.extractBibtex) {
            throw new IllegalArgumentException("specify either -bibtex or -refs");
        }

        if (!this.outputDir.exists()) {
            this.outputDir.mkdirs();
        }

        this.init(this.crfModelFile);

        List<File> inputFiles = new ArrayList<File>();
        String inputDirectoryPath = null;

        if (this.pdfFile != null) {
            inputFiles = FileUtils.asList(this.pdfFile);
            inputDirectoryPath = FileUtils.getDirctory(this.pdfFile).getAbsolutePath();

        } else {
            if (this.layoutFile != null) {
                inputFiles = FileUtils.asList(this.layoutFile);
                inputDirectoryPath = FileUtils.getDirctory(this.layoutFile).getAbsolutePath();
            }
        }

        for (File inputFile : inputFiles) {
            System.out.println("processing: " + inputFile);

            File outputFile = this.getOutputFile(inputFile, inputDirectoryPath);

            // skip if outputFile already exists
            if (this.skipIfOutputExists && outputFile.exists()) {
                continue;
            }

            List<String> referenceStrings = new ArrayList<String>();
            if (this.pdfFile != null) {
                // skip files that are larger than 10MB
                if (inputFile.length() > this.pdfFileSizeLimit) {
                    continue;
                }

                referenceStrings = this.extractReferencesFromPdf(inputFile);
            } else {
                if (this.layoutFile != null) {
                    referenceStrings = this.extractReferencesFromLayoutFile(inputFile);
                }
            }
            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(outputFile));
            for (String referenceString : referenceStrings) {
                if (this.extractReferenceStrings) {
                    bufferedWriter.write(referenceString);
                } else {
                    if (this.extractBibtex) {
                        BibEntry bibEntry = this.bibReferenceParser.parseBibReference(referenceString);
                        bufferedWriter.write(bibEntry.toBibTeX());
                    }
                }
                bufferedWriter.newLine();
            }
            bufferedWriter.close();

        }

    }

}

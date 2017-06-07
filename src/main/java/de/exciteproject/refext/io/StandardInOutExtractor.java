package de.exciteproject.refext.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FilenameUtils;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import com.beust.jcommander.converters.FileConverter;
import com.google.gson.Gson;

import de.exciteproject.refext.ReferenceExtractor;
import pl.edu.icm.cermine.exception.AnalysisException;

/**
 * Class that listens to inline JSON specifying the files to process
 */
public class StandardInOutExtractor {

    public static void main(String[] args) throws IOException, AnalysisException {
        StandardInOutExtractor standardInOutExtractor = new StandardInOutExtractor();

        JCommander jCommander = null;
        try {
            jCommander = new JCommander(standardInOutExtractor, args);
        } catch (ParameterException e) {
            e.printStackTrace();
            return;
        }

        if (standardInOutExtractor.help) {
            jCommander.usage();
        } else {
            standardInOutExtractor.run();
        }
    }

    @Parameter(names = { "-h", "--help" }, description = "print information about available parameters", help = true)
    private boolean help;

    @Parameter(names = { "-sizeLimit", "--pdf-file-size-limit" }, description = "limit in byte for pdf files")
    private long pdfFileSizeLimit = 10000000;

    @Parameter(names = { "-crfModel",
            "--crf-model-path" }, description = "File containing a CRF model (see SupervisedCrfTrainer)", required = true, converter = FileConverter.class)
    private File crfModelFile;

    private void run() throws AnalysisException, IOException {

        ReferenceExtractor referenceExtractor = new ReferenceExtractor(this.crfModelFile);
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));

        String line;
        while ((line = bufferedReader.readLine()) != null) {

            if ("q".equals(line)) {
                System.exit(0);
            }

            Gson gson = new Gson();

            ExtractorJsonInput extractorJsonInput = gson.fromJson(line, ExtractorJsonInput.class);
            // System.out.println("input : " + line);
            // System.out.println("file: " + extractorJsonInput.inputFilePath);
            // System.out.println("layout: " + extractorJsonInput.isLayoutFile);
            // System.out.println("pdf: " + extractorJsonInput.isPdfFile);
            // System.out.println("-----------\n");

            if (extractorJsonInput.isLayoutFile == extractorJsonInput.isPdfFile) {
                System.err.println("Wrong input: either set isPdfFile or isLayoutFile to true");
                continue;
            }
            File inputFile = new File(extractorJsonInput.inputFilePath);
            List<String> references = new ArrayList<String>();
            if (extractorJsonInput.isLayoutFile) {
                references = referenceExtractor.extractReferencesFromLayoutFile(inputFile, Charset.defaultCharset());
            }
            if (extractorJsonInput.isPdfFile) {
                references = referenceExtractor.extractReferencesFromPdf(inputFile);
            }

            String inputFileBaseName = FilenameUtils.getBaseName(inputFile.getName());
            for (String reference : references) {
                // TODO output as json?
                System.out.println(inputFileBaseName + "\t" + reference);
            }
        }
    }
}

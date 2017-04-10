package de.exciteproject.refext;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FilenameUtils;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import com.beust.jcommander.converters.FileConverter;
import com.google.gson.Gson;

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

    // @Parameter(names = { "-skipExist",
    // "--skip-existing-ouput-files" }, description = "will skip files for which
    // there is already an output file")
    // private boolean skipIfOutputExists = false;

    @Parameter(names = { "-sizeLimit", "--pdf-file-size-limit" }, description = "limit in byte for pdf files")
    private long pdfFileSizeLimit = 10000000;

    @Parameter(names = { "-crfModel",
            "--crf-model-path" }, description = "File containing a CRF model (see SupervisedCrfTrainer)", required = true, converter = FileConverter.class)
    private File crfModelFile;

    // @Parameter(names = { "-pdf",
    // "--input-pdf-path" }, description = "File or directory containing PDFs",
    // converter = FileConverter.class)
    // private File pdfFile;
    //
    // @Parameter(names = { "-layout",
    // "--layout-path" }, description = "File or directory where files contain
    // lines and layout information (see CermineLineLayoutExtractor)", converter
    // = FileConverter.class)
    // private File layoutFile;
    // @Parameter(names = { "-outputDir",
    // "--output-directory" }, description = "Directory to store the output",
    // required = true, converter = FileConverter.class)
    // private File outputDir;

    private void run() throws AnalysisException, IOException {

        ReferenceExtractor referenceExtractor = new ReferenceExtractor(this.crfModelFile);
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
        // int ch;
        // System.out.print("Enter some text: ");
        // String test = "";
        // while ((ch = System.in.read()) != '\n') {
        // test += (char) ch;
        // // System.out.print((char) ch);
        // }
        // System.out.println("total: " + test);

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
                references = referenceExtractor.extractReferencesFromLayoutFile(inputFile);
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
        // List<File> inputFiles = new ArrayList<File>();
        // String inputDirectoryPath = null;

        // if (this.pdfFile != null) {
        // inputFiles = FileUtils.asList(this.pdfFile);
        // inputDirectoryPath =
        // FileUtils.getDirctory(this.pdfFile).getAbsolutePath();

        // } else {
        // if (this.layoutFile != null) {
        // inputFiles = FileUtils.asList(this.layoutFile);
        // inputDirectoryPath =
        // FileUtils.getDirctory(this.layoutFile).getAbsolutePath();
        // }
        // }

        // for (File inputFile : inputFiles) {
        // System.out.println("processing: " + inputFile);

        // File outputFile = this.getOutputFile(inputFile, inputDirectoryPath);

        // // skip if outputFile already exists
        // if (this.skipIfOutputExists && outputFile.exists()) {
        // continue;
        // }

        // List<String> referenceStrings = new ArrayList<String>();
        // if (this.pdfFile != null) {
        // // skip files that are larger than 10MB
        // if (inputFile.length() > this.pdfFileSizeLimit) {
        // continue;
        // }

        // referenceStrings =
        // referenceExtractor.extractReferencesFromPdf(inputFile);
        // } else {
        // if (this.layoutFile != null) {
        // referenceStrings =
        // referenceExtractor.extractReferencesFromLayoutFile(inputFile);
        // }
        // }
        // BufferedWriter bufferedWriter = new BufferedWriter(new
        // FileWriter(outputFile));

        // for (String referenceString : referenceStrings) {
        // bufferedWriter.write(referenceString);
        // bufferedWriter.newLine();
        // }

        // bufferedWriter.close();
        // }
    }
}

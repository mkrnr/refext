package de.exciteproject.refext.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

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
public class StandardInOutReferenceExtractor {

    public static void main(String[] args) throws IOException, AnalysisException {
        StandardInOutReferenceExtractor standardInOutExtractor = new StandardInOutReferenceExtractor();

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

            ReferenceExtractorJsonInput referenceExtractorJsonInput = gson.fromJson(line,
                    ReferenceExtractorJsonInput.class);

            File inputFile = new File(referenceExtractorJsonInput.inputFilePath);
            List<String> references = new ArrayList<String>();

            references = referenceExtractor.extractReferencesFromLayoutFile(inputFile, Charset.defaultCharset());

            ReferenceExtractorJsonOutput referenceExtractorJsonOutput = new ReferenceExtractorJsonOutput();
            referenceExtractorJsonOutput.inputFilePath = referenceExtractorJsonInput.inputFilePath;
            referenceExtractorJsonOutput.references = references;
            System.out.println(gson.toJson(referenceExtractorJsonOutput));
        }
        bufferedReader.close();
    }
}

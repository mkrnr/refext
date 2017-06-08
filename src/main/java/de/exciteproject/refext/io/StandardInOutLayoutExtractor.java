package de.exciteproject.refext.io;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import com.google.gson.Gson;

import de.exciteproject.refext.extract.CermineLineLayoutExtractor;
import pl.edu.icm.cermine.ComponentConfiguration;
import pl.edu.icm.cermine.exception.AnalysisException;

/**
 * Class that listens to inline JSON specifying the files to process
 */
public class StandardInOutLayoutExtractor {

    public static void main(String[] args) throws IOException, AnalysisException {
        StandardInOutLayoutExtractor standardInOutExtractor = new StandardInOutLayoutExtractor();

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

    private void run() throws AnalysisException, IOException {

        CermineLineLayoutExtractor cermineLineLayoutExtractor = new CermineLineLayoutExtractor(
                new ComponentConfiguration());
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));

        String inputLine;
        while ((inputLine = bufferedReader.readLine()) != null) {

            if ("q".equals(inputLine)) {
                System.exit(0);
            }

            Gson gson = new Gson();

            LayoutExtractorJsonInput extractorJsonInput = gson.fromJson(inputLine, LayoutExtractorJsonInput.class);

            File inputFile = new File(extractorJsonInput.inputFilePath);
            File outputFile = new File(extractorJsonInput.outputFilePath);

            List<String> layoutLines = cermineLineLayoutExtractor.extract(inputFile);

            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(outputFile));
            for (String layoutLine : layoutLines) {
                bufferedWriter.write(layoutLine);
                bufferedWriter.newLine();
            }
            bufferedWriter.close();
        }
        bufferedReader.close();
    }
}

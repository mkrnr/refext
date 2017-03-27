package de.exciteproject.refext.postproc;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Scanner;

import org.apache.commons.io.FilenameUtils;

import de.exciteproject.refext.util.FileUtils;
import pl.edu.icm.cermine.exception.AnalysisException;

/**
 * Class for merging files into one CSV with the format: file-id TAB line
 */
public class FileToCsvDumpMerger {

    public static void main(String[] args) throws AnalysisException, IOException {
        File inputDir = new File(args[0]);
        File outputFile = new File(args[1]);

        FileToCsvDumpMerger fileToCsvDumpMerger = new FileToCsvDumpMerger();
        List<File> inputFiles = FileUtils.asList(inputDir);

        if (outputFile.exists()) {
            outputFile.delete();
        }
        for (File inputFile : inputFiles) {
            fileToCsvDumpMerger.appendToCsv(inputFile, outputFile);
        }
    }

    public void appendToCsv(File inputFile, File outputFile) throws IOException, AnalysisException {
        String fileId = FilenameUtils.getBaseName(inputFile.getName());

        BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(outputFile, true));
        Scanner inputScanner = new Scanner(inputFile);
        while (inputScanner.hasNextLine()) {
            bufferedWriter.write(fileId + "\t" + inputScanner.nextLine());
            bufferedWriter.newLine();
        }
        inputScanner.close();
        bufferedWriter.close();

    }

}
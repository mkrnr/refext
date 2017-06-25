package de.exciteproject.refext.train;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.apache.commons.io.FilenameUtils;

import de.exciteproject.refext.util.FileUtils;
import pl.edu.icm.cermine.exception.AnalysisException;

/**
 * Removes a certain number of lines with a specific label based on a predefined
 * ratio
 */
public class LabelFilterer {

    public static void main(String[] args) throws AnalysisException, IOException {
        File inputDir = new File(args[0]);
        File outputDir = new File(args[1]);
        int ratio = Integer.parseInt(args[2]);
        File ratioOutputDir = new File(outputDir.getAbsolutePath() + ratio);

        if (!ratioOutputDir.exists()) {
            ratioOutputDir.mkdirs();
        }
        LabelFilterer xmlToBioConverter = new LabelFilterer();
        List<File> inputFiles = FileUtils.asList(inputDir);
        String inputDirectoryPath = FileUtils.getDirctory(inputDir).getAbsolutePath();

        for (File inputFile : inputFiles) {
            String inputFileSubPath = inputFile.getAbsolutePath().replaceAll(inputDirectoryPath, "");

            inputFileSubPath = FilenameUtils.removeExtension(inputFileSubPath) + ".csv";
            File outputFile = new File(ratioOutputDir + inputFileSubPath);

            List<String> annotatedText = xmlToBioConverter.filter(inputFile, "O", ratio);
            Files.write(Paths.get(outputFile.getAbsolutePath()), annotatedText, Charset.defaultCharset());
        }
    }

    public List<String> filter(File inputFile, String label, double ratio) throws IOException, AnalysisException {
        Scanner s = new Scanner(inputFile,"UTF-8");
        ArrayList<String> bioAnnotatedLines = new ArrayList<String>();
        while (s.hasNextLine()) {
            bioAnnotatedLines.add(s.nextLine());
        }
        s.close();

        int labelCount = 0;
        for (String line : bioAnnotatedLines) {
            if (line.startsWith(label + "\t")) {
                labelCount++;
            }
        }

        double labelsToKeep = (bioAnnotatedLines.size() - labelCount) * ratio;

        double percentageToKeep = labelsToKeep / labelCount;

        List<String> filteredBioAnnotatedLines = new ArrayList<String>();
        for (String line : bioAnnotatedLines) {
            if (line.startsWith(label + "\t")) {
                if (Math.random() < percentageToKeep) {
                    filteredBioAnnotatedLines.add(line);
                }
            } else {
                filteredBioAnnotatedLines.add(line);
            }
        }

        return filteredBioAnnotatedLines;

    }

    private String getAnnotation(List<String> annotatedLines, int index) {
        if (annotatedLines.size() <= index) {
            return "";
        } else {
            String[] lineSplit = annotatedLines.get(index).split("\\t");
            return lineSplit[0];
        }
    }

    private String getLine(List<String> annotatedLines, int index) {
        if (annotatedLines.size() <= index) {
            return "";
        } else {
            String[] lineSplit = annotatedLines.get(index).split("\\t");
            return lineSplit[1];
        }
    }
}

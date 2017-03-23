package de.exciteproject.refext.train;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import de.exciteproject.refext.util.FileUtils;
import pl.edu.icm.cermine.exception.AnalysisException;

/**
 * Simplified annotator for generating training data. Does not handle
 * footers/headers that appear inside a reference string.
 */
public class XmlToBioConverter {

    public static void main(String[] args) throws AnalysisException, IOException {
        File inputDir = new File(args[0]);
        File outputDir = new File(args[1]);
        if (!outputDir.exists()) {
            outputDir.mkdirs();
        }
        XmlToBioConverter xmlToBioConverter = new XmlToBioConverter();
        List<File> inputFiles = FileUtils.asList(inputDir);
        String inputDirectoryPath = FileUtils.getDirctory(inputDir).getAbsolutePath();

        for (File inputFile : inputFiles) {
            String inputFileSubPath = inputFile.getAbsolutePath().replaceAll(inputDirectoryPath, "");

            inputFileSubPath = inputFileSubPath.replaceAll(".xml$", ".csv");
            File outputFile = new File(outputDir + inputFileSubPath);
            // if (outputFile.exists()) {
            // continue;
            // }

            List<String> annotatedText = xmlToBioConverter.annotateText(inputFile);
            Files.write(Paths.get(outputFile.getAbsolutePath()), annotatedText, Charset.defaultCharset());
        }
    }

    public List<String> annotateText(File inputFile) throws IOException, AnalysisException {
        Scanner s = new Scanner(inputFile);
        ArrayList<String> xmlAnnotatedLines = new ArrayList<String>();
        while (s.hasNextLine()) {
            xmlAnnotatedLines.add(s.nextLine());
        }
        s.close();

        List<String> bioAnnotatedLines = new ArrayList<String>();
        boolean insideRef = false;
        boolean insideOth = false;
        for (int i = 0; i < xmlAnnotatedLines.size(); i++) {
            String label = "";
            String line = xmlAnnotatedLines.get(i);
            if (line.contains("<ref>")) {
                label = "B-REF";
                insideRef = true;
                line = line.replaceFirst("<ref>", "");
            }
            if (line.contains("</ref")) {
                if (label.isEmpty()) {
                    label = "I-REF";
                }
                insideRef = false;
                line = line.replaceFirst("</ref>", "");
            }
            if (line.contains("<oth>")) {
                label = "B-REFO";
                insideOth = true;
                line = line.replaceFirst("<oth>", "");
            }
            if (line.contains("</oth")) {
                if (label.isEmpty()) {
                    label = "I-REFO";
                }
                insideOth = false;
                line = line.replaceFirst("</oth>", "");
            }
            if (label.isEmpty()) {
                if (insideOth) {
                    label = "I-REFO";
                } else {
                    if (insideRef) {
                        label = "I-REF";
                    } else {
                        label = "O";
                    }
                }
            }
            bioAnnotatedLines.add(label + "\t" + line);

        }
        return bioAnnotatedLines;
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

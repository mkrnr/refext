package de.exciteproject.refext.extract;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.apache.commons.io.FilenameUtils;

import de.exciteproject.refext.ReferenceExtractor;
import de.exciteproject.refext.extract.ReferenceLineAnnotation;
import de.exciteproject.refext.util.FileUtils;
import pl.edu.icm.cermine.exception.AnalysisException;

import java.util.ArrayList;
import java.util.List;

import pl.edu.icm.cermine.tools.CharacterUtils;
import pl.edu.icm.cermine.exception.AnalysisException;

/**
 * Class for extracting and merging references lines from a list of BIO tagged
 * input to produce reference strings lines.
 */
public class ReferenceLineMerger {

    private final static String hyphenList = String.valueOf(CharacterUtils.DASH_CHARS).replaceAll("-", "") + "-";

    public static void main(String[] args) throws AnalysisException, IOException {
        File inputDir = new File(args[0]);
        File outputDir = new File(args[1]);
        if (!outputDir.exists()) {
            outputDir.mkdirs();
        }

        List<File> inputFiles = FileUtils.asList(inputDir);
        for (File inputFile : inputFiles) {
            String subDirectories = inputFile.getParentFile().getAbsolutePath().replace("\\", "/")
                    .replaceFirst(inputDir.getAbsolutePath().replace("\\", "/"), "");
            //File currentOutputDirectory = new File(outputDir.getAbsolutePath() + File.separator + subDirectories);
            File currentOutputDirectory = new File(outputDir.getAbsolutePath());
            String outputFileName = FilenameUtils.removeExtension(inputFile.getName()) + ".csv";
            File outputFile = new File(currentOutputDirectory + File.separator + outputFileName);

            // skip computation if outputFile already exists
            if (outputFile.exists()) {
                continue;
            }
            if (!currentOutputDirectory.exists()) {
                currentOutputDirectory.mkdirs();
            }
            // Read Line
            List<String> list_of_references = org.apache.commons.io.FileUtils.readLines(inputFile, Charset.defaultCharset());
            List<String> refs_list_merged = ReferenceLineMerger.merge(list_of_references);
            Files.write(Paths.get(outputFile.getAbsolutePath()), refs_list_merged, Charset.defaultCharset());
        }
    }
    
    public static List<String> merge(List<String> inputLines) {
        List<String> references = new ArrayList<String>();

        String currentReference = null;
        for (String inputLine : inputLines) {
            String[] lineSplit = inputLine.split("\t");
            if (lineSplit[0].equals("O") || lineSplit[0].equals("B-REF")) {
                if (currentReference != null) {
                    references.add(currentReference);
                    currentReference = null;
                }
            }
            if (lineSplit[0].equals("B-REF")) {
                currentReference = lineSplit[1];
            }
            if (lineSplit[0].equals("I-REF")) {
                if (currentReference == null) {
                    continue;
                }
                if (lineSplit.length > 1) {
                    currentReference = ReferenceLineMerger.mergeLines(currentReference, lineSplit[1]);
                }
            }
        }
        if (currentReference != null) {
            references.add(currentReference);
        }
        return references;

    }

    /**
     * this approach on merging lines that end with a hyphen is based on
     * pl.edu.icm.cermine.bibref.KMeansBibReferenceExtractor
     *
     * @param currentString
     * @param stringToAdd
     * @return
     */
    public static String mergeLines(String currentString, String stringToAdd) {
        if (currentString.matches(".*[a-zA-Z][" + hyphenList + "]")) {
            currentString = currentString.substring(0, currentString.length() - 1);
        } else {
            currentString += " ";
        }
        currentString += stringToAdd;
        return currentString;
    }
}

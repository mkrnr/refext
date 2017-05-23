package de.exciteproject.refext.extract;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.exciteproject.refext.util.FileUtils;

/**
 * Class for extracting reference strings from XML files that are annotated with
 * <ref> and <oth> tags.
 */
public class XmlReferenceExtractor {

    public static void main(String[] args) throws IOException {
        File inputDir = new File(args[0]);
        File outputDir = new File(args[1]);
        int mode = Integer.parseInt(args[2]);
        if (!outputDir.exists()) {
            outputDir.mkdirs();
        }
        XmlReferenceExtractor xmlReferenceExtractor = new XmlReferenceExtractor();
        for (File inputFile : inputDir.listFiles()) {
            String outputFileEnding = "";
            switch (mode) {
            case 0:
                outputFileEnding = ".txt";
                break;
            case 1:
                outputFileEnding = ".csv";
                break;
            }
            File outputFile = new File(outputDir.getAbsolutePath() + File.separator
                    + inputFile.getName().split("\\.")[0] + outputFileEnding);

            try {
                List<String> references = new ArrayList<String>();
                switch (mode) {
                case 0:
                    references = xmlReferenceExtractor.extract(inputFile, false, true);
                    break;
                case 1:
                    references = xmlReferenceExtractor.extractAndAnnotateReferenceLines(inputFile, false);
                    break;
                }
                BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(outputFile));
                for (String reference : references) {
                    bufferedWriter.write(reference);
                    bufferedWriter.newLine();
                }
                bufferedWriter.close();
            } catch (StackOverflowError e) {
                // TODO: throw exception
                System.err.println("stackoverflow at file: " + inputFile.getAbsolutePath());
            }
        }

    }

    public List<String> extract(File inputFile, boolean fixEncoding, boolean merge) throws IOException {
        // TODO add testing
        List<String> references = new ArrayList<String>();

        String fileContent = FileUtils.readFile(inputFile);
        Pattern referenceStringPattern = Pattern.compile("<ref>((\\R||.)*)</ref>");

        Matcher matcher = referenceStringPattern.matcher(fileContent);

        while (matcher.find()) {

            String reference = matcher.group(1);

            // remove <oth> tags
            reference = reference.replaceAll("<oth>((\\R||.)*)</oth>", "");

            // remove empty lines
            reference = reference.replaceAll("\\R\\s*\\R", System.lineSeparator());

            if (fixEncoding) {
                reference = reference.replaceAll("\u00AD", "-");
            }

            if (merge) {
                String[] lineSplit = reference.split("\\R");
                String tempReference = lineSplit[0];
                for (int i = 1; i < lineSplit.length; i++) {
                    tempReference = ReferenceLineMerger.mergeLines(tempReference, lineSplit[i]);
                }
                references.add(tempReference);
            } else {
                references.add(reference);
            }
        }
        return references;
    }

    public List<String> extractAndAnnotateReferenceLines(File inputFile, boolean fixEncoding) throws IOException {
        List<String> references = this.extract(inputFile, fixEncoding, false);
        List<String> annotatedReferenceLines = new ArrayList<String>();
        for (String reference : references) {
            String tempReference = reference;
            tempReference = "B-REF\t" + tempReference;
            tempReference = tempReference.replaceAll("\n", "\nI-REF\t");

            annotatedReferenceLines.add(tempReference);
        }
        return annotatedReferenceLines;

    }

}

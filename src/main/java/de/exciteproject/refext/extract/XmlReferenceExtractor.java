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
        if (!outputDir.exists()) {
            outputDir.mkdirs();
        }
        XmlReferenceExtractor xmlReferenceExtractor = new XmlReferenceExtractor();
        for (File inputFile : inputDir.listFiles()) {
            File outputFile = new File(
                    outputDir.getAbsolutePath() + File.separator + inputFile.getName().replaceAll("\\.xml", ".txt"));

            if (outputFile.exists()) {
                continue;
            }
            try {
                List<String> references = xmlReferenceExtractor.extract(inputFile);
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

    public List<String> extract(File inputFile) throws IOException {
        // TODO add testing
        List<String> references = new ArrayList<String>();

        String fileContent = FileUtils.readFile(inputFile);
        Pattern referenceStringPattern = Pattern.compile("<ref>((\\R||.)*)</ref>");

        Matcher matcher = referenceStringPattern.matcher(fileContent);

        while (matcher.find()) {

            String referenceString = matcher.group(1);

            referenceString = referenceString.replaceAll("\u00AD", "-");
            // remove <oth> tags
            referenceString = referenceString.replaceAll("<oth>((\\R||.)*)</oth>", "");
            // remove empty lines
            referenceString = referenceString.replaceAll("\\R\\s*\\R", System.lineSeparator());

            // remove dashes at the end of a line when merging
            referenceString = referenceString.replaceAll("[?–-]+\\R", "");

            // merge all remaining lines by adding a space
            referenceString = referenceString.replaceAll("\\s*\\R\\s*", " ");

            references.add(referenceString);
        }
        return references;
    }

}

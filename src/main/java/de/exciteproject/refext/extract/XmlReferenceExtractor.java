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
import pl.edu.icm.cermine.tools.CharacterUtils;

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

            try {
                List<String> references = xmlReferenceExtractor.extract(inputFile, false, true);
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

    public List<String> extract(File inputFile, boolean fixEncoding, boolean fixHypensLikeCermine) throws IOException {
        // TODO add testing
        List<String> references = new ArrayList<String>();

        String fileContent = FileUtils.readFile(inputFile);
        Pattern referenceStringPattern = Pattern.compile("<ref>((\\R||.)*)</ref>");

        Matcher matcher = referenceStringPattern.matcher(fileContent);

        String hyphenList = String.valueOf(CharacterUtils.DASH_CHARS);
        hyphenList = hyphenList.replaceAll("-", "") + "-";

        while (matcher.find()) {

            String referenceString = matcher.group(1);

            // remove <oth> tags
            referenceString = referenceString.replaceAll("<oth>((\\R||.)*)</oth>", "");

            // remove empty lines
            referenceString = referenceString.replaceAll("\\R\\s*\\R", System.lineSeparator());

            if (fixEncoding) {
                referenceString = referenceString.replaceAll("\u00AD", "-");
            }

            // TODO test
            // this approach on merging lines that end with a hyphen is based on
            // pl.edu.icm.cermine.bibref.KMeansBibReferenceExtractor
            if (fixHypensLikeCermine) {
                String[] lineSplit = referenceString.split("\\R");
                String actRef = "";
                for (int i = 0; i < (lineSplit.length - 1); i++) {
                    actRef += lineSplit[i];
                    if (actRef.matches(".*[a-zA-Z][" + hyphenList + "]")) {
                        actRef = actRef.substring(0, actRef.length() - 1);
                    } else {
                        actRef += " ";
                    }
                }
                actRef += lineSplit[lineSplit.length - 1];

                referenceString = actRef;
            } else {
                // remove dashes at the end of a line when merging
                referenceString = referenceString.replaceAll("[" + hyphenList + "]+\\R", "");

                // merge all remaining lines by adding a space
                referenceString = referenceString.replaceAll("\\s*\\R\\s*", " ");
            }

            references.add(referenceString);
        }
        return references;
    }

}

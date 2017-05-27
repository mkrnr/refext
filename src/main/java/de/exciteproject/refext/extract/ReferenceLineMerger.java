package de.exciteproject.refext.extract;

import java.util.ArrayList;
import java.util.List;

import pl.edu.icm.cermine.tools.CharacterUtils;

/**
 * Class for extracting and merging references from a list of BIO tagged input
 * lines.
 */
public class ReferenceLineMerger {

    private final static String hyphenList = String.valueOf(CharacterUtils.DASH_CHARS).replaceAll("-", "") + "-";

    public static List<String> merge(List<String> inputLines) {
        List<String> references = new ArrayList<String>();

        String currentReference = null;
        for (String inputLine : inputLines) {
            String[] lineSplit = inputLine.split("\t");

            if (lineSplit[0].equals("B-REF")) {
                if (currentReference != null) {
                    references.add(currentReference);
                    currentReference = null;
                }
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

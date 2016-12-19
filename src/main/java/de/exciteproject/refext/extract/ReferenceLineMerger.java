package de.exciteproject.refext.extract;

import java.util.ArrayList;
import java.util.List;

public class ReferenceLineMerger {

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
                currentReference += " " + lineSplit[1];
            }
        }
        return references;

    }
}

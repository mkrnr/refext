package de.exciteproject.refext.extract;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import de.exciteproject.refext.ReferenceExtractor;
import pl.edu.icm.cermine.bibref.BibReferenceParser;
import pl.edu.icm.cermine.bibref.CRFBibReferenceParser;
import pl.edu.icm.cermine.bibref.model.BibEntry;
import pl.edu.icm.cermine.exception.AnalysisException;

public class ReferenceStringFromAnnotatedLinesExtractor {

    public static List<String> extract(List<String> inputLines) {
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

    public static void main(String[] args) throws IOException, AnalysisException {
        File inputFile = new File(args[0]);
        // File outputFile = new File(args[1]);
        File crfModelFile = new File(args[2]);
        ReferenceExtractor extractionRunner = new ReferenceExtractor(crfModelFile);

        List<String> lines = extractionRunner.run(inputFile);

        List<String> references = ReferenceStringFromAnnotatedLinesExtractor.extract(lines);

        BibReferenceParser<BibEntry> bibReferenceParser = CRFBibReferenceParser.getInstance();
        for (String reference : references) {
            BibEntry bibEntry = bibReferenceParser.parseBibReference(reference);
            System.out.println(bibEntry.toBibTeX());
        }
        // for (String reference : references) {
        // System.out.println(reference);
        // System.out.println("---");
        // }

    }
}

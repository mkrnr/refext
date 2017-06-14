package de.exciteproject.refext.extract;

import java.util.HashMap;
import java.util.Map;

/**
 * Container class for describing the annotation format used in
 * {@link ReferenceLineAnnotator}.
 */
public class ReferenceLineAnnotation {
    private String line;
    private Map<String, Double> annotations;

    public ReferenceLineAnnotation(String line) {
        this.line = line;
        this.annotations = new HashMap<String, Double>();
    }

    public void addAnnotation(String label, Double value) {
        this.annotations.put(label, value);
    }

    public String getBestAnnotation() {
        Map.Entry<String, Double> maxEntry = null;

        // from: https://stackoverflow.com/a/5911199/2174538
        for (Map.Entry<String, Double> entry : this.annotations.entrySet()) {
            if ((maxEntry == null) || (entry.getValue().compareTo(maxEntry.getValue()) > 0)) {
                maxEntry = entry;
            }
        }
        return maxEntry.getKey();
    }

    public String getLine() {
        return this.line;
    }
}

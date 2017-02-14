package de.exciteproject.refext.train.pipe;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import cc.mallet.pipe.Pipe;

/**
 * Class for generating feature pipes based on regular expressions.
 * <p>
 * TODO: move firstname and lastname pipes to separate class
 */
public class FeaturePipeProvider {
    private static final Map<String, String> regexMap;
    static {
        Map<String, String> tempRegexMap = new HashMap<String, String>();
        tempRegexMap.put("STARTSCAPITALIZED", "\\p{javaUpperCase}.*");
        tempRegexMap.put("STARTSNUMBER", "[0-9].*");
        tempRegexMap.put("CONTAINSYEAR", ".*\\D[0-9][0-9][0-9][0-9]\\D.*");
        tempRegexMap.put("CONTAINSPAGERANGE", ".*\\d(-|^|\")\\d.*");
        tempRegexMap.put("CONTAINSAMPHERSAND", ".*&.*");
        tempRegexMap.put("CONTAINSQUOTE", ".*[„“””‘’\"'].*");
        tempRegexMap.put("COLON", ".*:.*");
        tempRegexMap.put("SLASH", ".*/.*");
        tempRegexMap.put("BRACES", ".*\\(.*\\).*");
        tempRegexMap.put("ENDSPERIOD", ".*\\.");
        tempRegexMap.put("ENDSCOMMA", ".*,");
        tempRegexMap.put("ISNUMBER", "/d+");

        regexMap = Collections.unmodifiableMap(tempRegexMap);
    }

    private static final Map<String, String> countRegexMap;
    static {
        Map<String, String> tempCountRegexMap = new HashMap<String, String>();

        tempCountRegexMap.put("PERIODS", ".*\\..*");
        tempCountRegexMap.put("COMMAS", ".*,.*");
        tempCountRegexMap.put("CAPITALIZEDS", "\\p{Lu}.*");
        tempCountRegexMap.put("LOWERCASEDS", "\\p{javaLowerCase}.*");
        tempCountRegexMap.put("WORDS", ".+");

        countRegexMap = Collections.unmodifiableMap(tempCountRegexMap);
    }

    public static Map<String, String> getCountRegexMap() {
        return countRegexMap;
    }

    public static Map<String, String> getRegexMap() {
        return regexMap;
    }

    private HashMap<String, Pipe> featurePipes;
    private File firstNameFile;

    private File lastNameFile;
    private String csvSeparator = "\t";

    public FeaturePipeProvider(File firstNameFile, File lastNameFile) {
        this.firstNameFile = firstNameFile;
        this.lastNameFile = lastNameFile;

        this.createFeaturePipes();
    }

    public Pipe getPipe(String featureName) {
        if (!this.featurePipes.containsKey(featureName)) {
            throw new IllegalArgumentException("featureName does not exist: " + featureName);
        }
        return this.featurePipes.get(featureName);
    }

    public String[] getPipeLabels() {
        return this.featurePipes.keySet().toArray(new String[0]);
    }

    private void createFeaturePipes() {
        this.featurePipes = new HashMap<String, Pipe>();

        // add featurePipes that use a RegexPipe

        for (Entry<String, String> regexMapEntry : getRegexMap().entrySet()) {
            this.featurePipes.put(regexMapEntry.getKey(), new RegexPipe(regexMapEntry.getKey(),
                    Pattern.compile(regexMapEntry.getValue()), this.csvSeparator));
        }

        for (Entry<String, String> countRegexMapEntry : getCountRegexMap().entrySet()) {
            this.featurePipes.put(countRegexMapEntry.getKey(), new CountMatchesPipe(countRegexMapEntry.getKey(),
                    countRegexMapEntry.getValue(), this.csvSeparator));
        }

        this.featurePipes.put("INDENT", new IndentLayoutPipe("INDENT", this.csvSeparator));
        this.featurePipes.put("GAPABOVE", new GapAboveLayoutPipe("GAPABOVE", this.csvSeparator));
        // matches tokens where all letters are lower cased

        // pipes.add(new RegexMatches("CONTAINSURL",
        // Pattern.compile(".*(https?:\\/\\/)?([\\da-z\\.-]+)\\.([a-z\\.]{2,6})([\\/\\w
        // \\.-]*)*\\/?.*")));

        //
        // if (this.lastNameFile != null) {
        // String lastNamePipeLabel = "LASTNAMES";
        // this.featurePipes.put(lastNamePipeLabel, new
        // NamePipe(lastNamePipeLabel, this.lastNameFile));
        // }
        // if (this.lastNameFile != null) {
        // String lastNamePipeLabel = "FIRSTNAMES";
        // this.featurePipes.put(lastNamePipeLabel, new
        // NamePipe(lastNamePipeLabel, this.lastNameFile));
        // }

        // int[][] conjunctions = new int[2][];
        // conjunctions[0] = new int[] { -1 };
        // conjunctions[1] = new int[] { 1 };
        // this.featurePipes.put("CONJUNCTIONS", new
        // OffsetConjunctions(conjunctions));

    }

}

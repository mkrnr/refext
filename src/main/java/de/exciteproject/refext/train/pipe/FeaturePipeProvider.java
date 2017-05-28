package de.exciteproject.refext.train.pipe;

import java.io.IOException;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import com.cybozu.labs.langdetect.LangDetectException;

import cc.mallet.pipe.Pipe;
import cc.mallet.pipe.tsf.TokenText;
import cc.mallet.pipe.tsf.TokenTextCharPrefix;
import cc.mallet.pipe.tsf.TokenTextCharSuffix;

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

        int year = Calendar.getInstance().get(Calendar.YEAR);
        int decadeNumber = (year / 10) % 10;
        int yearNumber = year % 10;
        String yearRegex = "(1[6-9][0-9][0-9]|20[0-" + decadeNumber + "][0-" + yearNumber + "])";

        tempRegexMap.put("YEAR", ".*\\D*" + yearRegex + "\\D*.*");
        tempRegexMap.put("YEARINBRACES", ".*\\(" + yearRegex + "\\).*");
        tempRegexMap.put("YEARINBRACESCOLON", ".*\\(" + yearRegex + "\\):.*");

        tempRegexMap.put("SPACEINBRACES", ".*\\(.*\\s.*\\).*");
        tempRegexMap.put("PAGERANGE", ".*\\d(-|\\^|\")\\d.*");
        tempRegexMap.put("AMPHERSAND", ".*&.*");
        tempRegexMap.put("QUOTE", ".*[„“””‘’\"'].*");
        tempRegexMap.put("COLON", ".*:.*");
        tempRegexMap.put("SLASH", ".*/.*");
        tempRegexMap.put("BRACES", ".*\\(.*\\).*");
        tempRegexMap.put("ENDSPERIOD", ".*\\.");
        tempRegexMap.put("ENDSCOMMA", ".*,");
        tempRegexMap.put("ISNUMBER", "\\d+");

        // TODO generalize this
        tempRegexMap.put("STARTSTABELLE", "(?i)^(Tab\\.|Tabelle).*");
        tempRegexMap.put("STARTSQUELLE", "(?i)^Quelle.*");
        tempRegexMap.put("STARTSABBILDUNG", "(?i)^(Abb\\.|Abbildung).*");

        regexMap = Collections.unmodifiableMap(tempRegexMap);
    }

    private static final Map<String, String> countRegexMap;
    static {
        Map<String, String> tempCountRegexMap = new HashMap<String, String>();

        tempCountRegexMap.put("PERIODS", "\\.");
        tempCountRegexMap.put("COMMAS", ",");
        tempCountRegexMap.put("CAPITALIZEDS", "\\p{Lu}");
        tempCountRegexMap.put("LOWERCASEDS", "\\p{javaLowerCase}");
        tempCountRegexMap.put("WORDS", "\\s*\\S+\\s*");
        tempCountRegexMap.put("NUMBERS", "\\D*\\d+\\D*");
        tempCountRegexMap.put("CHARACTERS", ".");
        tempCountRegexMap.put("ONEUPPERCASEDS", "(^|\\P{L})\\p{Lu}(\\P{L}|$)");

        countRegexMap = Collections.unmodifiableMap(tempCountRegexMap);
    }

    public static Map<String, String> getCountRegexMap() {
        return countRegexMap;
    }

    public static Map<String, String> getRegexMap() {
        return regexMap;
    }

    private HashMap<String, Pipe> featurePipes;
    private String csvSeparator = "\t";

    public FeaturePipeProvider() throws LangDetectException, IOException {
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

    private void createFeaturePipes() throws LangDetectException, IOException {
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

        for (int i = 1; i < 10; i++) {
            this.featurePipes.put("S" + i, new TokenTextCharSuffix("S" + i + "=", i));
            this.featurePipes.put("P" + i, new TokenTextCharPrefix("P" + i + "=", i));
        }

        this.featurePipes.put("INDENT", new IndentLayoutPipe("INDENT", this.csvSeparator));
        this.featurePipes.put("GAPABOVE", new GapAboveLayoutPipe("GAPABOVE", this.csvSeparator));
        this.featurePipes.put("SHORTERLINE", new ShorterLinePipe("SHORTERLINE", this.csvSeparator));
        this.featurePipes.put("LANGUAGE", new LanguagePipe("LANGUAGE", this.csvSeparator));
        this.featurePipes.put("REFSEC", new ReferenceSectionPipe("REFSEC", this.csvSeparator));
        this.featurePipes.put("POSINDOC", new PositionInDocumentPipe("POSINDOC", this.csvSeparator));
        this.featurePipes.put("TOKENTEXT", new TokenText());

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

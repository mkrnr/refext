package de.exciteproject.refext.train.pipe;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Assert;
import org.junit.Test;

public class FeaturePipeProviderTest {

    // tempRegexMap.put("STARTSCAPITALIZED", "\\p{javaUpperCase}.*");
    // tempRegexMap.put("STARTSNUMBER", "[0-9].*");
    // tempRegexMap.put("CONTAINSYEAR", ".*\\D[0-9][0-9][0-9][0-9]\\D.*");
    // tempRegexMap.put("CONTAINSPAGERANGE", ".*\\d(-|^|\")\\d.*");
    // tempRegexMap.put("CONTAINSAMPHERSAND", ".*&.*");
    // tempRegexMap.put("CONTAINSQUOTE", ".*[„“””‘’\"'].*");
    // tempRegexMap.put("COLON", ".*:.*");
    // tempRegexMap.put("SLASH", ".*/.*");
    // tempRegexMap.put("BRACES", ".*\\(.*\\).*");
    // tempRegexMap.put("ENDSPERIOD", ".*\\.");
    // tempRegexMap.put("ENDSCOMMA", ".*,");
    // tempRegexMap.put("ISNUMBER", "/d+");
    private Map<String, String> regexMap = FeaturePipeProvider.getRegexMap();
    private Map<String, String> countRegexMap = FeaturePipeProvider.getCountRegexMap();

    // tempCountRegexMap.put("PERIODS", ".*\\..*");
    // tempCountRegexMap.put("COMMAS", ".*,.*");
    // tempCountRegexMap.put("CAPITALIZEDS", "\\p{Lu}.*");
    // tempCountRegexMap.put("LOWERCASEDS", "\\p{javaLowerCase}.*");
    // tempCountRegexMap.put("WORDS", ".+");

    @Test
    public void containsAmphersandTest() {
        String regex = this.regexMap.get("AMPHERSAND");

        Assert.assertTrue(Pattern.matches(regex, "a&b"));
        Assert.assertTrue(Pattern.matches(regex, "&"));
        Assert.assertTrue(Pattern.matches(regex, " & "));
        Assert.assertTrue(Pattern.matches(regex, "&&"));
        Assert.assertTrue(Pattern.matches(regex, "& test &"));

        Assert.assertFalse(Pattern.matches(regex, "12"));
        Assert.assertFalse(Pattern.matches(regex, ""));
        Assert.assertFalse(Pattern.matches(regex, " "));
    }

    @Test
    public void containsPageRangeTest() {
        String regex = this.regexMap.get("PAGERANGE");

        Assert.assertTrue(Pattern.matches(regex, "1-2"));
        Assert.assertTrue(Pattern.matches(regex, "1000-2000"));
        Assert.assertTrue(Pattern.matches(regex, "1^2"));
        Assert.assertTrue(Pattern.matches(regex, "1\"2"));

        // TODO possibly check if first number is smaller than the second
        Assert.assertTrue(Pattern.matches(regex, "2^1"));

        Assert.assertFalse(Pattern.matches(regex, "12"));
        Assert.assertFalse(Pattern.matches(regex, "1-b2"));
        Assert.assertFalse(Pattern.matches(regex, "1^b2"));
        Assert.assertFalse(Pattern.matches(regex, "10 83"));
    }

    @Test
    public void containsQuoteTest() {
        String regex = this.regexMap.get("QUOTE");

        Assert.assertTrue(Pattern.matches(regex, "\""));
        Assert.assertTrue(Pattern.matches(regex, "\"\""));
        Assert.assertTrue(Pattern.matches(regex, "ab„cd"));
        Assert.assertTrue(Pattern.matches(regex, "ab„"));
        Assert.assertTrue(Pattern.matches(regex, "„cd"));
        Assert.assertTrue(Pattern.matches(regex, "“"));
        Assert.assertTrue(Pattern.matches(regex, "„“””‘’\"'"));
        Assert.assertTrue(Pattern.matches(regex, "„“””‘’\"'"));
        Assert.assertTrue(Pattern.matches(regex, "„“””‘’\"'"));

        Assert.assertFalse(Pattern.matches(regex, "abc"));
        Assert.assertFalse(Pattern.matches(regex, "12"));
        Assert.assertFalse(Pattern.matches(regex, ""));
        Assert.assertFalse(Pattern.matches(regex, " "));
    }

    @Test
    public void containsYearInBracesTest() {
        String regex = this.regexMap.get("YEARINBRACES");

        Assert.assertTrue(Pattern.matches(regex, "(1982)"));
        Assert.assertTrue(Pattern.matches(regex, "test (1982) test"));
        Assert.assertTrue(Pattern.matches(regex, "test (1982): test"));

        Assert.assertFalse(Pattern.matches(regex, "1599"));
        Assert.assertFalse(Pattern.matches(regex, "1999"));
        Assert.assertFalse(Pattern.matches(regex, "3000"));
        Assert.assertFalse(Pattern.matches(regex, "19 83"));
    }

    @Test
    public void containsYearTest() {
        String regex = this.regexMap.get("YEAR");

        Assert.assertTrue(Pattern.matches(regex, "1982"));
        Assert.assertTrue(Pattern.matches(regex, "test 1982 test"));
        Assert.assertTrue(Pattern.matches(regex, "test (1982) test"));

        Assert.assertFalse(Pattern.matches(regex, "1599"));
        Assert.assertFalse(Pattern.matches(regex, "3000"));
        Assert.assertFalse(Pattern.matches(regex, "19 83"));
    }

    @Test
    public void countNumbersTest() {
        String regex = this.countRegexMap.get("NUMBERS");

        Assert.assertEquals(1, this.count(regex, "123"));
        Assert.assertEquals(2, this.count(regex, "123 456"));
        Assert.assertEquals(2, this.count(regex, "1-2"));
        Assert.assertEquals(2, this.count(regex, "123-456"));
        Assert.assertEquals(2, this.count(regex, "123asb456"));

        Assert.assertEquals(0, this.count(regex, "abc"));
        Assert.assertEquals(0, this.count(regex, "abc def "));
        Assert.assertEquals(0, this.count(regex, ""));
        Assert.assertEquals(0, this.count(regex, "    "));
    }

    @Test
    public void countOneUppercaseds() {
        String regex = this.countRegexMap.get("ONEUPPERCASEDS");

        Assert.assertEquals(1, this.count(regex, "T"));
        Assert.assertEquals(1, this.count(regex, "test T"));
        Assert.assertEquals(1, this.count(regex, "Test T test"));
        Assert.assertEquals(2, this.count(regex, "T Test T"));
        Assert.assertEquals(4, this.count(regex, "T. T; T, T"));

        Assert.assertEquals(0, this.count(regex, "Test"));
        Assert.assertEquals(0, this.count(regex, "test"));
        Assert.assertEquals(0, this.count(regex, "teSt"));
    }

    @Test
    public void countPeriodsTest() {
        String regex = this.countRegexMap.get("PERIODS");

        Assert.assertEquals(1, this.count(regex, "."));
        Assert.assertEquals(3, this.count(regex, "..."));
        Assert.assertEquals(3, this.count(regex, "a.b.c.d"));
        Assert.assertEquals(3, this.count(regex, " ... "));

        Assert.assertEquals(0, this.count(regex, ""));
        Assert.assertEquals(0, this.count(regex, "123"));
        Assert.assertEquals(0, this.count(regex, ","));
        Assert.assertEquals(0, this.count(regex, "asb"));
    }

    @Test
    public void countWordsTest() {
        String regex = this.countRegexMap.get("WORDS");

        Assert.assertEquals(1, this.count(regex, "test"));
        Assert.assertEquals(1, this.count(regex, "123"));
        Assert.assertEquals(1, this.count(regex, "."));
        Assert.assertEquals(4, this.count(regex, "this is a test"));
        Assert.assertEquals(4, this.count(regex, "this is a test."));
        Assert.assertEquals(4, this.count(regex, " this is a test. "));

        Assert.assertEquals(0, this.count(regex, ""));
        Assert.assertEquals(0, this.count(regex, " "));
        Assert.assertEquals(0, this.count(regex, "    "));
    }

    @Test
    public void endsPeriod() {
        String regex = this.regexMap.get("AMPHERSAND");

        Assert.assertTrue(Pattern.matches(regex, "a&b"));
        Assert.assertTrue(Pattern.matches(regex, "&"));
        Assert.assertTrue(Pattern.matches(regex, " & "));
        Assert.assertTrue(Pattern.matches(regex, "&&"));
        Assert.assertTrue(Pattern.matches(regex, "& test &"));

        Assert.assertFalse(Pattern.matches(regex, "12"));
        Assert.assertFalse(Pattern.matches(regex, ""));
        Assert.assertFalse(Pattern.matches(regex, " "));
    }

    @Test
    public void endsPeriodTest() {
        String regex = this.regexMap.get("ENDSPERIOD");

        Assert.assertTrue(Pattern.matches(regex, "abc."));
        Assert.assertTrue(Pattern.matches(regex, " ."));
        Assert.assertTrue(Pattern.matches(regex, "."));
        Assert.assertTrue(Pattern.matches(regex, "..."));

        Assert.assertFalse(Pattern.matches(regex, "1.2"));
        Assert.assertFalse(Pattern.matches(regex, ". "));
    }

    @Test
    public void inTest() {
        String regex = this.regexMap.get("IN");

        Assert.assertTrue(Pattern.matches(regex, "in"));
        Assert.assertTrue(Pattern.matches(regex, " in. "));
        Assert.assertTrue(Pattern.matches(regex, "In"));
        Assert.assertTrue(Pattern.matches(regex, " in "));
        Assert.assertTrue(Pattern.matches(regex, " in: "));

        Assert.assertFalse(Pattern.matches(regex, "abc"));
        Assert.assertFalse(Pattern.matches(regex, "Bingo"));
        Assert.assertFalse(Pattern.matches(regex, "1in2"));
        Assert.assertFalse(Pattern.matches(regex, ""));
    }

    @Test
    public void isNumberTest() {
        String regex = this.regexMap.get("ISNUMBER");

        Assert.assertTrue(Pattern.matches(regex, "1"));
        Assert.assertTrue(Pattern.matches(regex, "123"));

        Assert.assertFalse(Pattern.matches(regex, " 1"));
        Assert.assertFalse(Pattern.matches(regex, "1-23"));
        Assert.assertFalse(Pattern.matches(regex, "abc"));
    }

    @Test
    public void startsCapitalizedTest() {
        String regex = this.regexMap.get("STARTSCAPITALIZED");

        Assert.assertTrue(Pattern.matches(regex, "Test"));
        Assert.assertTrue(Pattern.matches(regex, "T"));
        Assert.assertTrue(Pattern.matches(regex, "Ü"));

        Assert.assertFalse(Pattern.matches(regex, "test"));
        Assert.assertFalse(Pattern.matches(regex, ""));
        Assert.assertFalse(Pattern.matches(regex, " Test"));
    }

    @Test
    public void startsNumberTest() {
        String regex = this.regexMap.get("STARTSNUMBER");

        Assert.assertTrue(Pattern.matches(regex, "1982"));
        Assert.assertTrue(Pattern.matches(regex, "1"));

        Assert.assertFalse(Pattern.matches(regex, " 1"));
        Assert.assertFalse(Pattern.matches(regex, "Test"));
        Assert.assertFalse(Pattern.matches(regex, ""));
    }

    private int count(String regex, String inputString) {
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(inputString);
        int count = 0;
        while (matcher.find()) {
            count++;
        }
        return count;
    }
}

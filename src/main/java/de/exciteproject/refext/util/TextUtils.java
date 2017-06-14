package de.exciteproject.refext.util;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringEscapeUtils;

public class TextUtils {
    private static final Map<Character, String> ACCENT_MAP;
    private static final Pattern ACCENT_PATTERN;
    static {
        Map<Character, String> map = new HashMap<Character, String>();
        map.put('¨', "uml");
        map.put('´', "acute");
        map.put('˚', "ring");

        ACCENT_MAP = Collections.unmodifiableMap(map);

        String accentPatternString = "\\w(";
        for (Entry<Character, String> accentMapEntry : ACCENT_MAP.entrySet()) {
            accentPatternString += accentMapEntry.getKey() + "|";
        }
        accentPatternString = accentPatternString.replaceFirst("\\|$", "");
        accentPatternString += ")(\\s)?";
        ACCENT_PATTERN = Pattern.compile(accentPatternString);
    }

    public static String fixAccents(String input) {
        String output = input;
        Matcher matcher = ACCENT_PATTERN.matcher(output);

        int startIndex = 0;
        while (matcher.find(startIndex)) {
            // can't set startIndex to matcher.end() since the output length
            // changes after a replacement
            startIndex = matcher.start() + 1;

            String sequenceToReplace = matcher.group();

            String htmlChar = "&" + sequenceToReplace.charAt(0);
            if (TextUtils.ACCENT_MAP.containsKey(sequenceToReplace.charAt(1))) {
                htmlChar += TextUtils.ACCENT_MAP.get(sequenceToReplace.charAt(1));
            } else {
                continue;
            }
            htmlChar += ";";
            

            String unescapedChar = StringEscapeUtils.unescapeHtml4(htmlChar);

            // if HTML string was escaped (is valid HTML)
            if (!htmlChar.equals(unescapedChar)) {
                output = output.replaceAll(sequenceToReplace, unescapedChar);
                matcher.reset(output);
            }
        }
        return output;
    }

    // TODO write test cases
    public static void main(String args[]) {
        String input = "A´ cs, Bo¨ hm,  Ra¨tsch, Bh¨ hm BA¨u¨ hm, Bo¨ hm Bo¨ hm ";
        System.out.println(TextUtils.fixAccents(input));
        // System.out.println("test");
        // output: Ács, Böhm, Rätsch, Bh¨ hm BÄühm, Böhm Böhm
    }
}

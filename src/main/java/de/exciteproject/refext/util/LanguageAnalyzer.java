package de.exciteproject.refext.util;

import java.io.IOException;
import java.io.InputStream;
import java.net.JarURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.apache.commons.io.IOUtils;

import com.cybozu.labs.langdetect.Detector;
import com.cybozu.labs.langdetect.DetectorFactory;
import com.cybozu.labs.langdetect.LangDetectException;

/**
 * Class for generating language statistics for a directory containing text
 * files of different languages.
 */
public class LanguageAnalyzer {

    public LanguageAnalyzer() throws LangDetectException, IOException {
        // solution for loading detector profiles from jar taken from:
        // http://stackoverflow.com/a/15332031

        String dirname = "profiles/";
        Enumeration<URL> en = Detector.class.getClassLoader().getResources(dirname);
        List<String> profiles = new ArrayList<>();
        if (en.hasMoreElements()) {
            URL url = en.nextElement();
            JarURLConnection urlcon = (JarURLConnection) url.openConnection();
            try (JarFile jar = urlcon.getJarFile();) {
                Enumeration<JarEntry> entries = jar.entries();
                while (entries.hasMoreElements()) {
                    String entry = entries.nextElement().getName();
                    if (entry.startsWith(dirname)) {
                        try (InputStream in = Detector.class.getClassLoader().getResourceAsStream(entry);) {
                            profiles.add(IOUtils.toString(in, Charset.defaultCharset()));
                        }
                    }
                }
            }
        }
        DetectorFactory.loadProfile(profiles);
    }

    /**
     *
     * @param inputDirectory:
     *            directory containing text files to be analyzed
     * @param outputFile:
     *            file in which the analysis results are written
     * @throws IOException
     * @throws LangDetectException
     */
    public String analyze(String string) {
        // List<File> textFiles =
        // FileUtils.listFilesRecursively(inputDirectory);

        // Map<String, Integer> languageMap = new HashMap<String, Integer>();

        try {
            // MapUtils.addCount(languageMap, detector.detect());
            // System.out.println(inputFile);
            Detector detector = DetectorFactory.create();
            detector.append(string);
            return detector.detect();
        } catch (LangDetectException e) {
            // MapUtils.addCount(languageMap, "unknown");
            return "unknown";
        }

        // BufferedWriter bufferedWriter = new BufferedWriter(new
        // FileWriter(outputFile));
        // bufferedWriter.write("number of files files: " + textFiles.size());
        // bufferedWriter.newLine();
        // bufferedWriter.newLine();
        //
        // for (Entry<String, Integer> languageMapEntry :
        // MapUtils.entriesReverselySortedByValues(languageMap)) {
        // bufferedWriter.write(languageMapEntry.getKey() + ": " +
        // languageMapEntry.getValue());
        // bufferedWriter.newLine();
        // }
        // bufferedWriter.close();

    }
}

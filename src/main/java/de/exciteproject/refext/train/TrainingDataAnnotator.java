package de.exciteproject.refext.train;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FilenameUtils;

import de.exciteproject.refext.ReferenceExtractor;
import de.exciteproject.refext.extract.ReferenceLineAnnotation;
import de.exciteproject.refext.util.FileUtils;
import pl.edu.icm.cermine.exception.AnalysisException;

/**
 * Simplified annotator for generating training data. Does not handle
 * footers/headers that appear inside a reference string.
 */
public class TrainingDataAnnotator {

    public static void main(String[] args) throws AnalysisException, IOException {
        File crfModelFile = new File(args[0]);
        File inputDir = new File(args[1]);
        File outputDir = new File(args[2]);
        if (!outputDir.exists()) {
            outputDir.mkdirs();
        }
        TrainingDataAnnotator trainingDataAnnotator = new TrainingDataAnnotator(crfModelFile);
        List<File> inputFiles = FileUtils.asList(inputDir);

        for (File inputFile : inputFiles) {
            String subDirectories = inputFile.getParentFile().getAbsolutePath().replace("\\", "/")
                    .replaceFirst(inputDir.getAbsolutePath().replace("\\", "/"), "");
            //File currentOutputDirectory = new File(outputDir.getAbsolutePath() + File.separator + subDirectories);
            File currentOutputDirectory = new File(outputDir.getAbsolutePath());

            String outputFileName = FilenameUtils.removeExtension(inputFile.getName()) + ".csv";
            File outputFile = new File(currentOutputDirectory + File.separator + outputFileName);

            // skip computation if outputFile already exists
            if (outputFile.exists()) {
                continue;
            }
            if (!currentOutputDirectory.exists()) {
                currentOutputDirectory.mkdirs();
            }

            List<String> annotatedText = trainingDataAnnotator.annotateText(inputFile);
            Files.write(Paths.get(outputFile.getAbsolutePath()), annotatedText, Charset.defaultCharset());
            //Files.write(Paths.get(outputFile.getAbsolutePath().replace("\\", "/")), annotatedText, Charset.defaultCharset());
        }
    }

    private ReferenceExtractor referenceExtractor;

    public TrainingDataAnnotator(File crfModelFile) throws AnalysisException {
        this.referenceExtractor = new ReferenceExtractor(crfModelFile);
    }
    

    public List<String> annotateText(File layoutFile) throws IOException, AnalysisException {
        List<ReferenceLineAnnotation> annotatedLines = this.referenceExtractor.annotateLinesFromLayoutFile(layoutFile,
                Charset.defaultCharset());

        List<String> xmlAnnotatedLines = new ArrayList<String>();
        for (int i = 0; i < annotatedLines.size(); i++) {
            boolean addStartTag = false;
            boolean addEndTag = false;
            // System.out.println(annotatedLines.get(i));
            String thisAnnotation = this.getAnnotation(annotatedLines, i);
            String nextAnnotation = this.getAnnotation(annotatedLines, i + 1);
            if (thisAnnotation.equals("B-REF")) {
                addStartTag = true;
                if (!nextAnnotation.equals("I-REF")) {
                    addEndTag = true;
                }
            } else {
                if (thisAnnotation.equals("I-REF")) {
                    if (!nextAnnotation.equals("I-REF")) {
                        addEndTag = true;
                    }
                }
            }

            String thisLine = this.getLine(annotatedLines, i);
            if (addStartTag) {
                thisLine = "<ref>" + thisLine;
            }
            if (addEndTag) {
                thisLine = thisLine + "</ref>";
            }
            xmlAnnotatedLines.add(thisLine);
        }
        return xmlAnnotatedLines;
    }

    private String getAnnotation(List<ReferenceLineAnnotation> annotatedLines, int index) {
        if (annotatedLines.size() <= index) {
            return "";
        } else {
            return annotatedLines.get(index).getBestAnnotation();
        }
    }

    private String getLine(List<ReferenceLineAnnotation> annotatedLines, int index) {
        if (annotatedLines.size() <= index) {
            return "";
        } else {
            return annotatedLines.get(index).getLine();
        }
    }
}

package de.exciteproject.refext.train;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import de.exciteproject.refext.extract.CermineLineLayoutExtractor;
import de.exciteproject.refext.extract.CrfReferenceLineAnnotator;
import de.exciteproject.refext.util.FileUtils;
import pl.edu.icm.cermine.ComponentConfiguration;
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
        String inputDirectoryPath = FileUtils.getDirctory(inputDir).getAbsolutePath();

        for (File inputFile : inputFiles) {
            String inputFileSubPath = inputFile.getAbsolutePath().replaceAll(inputDirectoryPath, "");

            inputFileSubPath = inputFileSubPath.replaceAll(".pdf$", ".xml");
            File outputFile = new File(outputDir + inputFileSubPath);
            if (outputFile.exists()) {
                continue;
            }

            List<String> annotatedText = trainingDataAnnotator.annotateText(inputFile);
            Files.write(Paths.get(outputFile.getAbsolutePath()), annotatedText, Charset.defaultCharset());
        }
    }

    private CrfReferenceLineAnnotator crfReferenceLineAnnotator;
    private CermineLineLayoutExtractor cermineLineLayoutExtractor;

    public TrainingDataAnnotator(File crfModelFile) throws AnalysisException {
        this.crfReferenceLineAnnotator = new CrfReferenceLineAnnotator(crfModelFile);
        ComponentConfiguration componentConfiguration = new ComponentConfiguration();

        this.cermineLineLayoutExtractor = new CermineLineLayoutExtractor(componentConfiguration);
    }

    public List<String> annotateText(File pdfFile) throws IOException, AnalysisException {
        List<String> layoutLines = this.cermineLineLayoutExtractor.extract(pdfFile);
        List<String> annotatedLines = this.crfReferenceLineAnnotator.annotate(layoutLines);

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

    private String getAnnotation(List<String> annotatedLines, int index) {
        if (annotatedLines.size() <= index) {
            return "";
        } else {
            String[] lineSplit = annotatedLines.get(index).split("\\t");
            return lineSplit[0];
        }
    }

    private String getLine(List<String> annotatedLines, int index) {
        if (annotatedLines.size() <= index) {
            return "";
        } else {
            String[] lineSplit = annotatedLines.get(index).split("\\t");
            return lineSplit[1];
        }
    }
}

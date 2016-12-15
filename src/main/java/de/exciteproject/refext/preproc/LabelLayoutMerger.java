package de.exciteproject.refext.preproc;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.List;

import de.exciteproject.refext.util.FileUtils;

/**
 * Class for merging labeled input files and files containing layout
 * information.
 */
public class LabelLayoutMerger {

    public static void main(String[] args) throws IOException {
        LabelLayoutMerger labelLayoutMerger = new LabelLayoutMerger();

        File labeledInputDirectory = new File(args[0]);
        File layoutInputDirectory = new File(args[1]);
        File outputDirectory = new File(args[2]);

        List<File> labeledInputFiles = FileUtils.listFilesRecursively(labeledInputDirectory);
        for (File labeledInputFile : labeledInputFiles) {

            String labelInputFileSubPath = labeledInputFile.getAbsolutePath()
                    .replaceFirst(labeledInputDirectory.getAbsolutePath(), "");
            File layoutInputFile = new File(layoutInputDirectory + labelInputFileSubPath);
            layoutInputFile.getParentFile().mkdirs();

            File outputFile = new File(outputDirectory + labelInputFileSubPath);
            outputFile.getParentFile().mkdirs();
            System.out.println("processing: " + labeledInputFile);
            try {
                labelLayoutMerger.merge(labeledInputFile, layoutInputFile, outputFile);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

        }

    }

    public void merge(File labeledInputFile, File layoutInputFile, File outputFile) throws IOException {

        if (this.countLines(labeledInputFile) != this.countLines(layoutInputFile)) {
            System.out.println("\tFAIL: skipping due to different file lengths");
            return;
        }
        BufferedReader labeledInputReader = new BufferedReader(new FileReader(labeledInputFile));
        BufferedReader layoutInputReader = new BufferedReader(new FileReader(layoutInputFile));
        BufferedWriter outputWriter = new BufferedWriter(new FileWriter(outputFile));

        String labeledLine;
        String layoutLine;
        while ((labeledLine = labeledInputReader.readLine()) != null) {
            layoutLine = layoutInputReader.readLine();
            String outputLine = labeledLine;
            String[] layoutLineSplit = layoutLine.split("\\t");
            for (int i = 1; i < layoutLineSplit.length; i++) {
                outputLine += "\t" + layoutLineSplit[i];
            }
            outputWriter.write(outputLine);
            outputWriter.newLine();

        }

        outputWriter.close();
        labeledInputReader.close();
        layoutInputReader.close();
    }

    private int countLines(File file) throws IOException {
        LineNumberReader lineNumberReader = new LineNumberReader(new FileReader(file));
        lineNumberReader.skip(Long.MAX_VALUE);
        int lineNumber = lineNumberReader.getLineNumber() + 1;

        lineNumberReader.close();
        return lineNumber;
    }
}

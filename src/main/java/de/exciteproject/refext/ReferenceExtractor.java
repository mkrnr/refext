package de.exciteproject.refext;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.io.FilenameUtils;

import cc.mallet.fst.CRF;
import cc.mallet.fst.CRFTrainerByLabelLikelihood;
import cc.mallet.pipe.Pipe;
import cc.mallet.pipe.iterator.LineGroupIterator;
import cc.mallet.types.Instance;
import cc.mallet.types.InstanceList;
import cc.mallet.types.Sequence;
import cc.mallet.util.FileUtils;
import de.exciteproject.refext.extract.CermineLineLayoutExtractor;
import de.exciteproject.refext.extract.ReferenceFromAnnotatedLinesExtractor;
import pl.edu.icm.cermine.bibref.BibReferenceParser;
import pl.edu.icm.cermine.bibref.CRFBibReferenceParser;
import pl.edu.icm.cermine.bibref.model.BibEntry;
import pl.edu.icm.cermine.exception.AnalysisException;

/**
 * Class for applying reference extraction on text files that include layout
 * information (see {@link CermineLineLayoutExtractor}).
 *
 */
public class ReferenceExtractor {

    public static void main(String[] args) throws IOException, AnalysisException, ClassNotFoundException {
        File inputDir = new File(args[0]);
        File outputDir = new File(args[1]);
        File crfModelFile = new File(args[2]);

        if (!outputDir.exists()) {
            outputDir.mkdirs();
        }

        ReferenceExtractor extractionRunner = new ReferenceExtractor(crfModelFile);
        for (File inputFile : de.exciteproject.refext.util.FileUtils.listFilesRecursively(inputDir)) {
            System.out.println(inputFile);
            File currentOutputDirectory;

            String subDirectories = inputFile.getParentFile().getAbsolutePath().replaceFirst(inputDir.getAbsolutePath(),
                    "");
            currentOutputDirectory = new File(outputDir.getAbsolutePath() + File.separator + subDirectories);

            String outputFileName = FilenameUtils.removeExtension(inputFile.getName()) + ".txt";
            File outputFile = new File(currentOutputDirectory.getAbsolutePath() + File.separator + outputFileName);

            // skip files that are larger than 10MB
            if (inputFile.length() > 10000000) {
                continue;
            }
            // skip if outputFile already exists
            if (outputFile.exists()) {
                continue;
            }
            List<String> annotatedLines = extractionRunner.extractAnnotatedLines(inputFile);
            List<String> referenceStrings = ReferenceFromAnnotatedLinesExtractor.extract(annotatedLines);
            BibReferenceParser<BibEntry> bibReferenceParser = CRFBibReferenceParser.getInstance();

            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(outputFile));
            for (String referenceString : referenceStrings) {
                BibEntry bibEntry = bibReferenceParser.parseBibReference(referenceString);
                bufferedWriter.write(bibEntry.toBibTeX());
                bufferedWriter.newLine();
            }
            bufferedWriter.close();
        }
    }

    private CRF crf;
    private InstanceList inputInstances;
    private Pipe pipe;
    private CermineLineLayoutExtractor cermineLineLayoutExtractor;

    public ReferenceExtractor(File crfModelFile) throws IOException, AnalysisException {
        this.crf = (CRF) FileUtils.readObject(crfModelFile);
        this.pipe = this.crf.getInputPipe();
        this.cermineLineLayoutExtractor = new CermineLineLayoutExtractor();
    }

    public List<String> extractAnnotatedLines(File pdfFile) throws IOException, AnalysisException {

        List<String> linesWithLayout = this.cermineLineLayoutExtractor.extract(pdfFile);

        StringBuilder lineStringBuilder = new StringBuilder();
        for (String line : linesWithLayout) {
            lineStringBuilder.append(line).append("\n");
        }
        BufferedReader lineReader = new BufferedReader(new StringReader(lineStringBuilder.toString()));

        this.inputInstances = new InstanceList(this.pipe);
        this.inputInstances.addThruPipe(new LineGroupIterator(lineReader, Pattern.compile("^\\s*$"), true));
        lineReader.close();

        CRFTrainerByLabelLikelihood trainer = new CRFTrainerByLabelLikelihood(this.crf);
        trainer.setUseSparseWeights(false);

        List<String> annotatedLines = new ArrayList<String>();

        for (Instance instance : this.inputInstances) {
            @SuppressWarnings("unchecked")
            Sequence<String> output = this.crf.transduce((Sequence<String>) instance.getData());
            for (int i = 0; i < output.size(); i++) {

                annotatedLines.add(output.get(i).toString() + "\t" + linesWithLayout.get(i).split("\t")[0]);
            }
        }
        return annotatedLines;
    }

    public List<String> extractReferences(File pdfFile) throws IOException, AnalysisException {
        List<String> annotatedLines = this.extractAnnotatedLines(pdfFile);
        List<String> referenceStrings = ReferenceFromAnnotatedLinesExtractor.extract(annotatedLines);
        return referenceStrings;
    }

}

package de.exciteproject.refext.extract;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import cc.mallet.fst.CRF;
import cc.mallet.pipe.Pipe;
import cc.mallet.types.Instance;
import cc.mallet.types.InstanceList;
import cc.mallet.types.Sequence;
import cc.mallet.util.FileUtils;
import de.exciteproject.refext.train.LineGroupIterator;
import de.exciteproject.refext.train.ReferenceExtractorTrainer;
import pl.edu.icm.cermine.exception.AnalysisException;

/**
 * Class for annotating given lines with layout (see
 * {@link CermineLineLayoutExtractor} for format) using a trained CRF with
 * {@link ReferenceExtractorTrainer} in a BIO format.
 */
public class CrfReferenceLineAnnotator {

    private CRF crf;
    private Pipe pipe;

    public CrfReferenceLineAnnotator(File crfModelFile) throws AnalysisException {
        this.crf = (CRF) FileUtils.readObject(crfModelFile);
        this.pipe = this.crf.getInputPipe();
    }

    public List<String> annotate(List<String> linesWithLayout) throws IOException, AnalysisException {

        StringBuilder lineStringBuilder = new StringBuilder();
        for (String line : linesWithLayout) {
            lineStringBuilder.append(line).append(System.lineSeparator());
        }
        BufferedReader lineReader = new BufferedReader(new StringReader(lineStringBuilder.toString()));

        InstanceList inputInstances = new InstanceList(this.pipe);
        inputInstances.addThruPipe(new LineGroupIterator(lineReader, Pattern.compile("^\\s*$"), true));
        lineReader.close();

        // CRFTrainerByLabelLikelihood trainer = new
        // CRFTrainerByLabelLikelihood(this.crf);
        // trainer.setUseSparseWeights(false);

        List<String> annotatedLines = new ArrayList<String>();

        for (Instance instance : inputInstances) {
            @SuppressWarnings("unchecked")
            Sequence<String> output = this.crf.transduce((Sequence<String>) instance.getData());
            for (int i = 0; i < output.size(); i++) {
                // annotatedLines.add(output.get(i).toString() + "\t" +
                // linesWithLayout.get(i).split("\t")[0]);
                String[] layoutLineSplit = linesWithLayout.get(i).split("\t");
                if (layoutLineSplit.length > 0) {
                    annotatedLines.add(output.get(i).toString() + "\t" + layoutLineSplit[0]);
                }
            }
        }
        return annotatedLines;
    }

}

package de.exciteproject.refext.extract;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import cc.mallet.fst.CRF;
import cc.mallet.fst.SumLatticeDefault;
import cc.mallet.fst.Transducer.State;
import cc.mallet.pipe.Pipe;
import cc.mallet.types.Alphabet;
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

    public List<ReferenceLineAnnotation> annotate(List<String> linesWithLayout) throws IOException, AnalysisException {

        StringBuilder lineStringBuilder = new StringBuilder();
        for (String line : linesWithLayout) {
            lineStringBuilder.append(line).append(System.lineSeparator());
        }
        BufferedReader lineReader = new BufferedReader(new StringReader(lineStringBuilder.toString()));

        InstanceList inputInstances = new InstanceList(this.pipe);
        inputInstances.addThruPipe(new LineGroupIterator(lineReader, Pattern.compile("^\\s*$"), true));
        lineReader.close();

        List<ReferenceLineAnnotation> referenceLineAnnotations = new ArrayList<ReferenceLineAnnotation>();

        for (Instance instance : inputInstances) {
            @SuppressWarnings("unchecked")
            Sequence<String> inputSequence = (Sequence<String>) instance.getData();
            SumLatticeDefault latticeDefault = new SumLatticeDefault(this.crf, inputSequence);
            Alphabet outputAlphabet = this.crf.getOutputAlphabet();
            if (linesWithLayout.size() != inputSequence.size()) {
                throw new IllegalStateException("linesWithLayout.size()!=inputSequence.size()");
            }
            for (int i = 0; i < inputSequence.size(); i++) {
                ReferenceLineAnnotation referenceLineAnnotation = new ReferenceLineAnnotation(
                        linesWithLayout.get(i).split("\\t")[0]);
                for (int j = 1; j <= outputAlphabet.size(); j++) {
                    State state = this.crf.getState(j);
                    referenceLineAnnotation.addAnnotation(state.getName(),
                            latticeDefault.getGammaProbability(i + 1, state));
                }
                referenceLineAnnotations.add(referenceLineAnnotation);
            }
        }
        return referenceLineAnnotations;
    }

}

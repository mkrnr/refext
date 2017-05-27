package de.exciteproject.refext.train;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.lang3.ArrayUtils;

import com.cybozu.labs.langdetect.LangDetectException;

import cc.mallet.fst.CRF;
import cc.mallet.fst.CRFTrainerByL1LabelLikelihood;
import cc.mallet.fst.CRFTrainerByLabelLikelihood;
import cc.mallet.fst.PerClassAccuracyEvaluator;
import cc.mallet.fst.TokenAccuracyEvaluator;
import cc.mallet.fst.Transducer;
import cc.mallet.fst.TransducerTrainer;
import cc.mallet.pipe.LineGroupString2TokenSequence;
import cc.mallet.pipe.Pipe;
import cc.mallet.pipe.SerialPipes;
import cc.mallet.pipe.Target2LabelSequence;
import cc.mallet.pipe.TokenSequence2FeatureVectorSequence;
import cc.mallet.pipe.iterator.LineGroupIterator;
import cc.mallet.pipe.tsf.OffsetConjunctions;
import cc.mallet.pipe.tsf.TokenTextCharPrefix;
import cc.mallet.pipe.tsf.TokenTextCharSuffix;
import cc.mallet.types.InstanceList;
import de.exciteproject.refext.train.pipe.AddTargetToLinePipe;
import de.exciteproject.refext.train.pipe.FeaturePipeProvider;
import de.exciteproject.refext.train.pipe.LineToTargetTextPipe;
import de.exciteproject.refext.train.pipe.TargetReplacementPipe;

/**
 * Class for training a supervised CRF for extracting reference strings from a
 * text by considering layout information as well as content information.
 */
public class ReferenceExtractorTrainer {

    private TransducerTrainer transducerTrainer;

    private CRF crf;

    private SerialPipes serialPipes;

    public ReferenceExtractorTrainer(List<String> featureNames, List<String> replacements)
            throws LangDetectException, IOException {
        this.serialPipes = this.buildSerialPipes(featureNames, replacements);

        this.crf = new CRF(this.serialPipes, null);

    }

    /**
     *
     * @param n:
     *            positive integer. Creates all possible orders from 0 until and
     *            including n
     */
    public void addOrderNStates(int n, InstanceList trainingInstances) {

        Pattern forbiddenPat = Pattern.compile("\\s");
        Pattern allowedPat = Pattern.compile(".*");
        List<Integer> orders = new ArrayList<Integer>();
        for (int i = 0; i <= n; i++) {
            orders.add(i);
        }
        int[] ordersArray = null;
        if (orders.size() > 0) {
            ordersArray = ArrayUtils.toPrimitive(orders.toArray(new Integer[orders.size()]));
        }
        String startName = this.crf.addOrderNStates(trainingInstances, ordersArray, null, "O", forbiddenPat, allowedPat,
                true);
        for (int i = 0; i < this.crf.numStates(); i++) {
            this.crf.getState(i).setInitialWeight(Transducer.IMPOSSIBLE_WEIGHT);
        }
        this.crf.getState(startName).setInitialWeight(0.0);
        this.crf.setWeightsDimensionDensely();
    }

    public void addStartState() {
        this.crf.addStartState();
    }

    public void addStatesForLabelsConnectedAsIn(InstanceList trainingInstances) {
        this.crf.addStatesForLabelsConnectedAsIn(trainingInstances);
    }

    public void addStatesForThreeQuarterLabelsConnectedAsIn(InstanceList trainingInstances) {
        this.crf.addStatesForThreeQuarterLabelsConnectedAsIn(trainingInstances);
    }

    public InstanceList buildInstanceList(File inputFile) throws FileNotFoundException {
        InstanceList instanceList;
        instanceList = new InstanceList(this.serialPipes);

        instanceList.addThruPipe(
                new LineGroupIterator(new BufferedReader(new InputStreamReader(new FileInputStream(inputFile))),
                        Pattern.compile("^\\s*$"), true));

        return instanceList;
    }

    public InstanceList buildInstanceListFromDir(File inputDirectory) throws FileNotFoundException {
        InstanceList instanceList;
        instanceList = new InstanceList(this.serialPipes);

        for (File inputFile : inputDirectory.listFiles()) {
            instanceList.addThruPipe(
                    new LineGroupIterator(new BufferedReader(new InputStreamReader(new FileInputStream(inputFile))),
                            Pattern.compile("^\\s*$"), true));
        }

        return instanceList;
    }

    public void setCRFTrainerByL1LabelLikelihood(double l1Weight) {
        CRFTrainerByL1LabelLikelihood crfTrainerByL1LabelLikelihood = new CRFTrainerByL1LabelLikelihood(this.crf,
                l1Weight);
        this.transducerTrainer = crfTrainerByL1LabelLikelihood;
    }

    public void setCRFTrainerByLabelLikelihood(double gaussianPriorVariance) {
        CRFTrainerByLabelLikelihood crfTrainerByLabelLikelihood = new CRFTrainerByLabelLikelihood(this.crf);
        crfTrainerByLabelLikelihood.setGaussianPriorVariance(gaussianPriorVariance);
        this.transducerTrainer = crfTrainerByLabelLikelihood;
    }

    public CRF train(InstanceList trainingInstances, InstanceList testingInstances)
            throws FileNotFoundException, IOException {

        if (this.transducerTrainer == null) {
            throw new IllegalStateException("crfTrainer needs to be set via one of the available methods");
        }
        // trainer.addEvaluator(new PerClassAccuracyEvaluator(trainingInstances,
        // "training"));
        this.transducerTrainer.addEvaluator(new PerClassAccuracyEvaluator(testingInstances, "testing"));
        this.transducerTrainer.addEvaluator(new TokenAccuracyEvaluator(testingInstances, "testing"));
        // TODO remove
        // this.transducerTrainer
        // .addEvaluator(new FixedViterbiWriter(new
        // File("/home/mkoerner/viterbi.txt"), testingInstances, "test"));

        this.transducerTrainer.train(trainingInstances);
        return this.crf;
    }

    // TODO Add configurations (optional with default value)
    private SerialPipes buildSerialPipes(List<String> featureNames, List<String> replacements)
            throws LangDetectException, IOException {
        ArrayList<Pipe> pipes = new ArrayList<Pipe>();
        pipes.add(new LineGroupString2TokenSequence());
        pipes.add(new AddTargetToLinePipe(6));
        pipes.add(new LineToTargetTextPipe());
        pipes.add(new TargetReplacementPipe(replacements));

        FeaturePipeProvider featurePipeProvider = new FeaturePipeProvider();
        for (String featureName : featureNames) {
            pipes.add(featurePipeProvider.getPipe(featureName));
        }
        // pipes.add(new NamePipe("FIRSTNAME", new File(args[3])));
        // pipes.add(new NamePipe("LASTNAME", new File(args[4])));
        // pipes.add(new TokenText());
        // pipes.add(new TokenTextCharSuffix("C1=", 1));
        // pipes.add(new TokenTextCharSuffix("C2=", 2));
        // pipes.add(new TokenTextCharSuffix("C3=", 3));

        pipes.add(new TokenTextCharSuffix("SUFFIX=", 1));
        pipes.add(new TokenTextCharPrefix("PREFIX=", 1));

        int[][] conjunctions = new int[4][];
        conjunctions[0] = new int[] { -2 };
        conjunctions[1] = new int[] { -1 };
        conjunctions[2] = new int[] { 1 };
        conjunctions[3] = new int[] { 2 };
        pipes.add(new OffsetConjunctions(conjunctions));

        pipes.add(new TokenSequence2FeatureVectorSequence(false, false));
        pipes.add(new Target2LabelSequence());

        // pipes.add(new PrintInputAndTarget());

        return new SerialPipes(pipes);

    }
}

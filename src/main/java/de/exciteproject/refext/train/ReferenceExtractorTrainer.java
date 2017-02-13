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

import cc.mallet.fst.CRF;
import cc.mallet.fst.CRFTrainerByLabelLikelihood;
import cc.mallet.fst.PerClassAccuracyEvaluator;
import cc.mallet.fst.TokenAccuracyEvaluator;
import cc.mallet.fst.Transducer;
import cc.mallet.pipe.LineGroupString2TokenSequence;
import cc.mallet.pipe.Pipe;
import cc.mallet.pipe.SerialPipes;
import cc.mallet.pipe.Target2LabelSequence;
import cc.mallet.pipe.TokenSequence2FeatureVectorSequence;
import cc.mallet.pipe.TokenSequenceMatchDataAndTarget;
import cc.mallet.pipe.iterator.LineGroupIterator;
import cc.mallet.types.InstanceList;
import de.exciteproject.refext.train.pipe.FeaturePipeProvider;
import de.exciteproject.refext.train.pipe.LayoutPipe;
import de.exciteproject.refext.train.pipe.XmlRefTagToTargetPipe;

/**
 * Class for training a supervised CRF for extracting reference strings from a
 * text by considering layout information as well as content information.
 */
public class ReferenceExtractorTrainer {

    private File trainingFile;
    private File testingFile;
    private File modelFile;
    private List<String> featureNames;
    private File firstNameFile;
    private File lastNameFile;

    public ReferenceExtractorTrainer(File trainingFile, File testingFile, File modelFile, List<String> featureNames,
            File firstNameFile, File lastNameFile) {
        this.trainingFile = trainingFile;
        this.testingFile = testingFile;
        this.modelFile = modelFile;
        this.featureNames = featureNames;
        this.firstNameFile = firstNameFile;
        this.lastNameFile = lastNameFile;

    }

    // TODO Add configurations (optional with default value)

    public void train() throws FileNotFoundException, IOException {

        ArrayList<Pipe> pipes = new ArrayList<Pipe>();
        pipes.add(new LineGroupString2TokenSequence());

        pipes.add(new XmlRefTagToTargetPipe("ref", "oth", "REF", "REFO", "O"));
        pipes.add(new TokenSequenceMatchDataAndTarget(Pattern.compile("([A-Z]*\\-*[A-Z]+) (.*)"), 2, 1));

        pipes.add(new LayoutPipe("INDENT", "PREVGAP", "DIFFZONE", "\\t"));

        FeaturePipeProvider featurePipeProvider = new FeaturePipeProvider(this.firstNameFile, this.lastNameFile);
        for (String featureName : this.featureNames) {
            pipes.add(featurePipeProvider.getPipe(featureName));
        }
        // pipes.add(new NamePipe("FIRSTNAME", new File(args[3])));
        // pipes.add(new NamePipe("LASTNAME", new File(args[4])));
        // pipes.add(new TokenText());
        // pipes.add(new TokenTextCharSuffix("C1=", 1));
        // pipes.add(new TokenTextCharSuffix("C2=", 2));
        // pipes.add(new TokenTextCharSuffix("C3=", 3));

        // pipes.add(new TokenTextCharSuffix("SUFFIX=", 1));
        // pipes.add(new TokenTextCharPrefix("PREFIX=", 1));

        // int[][] conjunctions = new int[3][];
        // conjunctions[0] = new int[] { -2 };
        // conjunctions[1] = new int[] { -1 };
        // conjunctions[2] = new int[] { 1 };
        // pipes.add(new OffsetConjunctions(conjunctions));

        pipes.add(new TokenSequence2FeatureVectorSequence(false, false));
        pipes.add(new Target2LabelSequence());

        // pipes.add(new PrintInputAndTarget());

        Pipe pipe = new SerialPipes(pipes);

        InstanceList trainingInstances = new InstanceList(pipe);
        InstanceList testingInstances = new InstanceList(pipe);

        trainingInstances.addThruPipe(
                new LineGroupIterator(new BufferedReader(new InputStreamReader(new FileInputStream(this.trainingFile))),
                        Pattern.compile("^\\s*$"), true));

        testingInstances.addThruPipe(
                new LineGroupIterator(new BufferedReader(new InputStreamReader(new FileInputStream(this.testingFile))),
                        Pattern.compile("^\\s*$"), true));

        CRF crf = new CRF(pipe, null);

        Pattern forbiddenPat = Pattern.compile("\\s");
        Pattern allowedPat = Pattern.compile(".*");
        List<Integer> orders = new ArrayList<Integer>();
        orders.add(0);
        orders.add(1);
        // orders.add(2);
        int[] ordersArray = null;
        if (orders.size() > 0) {
            ordersArray = ArrayUtils.toPrimitive(orders.toArray(new Integer[orders.size()]));
        }
        String startName = crf.addOrderNStates(trainingInstances, ordersArray, null, "O", forbiddenPat, allowedPat,
                true);
        for (int i = 0; i < crf.numStates(); i++) {
            crf.getState(i).setInitialWeight(Transducer.IMPOSSIBLE_WEIGHT);
        }
        crf.getState(startName).setInitialWeight(0.0);

        crf.setWeightsDimensionDensely();

        // crf.addStartState();
        // crf.addStatesForLabelsConnectedAsIn(trainingInstances);
        // crf.addStatesForHalfLabelsConnectedAsIn(trainingInstances);
        // crf.addStatesForThreeQuarterLabelsConnectedAsIn(trainingInstances);
        // crf.addStatesForBiLabelsConnectedAsIn(trainingInstances);

        CRFTrainerByLabelLikelihood trainer = new CRFTrainerByLabelLikelihood(crf);
        trainer.setGaussianPriorVariance(10.0);

        // CRFTrainerByStochasticGradient trainer =
        // new CRFTrainerByStochasticGradient(crf, 1.0);

        // CRFTrainerByL1LabelLikelihood trainer =
        // new CRFTrainerByL1LabelLikelihood(crf, 0.75);

        // trainer.addEvaluator(new PerClassAccuracyEvaluator(trainingInstances,
        // "training"));
        trainer.addEvaluator(new PerClassAccuracyEvaluator(testingInstances, "testing"));
        trainer.addEvaluator(new TokenAccuracyEvaluator(testingInstances, "testing"));
        trainer.train(trainingInstances);
        crf.write(this.modelFile);
    }

    public void writeConfigurations() {

    }
}

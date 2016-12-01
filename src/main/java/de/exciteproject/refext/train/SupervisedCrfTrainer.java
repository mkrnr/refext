package de.exciteproject.refext.train;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.regex.Pattern;

import cc.mallet.fst.CRF;
import cc.mallet.fst.CRFTrainerByLabelLikelihood;
import cc.mallet.fst.PerClassAccuracyEvaluator;
import cc.mallet.fst.TokenAccuracyEvaluator;
import cc.mallet.pipe.LineGroupString2TokenSequence;
import cc.mallet.pipe.Pipe;
import cc.mallet.pipe.SerialPipes;
import cc.mallet.pipe.Target2LabelSequence;
import cc.mallet.pipe.TokenSequence2FeatureVectorSequence;
import cc.mallet.pipe.TokenSequenceMatchDataAndTarget;
import cc.mallet.pipe.iterator.LineGroupIterator;
import cc.mallet.pipe.tsf.CountMatches;
import cc.mallet.pipe.tsf.OffsetConjunctions;
import cc.mallet.pipe.tsf.RegexMatches;
import cc.mallet.pipe.tsf.TokenText;
import cc.mallet.types.InstanceList;
import de.exciteproject.refext.pipe.LayoutPipe;
import de.exciteproject.refext.pipe.XmlRefTagToTargetPipe;

/**
 * Class for training a supervised CRF for extracting reference strings from a
 * text by considering layout information as well as content information.
 */
public class SupervisedCrfTrainer {

    public static void main(String[] args) throws FileNotFoundException, IOException {
	// TODO Auto-generated method stub

	File trainingFile = new File(args[0]);
	File testingFile = new File(args[1]);
	ArrayList<Pipe> pipes = new ArrayList<Pipe>();
	pipes.add(new LineGroupString2TokenSequence());
	pipes.add(new XmlRefTagToTargetPipe("ref", "oth", "REF", "REFO", "O"));
	pipes.add(new TokenSequenceMatchDataAndTarget(Pattern.compile("([A-Z]*\\-*[A-Z]+) (.*)"), 2, 1));
	pipes.add(new LayoutPipe("INDENT", "DIFFZONE", "\\t"));
	// pipes.add(new NamePipeOld("FIRSTNAMES", "LASTNAMES", new
	// File(args[2])));

	// pipes.add(new TokenSequenceParseFeatureString(false));
	pipes.add(new TokenText());
	// pipes.add(new TokenTextCharSuffix("C1=", 1));
	// pipes.add(new TokenTextCharSuffix("C2=", 2));
	// pipes.add(new TokenTextCharSuffix("C3=", 3));
	pipes.add(new RegexMatches("CAPITALIZED", Pattern.compile("\\p{Lu}.*")));
	pipes.add(new RegexMatches("STARTSNUMBER", Pattern.compile("[0-9].*")));
	pipes.add(new RegexMatches("CONTAINSYEAR", Pattern.compile(".*\\D[0-9][0-9][0-9][0-9]\\D.*")));
	pipes.add(new RegexMatches("CONTAINSPAGERANGE", Pattern.compile(".*\\d(-|^|\")\\d.*")));
	pipes.add(new RegexMatches("CONTAINSAMPHERSAND", Pattern.compile(".*&.*")));
	// pipes.add(new RegexMatches("HYPHENATED",
	// Pattern.compile(".*\\-.*")));
	// pipes.add(new RegexMatches("DOLLARSIGN",
	// Pattern.compile(".*\\$.*")));
	pipes.add(new RegexMatches("ENDSPERIOD", Pattern.compile(".*\\.")));
	pipes.add(new RegexMatches("ISNUMBER", Pattern.compile("/d+")));

	pipes.add(new CountMatches("PERIODS", Pattern.compile("\\.")));
	pipes.add(new CountMatches("COMMAS", Pattern.compile(",")));
	pipes.add(new CountMatches("CAPITALIZEDS", Pattern.compile("\\s\\p{Lu}\\S")));

	pipes.add(new RegexMatches("CONTAINSQUOTE", Pattern.compile(".*[„“””‘’\"'].*")));

	// pipes.add(new RegexMatches("CONTAINSURL",
	// Pattern.compile(".*(https?:\\/\\/)?([\\da-z\\.-]+)\\.([a-z\\.]{2,6})([\\/\\w
	// \\.-]*)*\\/?.*")));

	// makes things worse:
	// pipes.add(new TokenTextCharSuffix("SUFFIX=", 1));
	// pipes.add(new TokenTextCharPrefix("PREFIX=", 1));

	int[][] conjunctions = new int[2][];
	conjunctions[0] = new int[] { -1 };
	conjunctions[1] = new int[] { 1 };
	pipes.add(new OffsetConjunctions(conjunctions));

	pipes.add(new TokenSequence2FeatureVectorSequence(false, false));
	pipes.add(new Target2LabelSequence());
	// pipes.add(new PrintInputAndTarget());

	Pipe pipe = new SerialPipes(pipes);

	InstanceList trainingInstances = new InstanceList(pipe);
	InstanceList testingInstances = new InstanceList(pipe);

	trainingInstances.addThruPipe(
		new LineGroupIterator(new BufferedReader(new InputStreamReader(new FileInputStream(trainingFile))),
			Pattern.compile("^\\s*$"), true));
	testingInstances.addThruPipe(
		new LineGroupIterator(new BufferedReader(new InputStreamReader(new FileInputStream(testingFile))),
			Pattern.compile("^\\s*$"), true));

	CRF crf = new CRF(pipe, null);

	crf.addStartState();
	// crf.addStatesForLabelsConnectedAsIn(trainingInstances);
	// crf.addStatesForHalfLabelsConnectedAsIn(trainingInstances);
	crf.addStatesForThreeQuarterLabelsConnectedAsIn(trainingInstances);
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
	crf.write(new File("/home/mkoerner/crf.ser"));
	// CRFWriter crfWriter = new CRFWriter("/home/mkoerner/crf.ser") {
	// @Override
	// public boolean precondition(TransducerTrainer tt) {
	// // save the trained model after training finishes
	// return (tt.getIteration() % Integer.MAX_VALUE) == 0;
	// }
	// };
	// trainer.addEvaluator(crfWriter);

    }

}

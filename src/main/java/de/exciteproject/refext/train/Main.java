package de.exciteproject.refext.train;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import com.beust.jcommander.converters.FileConverter;
import com.cybozu.labs.langdetect.LangDetectException;

import cc.mallet.fst.CRF;
import cc.mallet.types.InstanceList;

/**
 * Class for training a supervised CRF for extracting reference strings from a
 * text by considering layout information as well as content information.
 */
public class Main {

    public static void main(String[] args) throws IOException, LangDetectException {
        Main supervisedCrfTrainer = new Main();

        JCommander jCommander;
        try {
            jCommander = new JCommander(supervisedCrfTrainer, args);
        } catch (ParameterException e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
            return;
        }

        if (supervisedCrfTrainer.help) {
            jCommander.usage();
        } else {
            supervisedCrfTrainer.run();
        }
    }

    @Parameter(names = { "-h", "--help" }, description = "print information about available parameters")
    private boolean help;

    @Parameter(names = { "-train",
            "--training-dir" }, description = "directory containing files where each line corresponds to the output ormat of CermineLineLayoutExtractor including XML tags ", required = true, converter = FileConverter.class)
    private File trainingDirectory;

    @Parameter(names = { "-test",
            "--testing-dir" }, description = "directory containing files where each line corresponds to the output ormat of CermineLineLayoutExtractor including XML tags ", required = true, converter = FileConverter.class)
    private File testingDirectory;

    @Parameter(names = { "-model",
            "--model-output-file" }, description = "file in which the trained crf model is saved", required = true, converter = FileConverter.class)
    private File modelFile;

    @Parameter(names = { "-weight",
            "--trainer-weight" }, description = "weight for crf trainer, depending which trainer is used")
    private double trainerWeight = 10.0;

    @Parameter(names = { "-feat",
            "--features" }, description = "comma separated list of features", variableArity = true, required = true)
    private List<String> featureNames;

    @Parameter(names = { "-replace",
            "--replacements" }, description = "comma separated list of values to replace", variableArity = true)
    private List<String> replacements;

    @Parameter(names = { "-conjunc",
            "--conjunctions" }, description = "comma separated list of cunjunctions that are separated by semicolons. \"min\" is used instead of the minus sign", variableArity = true)
    private List<String> conjunctions = new ArrayList<String>();

    @Parameter(names = { "-states",
            "--add-states-name" }, description = "string that specifies which states should be added.")
    private String addStatesName = "ThreeQuarterLabels";

    @Parameter(names = { "-trainer",
            "--trainer-name" }, description = "string that specifies which states should be added.")
    private String trainerName = "ByL1LabelLikelihood";

    public void run() throws FileNotFoundException, IOException, LangDetectException {

        // add default conjunctions if empty
        if (this.conjunctions.isEmpty()) {
            //this.conjunctions.add("min2;min1");
            this.conjunctions.add("min2");
            this.conjunctions.add("min1");
            this.conjunctions.add("1");
            this.conjunctions.add("2");
            //this.conjunctions.add("1;2");
        }

        ReferenceExtractorTrainer referenceExtractorTrainer = new ReferenceExtractorTrainer(this.featureNames,
                this.replacements, this.conjunctions);

        InstanceList trainingInstances = referenceExtractorTrainer.buildInstanceListFromDir(this.trainingDirectory);
        InstanceList testingInstances = referenceExtractorTrainer.buildInstanceListFromDir(this.testingDirectory);

        referenceExtractorTrainer.crf.addStartState();
        switch (this.addStatesName) {
        case "ThreeQuarterLabels":
            referenceExtractorTrainer.crf.addStatesForThreeQuarterLabelsConnectedAsIn(trainingInstances);
            break;
        case "BiLabels":
            referenceExtractorTrainer.crf.addStatesForBiLabelsConnectedAsIn(trainingInstances);
            break;
        case "Labels":
            referenceExtractorTrainer.crf.addStatesForLabelsConnectedAsIn(trainingInstances);
            break;
        case "HalfLabels":
            referenceExtractorTrainer.crf.addStatesForHalfLabelsConnectedAsIn(trainingInstances);
            break;
        }

        switch (this.trainerName) {
        case "ByLabelLikelihood":
            referenceExtractorTrainer.setCRFTrainerByLabelLikelihood(this.trainerWeight);
            break;
        case "ByL1LabelLikelihood":
            referenceExtractorTrainer.setCRFTrainerByL1LabelLikelihood(this.trainerWeight);
            break;
        }

        CRF crf = referenceExtractorTrainer.train(trainingInstances, testingInstances);
        crf.write(this.modelFile);

    }
}

package de.exciteproject.refext.train;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
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

    @Parameter(names = { "-test",
            "--testing-dir" }, description = "directory containing files where each line corresponds to the output ormat of CermineLineLayoutExtractor including XML tags ", required = true, converter = FileConverter.class)
    private File testingDirectory;

    @Parameter(names = { "-train",
            "--training-dir" }, description = "directory containing files where each line corresponds to the output ormat of CermineLineLayoutExtractor including XML tags ", required = true, converter = FileConverter.class)
    private File trainingDirectory;

    @Parameter(names = { "-model",
            "--model-output-file" }, description = "file in which the trained crf model is saved", required = true, converter = FileConverter.class)
    private File modelFile;

    @Parameter(names = { "-feat",
            "--features" }, description = "comma separated list of features", variableArity = true, required = true)
    private List<String> featureNames;

    @Parameter(names = { "-first-names",
            "--first-names-file" }, description = "file containing first names and counts, separated by tab", required = true, converter = FileConverter.class)
    private File firstNameFile;

    @Parameter(names = { "-last-names",
            "--last-names-file" }, description = "file containing last names and counts, separated by tab", required = true, converter = FileConverter.class)
    private File lastNameFile;

    // TODO Add configurations (optional with default value)

    public void run() throws FileNotFoundException, IOException, LangDetectException {

        ReferenceExtractorTrainer referenceExtractorTrainer = new ReferenceExtractorTrainer(this.featureNames,
                this.firstNameFile, this.lastNameFile);

        InstanceList trainingInstances = referenceExtractorTrainer.buildInstanceListFromDir(this.trainingDirectory);
        InstanceList testingInstances = referenceExtractorTrainer.buildInstanceListFromDir(this.testingDirectory);

        // TODO add parameter

        referenceExtractorTrainer.addStartState();
        referenceExtractorTrainer.addStatesForThreeQuarterLabelsConnectedAsIn(trainingInstances);
        // referenceExtractorTrainer.setCRFTrainerByLabelLikelihood(10.0);
        referenceExtractorTrainer.setCRFTrainerByL1LabelLikelihood(20.0);
        // referenceExtractorTrainer.setCRFTrainerByL1LabelLikelihood(0.75);

        CRF crf = referenceExtractorTrainer.train(trainingInstances, testingInstances);
        crf.write(this.modelFile);

    }
}

package de.exciteproject.refext.train;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import com.beust.jcommander.converters.FileConverter;

/**
 * Class for training a supervised CRF for extracting reference strings from a
 * text by considering layout information as well as content information.
 */
public class Main {

    public static void main(String[] args) throws IOException {
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
            "--testing-file" }, description = "file that contains per line: word <space> label", required = true, converter = FileConverter.class)
    private File testingFile;

    @Parameter(names = { "-train",
            "--training-file" }, description = "file that contains per line: word <space> label", required = true, converter = FileConverter.class)
    private File trainingFile;

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

    public void run() throws FileNotFoundException, IOException {
        ReferenceExtractorTrainer referenceExtractorTrainer = new ReferenceExtractorTrainer(this.trainingFile,
                this.testingFile, this.modelFile, this.featureNames, this.firstNameFile, this.lastNameFile);
        referenceExtractorTrainer.train();

    }
}

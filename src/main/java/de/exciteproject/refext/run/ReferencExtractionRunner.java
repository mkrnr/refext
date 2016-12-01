package de.exciteproject.refext.run;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import cc.mallet.fst.CRF;
import cc.mallet.fst.CRFTrainerByLabelLikelihood;
import cc.mallet.pipe.Pipe;
import cc.mallet.pipe.iterator.LineGroupIterator;
import cc.mallet.types.Instance;
import cc.mallet.types.InstanceList;
import cc.mallet.types.Sequence;
import cc.mallet.util.FileUtils;
import de.exciteproject.refext.extract.CermineLineLayoutExtractor;

/**
 * Class for applying reference extraction on text files that include layout
 * information (see {@link CermineLineLayoutExtractor}).
 *
 */
public class ReferencExtractionRunner {

    public static void main(String[] args) throws IOException {
	File inputDir = new File(args[0]);
	File outputDir = new File(args[1]);
	File crfModelFile = new File(args[2]);

	if (!outputDir.exists()) {
	    outputDir.mkdirs();
	}

	for (File file : de.exciteproject.refext.util.FileUtils.listFilesRecursively(inputDir)) {
	    List<String> dataLines = new ArrayList<String>();
	    BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
	    String line;
	    while ((line = bufferedReader.readLine()) != null) {
		dataLines.add(line.split("\t")[0]);
	    }
	    bufferedReader.close();

	    CRF crf = (CRF) FileUtils.readObject(crfModelFile);

	    Pipe pipe = crf.getInputPipe();
	    InstanceList trainingInstances = new InstanceList(pipe);

	    trainingInstances.addThruPipe(
		    new LineGroupIterator(new BufferedReader(new InputStreamReader(new FileInputStream(file))),
			    Pattern.compile("^\\s*$"), true));

	    CRFTrainerByLabelLikelihood trainer = new CRFTrainerByLabelLikelihood(crf);
	    trainer.setUseSparseWeights(false);

	    BufferedWriter bufferedWriter = new BufferedWriter(
		    new FileWriter(new File(outputDir + File.separator + file.getName())));
	    for (Instance instance : trainingInstances) {
		@SuppressWarnings("unchecked")
		Sequence<String> output = crf.transduce((Sequence<String>) instance.getData());
		for (int i = 0; i < output.size(); i++) {
		    bufferedWriter.write(output.get(i).toString() + "\t" + dataLines.get(i));

		    bufferedWriter.newLine();
		    // System.out.println(output.get(i).toString() + "\t" +
		    // dataLines.get(i));
		}
	    }
	    bufferedWriter.close();
	}
    }

}

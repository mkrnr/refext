package de.exciteproject.refext.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;

import cc.mallet.fst.TransducerEvaluator;
import cc.mallet.fst.TransducerTrainer;
import cc.mallet.types.FeatureVector;
import cc.mallet.types.Instance;
import cc.mallet.types.InstanceList;
import cc.mallet.types.Sequence;
import cc.mallet.types.TokenSequence;

/**
 * Class based on the ViterbiWriter class in MALLET. It also includes a
 * constructor for specifying the outputfile destination and modified the output
 * format.
 */
public class FixedViterbiWriter extends TransducerEvaluator {

    String outputEncoding = "UTF-8";
    private File outputFile;

    public FixedViterbiWriter(File outputFile, InstanceList instanceList1, String description1) {
        this(outputFile, new InstanceList[] { instanceList1 }, new String[] { description1 });
    }

    public FixedViterbiWriter(File outputFile, InstanceList instanceList1, String description1,
            InstanceList instanceList2, String description2) {
        this(outputFile, new InstanceList[] { instanceList1, instanceList2 },
                new String[] { description1, description2 });
    }

    public FixedViterbiWriter(File outputFile, InstanceList instanceList1, String description1,
            InstanceList instanceList2, String description2, InstanceList instanceList3, String description3) {
        this(outputFile, new InstanceList[] { instanceList1, instanceList2, instanceList3 },
                new String[] { description1, description2, description3 });
    }

    public FixedViterbiWriter(File outputFile, InstanceList[] instanceLists, String[] descriptions) {
        super(instanceLists, descriptions);
        this.outputFile = outputFile;
    }

    @SuppressWarnings({ "resource", "rawtypes" })
    @Override
    public void evaluateInstanceList(TransducerTrainer transducerTrainer, InstanceList instances, String description) {
        PrintStream viterbiOutputStream;
        try {
            FileOutputStream fos = new FileOutputStream(this.outputFile);
            if (this.outputEncoding == null) {
                viterbiOutputStream = new PrintStream(fos);
            } else {
                viterbiOutputStream = new PrintStream(fos, true, this.outputEncoding);
                // ((CRF)model).write (new File(viterbiOutputFilePrefix +
                // "."+description + iteration+".model"));
            }
        } catch (IOException e) {
            System.err.println("Couldn't open Viterbi output file '" + this.outputFile.getAbsolutePath()
                    + "'; continuing without Viterbi output trace.");
            return;
        }

        for (int i = 0; i < instances.size(); i++) {
            if (viterbiOutputStream != null) {
                viterbiOutputStream.println("Viterbi path for " + description + " instance #" + i);
            }
            Instance instance = instances.get(i);
            Sequence input = (Sequence) instance.getData();
            TokenSequence sourceTokenSequence = null;
            if (instance.getSource() instanceof TokenSequence) {
                sourceTokenSequence = (TokenSequence) instance.getSource();
            }

            Sequence trueOutput = (Sequence) instance.getTarget();
            assert (input.size() == trueOutput.size());
            Sequence predOutput = transducerTrainer.getTransducer().transduce(input);
            assert (predOutput.size() == trueOutput.size());

            for (int j = 0; j < trueOutput.size(); j++) {
                FeatureVector fv = (FeatureVector) input.get(j);
                // viterbiOutputStream.println (tokens.charAt(j)+"
                // "+trueOutput.get(j).toString()+
                // '/'+predOutput.get(j).toString()+" "+ fv.toString(true));
                if (sourceTokenSequence != null) {
                    viterbiOutputStream.print(sourceTokenSequence.get(j).getText() + ": ");
                }
                String result = trueOutput.get(j).toString() + '/' + predOutput.get(j).toString();
                while (result.toCharArray().length < 9) {
                    result += " ";
                }
                viterbiOutputStream.println(result + "  " + fv.toString(true));
            }
        }
    }

    @Override
    protected void preamble(TransducerTrainer tt) {
        // We don't want to print iteration number and cost, so here we override
        // this behavior in the superclass.
    }

}

package de.exciteproject.refext.train.pipe;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import com.cybozu.labs.langdetect.LangDetectException;

import cc.mallet.pipe.Pipe;
import cc.mallet.types.Instance;
import cc.mallet.types.Token;
import cc.mallet.types.TokenSequence;

/**
 * Pipe that adds a feature based on the position of a given line in the
 * document
 */
public class PositionInDocumentPipe extends Pipe implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final int CURRENT_SERIAL_VERSION = 0;

    private String feature;
    private String csvSeparator;

    public PositionInDocumentPipe(String featureName, String csvSeparator) throws LangDetectException, IOException {
        this.feature = featureName;
        this.csvSeparator = csvSeparator;
    }

    @Override
    public Instance pipe(Instance carrier) {
        TokenSequence tokenSequence = (TokenSequence) carrier.getData();
        int totalLineCount = 0;
        for (Token token : tokenSequence) {
            totalLineCount++;
        }
        int currentLineCount = 0;
        for (Token token : tokenSequence) {
            currentLineCount++;

            double value = ((double) currentLineCount) / totalLineCount;
            token.setFeatureValue(this.feature, value);
        }
        return carrier;
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        // read serial version
        in.readInt();

        this.feature = (String) in.readObject();
        this.csvSeparator = (String) in.readObject();
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(CURRENT_SERIAL_VERSION);
        out.writeObject(this.feature);
        out.writeObject(this.csvSeparator);
    }

}

package de.exciteproject.refext.pipe;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import cc.mallet.pipe.Pipe;
import cc.mallet.types.Instance;
import cc.mallet.types.Token;
import cc.mallet.types.TokenSequence;

/**
 * A class that counts the number of matches of given regular expression per
 * token in a tokenSequence.
 */
public class CountMatchesPipe extends Pipe implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final int CURRENT_SERIAL_VERSION = 0;

    private String feature;
    private String wordRegex;

    public CountMatchesPipe(String featureName, String wordRegex) {
        this.feature = featureName;
        this.wordRegex = wordRegex;
    }

    @Override
    public Instance pipe(Instance carrier) {
        TokenSequence tokenSequence = (TokenSequence) carrier.getData();
        for (int i = 0; i < tokenSequence.size(); i++) {
            Token token = tokenSequence.get(i);
            String tokenText = token.getText();
            String[] tokenTextSplit = tokenText.split("\\s");
            int count = 0;
            for (String string : tokenTextSplit) {
                if (string.matches(this.wordRegex)) {
                    count++;
                }
            }
            // int count = StringUtils.countMatches(tokenText, this.subString);
            if (count > 0) {
                token.setFeatureValue(this.feature + "=" + count, 1.0);
            }
        }
        return carrier;
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        // read serial version
        in.readInt();

        this.feature = (String) in.readObject();
        this.wordRegex = (String) in.readObject();
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(CURRENT_SERIAL_VERSION);
        out.writeObject(this.feature);
        out.writeObject(this.wordRegex);
    }

}

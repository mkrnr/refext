package de.exciteproject.refext.train.pipe;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.regex.Pattern;

import cc.mallet.pipe.Pipe;
import cc.mallet.types.Instance;
import cc.mallet.types.Token;
import cc.mallet.types.TokenSequence;

/**
 * A class that matches the Tokens in a given TokenSequence against a given
 * regular expression. The functionality is derived from the
 * cc.mallet.pipe.tsf.RegexMatches class but removes the "dealing with([a-z]+),
 * ([a-z]+, [a-z]+), [a-z]+." functionality.
 */
public class RegexPipe extends Pipe implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final int CURRENT_SERIAL_VERSION = 0;

    private Pattern regex;
    private String feature;
    private String csvSeparator;

    public RegexPipe(String featureName, Pattern regex, String csvSeparator) {
        this.feature = featureName;
        this.regex = regex;
        this.csvSeparator = csvSeparator;
    }

    @Override
    public Instance pipe(Instance carrier) {
        TokenSequence tokenSequence = (TokenSequence) carrier.getData();
        for (int i = 0; i < tokenSequence.size(); i++) {
            Token token = tokenSequence.get(i);
            String tokenText = token.getText().split(this.csvSeparator)[0];
            if (this.regex.matcher(tokenText).matches()) {
                token.setFeatureValue(this.feature, 1.0);
            }
        }
        return carrier;
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        // read serial version
        in.readInt();

        this.regex = (Pattern) in.readObject();
        this.feature = (String) in.readObject();
        this.csvSeparator = (String) in.readObject();
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(CURRENT_SERIAL_VERSION);
        out.writeObject(this.regex);
        out.writeObject(this.feature);
        out.writeObject(this.csvSeparator);
    }

}

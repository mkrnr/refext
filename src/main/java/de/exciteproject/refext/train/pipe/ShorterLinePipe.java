package de.exciteproject.refext.train.pipe;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cc.mallet.pipe.Pipe;
import cc.mallet.types.Instance;
import cc.mallet.types.Token;
import cc.mallet.types.TokenSequence;

/**
 * A class that counts the number of matches of given regular expression per
 * token in a tokenSequence.
 */
public class ShorterLinePipe extends Pipe implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final int CURRENT_SERIAL_VERSION = 0;

    private String feature;
    private Pattern pattern;
    private String csvSeparator;

    public ShorterLinePipe(String featureName, String csvSeparator) {
        this.feature = featureName;
        this.pattern = Pattern.compile(".");
        this.csvSeparator = csvSeparator;
    }

    @Override
    public Instance pipe(Instance carrier) {
        TokenSequence tokenSequence = (TokenSequence) carrier.getData();
        int prevCount = 0;
        for (int i = 0; i < tokenSequence.size(); i++) {
            Token token = tokenSequence.get(i);
            String tokenText = token.getText().split(this.csvSeparator)[0];
            int count = 0;
            Matcher matcher = this.pattern.matcher(tokenText);
            while (matcher.find()) {
                count++;
            }
            // int count = StringUtils.countMatches(tokenText, this.subString);
            if (count < prevCount) {
                token.setFeatureValue(this.feature, 1.0);
            }
            prevCount = count;
        }
        return carrier;
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        // read serial version
        in.readInt();

        this.feature = (String) in.readObject();
        this.pattern = Pattern.compile((String) in.readObject());
        this.csvSeparator = (String) in.readObject();
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(CURRENT_SERIAL_VERSION);
        out.writeObject(this.feature);
        out.writeObject(this.pattern.pattern());
        out.writeObject(this.csvSeparator);
    }

}

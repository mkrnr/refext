package de.exciteproject.refext.pipe;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import org.apache.commons.lang.StringUtils;

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
    private String subString;

    public CountMatchesPipe(String featureName, String subString) {
	this.feature = featureName;
	this.subString = subString;
    }

    @Override
    public Instance pipe(Instance carrier) {
	TokenSequence tokenSequence = (TokenSequence) carrier.getData();
	for (int i = 0; i < tokenSequence.size(); i++) {
	    Token token = tokenSequence.get(i);
	    String tokenText = token.getText();
	    int count = StringUtils.countMatches(tokenText, this.subString);
	    if (count > 0) {
		token.setFeatureValue(this.feature, count);
	    }
	}
	return carrier;
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
	// read serial version
	in.readInt();

	this.feature = (String) in.readObject();
	this.subString = (String) in.readObject();
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
	out.writeInt(CURRENT_SERIAL_VERSION);
	out.writeObject(this.feature);
	out.writeObject(this.subString);
    }

}

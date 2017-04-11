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
import de.exciteproject.refext.util.LanguageAnalyzer;

/**
 * Computes a feature based on the language of a given line
 */
public class LanguagePipe extends Pipe implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final int CURRENT_SERIAL_VERSION = 0;

    private String feature;
    private String csvSeparator;
    private LanguageAnalyzer languageAnalyzer;

    public LanguagePipe(String featureName, String csvSeparator) throws LangDetectException, IOException {
        this.feature = featureName;
        this.csvSeparator = csvSeparator;
        this.languageAnalyzer = new LanguageAnalyzer();
    }

    @Override
    public Instance pipe(Instance carrier) {
        TokenSequence tokenSequence = (TokenSequence) carrier.getData();
        for (Token token : tokenSequence) {
            String tokenText = token.getText().split(this.csvSeparator)[0];

            String language = this.languageAnalyzer.analyze(tokenText);
            token.setFeatureValue(this.feature + "=" + language, 1.0);
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

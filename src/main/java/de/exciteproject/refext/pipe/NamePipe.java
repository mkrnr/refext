package de.exciteproject.refext.pipe;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashMap;

import cc.mallet.pipe.Pipe;
import cc.mallet.types.Instance;
import cc.mallet.types.Token;
import cc.mallet.types.TokenSequence;

/**
 * A Pipe that adds a feature with the value 1.0 if the token was found in a
 * data base for names.
 */
public class NamePipe extends Pipe implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final int CURRENT_SERIAL_VERSION = 1;

    private String featureName;

    private HashMap<String, Double> nameCountMap;

    /**
     *
     * @param featureName
     *            the label that is used when a last name was detected
     * @param nameFile
     *            a file containing per line: name tab count
     */
    public NamePipe(String featureName, File nameFile) {
        this.featureName = featureName;

        this.nameCountMap = new HashMap<String, Double>();
        try {
            BufferedReader lastNameFileReader = new BufferedReader(new FileReader(nameFile));
            String line;
            while ((line = lastNameFileReader.readLine()) != null) {
                // removes the name counts
                String[] lineSplit = line.split("\t");
                if (lineSplit.length != 2) {
                    lastNameFileReader.close();
                    throw new IllegalStateException("line is in wrong format: " + line);
                }
                Integer currentCount = Integer.valueOf(lineSplit[1]);
                double featureValue = Math.log1p(currentCount);
                this.nameCountMap.put(lineSplit[0].trim(), featureValue);
            }
            lastNameFileReader.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Instance pipe(Instance instance) {

        TokenSequence sequence = (TokenSequence) instance.getData();

        for (Token token : sequence) {
            String[] tokenSplit = token.getText().trim().split("\\s");
            for (int i = 0; i < tokenSplit.length; i++) {
                String normalizedTokenPart = tokenSplit[i].replaceAll("[^\\p{L}]", "");
                if ((normalizedTokenPart.length() > 0) && this.nameCountMap.containsKey((normalizedTokenPart))) {
                    token.setFeatureValue(this.featureName + "@" + i, 1.0);
                }
            }
        }
        return instance;
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        this.featureName = (String) in.readObject();
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(CURRENT_SERIAL_VERSION);
        out.writeObject(this.featureName);
    }

}

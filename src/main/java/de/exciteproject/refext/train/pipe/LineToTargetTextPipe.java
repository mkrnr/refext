package de.exciteproject.refext.train.pipe;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import cc.mallet.pipe.Pipe;
import cc.mallet.types.Instance;
import cc.mallet.types.Token;
import cc.mallet.types.TokenSequence;

/**
 * Pipe that removes the characters until the first tab character and sets them
 * as the target
 */

public class LineToTargetTextPipe extends Pipe implements Serializable {

    private static final long serialVersionUID = 1;
    private static final int CURRENT_SERIAL_VERSION = 0;

    // Serialization

    @Override
    public Instance pipe(Instance carrier) {

        TokenSequence ts = (TokenSequence) carrier.getData();
        TokenSequence targetTokenSeq = new TokenSequence(ts.size());

        for (int i = 0; i < ts.size(); i++) {

            Token t = ts.get(i);
            // System.out.println(t.getText());
            String lineWithoutFirst = t.getText().replaceFirst("[^\\t]*\t", "");
            // System.out.println(lineWithoutFirst);
            // targetTokenSeq.add(lineSplit[0]);
            targetTokenSeq.add(t.getText().split("\t")[0]);
            t.setText(lineWithoutFirst);

        }
        carrier.setTarget(targetTokenSeq);
        carrier.setData(ts);

        return carrier;
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        // read version number
        in.readInt();

    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(CURRENT_SERIAL_VERSION);

    }

}

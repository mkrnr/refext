/* Copyright (C) 2002 Univ. of Massachusetts Amherst, Computer Science Dept.
   This file is part of "MALLET" (MAchine Learning for LanguagE Toolkit).
   http://www.cs.umass.edu/~mccallum/mallet
   This software is provided under the terms of the Common Public License,
   version 1.0, as published by http://www.opensource.org.  For further
   information, see the file `LICENSE' included with this distribution. */

package de.exciteproject.refext.train.pipe;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import cc.mallet.pipe.Pipe;
import cc.mallet.types.Instance;
import cc.mallet.types.Token;
import cc.mallet.types.TokenSequence;
import de.exciteproject.refext.extract.CermineLineLayoutExtractor;

/**
 * Adds a O-tag in front of a given layout line if length==minLineLength. This is needed 
 * for files that generated with {@link CermineLineLayoutExtractor}. This is needed 
 * since the mallet CRF expects a target variable, even for applying a trained model.
 */

public class AddTargetToLinePipe extends Pipe implements Serializable {

    private static final long serialVersionUID = 1;
    private static final int CURRENT_SERIAL_VERSION = 0;
    private int minLineLength;

    /**
     *
     * @param minLineLength
     *            minimum line length for layout file. That is, without a
     *            leading target variable declaration. If a target variable
     *            declaration is present, the line length is assumed to be
     *            minLineLength + 1
     */
    public AddTargetToLinePipe(int minLineLength) {
        this.minLineLength = minLineLength;
    }

    @Override
    public Instance pipe(Instance carrier) {

        TokenSequence ts = (TokenSequence) carrier.getData();
        for (int i = 0; i < ts.size(); i++) {
            Token t = ts.get(i);
            int splitLength = t.getText().split("\t").length;
            if (splitLength == this.minLineLength) {
                t.setText("O\t" + t.getText());
            } else {
                if (splitLength != (this.minLineLength + 1)) {
                    System.err.println("input line does not have length " + this.minLineLength + " or "
                            + (this.minLineLength + 1) + " but " + splitLength + ": " + t.getText());
                }
            }
        }
        carrier.setData(ts);

        return carrier;
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        // read version number
        in.readInt();
        this.minLineLength = in.readInt();

    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(CURRENT_SERIAL_VERSION);
        out.writeInt(this.minLineLength);

    }

}

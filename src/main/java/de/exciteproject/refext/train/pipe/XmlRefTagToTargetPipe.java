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

/**
 * Converts lines that are tagged by ref and oth XML tags to B-REF, I-REF,
 * B-REFO, I-REFO, and O target notation per line.
 */

public class XmlRefTagToTargetPipe extends Pipe implements Serializable {

    private static final long serialVersionUID = 1;
    private static final int CURRENT_SERIAL_VERSION = 0;
    private String referenceTagName;
    private String otherInReferenceTagName;
    private String referenceTargetLabel;

    private String otherInReferenceTargetLabel;

    private String otherTargetLabel;

    // Serialization

    public XmlRefTagToTargetPipe(String referenceTagName, String otherInReferenceTagName, String referenceTargetLabel,
            String otherInReferenceTargetLabel, String otherTargetLabel) {
        this.referenceTagName = referenceTagName;
        this.otherInReferenceTagName = otherInReferenceTagName;
        this.referenceTargetLabel = referenceTargetLabel;
        this.otherInReferenceTargetLabel = otherInReferenceTargetLabel;
        this.otherTargetLabel = otherTargetLabel;
    }

    @Override
    public Instance pipe(Instance carrier) {

        TokenSequence ts = (TokenSequence) carrier.getData();
        TokenSequence targetTokenSeq = new TokenSequence(ts.size());

        boolean inReferenceString = false;
        boolean inOtherInReferenceString = false;

        for (int i = 0; i < ts.size(); i++) {
            Token t = ts.get(i);
            String line = t.getText();
            String targetLabel;
            if (line.contains("<" + this.referenceTagName + ">")) {
                inReferenceString = true;
                line = line.replaceFirst("<" + this.referenceTagName + ">", "");
                targetLabel = "B-" + this.referenceTargetLabel;
            } else {
                if (line.contains("<" + this.otherInReferenceTagName + ">")) {
                    if (inReferenceString) {
                        inOtherInReferenceString = true;
                        line = line.replaceFirst("<" + this.otherInReferenceTagName + ">", "");
                        targetLabel = "B-" + this.otherInReferenceTargetLabel;
                    } else {
                        throw new IllegalStateException(
                                "<" + inOtherInReferenceString + "> not inside <" + this.referenceTagName + ">");
                    }
                } else {
                    if (inReferenceString) {
                        if (inOtherInReferenceString) {
                            targetLabel = "I-" + this.otherInReferenceTargetLabel;
                        } else {
                            targetLabel = "I-" + this.referenceTargetLabel;
                        }
                    } else {
                        targetLabel = this.otherTargetLabel;
                    }
                }
            }

            if (line.contains("</" + this.referenceTagName + ">")) {
                line = line.replaceFirst("</" + this.referenceTagName + ">", "");
                inReferenceString = false;
            }
            if (line.contains("</" + this.otherInReferenceTagName + ">")) {
                line = line.replaceFirst("</" + this.otherInReferenceTagName + ">", "");
                inOtherInReferenceString = false;
            }

            t.setText(line);
            targetTokenSeq.add(targetLabel);

        }
        carrier.setTarget(targetTokenSeq);
        carrier.setData(ts);

        return carrier;
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        // read version number
        in.readInt();

        this.referenceTagName = (String) in.readObject();
        this.otherInReferenceTagName = (String) in.readObject();
        this.referenceTargetLabel = (String) in.readObject();
        this.otherInReferenceTargetLabel = (String) in.readObject();
        this.otherTargetLabel = (String) in.readObject();
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(CURRENT_SERIAL_VERSION);

        out.writeObject(this.referenceTagName);
        out.writeObject(this.otherInReferenceTagName);
        out.writeObject(this.referenceTargetLabel);
        out.writeObject(this.otherInReferenceTargetLabel);
        out.writeObject(this.otherTargetLabel);
    }

}

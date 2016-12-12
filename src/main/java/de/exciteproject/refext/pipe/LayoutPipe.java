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
 * Computes features based on the layout of lines in the PDF file <br>
 * Input per line:
 * text[tab]x-coordinate[tab]y-coordinate[tab]height[tab]width[tab]zone-id
 */
public class LayoutPipe extends Pipe implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final int CURRENT_SERIAL_VERSION = 0;

    private String indentFeatureLabel;
    private String gapAboveLabel;
    private String differentZoneLabel;
    private String csvSeparator;

    public LayoutPipe(String indentFeatureLabel, String gapAboveLabel, String differentZoneLabel, String csvSeparator) {
	this.indentFeatureLabel = indentFeatureLabel;
	this.gapAboveLabel = gapAboveLabel;
	this.differentZoneLabel = differentZoneLabel;
	this.csvSeparator = csvSeparator;

    }

    @Override
    public Instance pipe(Instance carrier) {
	TokenSequence tokenSequence = (TokenSequence) carrier.getData();
	double prevXPos = Double.MAX_VALUE;
	double prevYPos = Double.MAX_VALUE;
	double prevHeight = Double.MAX_VALUE;
	double prevWidth = Double.MAX_VALUE;
	double prevZoneId = 0.0;
	double prevDistance = Double.MAX_VALUE;

	boolean prevWasIndent = false;
	for (int i = 0; i < tokenSequence.size(); i++) {
	    Token token = tokenSequence.get(i);
	    String tokenText = token.getText();

	    String[] tokenSplit = tokenText.split(this.csvSeparator);

	    if (tokenSplit.length != 6) {
		System.out.println(tokenText);
		throw new IllegalStateException("token split length is not 5 but " + tokenSplit.length);
	    }

	    // format (copied from CermineLineLayoutExtractor)
	    // fixedLine += "\t" + bxLine.getX() + "\t" + bxLine.getY() + "\t" +
	    // bxLine.getHeight() + "\t" + bxLine.getWidth()
	    double thisXPos = Double.parseDouble(tokenSplit[1]);
	    double thisYPos = Double.parseDouble(tokenSplit[2]);
	    double thisHeight = Double.parseDouble(tokenSplit[3]);
	    double thisWidth = Double.parseDouble(tokenSplit[4]);
	    double thisZoneId = Double.parseDouble(tokenSplit[5]);

	    if (thisZoneId != prevZoneId) {
		// token.setFeatureValue(this.differentZoneLabel, 1.0);
		prevWasIndent = false;

		// token.setFeatureValue("NEWZONE", 1.0);
	    } else {
		if (prevWasIndent) {
		    if (Math.abs(thisXPos - prevXPos) < 0.001) {
			token.setFeatureValue(this.indentFeatureLabel, 1.0);
			prevWasIndent = true;
		    } else {
			prevWasIndent = false;
		    }
		} else {
		    if (((thisXPos - prevXPos) > 5.0) && (prevYPos < thisYPos)) {
			token.setFeatureValue(this.indentFeatureLabel, 1.0);
			prevWasIndent = true;
		    } else {
			prevWasIndent = false;
		    }
		}

	    }

	    // calculate distance to prev line
	    double thisDistance = thisYPos - (prevYPos + prevHeight);
	    if ((thisDistance - prevDistance) > 5) {
		token.setFeatureValue(this.gapAboveLabel, 1.0);
	    }
	    prevDistance = thisDistance;

	    token.setText(tokenSplit[0]);

	    prevXPos = thisXPos;
	    prevYPos = thisYPos;
	    prevHeight = thisHeight;
	    prevWidth = thisWidth;
	    prevZoneId = thisZoneId;

	}
	return carrier;
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
	// read serial version
	in.readInt();

	this.indentFeatureLabel = (String) in.readObject();
	this.gapAboveLabel = (String) in.readObject();
	this.differentZoneLabel = (String) in.readObject();
	this.csvSeparator = (String) in.readObject();
	// this.yPosFeatureLabel = (String) in.readObject();
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
	out.writeInt(CURRENT_SERIAL_VERSION);
	out.writeObject(this.indentFeatureLabel);
	out.writeObject(this.gapAboveLabel);
	out.writeObject(this.differentZoneLabel);
	out.writeObject(this.csvSeparator);
	// out.writeObject(this.yPosFeatureLabel);
    }

}

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
 * Computes features based on the layout of lines in the PDF file <br>
 * Input per line:
 * text[tab]x-coordinate[tab]y-coordinate[tab]height[tab]width[tab]zone-id
 */
public abstract class LayoutPipe extends Pipe implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final int CURRENT_SERIAL_VERSION = 0;

    protected double prevXPos = Double.MAX_VALUE;
    protected double prevYPos = Double.MAX_VALUE;
    protected double prevHeight = Double.MAX_VALUE;
    protected double prevWidth = Double.MAX_VALUE;
    protected double prevZoneId = 0.0;
    protected double thisXPos;
    protected double thisYPos;
    protected double thisHeight;
    protected double thisWidth;
    protected double thisZoneId;
    protected String featureLabel;
    private String csvSeparator;

    public LayoutPipe(String featureLabel, String csvSeparator) {
        this.featureLabel = featureLabel;
        this.csvSeparator = csvSeparator;

    }

    @Override
    public Instance pipe(Instance carrier) {
        TokenSequence tokenSequence = (TokenSequence) carrier.getData();

        for (int i = 0; i < tokenSequence.size(); i++) {
            Token token = tokenSequence.get(i);
            String tokenText = token.getText();

            String[] tokenSplit = tokenText.split(this.csvSeparator);

            if (tokenSplit.length != 6) {
                // TODO throw exception
                System.err.println("length!=6: " + tokenText);
                continue;
            }

            // format (copied from CermineLineLayoutExtractor)
            // fixedLine += "\t" + bxLine.getX() + "\t" + bxLine.getY() + "\t" +
            // bxLine.getHeight() + "\t" + bxLine.getWidth()
            this.thisXPos = Double.parseDouble(tokenSplit[1]);
            this.thisYPos = Double.parseDouble(tokenSplit[2]);
            this.thisHeight = Double.parseDouble(tokenSplit[3]);
            this.thisWidth = Double.parseDouble(tokenSplit[4]);
            this.thisZoneId = Double.parseDouble(tokenSplit[5]);

            this.computeFeatureLabel(token);

            // token.setText(tokenSplit[0]);

            this.prevXPos = this.thisXPos;
            this.prevYPos = this.thisYPos;
            this.prevHeight = this.thisHeight;
            this.prevWidth = this.thisWidth;
            this.prevZoneId = this.thisZoneId;

        }
        return carrier;
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        // read serial version
        in.readInt();

        this.featureLabel = (String) in.readObject();
        this.csvSeparator = (String) in.readObject();
        // this.yPosFeatureLabel = (String) in.readObject();
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(CURRENT_SERIAL_VERSION);
        out.writeObject(this.featureLabel);
        out.writeObject(this.csvSeparator);
        // out.writeObject(this.yPosFeatureLabel);
    }

    protected abstract void computeFeatureLabel(Token token);

}

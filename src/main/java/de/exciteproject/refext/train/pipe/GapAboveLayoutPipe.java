package de.exciteproject.refext.train.pipe;

import cc.mallet.types.Token;

/**
 * Computes a feature that signalizes a vertical gap above a given line
 */
public class GapAboveLayoutPipe extends LayoutPipe {

    private static final long serialVersionUID = 1L;
    private double prevDistance;

    public GapAboveLayoutPipe(String featureLabel, String csvSeparator) {
        super(featureLabel, csvSeparator);
        this.prevDistance = Double.MAX_VALUE;

    }

    @Override
    protected void computeFeatureLabel(Token token) {

        // calculate distance to prev line
        double thisDistance = this.thisYPos - (this.prevYPos + this.prevHeight);
        if ((thisDistance - this.prevDistance) > 5) {
            token.setFeatureValue(this.featureLabel, 1.0);
        }
        this.prevDistance = thisDistance;

    }

}

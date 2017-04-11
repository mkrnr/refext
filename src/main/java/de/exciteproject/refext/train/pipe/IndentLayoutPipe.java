package de.exciteproject.refext.train.pipe;

import cc.mallet.types.Token;

/**
 * Computes a feature that indicates an indentation of a given line in comparison with the previous line
 */
public class IndentLayoutPipe extends LayoutPipe {

    private static final long serialVersionUID = 1L;
    private boolean prevWasIndent;

    public IndentLayoutPipe(String featureLabel, String csvSeparator) {
        super(featureLabel, csvSeparator);
        this.prevWasIndent = false;

    }

    @Override
    protected void computeFeatureLabel(Token token) {
        // if (this.thisZoneId != this.prevZoneId) {
        // // token.setFeatureValue(this.differentZoneLabel, 1.0);
        // this.prevWasIndent = false;
        //
        // // token.setFeatureValue("NEWZONE", 1.0);
        // } else {
        if (this.prevWasIndent) {
            if (Math.abs(this.thisXPos - this.prevXPos) < 0.001) {
                token.setFeatureValue(this.featureLabel, 1.0);
                this.prevWasIndent = true;
            } else {
                this.prevWasIndent = false;
            }
        } else {

            if (((this.thisXPos - this.prevXPos) > 5.0) && (this.prevYPos < this.thisYPos)) {
                token.setFeatureValue(this.featureLabel, 1.0);
                this.prevWasIndent = true;
            } else {
                this.prevWasIndent = false;
            }
        }
        // }

    }

}

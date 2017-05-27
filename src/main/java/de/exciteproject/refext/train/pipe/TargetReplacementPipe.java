package de.exciteproject.refext.train.pipe;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import cc.mallet.pipe.Pipe;
import cc.mallet.types.Instance;
import cc.mallet.types.Token;
import cc.mallet.types.TokenSequence;

/**
 * Pipe that removes the characters until the first tab character and sets them
 * as the target
 */

public class TargetReplacementPipe extends Pipe implements Serializable {

    private static final long serialVersionUID = 1;
    private static final int CURRENT_SERIAL_VERSION = 0;
    private Map<String, String> replacementMap;

    public TargetReplacementPipe(List<String> replacements) {
        this.setReplacementMap(replacements);
    }

    @Override
    public Instance pipe(Instance carrier) {

        TokenSequence targets = (TokenSequence) carrier.getTarget();

        for (int i = 0; i < targets.size(); i++) {

            Token target = targets.get(i);
            // System.out.println(t.getText());
            String targetLabel = target.getText();
            // System.out.println(lineWithoutFirst);
            // targetTokenSeq.add(lineSplit[0]);
            if (this.replacementMap.containsKey(targetLabel)) {
                target.setText(this.replacementMap.get(targetLabel));
            }
        }
        carrier.setTarget(targets);

        return carrier;
    }

    public void setReplacementMap(List<String> replacements) {
        this.replacementMap = new HashMap<String, String>();
        if (replacements != null) {
            for (int i = 0; i < (replacements.size() - 1); i++) {
                this.replacementMap.put(replacements.get(i), replacements.get(i + 1));
            }
        }
    }

    // Serialization
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        // read version number
        in.readInt();
        String replacementsString = (String) in.readObject();
        this.setReplacementMap(Arrays.asList(replacementsString.split(",")));
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(CURRENT_SERIAL_VERSION);
        String replacementsString = "";
        for (Entry<String, String> entry : this.replacementMap.entrySet()) {
            if (!replacementsString.isEmpty()) {
                replacementsString += ",";
            }
            replacementsString += entry.getKey() + "," + entry.getValue();
        }
        out.writeObject(replacementsString);
    }

}

package de.exciteproject.refext.train;

/* Copyright (C) 2002 Univ. of Massachusetts Amherst, Computer Science Dept.
This file is part of "MALLET" (MAchine Learning for LanguagE Toolkit).
http://www.cs.umass.edu/~mccallum/mallet
This software is provided under the terms of the Common Public License,
version 1.0, as published by http://www.opensource.org.  For further
information, see the file `LICENSE' included with this distribution. */

/**
@author Andrew McCallum <a href="mailto:mccallum@cs.umass.edu">mccallum@cs.umass.edu</a>
*/
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.Reader;
import java.util.Iterator;
import java.util.regex.Pattern;

import cc.mallet.types.Instance;

/**
 * This class is copied from cc.mallet.pipe.iterator.LineGroupIterator.java and
 * replaces the platform dependent "\n" with "System.lineSeparator"
 *
 * Iterate over groups of lines of text, separated by lines that match a regular
 * expression. For example, the WSJ BaseNP data consists of sentences with one
 * word per line, each sentence separated by a blank line. If the "boundary"
 * line is to be included in the group, it is placed at the end of the group.
 */

public class LineGroupIterator implements Iterator<Instance> {
    LineNumberReader reader;
    Pattern lineBoundaryRegex;
    boolean skipBoundary;
    // boolean putBoundaryLineAtEnd; // Not yet implemented
    String nextLineGroup;
    String nextBoundary;
    String nextNextBoundary;
    int groupIndex = 0;
    boolean putBoundaryInSource = true;

    public LineGroupIterator(Reader input, Pattern lineBoundaryRegex, boolean skipBoundary) {
        this.reader = new LineNumberReader(input);
        this.lineBoundaryRegex = lineBoundaryRegex;
        this.skipBoundary = skipBoundary;
        this.setNextLineGroup();
    }

    @Override
    public boolean hasNext() {
        return this.nextLineGroup != null;
    }

    @Override
    public Instance next() {
        assert (this.nextLineGroup != null);
        Instance carrier = new Instance(this.nextLineGroup, null, "linegroup" + this.groupIndex++,
                this.putBoundaryInSource ? this.nextBoundary : null);
        this.setNextLineGroup();
        return carrier;
    }

    public String peekLineGroup() {
        return this.nextLineGroup;
    }

    @Override
    public void remove() {
        throw new IllegalStateException("This Iterator<Instance> does not support remove().");
    }

    private void setNextLineGroup() {
        StringBuffer sb = new StringBuffer();
        String line;
        if (!this.skipBoundary && (this.nextBoundary != null)) {
            sb.append(this.nextBoundary + System.getProperty("line.separator"));
        }
        while (true) {
            try {
                line = this.reader.readLine();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            // System.out.println ("LineGroupIterator: got line: "+line);
            if (line == null) {
                break;
            } else if (this.lineBoundaryRegex.matcher(line).matches()) {
                if (sb.length() > 0) {
                    this.nextBoundary = this.nextNextBoundary;
                    this.nextNextBoundary = line;
                    break;
                } else { // The first line of the file.
                    if (!this.skipBoundary) {
                        sb.append(line + System.getProperty("line.separator"));
                    }
                    this.nextNextBoundary = line;
                }
            } else {
                sb.append(line);
                sb.append(System.getProperty("line.separator"));
            }
        }
        if (sb.length() == 0) {
            this.nextLineGroup = null;
        } else {
            this.nextLineGroup = sb.toString();
        }
    }

}
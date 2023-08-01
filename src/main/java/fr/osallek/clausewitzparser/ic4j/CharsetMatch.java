// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html
/**
 * ******************************************************************************
 * Copyright (C) 2005-2016, International Business Machines Corporation and    *
 * others. All Rights Reserved.                                                *
 * ******************************************************************************
 */
package fr.osallek.clausewitzparser.ic4j;

import java.io.InputStream;
import java.io.InputStreamReader;


/**
 * This class represents a charset that has been identified by a CharsetDetector
 * as a possible encoding for a set of input data.  From an instance of this
 * class, you can ask for a confidence level in the charset identification,
 * or for Java Reader or String to access the original byte data in Unicode form.
 * <p>
 * Instances of this class are created only by CharsetDetectors.
 * <p>
 * Note:  this class has a natural ordering that is inconsistent with equals.
 * The natural ordering is based on the match confidence value.
 *
 * @stable ICU 3.4
 */
public class CharsetMatch implements Comparable<CharsetMatch> {

    /**
     * Get the name of the detected charset.
     * The name will be one that can be used with other APIs on the
     * platform that accept charset names.  It is the "Canonical name"
     * as defined by the class java.nio.charset.Charset; for
     * charsets that are registered with the IANA charset registry,
     * this is the MIME-preferred registerd name.
     *
     * @return The name of the charset.
     *
     * @stable ICU 3.4
     * @see java.nio.charset.Charset
     * @see InputStreamReader
     */
    public String getName() {
        return fCharsetName;
    }

    /**
     * Compare to other CharsetMatch objects.
     * Comparison is based on the match confidence value, which
     * allows CharsetDetector.detectAll() to order its results.
     *
     * @param other the CharsetMatch object to compare against.
     *
     * @return a negative integer, zero, or a positive integer as the
     * confidence level of this CharsetMatch
     * is less than, equal to, or greater than that of
     * the argument.
     *
     * @throws ClassCastException if the argument is not a CharsetMatch.
     * @stable ICU 4.4
     */
    @Override
    public int compareTo(CharsetMatch other) {
        int compareResult = 0;
        if (this.fConfidence > other.fConfidence) {
            compareResult = 1;
        } else if (this.fConfidence < other.fConfidence) {
            compareResult = -1;
        }
        return compareResult;
    }

    /*
     *  Constructor.  Implementation internal
     */
    CharsetMatch(CharsetDetector det, CharsetRecognizer rec, int conf) {
        this.fConfidence = conf;

        // The references to the original application input data must be copied out
        //   of the charset recognizer to here, in case the application resets the
        //   recognizer before using this CharsetMatch.
        if (det.fInputStream == null) {
            // We only want the existing input byte data if it came straight from the user,
            //   not if is just the head of a stream.
            this.fRawInput = det.fRawInput;
            this.fRawLength = det.fRawLength;
        }
        this.fInputStream = det.fInputStream;
        this.fCharsetName = rec.getName();
        this.fLang = rec.getLanguage();
    }

    /*
     *  Constructor.  Implementation internal
     */
    CharsetMatch(CharsetDetector det, int conf, String csName, String lang) {
        this.fConfidence = conf;

        // The references to the original application input data must be copied out
        //   of the charset recognizer to here, in case the application resets the
        //   recognizer before using this CharsetMatch.
        if (det.fInputStream == null) {
            // We only want the existing input byte data if it came straight from the user,
            //   not if is just the head of a stream.
            this.fRawInput = det.fRawInput;
            this.fRawLength = det.fRawLength;
        }
        this.fInputStream = det.fInputStream;
        this.fCharsetName = csName;
        this.fLang = lang;
    }


    //
    //   Private Data
    //
    private int fConfidence;
    private byte[] fRawInput = null;     // Original, untouched input bytes.
    //  If user gave us a byte array, this is it.
    private int fRawLength;           // Length of data in fRawInput array.

    private InputStream fInputStream;  // User's input stream, or null if the user
    //   gave us a byte array.

    private String fCharsetName;         // The name of the charset this CharsetMatch
    //   represents.  Filled in by the recognizer.
    private String fLang;                // The language, if one was determined by
    //   the recognizer during the detect operation.
}

// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html
/**
 * ******************************************************************************
 * Copyright (C) 2005-2016, International Business Machines Corporation and    *
 * others. All Rights Reserved.                                                *
 * ******************************************************************************
 */
package fr.osallek.clausewitzparser.ic4j;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class CharsetDetector {

    public static Charset detect(InputStream in) throws IOException {
        return Charset.forName(new CharsetDetector(in).detect().getName());
    }

    public CharsetDetector() {
    }

    public CharsetDetector(InputStream in) throws IOException {
        setText(in);
    }

    private static final int K_BUF_SIZE = 8000;

    public CharsetDetector setText(InputStream in) throws IOException {
        this.fInputStream = in;
        this.fInputStream.mark(K_BUF_SIZE);
        this.fRawInput = new byte[K_BUF_SIZE];   // Always make a new buffer because the
        //   previous one may have come from the caller,
        //   in which case we can't touch it.
        this.fRawLength = 0;
        int remainingLength = K_BUF_SIZE;
        while (remainingLength > 0) {
            // read() may give data in smallish chunks, esp. for remote sources.  Hence, this loop.
            int bytesRead = this.fInputStream.read(this.fRawInput, this.fRawLength, remainingLength);
            if (bytesRead <= 0) {
                break;
            }
            this.fRawLength += bytesRead;
            remainingLength -= bytesRead;
        }

        this.fInputStream.reset();

        return this;
    }

    public CharsetMatch detect() {
        //   TODO:  A better implementation would be to copy the detect loop from
        //          detectAll(), and cut it short as soon as a match with a high confidence
        //          is found.  This is something to be done later, after things are otherwise
        //          working.
        CharsetMatch matches[] = detectAll();

        if (matches == null || matches.length == 0) {
            return null;
        }

        return matches[0];
    }

    public CharsetMatch[] detectAll() {
        ArrayList<CharsetMatch> matches = new ArrayList<>();

        MungeInput();  // Strip html markup, collect byte stats.

        //  Iterate over all possible charsets, remember all that
        //    give a match quality > 0.
        for (int i = 0; i < ALL_CS_RECOGNIZERS.size(); i++) {
            CSRecognizerInfo rcinfo = ALL_CS_RECOGNIZERS.get(i);
            boolean active = (this.fEnabledRecognizers != null) ? this.fEnabledRecognizers[i] : rcinfo.isDefaultEnabled;

            if (active) {
                CharsetMatch m = rcinfo.recognizer.match(this);

                if (m != null) {
                    matches.add(m);
                }
            }
        }

        Collections.sort(matches);      // CharsetMatch compares on confidence
        Collections.reverse(matches);   //  Put best match first.
        CharsetMatch[] resultArray = new CharsetMatch[matches.size()];
        resultArray = matches.toArray(resultArray);
        return resultArray;
    }

    /*
     *  MungeInput - after getting a set of raw input data to be analyzed, preprocess
     *               it by removing what appears to be html markup.
     */
    private void MungeInput() {
        int srci = 0;
        int dsti = 0;
        byte b;
        boolean inMarkup = false;
        int openTags = 0;
        int badTags = 0;

        //
        //  html / xml markup stripping.
        //     quick and dirty, not 100% accurate, but hopefully good enough, statistically.
        //     discard everything within < brackets >
        //     Count how many total '<' and illegal (nested) '<' occur, so we can make some
        //     guess as to whether the input was actually marked up at all.
        if (fStripTags) {
            for (srci = 0; srci < fRawLength && dsti < fInputBytes.length; srci++) {
                b = fRawInput[srci];
                if (b == (byte) '<') {
                    if (inMarkup) {
                        badTags++;
                    }
                    inMarkup = true;
                    openTags++;
                }

                if (!inMarkup) {
                    fInputBytes[dsti++] = b;
                }

                if (b == (byte) '>') {
                    inMarkup = false;
                }
            }

            fInputLen = dsti;
        }

        //
        //  If it looks like this input wasn't marked up, or if it looks like it's
        //    essentially nothing but markup abandon the markup stripping.
        //    Detection will have to work on the unstripped input.
        //
        if (openTags < 5 || openTags / 5 < badTags || (fInputLen < 100 && fRawLength > 600)) {
            int limit = fRawLength;

            if (limit > K_BUF_SIZE) {
                limit = K_BUF_SIZE;
            }

            for (srci = 0; srci < limit; srci++) {
                fInputBytes[srci] = fRawInput[srci];
            }
            fInputLen = srci;
        }

        //
        // Tally up the byte occurrence statistics.
        //   These are available for use by the various detectors.
        //
        Arrays.fill(fByteStats, (short) 0);
        for (srci = 0; srci < fInputLen; srci++) {
            int val = fInputBytes[srci] & 0x00ff;
            fByteStats[val]++;
        }

        fC1Bytes = false;
        for (int i = 0x80; i <= 0x9F; i += 1) {
            if (fByteStats[i] != 0) {
                fC1Bytes = true;
                break;
            }
        }
    }

    /*
     *  The following items are accessed by individual CharsetRecongizers during
     *     the recognition process
     *
     */ byte[] fInputBytes =       // The text to be checked.  Markup will have been
            new byte[K_BUF_SIZE];  //   removed if appropriate.

    int fInputLen;          // Length of the byte data in fInputBytes.

    short[] fByteStats =      // byte frequency statistics for the input text.
            new short[256];  //   Value is percent, not absolute.
    //   Value is rounded up, so zero really means zero occurrences.

    boolean fC1Bytes =          // True if any bytes in the range 0x80 - 0x9F are in the input;
            false;

    String fDeclaredEncoding;


    byte[] fRawInput;     // Original, untouched input bytes.
    //  If user gave us a byte array, this is it.
    //  If user gave us a stream, it's read to a
    //  buffer here.
    int fRawLength;    // Length of data in fRawInput array.

    InputStream fInputStream;  // User's input stream, or null if the user
    //   gave us a byte array.

    //
    //  Stuff private to CharsetDetector
    //
    private boolean fStripTags =   // If true, setText() will strip tags from input text.
            false;

    private boolean[] fEnabledRecognizers;   // If not null, active set of charset recognizers had
    // been changed from the default. The array index is
    // corresponding to ALL_RECOGNIZER. See setDetectableCharset().

    private static class CSRecognizerInfo {

        CharsetRecognizer recognizer;
        boolean isDefaultEnabled;

        CSRecognizerInfo(CharsetRecognizer recognizer, boolean isDefaultEnabled) {
            this.recognizer = recognizer;
            this.isDefaultEnabled = isDefaultEnabled;
        }
    }

    /*
     * List of recognizers for all charsets known to the implementation.
     */
    private static final List<CSRecognizerInfo> ALL_CS_RECOGNIZERS;

    static {
        ALL_CS_RECOGNIZERS = List.of(
                new CSRecognizerInfo(new CharsetRecog_UTF8(), true),
                new CSRecognizerInfo(new CharsetRecog_Unicode.CharsetRecog_UTF_16_BE(), true),
                new CSRecognizerInfo(new CharsetRecog_Unicode.CharsetRecog_UTF_16_LE(), true),
                new CSRecognizerInfo(new CharsetRecog_Unicode.CharsetRecog_UTF_32_BE(), true),
                new CSRecognizerInfo(new CharsetRecog_Unicode.CharsetRecog_UTF_32_LE(), true),

                new CSRecognizerInfo(new CharsetRecog_mbcs.CharsetRecog_sjis(), true),
                new CSRecognizerInfo(new CharsetRecog_2022.CharsetRecog_2022JP(), true),
                new CSRecognizerInfo(new CharsetRecog_2022.CharsetRecog_2022CN(), true),
                new CSRecognizerInfo(new CharsetRecog_2022.CharsetRecog_2022KR(), true),
                new CSRecognizerInfo(new CharsetRecog_mbcs.CharsetRecog_euc.CharsetRecog_gb_18030(), true),
                new CSRecognizerInfo(new CharsetRecog_mbcs.CharsetRecog_euc.CharsetRecog_euc_jp(), true),
                new CSRecognizerInfo(new CharsetRecog_mbcs.CharsetRecog_euc.CharsetRecog_euc_kr(), true),
                new CSRecognizerInfo(new CharsetRecog_mbcs.CharsetRecog_big5(), true),

                new CSRecognizerInfo(new CharsetRecog_sbcs.CharsetRecog_8859_1(), true),
                new CSRecognizerInfo(new CharsetRecog_sbcs.CharsetRecog_8859_2(), true),
                new CSRecognizerInfo(new CharsetRecog_sbcs.CharsetRecog_8859_5_ru(), true),
                new CSRecognizerInfo(new CharsetRecog_sbcs.CharsetRecog_8859_6_ar(), true),
                new CSRecognizerInfo(new CharsetRecog_sbcs.CharsetRecog_8859_7_el(), true),
                new CSRecognizerInfo(new CharsetRecog_sbcs.CharsetRecog_8859_8_I_he(), true),
                new CSRecognizerInfo(new CharsetRecog_sbcs.CharsetRecog_8859_8_he(), true),
                new CSRecognizerInfo(new CharsetRecog_sbcs.CharsetRecog_windows_1251(), true),
                new CSRecognizerInfo(new CharsetRecog_sbcs.CharsetRecog_windows_1256(), true),
                new CSRecognizerInfo(new CharsetRecog_sbcs.CharsetRecog_KOI8_R(), true),
                new CSRecognizerInfo(new CharsetRecog_sbcs.CharsetRecog_8859_9_tr(), true),

                // IBM 420/424 recognizers are disabled by default
                new CSRecognizerInfo(new CharsetRecog_sbcs.CharsetRecog_IBM424_he_rtl(), false),
                new CSRecognizerInfo(new CharsetRecog_sbcs.CharsetRecog_IBM424_he_ltr(), false),
                new CSRecognizerInfo(new CharsetRecog_sbcs.CharsetRecog_IBM420_ar_rtl(), false),
                new CSRecognizerInfo(new CharsetRecog_sbcs.CharsetRecog_IBM420_ar_ltr(), false));
    }
}

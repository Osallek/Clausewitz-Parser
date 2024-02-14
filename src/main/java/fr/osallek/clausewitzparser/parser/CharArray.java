package fr.osallek.clausewitzparser.parser;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.file.Files;

public class CharArray {

    private final char[] array;

    private final int length;

    private int position;

    private int mark;

    public CharArray(char[] array) {
        this.array = array;
        this.length = array.length;
    }

    public CharArray(File file, Charset charset) throws IOException {
        this(charset.newDecoder().decode(ByteBuffer.wrap(Files.readAllBytes(file.toPath()))).array());
    }

    public CharArray(InputStream inputStream, Charset charset) throws IOException {
        this(charset.newDecoder().decode(ByteBuffer.wrap(inputStream.readAllBytes())).array());
    }

    public void reset() {
        this.position = this.mark;
    }

    public void mark() {
        this.mark = this.position;
    }

    public int position() {
        return this.position;
    }

    public void position(int position) {
        this.position = position;
    }

    public void addPosition(int position) {
        this.position += position;
    }

    public int available() {
        return this.length - this.position;
    }

    public int read() {
        if (this.length == this.position) {
            return -1;
        }

        return this.array[this.position++];
    }

    public void read(char[] c, int offset, int length) {
        if (this.length == this.position) {
            return;
        }

        int n = Math.min(length, this.length - this.position);
        System.arraycopy(this.array, this.position, c, offset, n);
        this.position += n;
    }

    public void read(char[] c) {
        read(c, 0, c.length);
    }

    public char[] read(int length, int offset) {
        char[] buffer = new char[length + offset];

        read(buffer, offset, buffer.length - offset);

        return buffer;
    }

    public String readLine() {
        int nb = 0;
        mark();

        while (true) {
            if (available() <= 0) {
                reset();
                return new String(read(nb, 0));
            }

            char c = (char) read();

            if ((c == '\n') || (c == '\r')) {
                reset();
                String s = new String(read(nb, 0));

                read(); //Read \n or \r

                return s;
            }

            nb++;
        }
    }

    public void skipLine() {
        mark();

        while (true) {
            if (available() <= 0) {
                return;
            }

            char c = (char) read();

            if ((c == '\n') || (c == '\r')) {
                return;
            }
        }
    }

    public String readQuoted(boolean keepQuotes) {
        int letter;
        int nb = 0;
        mark();

        while ((letter = read()) > -1 && '"' != letter) {
            nb++;
        }

        reset();
        char[] chars = new char[nb + (keepQuotes ? 2 : 0)];
        read(chars, keepQuotes ? 1 : 0, chars.length - (keepQuotes ? 2 : 0));
        read(); //Read the trailing "

        if (keepQuotes) {
            chars[0] = '"';
            chars[chars.length - 1] = '"';
        }

        return new String(chars);
    }

    public String readString(int firstChar) {
        int letter;
        int nb = 0;
        mark();

        while ((letter = read()) > -1) {
            if (Character.isWhitespace(letter)) {
                break;
            }

            if ('_' == letter || Character.isLetterOrDigit(letter)) {
                nb++;
                continue;
            }

            break;
        }

        reset();
        char[] chars = read(nb, 1);
        chars[0] = (char) firstChar;
        return new String(chars);
    }

    public String readStringOrNumber(int firstChar) {
        int letter;
        int nb = 0;
        mark();

        while ((letter = read()) > -1) {
            if (Character.isWhitespace(letter)) {
                break;
            }

            if ('\'' == letter || '/' == letter || '.' == letter || '_' == letter || '-' == letter || ':' == letter || Character.isLetterOrDigit(letter)) {
                nb++;
            } else {
                break;
            }
        }

        reset();
        char[] chars = read(nb, 1);
        chars[0] = (char) firstChar;
        return new String(chars);
    }

    public void readEndOfLine() {
        skipTillNext('\n', true);
    }

    public void skipTillNext(int stopChar, boolean stopEndOfLine) {
        int letter;

        while ((letter = read()) > -1) {
            if (stopEndOfLine && '\n' == letter) {
                break;
            }

            if (stopChar == letter) {
                break;
            }
        }

        addPosition(-1);
    }

    public Number readNumber(int... firstChar) {
        int letter;
        boolean isDouble = false;
        int nb = 0;
        mark();


        while ((letter = read()) > -1 && (Character.isDigit(letter) || '.' == letter)) {
            nb++;

            if ('.' == letter) {
                isDouble = true;
            }

        }

        reset();
        char[] chars = read(nb, firstChar.length);
        for (int i = 0; i < firstChar.length; i++) {
            chars[i] = (char) firstChar[i];
        }

        if (isDouble) {
            return Double.parseDouble(new String(chars));
        } else {
            return Integer.parseInt(new String(chars));
        }
    }

    public long length() {
        return length;
    }
}

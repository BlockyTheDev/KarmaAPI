package ml.karmaconfigs.api.common.utils.reader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.Serializable;

/*
 * This file is part of KarmaAPI, licensed under the MIT License.
 *
 *  Copyright (c) karma (KarmaDev) <karmaconfigs@gmail.com>
 *  Copyright (c) contributors
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in all
 *  copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *  SOFTWARE.
 */

/**
 * Read big files, with custom max line length and
 * max bytes per line
 */
public final class BoundedBufferedReader extends BufferedReader implements Serializable {

    private static final int DEFAULT_MAX_LINES = 1024;            //Max lines per file
    private static final int DEFAULT_MAX_LINE_LENGTH = 1024;    //Max bytes per line

    private final int readerMaxLines;
    private final int readerMaxLineLen;
    private int currentLine = 1;

    /**
     * Initialize the bounded buffered reader
     *
     * @param reader the file reader
     * @param maxLines the maximum amount of lines to read
     * @param maxLineLen the max lines length
     */
    public BoundedBufferedReader(Reader reader, int maxLines, int maxLineLen) {
        super(reader);
        if ((maxLines <= 0) || (maxLineLen <= 0))
            throw new IllegalArgumentException("BoundedBufferedReader - maxLines and maxLineLen must be greater than 0");

        readerMaxLines = maxLines;
        readerMaxLineLen = maxLineLen;
    }

    /**
     * Initialize the bounded buffer reader
     *
     * @param reader the file reader
     */
    public BoundedBufferedReader(Reader reader) {
        super(reader);
        readerMaxLines = DEFAULT_MAX_LINES;
        readerMaxLineLen = DEFAULT_MAX_LINE_LENGTH;
    }

    /**
     * Try to read the next line
     *
     * @return the next file line
     * @throws IOException if something goes wrong.
     */
    public final String readLine() throws IOException {
        //Check readerMaxLines limit
        if (currentLine > readerMaxLines)
            throw new IOException("BoundedBufferedReader - Line read limit has been reached.");
        currentLine++;

        int currentPos = 0;
        char[] data = new char[readerMaxLineLen];
        final int CR = 13;
        final int LF = 10;
        int currentCharVal = super.read();

        //Read characters and add them to the data buffer until we hit the end of a line or the end of the file.
        while ((currentCharVal != CR) && (currentCharVal != LF) && (currentCharVal >= 0)) {
            data[currentPos++] = (char) currentCharVal;
            //Check readerMaxLineLen limit
            if (currentPos < readerMaxLineLen)
                currentCharVal = super.read();
            else
                break;
        }

        if (currentCharVal < 0) {
            //End of file
            if (currentPos > 0)
                //Return last line
                return (new String(data, 0, currentPos));
            else
                return null;
        } else {
            //Remove newline characters from the buffer
            if (currentCharVal == CR) {
                //Check for LF and remove from buffer
                super.mark(1);
                if (super.read() != LF)
                    super.reset();
            } else if (currentCharVal != LF) {
                //readerMaxLineLen has been hit, but we still need to remove newline characters.
                super.mark(1);
                int nextCharVal = super.read();
                if (nextCharVal == CR) {
                    super.mark(1);
                    if (super.read() != LF)
                        super.reset();
                } else if (nextCharVal != LF)
                    super.reset();
            }
            return (new String(data, 0, currentPos));
        }
    }
}
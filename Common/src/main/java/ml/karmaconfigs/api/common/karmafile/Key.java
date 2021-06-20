package ml.karmaconfigs.api.common.karmafile;

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
 * KarmaFile key object
 *
 * This object contains the value, in case
 * of no value, the key will be used as value
 */
public final class Key implements Serializable {

    private final String path;
    private final Object value;

    /**
     * Initialize the file value
     *
     * @param keyPath   the key path
     * @param keyValue  the key value
     */
    public Key(final String keyPath, final Object keyValue) {
        path = keyPath;
        value = keyValue;
    }

    /**
     * Get the key
     *
     * @return the key
     */
    public final String getPath() {
        return path;
    }

    /**
     * Get the value
     *
     * @return the value
     */
    public final Object getValue() {
        return value;
    }
}

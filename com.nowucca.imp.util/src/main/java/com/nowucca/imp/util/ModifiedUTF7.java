/**
 * Copyright (c) 2012-2014, Steven Atkinson. All rights reserved.
 */
package com.nowucca.imp.util;

import com.beetstra.jutf7.CharsetProvider;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

/**
 */
public final class ModifiedUTF7 {

    private static final String X_MODIFIED_UTF_7 = "X-MODIFIED-UTF-7";
    private static final Charset X_MODIFIED_UTF_7_CHARSET = new CharsetProvider().charsetForName(X_MODIFIED_UTF_7);

    /**
     * Decode the given UTF7 encoded <code>String</code>
     *
     */
    public static String decode(String string) {
        return X_MODIFIED_UTF_7_CHARSET.decode(ByteBuffer.wrap(string.getBytes())).toString();

    }


    /**
     * Encode the given <code>String</code> to modified UTF7.
     * See RFC3501 for more details
     *
     */
    public static String encode(String string) {
        final ByteBuffer encode = X_MODIFIED_UTF_7_CHARSET.encode(string);
        return new String(encode.array(), 0, encode.remaining());

    }

    private ModifiedUTF7() {
    }


}

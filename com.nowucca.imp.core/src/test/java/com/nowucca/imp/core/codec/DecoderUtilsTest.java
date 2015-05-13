/**
 * Copyright (c) 2012-2014, Steven Atkinson. All rights reserved.
 */
package com.nowucca.imp.core.codec;

import com.nowucca.imp.util.UTF8;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import java.util.Base64;
import org.junit.Test;
import static org.junit.Assert.*;

public class DecoderUtilsTest {

    @Test
    public void shouldReadQuotedBuffers() throws Exception {
        final ByteBuf in = Unpooled.wrappedBuffer("\"This is a \\\"quote\\\" ok\"".getBytes(UTF8.charset()));
        assertEquals("This is a \"quote\" ok", DecoderUtils.readQuoted(in).toString());
    }

    @Test
    public void shouldReadBase64Text() throws Exception {
        final ByteBuf in = Unpooled.wrappedBuffer("aGVsbG8=".getBytes(UTF8.charset()));
        final CharSequence base64Encoded = DecoderUtils.readBase64(in);
        assertEquals("aGVsbG8=", base64Encoded.toString());
        assertEquals("hello", new String(Base64.getDecoder().decode(base64Encoded.toString())));
    }
}

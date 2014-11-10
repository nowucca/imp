/**
 * Copyright (c) 2012-2014, Steven Atkinson. All rights reserved.
 */
package com.nowucca.imp.core.codec;

import io.netty.handler.codec.TooLongFrameException;
import org.junit.Test;

public class ImapRequestDecoderTest extends BaseDecoderTest {


    @Test
    public void shouldSeeTooLongFrameExceptionWhenTagTooLong() throws Exception {
        final StringBuilder longTag = new StringBuilder(ImapCodecConstants.MAXIMUM_TAG_LENGTH + 1);
        for (int i = 0; i < ImapCodecConstants.MAXIMUM_TAG_LENGTH + 1; i++) {
            longTag.append('A');
        }
        writeToChannel(longTag.toString() + "  NOOP\r\n");
        expectInvalidRequest(TooLongFrameException.class);
    }

    @Test
    public void shouldSeeUnsupportedOperationExceptionWhenPresentingXtendedCommand() throws Exception {
        writeToChannel("A005 XCOMMAND\r\n");
        expectInvalidRequest(UnsupportedOperationException.class);
    }


    @Test
    public void shouldSeeIllegalArgumentExceptionWhenPresentingUnexpectedCommandWithValidPrefix() throws Exception {
        writeToChannel("A006 NOTNOOP\r\n");
        expectInvalidRequest(IllegalArgumentException.class);
    }

    @Test
    public void shouldSeeIllegalArgumentExceptionWhenPresentingUnexpectedNonCRLFTerminator() throws Exception {
        writeToChannel("A006 NOOPZZ");
        expectInvalidRequest(IllegalArgumentException.class);
    }


}

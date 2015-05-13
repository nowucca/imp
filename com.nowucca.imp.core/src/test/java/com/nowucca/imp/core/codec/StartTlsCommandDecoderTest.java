/**
 * Copyright (c) 2012-2014, Steven Atkinson. All rights reserved.
 */
package com.nowucca.imp.core.codec;

import com.nowucca.imp.core.message.command.ImapRequest;
import org.junit.Test;
import static org.junit.Assert.assertNull;

public class StartTlsCommandDecoderTest extends BaseDecoderTest {

    @Test
    public void shouldNotProduceResultWithUnterminatedCommandString() throws Exception {
        writeToChannel("A001 STARTTLS");
        final ImapRequest imapRequest = (ImapRequest) channel.readInbound();
        assertNull(imapRequest);
    }


    @Test
    public void shouldParseTaggedCommand() throws Exception {
        writeToChannel("A001 STARTTLS\r\n");
        expectSuccessfulRequest("A001", "STARTTLS");
    }

    @Test
    public void shouldParseFragmentedCommandName() throws Exception {
        writeToChannel("A006 START");
        writeToChannel("TLS\r\n");
        expectSuccessfulRequest("A006", "STARTTLS");
    }

    @Test
    public void shouldSeeIllegalArgumentExceptionWhenPresentingUnexpectedNonCRLFTerminator() throws Exception {
        writeToChannel("A006 STARTTLSZZ");
        expectInvalidRequest(IllegalArgumentException.class);
    }



}

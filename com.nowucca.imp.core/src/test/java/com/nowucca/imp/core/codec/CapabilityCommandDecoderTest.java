/**
 * Copyright (c) 2012-2014, Steven Atkinson. All rights reserved.
 */
package com.nowucca.imp.core.codec;

import org.junit.Test;

public class CapabilityCommandDecoderTest extends BaseDecoderTest {

    @Test
    public void shouldParseTaggedCapabilityCommand() throws Exception {
        writeToChannel("A002 CAPABILITY\r\n");
        expectSuccessfulRequest("A002", "CAPABILITY");
    }

    @Test
    public void shouldParseTaggedCapabilityCommandCaseInsensitive() throws Exception {
        writeToChannel("A002 CAPABILITY\r\n");
        expectSuccessfulRequest("A002", "CaPaBiLiTy");
    }

    @Test
    public void shouldParseFragmentedCommandName() throws Exception {
        writeToChannel("A006 CAPA");
        writeToChannel("BILITY\r\n");
        expectSuccessfulRequest("A006", "CAPABILITY");
    }

}

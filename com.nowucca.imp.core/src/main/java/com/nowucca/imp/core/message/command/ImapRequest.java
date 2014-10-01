/**
 * Copyright (c) 2012-2014, Steven Atkinson. All rights reserved.
 */
package com.nowucca.imp.core.message.command;

import com.nowucca.imp.core.DecoderResultProvider;

/**
 * The IMAP request with a tag and a command.
 */
public interface ImapRequest extends DecoderResultProvider {

    ImapCommand getCommand();

    String getTag();

}

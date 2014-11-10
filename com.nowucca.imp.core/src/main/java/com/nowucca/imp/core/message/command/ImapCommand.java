/**
 * Copyright (c) 2012-2014, Steven Atkinson. All rights reserved.
 */
package com.nowucca.imp.core.message.command;

import java.util.List;

/**
 */
public interface ImapCommand {

    String getCommandName();

    Kind getKind();

    List<?> getArguments();

    public enum Kind {
        APPEND,
        AUTHENTICATE,
        CAPABILITY,
        LOGIN,
        LOGOUT,
        NOOP,
        SELECT,
        INVALID, STARTTLS
    }
}

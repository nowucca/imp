/**
 * Copyright (c) 2012-2014, Steven Atkinson. All rights reserved.
 */
package com.nowucca.imp.core.message.command;

import java.util.List;

/**
 */
public class InvalidImapCommand implements ImapCommand {

    private Exception cause;

    public InvalidImapCommand() {
    }

    public InvalidImapCommand(Exception cause) {
        this.cause = cause;
    }

    public Exception getCause() {
        return cause;
    }

    @Override
    public Kind getKind() {
        return Kind.INVALID;
    }

    @Override
    public String getCommandName() {
        return "INVALID_COMMAND";
    }

    @Override
    public List<String> getArguments() {
        return null;
    }
}

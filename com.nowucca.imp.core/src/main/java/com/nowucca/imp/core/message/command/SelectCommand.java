/**
 * Copyright (c) 2012-2014, Steven Atkinson. All rights reserved.
 */
package com.nowucca.imp.core.message.command;

import java.util.Arrays;
import java.util.List;

/**
 */
public class SelectCommand implements ImapCommand {
    private String mailboxName;

    public SelectCommand(String mailboxName) {
        this.mailboxName = mailboxName;
    }

    @Override
    public Kind getKind() {
        return Kind.SELECT;
    }

    @Override
    public String getCommandName() {
        return "SELECT";
    }

    @Override
    public List<?> getArguments() {
        return Arrays.asList(mailboxName);
    }

    public String getMailboxName() {
        return mailboxName;
    }
}

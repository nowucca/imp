/**
 * Copyright (c) 2012-2014, Steven Atkinson. All rights reserved.
 */
package com.nowucca.imp.core.message.command;

import io.netty.buffer.ByteBuf;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import javax.mail.Flags;

/**
 */
public class AppendCommand implements ImapCommand  {

    private String mailboxName;
    private Flags flags;
    private Date dateTime;
    private ByteBuf messageLiteral;

    public AppendCommand(String mailboxName, Flags flags, Date dateTime, ByteBuf messageLiteral) {
        this.mailboxName = mailboxName;
        this.flags = flags;
        this.dateTime = dateTime;
        this.messageLiteral = messageLiteral;
    }

    @Override
    public String getCommandName() {
        return "APPEND";
    }

    @Override
    public List<?> getArguments() {
        return Arrays.asList(mailboxName, flags, dateTime, messageLiteral);
    }
}

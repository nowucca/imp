/**
 * Copyright (c) 2012-2014, Steven Atkinson. All rights reserved.
 */
package com.nowucca.imp.core.message.command;

import io.netty.handler.codec.DecoderResult;

/**
 */
public class InvalidImapRequest implements ImapRequest {

    public static final String INVALID_TAG = "INVALID_TAG";

    private String tag;
    private ImapCommand command;
    private DecoderResult decoderResult;

    public InvalidImapRequest() {
        this.tag = INVALID_TAG;
        this.command = new InvalidImapCommand();
    }

    public InvalidImapRequest(Exception cause) {
        this.tag = INVALID_TAG;
        this.command = new InvalidImapCommand();
    }

    public InvalidImapRequest(String tag, Exception cause) {
        this.tag = tag;
        this.command = new InvalidImapCommand(cause);
    }

    @Override
    public ImapCommand getCommand() {
        return command;
    }

    @Override
    public String getTag() {
        return tag;
    }

    @Override
    public void setDecoderResult(DecoderResult result) {
        this.decoderResult = result;
    }

    @Override
    public DecoderResult getDecoderResult() {
        return decoderResult;
    }

    @Override
    public String toString() {
        return "InvalidImapRequest{" +
                "tag='" + tag + '\'' +
                ", command=" + command +
                ", decoderResult=" + decoderResult +
                '}';
    }
}

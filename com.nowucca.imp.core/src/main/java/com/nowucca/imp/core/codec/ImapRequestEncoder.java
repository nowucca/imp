/**
 * Copyright (c) 2012-2014, Steven Atkinson. All rights reserved.
 */
package com.nowucca.imp.core.codec;

import com.nowucca.imp.core.message.command.ImapRequest;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 */
public class ImapRequestEncoder extends MessageToByteEncoder<ImapRequest> {

    @Override
    protected void encode(ChannelHandlerContext ctx, ImapRequest msg, ByteBuf out) throws Exception {
        if (msg != null) {
            //TODO
        }
    }
}

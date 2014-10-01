/**
 * Copyright (c) 2012-2014, Steven Atkinson. All rights reserved.
 */
package com.nowucca.imp.core.codec;

import com.nowucca.imp.core.message.command.CapabilityCommand;
import com.nowucca.imp.core.message.command.ImapCommand;
import com.nowucca.imp.core.message.command.ImapRequest;
import com.nowucca.imp.core.message.command.InvalidImapRequest;
import com.nowucca.imp.core.message.command.LogoutCommand;
import com.nowucca.imp.core.message.command.NoopCommand;
import com.nowucca.imp.util.UTF8;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufProcessor;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.DecoderResult;
import io.netty.handler.codec.ReplayingDecoder;
import io.netty.handler.codec.TooLongFrameException;
import io.netty.util.internal.AppendableCharSequence;
import java.util.List;
import static com.nowucca.imp.core.codec.ImapCodecConstants.MAXIMUM_TAG_LENGTH;
import static com.nowucca.imp.core.codec.ImapRequestDecoder.State.READ_COMMAND_NAME;
import static java.lang.String.format;

/**
 */
public class ImapRequestDecoder extends ReplayingDecoder<ImapRequestDecoder.State> {

    public static enum State {
        READ_TAG,
        READ_COMMAND_NAME,
        READ_COMMAND_DATA,
        READ_CRLF,
        INVALID_REQUEST
    }

    AppendableCharSequence tag;
    ImapCommand imapCommand;
    ImapRequest imapRequest;

    private final AppendableCharSequence seq = new AppendableCharSequence(128);
    private final TagParser tagParser = new TagParser(seq);



    public ImapRequestDecoder() {
        super(State.READ_TAG);
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        switch (state()) {
            case READ_TAG:
                try {
                    tag = tagParser.parse(in);
                    checkpoint(READ_COMMAND_NAME);

                } catch (Exception e) {
                    out.add(invalidRequest(e));
                    return;
                }

            case READ_COMMAND_NAME: {
                try {
                    final char c = (char) in.readByte();
                    switch(c) {
                        case 'C': {
                            readExpectedCommandName(in, "CAPABILITY");
                            imapCommand = new CapabilityCommand();
                            checkpoint(State.READ_CRLF);
                            return;
                        }
                        case 'L': {
                            readExpectedCommandName(in, "LOGOUT");
                            imapCommand = new LogoutCommand();
                            checkpoint(State.READ_CRLF);
                            return;
                        }
                        case 'N': {
                            readExpectedCommandName(in, "NOOP");
                            imapCommand = new NoopCommand();
                            checkpoint(State.READ_CRLF);
                            return;
                        }
                        case 'X': {
                            throw new UnsupportedOperationException(format("No extension commands are supported."));
                        }
                    }
                } catch (Exception e) {
                    out.add(invalidRequest(e));
                    return;
                }
            }

            case READ_COMMAND_DATA: {
            }

            case READ_CRLF: {
                try {
                    final byte b = in.readByte();
                    final byte c = in.readByte();
                    if (b != '\r' || c != '\n') {
                        throw new IllegalArgumentException(format("Expected CRLF, received %s.",
                                new String(new byte[]{b, c})));
                    }
                    out.add(new ImapRequest() {
                        @Override
                        public ImapCommand getCommand() {
                            return imapCommand;
                        }

                        @Override
                        public String getTag() {
                            return tag.toString();
                        }

                        @Override
                        public void setDecoderResult(DecoderResult result) {
                        }

                        @Override
                        public DecoderResult getDecoderResult() {
                            return DecoderResult.SUCCESS;
                        }
                    });
                    checkpoint(State.READ_TAG);
                    return;
                } catch (RuntimeException e) {
                    out.add(invalidRequest(e));
                    return;
                }
            }

            case INVALID_REQUEST: {
                // chew up any extra bytes after a bad request read has been attempted.
                in.skipBytes(actualReadableBytes());
                break;
            }
        }
    }

    private void readExpectedCommandName(ByteBuf in, String expectedCommandName) {
        final int expectedLength = expectedCommandName.length() - 1;

        final String commandName = expectedCommandName.charAt(0) +
                in.readBytes(expectedLength).toString(UTF8.charset());

        if (!expectedCommandName.equals(commandName)) {
            throw new IllegalArgumentException(format("Unknown command name '%s'", commandName));
        }
    }

    private ImapRequest invalidRequest(Exception cause) {
        checkpoint(State.INVALID_REQUEST);
        imapRequest = new InvalidImapRequest(cause);
        imapRequest.setDecoderResult(DecoderResult.failure(cause));
        final ImapRequest ret = imapRequest;
        imapRequest = null;
        return ret;
    }

    private final class TagParser implements ByteBufProcessor {
        private final AppendableCharSequence seq;
        private int size;

        TagParser(AppendableCharSequence seq) {
            this.seq = seq;
        }

        public AppendableCharSequence parse(ByteBuf buffer) {
            seq.reset();
            size = 0;
            final int i = buffer.forEachByte(this);
            buffer.readerIndex(i + 1);
            return seq;
        }


        @Override
        public boolean process(byte value) throws Exception {
            final char nextByte = (char) value;
            if (nextByte == ' ') {
                return false;
            } else {
                if (size >= MAXIMUM_TAG_LENGTH) {
                    throw new TooLongFrameException(
                            format("An IMAP tag is larger than %d bytes.", MAXIMUM_TAG_LENGTH));
                }
                size++;
                seq.append(nextByte);
                return true;
            }
        }
    }

}

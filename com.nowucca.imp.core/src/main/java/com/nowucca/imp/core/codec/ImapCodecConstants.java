/**
 * Copyright (c) 2012-2014, Steven Atkinson. All rights reserved.
 */
package com.nowucca.imp.core.codec;

import io.netty.util.internal.AppendableCharSequence;

/**
 */
public final class ImapCodecConstants {

    public static final int MAXIMUM_COMMAND_LENGTH = 8192;
    public static final String INBOX = "INBOX";

    private ImapCodecConstants() {
    }

    public static final int MAXIMUM_TAG_LENGTH = 100;

    public static final int DEFAULT_MAXIMUM_SIZE = 8192;

    public static final String ANSWERED_ALL_CAPS = "\\ANSWERED";

    public static final String DELETED_ALL_CAPS = "\\DELETED";

    public static final String DRAFT_ALL_CAPS = "\\DRAFT";

    public static final String FLAGGED_ALL_CAPS = "\\FLAGGED";

    public static final String SEEN_ALL_CAPS = "\\SEEN";

    public static final String RECENT_ALL_CAPS = "\\RECENT";
}

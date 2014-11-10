/**
 * Copyright (c) 2012-2014, Steven Atkinson. All rights reserved.
 */
package com.nowucca.imp.core.codec;

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

    public static final byte[] ANSWERED_ALL_CAPS_BYTES = ANSWERED_ALL_CAPS.getBytes();

    public static final String DELETED_ALL_CAPS = "\\DELETED";
    public static final byte[] DELETED_ALL_CAPS_BYTES = DELETED_ALL_CAPS.getBytes();


    public static final String DRAFT_ALL_CAPS = "\\DRAFT";
    public static final byte[] DRAFT_ALL_CAPS_BYTES = DRAFT_ALL_CAPS.getBytes();


    public static final String FLAGGED_ALL_CAPS = "\\FLAGGED";
    public static final byte[] FLAGGED_ALL_CAPS_BYTES = FLAGGED_ALL_CAPS.getBytes();


    public static final String SEEN_ALL_CAPS = "\\SEEN";
    public static final byte[] SEEN_ALL_CAPS_BYTES = SEEN_ALL_CAPS.getBytes();


    public static final String RECENT_ALL_CAPS = "\\RECENT";
    public static final byte[] RECENT_ALL_CAPS_BYTES = RECENT_ALL_CAPS.getBytes();

}

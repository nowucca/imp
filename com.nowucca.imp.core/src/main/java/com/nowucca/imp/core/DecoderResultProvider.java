/**
 * Copyright (c) 2012-2014, Steven Atkinson. All rights reserved.
 */
package com.nowucca.imp.core;

import io.netty.handler.codec.DecoderResult;

/**
 */
public interface DecoderResultProvider {

    void setDecoderResult(DecoderResult result);

    DecoderResult getDecoderResult();
}

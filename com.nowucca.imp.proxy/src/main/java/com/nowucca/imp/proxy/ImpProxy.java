/**
 * Copyright (c) 2012-2014, Steven Atkinson. All rights reserved.
 */
package com.nowucca.imp.proxy;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

public class ImpProxy {

    static final int LOCAL_PORT = Integer.parseInt(System.getProperty("localPort", "8080"));
    static final String REMOTE_HOST = System.getProperty("remoteHost", "www.cnn.com");
    static final int REMOTE_PORT = Integer.parseInt(System.getProperty("remotePort", "80"));

    public static void main(String[] args) throws Exception {
        new ImpProxy().run();
    }

    public ImpProxy() {
    }

    public void run() throws Exception {
        final long start = System.currentTimeMillis();

        printWelcomeMessage();

        // Configure the bootstrap.
        final EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        final EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            final ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(new ImpProxyInitializer(REMOTE_HOST, REMOTE_PORT))
                    .childOption(ChannelOption.AUTO_READ, false);

            System.out.format("Started in %3.3f seconds.\n", (System.currentTimeMillis() - start) / 1000f);

            b.bind(LOCAL_PORT).sync().channel().closeFuture().sync();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    private void printWelcomeMessage() {
        System.out.format("Imp Server (c) 2014 Steven Atkinson.  All Rights Reserved.\n\n");
        System.out.println("Proxying *:" + LOCAL_PORT + " to " + REMOTE_HOST + ':' + REMOTE_PORT + " ...");
        System.out.println();
    }
}

package com.netty.barrage.service;

import javax.annotation.PostConstruct;

import com.netty.barrage.Initializer.NettyServerInitializer;
import io.netty.channel.ChannelFuture;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.ImmediateEventExecutor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import java.net.InetSocketAddress;

@Service
public class NettyServer {
    @Value("${netty.port}")
    private int port;
    //创建 DefaultChannelGroup，其将保存所有已经连接的 WebSocket Channel
    private final ChannelGroup channelGroup = new DefaultChannelGroup(ImmediateEventExecutor.INSTANCE);
    private final EventLoopGroup parentGroup = new NioEventLoopGroup();
    private final EventLoopGroup childGroup = new NioEventLoopGroup();
    private Channel channel;

    @PostConstruct
    public void initNetty() {
        new Thread() {
            public void run() {
                final NettyServer nettyServer = new NettyServer();
                ChannelFuture future = nettyServer.start(new InetSocketAddress(port));
                Runtime.getRuntime().addShutdownHook(new Thread() {
                    @Override
                    public void run() {
                        nettyServer.destroy();
                    }
                });
                future.channel().closeFuture().syncUninterruptibly();
            }
        }.start();
    }

    public ChannelFuture start(InetSocketAddress address) {
        //引导服务器
        ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap.group(parentGroup, childGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(new NettyServerInitializer(channelGroup));
        ChannelFuture future = bootstrap.bind(address).syncUninterruptibly();
        channel = future.channel();
        return future;
    }

    //处理服务器关闭，并释放所有的资源
    public void destroy() {
        if (channel != null) {
            channel.close();
        }
        channelGroup.close();
        parentGroup.shutdownGracefully();
        childGroup.shutdownGracefully();
    }
}

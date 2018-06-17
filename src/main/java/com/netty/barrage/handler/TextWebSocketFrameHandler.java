package com.netty.barrage.handler;

import com.netty.barrage.util.BusinessThreadUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;

public class TextWebSocketFrameHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {
    private final ChannelGroup group;

    public TextWebSocketFrameHandler(ChannelGroup group) {
        this.group = group;
    }

    /**
     * 当客户端主动链接服务端的链接后，这个通道就是活跃的了
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
    }

    /**
     * 当客户端主动断开服务端的链接后，这个通道就是不活跃的
     */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        // 通道关闭时，自动移除，此处代码多余
        //group.remove(ctx.channel());
        //System.out.println("客户端与服务端连接关闭：" + ctx.channel().remoteAddress().toString());
    }

    //重写 userEventTriggered()方法以处理自定义事件
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        //如果该事件表示握手成功，则从该 ChannelPipeline 中移除HttpRequest-Handler，因为将不会接收到任何HTTP消息了
        if (evt == WebSocketServerProtocolHandler.ServerHandshakeStateEvent.HANDSHAKE_COMPLETE) {
            ctx.pipeline().remove(HttpRequestHandler.class);
            //通知所有已经连接的 WebSocket 客户端新的客户端已经连接上了
            //group.writeAndFlush(new TextWebSocketFrame("用户" + ctx.channel() + "上线！"));
            //将新的 WebSocket Channel 添加到 ChannelGroup 中，以便它可以接收到所有的消息
            group.add(ctx.channel());
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame msg) throws Exception {
        //增加消息的引用计数，并将它写到 ChannelGroup 中所有已经连接的客户端
        group.writeAndFlush(msg.retain());

        ByteBuf bf =msg.content();
        byte[] byteArray = new byte[bf.capacity()];
        bf.readBytes(byteArray);
        BusinessThreadUtil.doPersistenceBusiness(new String(byteArray));
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}

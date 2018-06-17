package com.netty.barrage.handler;

import com.netty.barrage.util.BusinessThreadUtil;
import com.netty.barrage.util.RequestValidateUtil;
import io.netty.channel.*;
import io.netty.handler.codec.http.*;

import java.net.URL;

public class HttpRequestHandler extends SimpleChannelInboundHandler<FullHttpRequest> {
    private final String wsUri;
    private static final URL location = HttpRequestHandler.class.getProtectionDomain().getCodeSource().getLocation();

    public HttpRequestHandler(String wsUri) {
        this.wsUri = wsUri;
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request) throws Exception {
        String staticResourcePath = location.toURI().toString();
        staticResourcePath = !staticResourcePath.contains("file:") ? staticResourcePath : staticResourcePath.substring(5);
        String MIMEtype = null;
        //如果请求了 WebSocket 协议升级，则增加引用计数，并将它传递给下一 个 ChannelInboundHandler
        if (wsUri.equalsIgnoreCase(request.uri())) {
            ctx.fireChannelRead(request.retain());
            return;
        } else if (request.uri().equals("/") && RequestValidateUtil.isGetRequest(request)) {
            //访问主页
            staticResourcePath += "static/index.html";
            MIMEtype = "text/html; charset=UTF-8";
        } else if ("/login".equalsIgnoreCase(request.uri()) && RequestValidateUtil.isPostJsonRequest(request)) {
            //通过异步线程池来进行验证登录
            BusinessThreadUtil.doLoginValidateBusiness(ctx, request);
            return;
        } else {
            //请求静态资源文件css,js,image,gif等
            String typeTail = request.uri().substring(request.uri().lastIndexOf('.') + 1);
            staticResourcePath += request.uri();
            switch (typeTail) {
                case "css":
                    MIMEtype = "text/css; charset=UTF-8";
                    break;
                case "js":
                    MIMEtype = "text/javascript; charset=UTF-8";
                    break;
                case "png":
                    MIMEtype = "image/png; charset=UTF-8";
                    break;
                case "jpg":
                    MIMEtype = "image/jpeg; charset=UTF-8";
                    break;
                case "gif":
                    MIMEtype = "image/gif; charset=UTF-8";
                    break;
            }

            //如果没有匹配的类型则忽略
            if (MIMEtype == null) {
                return;
            }
        }

        //通过异步线程池来输出静态资源
        BusinessThreadUtil.doResponseHtmlBusiness(ctx, request, MIMEtype, staticResourcePath);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }


}




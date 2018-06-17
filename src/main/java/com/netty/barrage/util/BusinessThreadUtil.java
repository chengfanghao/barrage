package com.netty.barrage.util;

import com.netty.barrage.config.SpringContextAware;
import com.netty.barrage.domain.Barrage;
import com.netty.barrage.domain.User;
import com.netty.barrage.repository.BarrageRepository;
import com.netty.barrage.repository.UserRepository;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.DefaultFileRegion;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


public class BusinessThreadUtil {

    private static final ExecutorService responseExecutor = new ThreadPoolExecutor(2, 4, 1000L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>(100000));
    private static final ExecutorService persistenceExecutor = new ThreadPoolExecutor(1, 1, 1000L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>(100000));

    private static final UserRepository userRepository = (UserRepository) SpringContextAware.getBean("userRepository");
    private static final BarrageRepository barrageRepository = (BarrageRepository) SpringContextAware.getBean("barrageRepository");

    public static void doResponseHtmlBusiness(ChannelHandlerContext ctx, FullHttpRequest request, String MIMEType, String staticResourcePath) {
        //异步线程池处理
        responseExecutor.submit(new Runnable() {
            @Override
            public void run() {
                httpStaticResourceHandler(ctx, request, MIMEType, staticResourcePath);
            }
        });
    }

    public static void doPersistenceBusiness(String barrageContent) {
        //异步线程池处理
        persistenceExecutor.submit(new Runnable() {
            @Override
            public void run() {
                Barrage barrage = new Barrage();
                barrage.setTime(new Date());
                barrage.setContent(barrageContent);
                barrageRepository.save(barrage);
            }
        });
    }

    public static void doLoginValidateBusiness(ChannelHandlerContext ctx, FullHttpRequest request) throws Exception {
        //处理登录请求
        HttpHeaders httpHeaders = request.headers();
        ByteBuf byteBuf = request.content();
        String jsonStr = byteBuf.toString(CharsetUtil.UTF_8);
        User user = (User) JsonUtil.getObj(jsonStr, User.class);
        User dbUser = null;
        if (userRepository.findById(user.getId()).isPresent()) {
            dbUser = userRepository.findById(user.getId()).get();
        }
        String responseText = "";

        if (dbUser == null || !dbUser.getPassword().equals(user.getPassword())) {
            responseText = "fail";
        } else {
            responseText = "success";
        }
        httpResponseTextHandler(ctx, request, responseText);
    }

    private static void httpStaticResourceHandler(ChannelHandlerContext ctx, FullHttpRequest request, String MIMEType, String staticResourcePath) {
        //读取静态资源文件
        File index = new File(staticResourcePath);
        RandomAccessFile file = null;

        try {
            file = new RandomAccessFile(index, "r");
            HttpResponse response = new DefaultHttpResponse(request.protocolVersion(), HttpResponseStatus.OK);
            response.headers().set(HttpHeaderNames.CONTENT_TYPE, MIMEType);

            //将 HttpResponse 写到客户端
            ctx.write(response);
            //将静态资源文件写到客户端
            ctx.write(new DefaultFileRegion(file.getChannel(), 0, file.length()));
            //写 LastHttpContent 并冲刷至客户端
            ChannelFuture future = ctx.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT);
            //在写操作完成后关闭 Channel
            future.addListener(ChannelFutureListener.CLOSE);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void httpResponseTextHandler(ChannelHandlerContext ctx, FullHttpRequest request, String responseText) {
        HttpResponse response = new DefaultFullHttpResponse(request.protocolVersion(), HttpResponseStatus.OK, Unpooled.copiedBuffer(responseText, CharsetUtil.UTF_8));
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/plain; charset=UTF-8");
        //写 LastHttpContent 并冲刷至客户端
        ChannelFuture future = ctx.writeAndFlush(response);
        //在写操作完成后关闭 Channel
        future.addListener(ChannelFutureListener.CLOSE);
    }
}

package com.netty.barrage.util;

import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpMethod;

public class RequestValidateUtil {
    public static boolean isPostJsonRequest(FullHttpRequest request) {
        if (HttpMethod.POST == request.method() && request.headers().get("Content-Type").equals("application/json")) {
            return true;
        }
        return false;
    }

    public static boolean isGetRequest(FullHttpRequest request) {
        if (HttpMethod.GET == request.method()) {
            return true;
        }

        return false;
    }
}

package com.netty.barrage.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.text.SimpleDateFormat;

public class JsonUtil {

    /**
     * 将Java对象转化为JSON字符串
     *
     * @param obj
     * @return
     * @throws IOException
     */
    public static String getJSON(Object obj) throws IOException {
        if (null == obj) {
            return "";
        }
        ObjectMapper mapper = new ObjectMapper();
        mapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
        String jsonStr = mapper.writeValueAsString(obj);
        return jsonStr;
    }

    /**
     * 将JSON字符串转化为Java对象
     *
     * @return
     * @throws IOException
     */
    @SuppressWarnings("unchecked")
    public static <T> T getObj(String json, TypeReference<T> ref) throws IOException {
        if (null == json || json.length() == 0) {
            return null;
        }
        ObjectMapper mapper = new ObjectMapper();
        mapper.getDeserializationConfig().with(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
        return (T) mapper.readValue(json, ref);
    }

    public static Object getObj(String json, Class pojoClass) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(json, pojoClass);
    }
}

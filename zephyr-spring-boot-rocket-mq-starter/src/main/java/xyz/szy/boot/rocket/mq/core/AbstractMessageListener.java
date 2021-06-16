package xyz.szy.boot.rocket.mq.core;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.common.message.MessageExt;
import java.lang.reflect.Method;

@Slf4j
public class AbstractMessageListener {
    private static final Gson gson = new Gson();
    private final Class<?> messageType;
    private final Object bean;
    private final Method method;

    AbstractMessageListener(Class<?> messageType, Object bean, Method method) {
        this.messageType = messageType;
        this.bean = bean;
        this.method = method;
    }

    private Object parseMessage(MessageExt msg) {
        if (msg != null && msg.getBody() != null) {
            try {
                return gson.fromJson(new String(msg.getBody()), this.messageType);
            } catch (JsonSyntaxException e) {
                log.error("parse message json fail : {}", e.getMessage());
                return null;
            }
        } else {
            log.warn("message is empty.");
            return null;
        }
    }

    boolean process(MessageExt msg) {
        try {
            Object body = this.parseMessage(msg);
            this.method.invoke(this.bean, body);
            return Boolean.TRUE;
        } catch (Exception e) {
            e.printStackTrace();
            log.warn("consume fail , ask for re-consume , msgId: {}", msg.getMsgId());
            return Boolean.FALSE;
        }
    }
}

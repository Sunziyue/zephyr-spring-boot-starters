package xyz.szy.boot.rocket.mq.exception;

public class MessageSendException extends RuntimeException {
    public MessageSendException(String topic, Exception e) {
        super("消息发送失败，topic :" + topic + ",e:" + e.getMessage());
    }
}

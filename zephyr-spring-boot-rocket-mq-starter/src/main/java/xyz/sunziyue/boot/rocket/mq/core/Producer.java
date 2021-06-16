package xyz.sunziyue.boot.rocket.mq.core;

import com.google.gson.Gson;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.message.Message;
import org.springframework.beans.factory.annotation.Autowired;
import xyz.sunziyue.boot.rocket.mq.exception.MessageSendException;
import xyz.sunziyue.boot.rocket.mq.properties.RocketMQProperties;

public class Producer {
    private static final Gson gson = new Gson();
    private final DefaultMQProducer producer;

    @Autowired
    public Producer(RocketMQProperties properties) throws MQClientException {
        this.producer = new DefaultMQProducer(properties.getProducerGroup());
        this.producer.setNamesrvAddr(properties.getNameServerAddress());
        this.producer.setSendMsgTimeout(properties.getSendMsgTimeout());
        this.producer.start();
    }

    public SendResult send(String topic, Object body) {
        return this.send(topic, "", body);
    }

    public SendResult send(String topic, String tags, Object body) {
        try {
            Message message = this.buildMessage(topic, tags, body);
            return this.producer.send(message);
        } catch (Exception var5) {
            throw new MessageSendException(topic, var5);
        }
    }

    public void asyncSend(String topic, Object body, SendCallback sendCallback) {
        this.asyncSend(topic, "", body, sendCallback);
    }

    public void asyncSend(String topic, String tags, Object body, SendCallback sendCallback) {
        try {
            Message message = this.buildMessage(topic, tags, body);
            this.producer.send(message, sendCallback);
        } catch (Exception var6) {
            throw new MessageSendException(topic, var6);
        }
    }

    private Message buildMessage(String topic, String tag, Object body) {
        byte[] bodyBytes = gson.toJson(body).getBytes();
        return new Message(topic, tag, bodyBytes);
    }
}

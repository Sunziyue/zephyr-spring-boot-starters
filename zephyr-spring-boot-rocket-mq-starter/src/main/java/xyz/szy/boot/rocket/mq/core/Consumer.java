package xyz.szy.boot.rocket.mq.core;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.MessageListener;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.client.consumer.listener.MessageListenerOrderly;
import org.apache.rocketmq.client.exception.MQClientException;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import xyz.szy.boot.rocket.mq.annotation.ConsumeMode;
import xyz.szy.boot.rocket.mq.annotation.MQConsumer;
import xyz.szy.boot.rocket.mq.annotation.MQSubscribe;
import xyz.szy.boot.rocket.mq.properties.RocketMQProperties;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.UUID;

@Slf4j
public class Consumer implements ApplicationContextAware {
    private final RocketMQProperties properties;

    @Autowired
    public Consumer(RocketMQProperties properties) {
        this.properties = properties;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        Map<String, Object> consumers = applicationContext.getBeansWithAnnotation(MQConsumer.class);
        for (Map.Entry<String, Object> entry : consumers.entrySet()) {
            Object bean = entry.getValue();
            MQConsumer consumer = bean.getClass().getAnnotation(MQConsumer.class);
            Method[] methods = bean.getClass().getDeclaredMethods();
            for (Method method : methods) {
                MQSubscribe subscribe = method.getAnnotation(MQSubscribe.class);
                if (subscribe != null) {
                    try {
                        Type[] parameterTypes = method.getGenericParameterTypes();
                        if (ArrayUtils.isEmpty(parameterTypes)) {
                            log.warn("subscribe method parameter is empty.");
                            return;
                        }
                        Type type = parameterTypes[0];
                        if (type instanceof Class) {
                            String consumerGroup = StringUtils.defaultIfEmpty(consumer.consumerGroup(), this.properties.getProducerGroup());
                            DefaultMQPushConsumer defaultConsumer = new DefaultMQPushConsumer(consumerGroup);
                            defaultConsumer.setNamesrvAddr(this.properties.getNameServerAddress());
                            defaultConsumer.setMessageModel(subscribe.messageMode());
                            defaultConsumer.subscribe(subscribe.topic(), StringUtils.join(subscribe.tag(), "||"));
                            defaultConsumer.setInstanceName(UUID.randomUUID().toString());
                            MessageListener listener;
                            if (ConsumeMode.CONCURRENTLY.equals(subscribe.consumeMode())) {
                                listener = new ConcurrentlyMessageListener((Class) type, bean, method);
                                defaultConsumer.registerMessageListener((MessageListenerConcurrently) listener);
                            } else {
                                listener = new OrderlyMessageListener((Class) type, bean, method);
                                defaultConsumer.registerMessageListener((MessageListenerOrderly) listener);
                            }
                            defaultConsumer.start();
                        }
                    } catch (MQClientException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}

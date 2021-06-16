package xyz.szy.boot.rocket.mq.core;

import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.common.message.MessageExt;

import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.List;

public class ConcurrentlyMessageListener extends AbstractMessageListener implements MessageListenerConcurrently {

    ConcurrentlyMessageListener(Class<?> messageType, Object bean, Method method) {
        super(messageType, bean, method);
    }

    @Override
    public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> messageList, ConsumeConcurrentlyContext consumeConcurrentlyContext) {
        Iterator<MessageExt> messageExtIterator = messageList.iterator();
        MessageExt messageExt;
        do {
            if (!messageExtIterator.hasNext()) {
                return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
            }
            messageExt = messageExtIterator.next();
        } while (this.process(messageExt));
        return ConsumeConcurrentlyStatus.RECONSUME_LATER;
    }
}

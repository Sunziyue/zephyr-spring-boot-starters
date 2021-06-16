package xyz.szy.boot.rocket.mq.core;

import org.apache.rocketmq.client.consumer.listener.ConsumeOrderlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeOrderlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerOrderly;
import org.apache.rocketmq.common.message.MessageExt;

import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.List;

public class OrderlyMessageListener extends AbstractMessageListener implements MessageListenerOrderly {

    OrderlyMessageListener(Class<?> messageType, Object bean, Method method) {
        super(messageType, bean, method);
    }

    @Override
    public ConsumeOrderlyStatus consumeMessage(List<MessageExt> messageList, ConsumeOrderlyContext consumeOrderlyContext) {
        Iterator<MessageExt> messageExtIterator = messageList.iterator();
        MessageExt messageExt;
        do {
            if (!messageExtIterator.hasNext()) {
                return ConsumeOrderlyStatus.SUCCESS;
            }
            messageExt = messageExtIterator.next();
        } while (this.process(messageExt));
        return ConsumeOrderlyStatus.SUSPEND_CURRENT_QUEUE_A_MOMENT;
    }
}

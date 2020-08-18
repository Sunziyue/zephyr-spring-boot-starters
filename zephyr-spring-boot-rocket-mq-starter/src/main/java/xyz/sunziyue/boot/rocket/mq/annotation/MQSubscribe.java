package xyz.sunziyue.boot.rocket.mq.annotation;

import org.apache.rocketmq.common.protocol.heartbeat.MessageModel;

import java.lang.annotation.*;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface MQSubscribe {
    String topic();
    MessageModel messageMode() default MessageModel.CLUSTERING;
    ConsumeMode consumeMode() default ConsumeMode.ORDERLY;
    String[] tag() default {"*"};
}
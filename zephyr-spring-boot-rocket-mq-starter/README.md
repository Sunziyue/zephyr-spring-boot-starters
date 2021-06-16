# Rocket MQ 的 starter 工程
### application.yml
```yaml
rocketmq:
  nameServerAddress: ${ROCKETMQ_NAMESRV_HOST}:${ROCKETMQ_NAMESRV_PORT}
  producerGroup: ${MQ_GROUP}
```
---
### Producer java code

```java
package com.example.demo;

import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import Consumer;
import Producer;

@Slf4j
@Component
public class MqManager {

    private final Producer producer;

    @Autowired
    public MqManager(Producer producer, Consumer consumer) {
        this.producer = producer;
    }

    public void send() {
        Man man = Man.builder().name("老王").age("108").build();
        this.producer.asyncSend("test", man, new SendCallback() {
            @Override
            public void onSuccess(SendResult sendResult) {
                log.info("发送成功回调");
            }

            @Override
            public void onException(Throwable throwable) {
                log.info("发送失败回调");
            }
        });

    }
}
```
---
### Consumer java code

```java
package com.example.demo;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ConsumeMode;
import MQConsumer;
import MQSubscribe;

@Slf4j
@Component
@MQConsumer
public class Consumer {
    @MQSubscribe(topic = "test", consumeMode = ConsumeMode.CONCURRENTLY)
    public void test(Man man) {
        log.info("msg: {}", JSON.toJSONString(man));
    }
}
```
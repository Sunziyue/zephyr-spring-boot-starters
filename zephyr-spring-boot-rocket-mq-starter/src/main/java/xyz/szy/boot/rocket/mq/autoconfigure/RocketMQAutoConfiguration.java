package xyz.szy.boot.rocket.mq.autoconfigure;

import org.apache.rocketmq.client.exception.MQClientException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import xyz.szy.boot.rocket.mq.core.Consumer;
import xyz.szy.boot.rocket.mq.core.Producer;
import xyz.szy.boot.rocket.mq.properties.RocketMQProperties;

@EnableConfigurationProperties({RocketMQProperties.class})
public class RocketMQAutoConfiguration {

    private final RocketMQProperties properties;

    @Autowired
    public RocketMQAutoConfiguration(RocketMQProperties properties) {
        this.properties = properties;
    }

    @Bean
    public Producer producer() throws MQClientException {
        return new Producer(this.properties);
    }

    @Bean
    public Consumer consumer() {
        return new Consumer(this.properties);
    }
}

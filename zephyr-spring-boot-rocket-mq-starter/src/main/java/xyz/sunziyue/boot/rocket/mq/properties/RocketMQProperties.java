package xyz.sunziyue.boot.rocket.mq.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(
        prefix = "rocketmq"
)
public class RocketMQProperties {
    private String nameServerAddress;
    private String producerGroup;
    private Integer sendMsgTimeout = 3000;
}

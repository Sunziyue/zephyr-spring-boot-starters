package xyz.szy.elastic.job.autoconfigure;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author Sunziyue
 */
@ConfigurationProperties(
        prefix = "elastic.job.zk"
)
@Data
public class ZookeeperProperties {
    private String serverLists;
    private String namespace;
    private int baseSleepTimeMilliseconds = 1000;
    private int maxSleepTimeMilliseconds = 3000;
    private int maxRetries = 3;
    private int sessionTimeoutMilliseconds;
    private int connectionTimeoutMilliseconds;
    private String digest;
}

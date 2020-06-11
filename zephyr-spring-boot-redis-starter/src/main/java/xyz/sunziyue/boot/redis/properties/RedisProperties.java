package xyz.sunziyue.boot.redis.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@ConfigurationProperties(prefix = "redis")
public class RedisProperties {
    private String auth;
    private int port = 6379;
    private int maxTotal = 5;
    private int maxIdle = 5;
    private int maxWaitMillis = 10000;
    private boolean testOnBorrow = true;
    private boolean cluster = false;
    private String host;
    private String sentinelHosts;
    private String sentinelMasterName;

    public boolean isTestOnBorrow() {
        return this.testOnBorrow;
    }

    public boolean isCluster() {
        return this.cluster;
    }
}

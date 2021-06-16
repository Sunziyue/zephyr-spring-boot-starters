package xyz.szy.boot.redis.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "redis")
public class RedisProperties {
    /**
     * 认证
     */
    private String auth;

    /**
     * 最大活动对象数
     */
    private int maxTotal = 5;

    /**
     * 最大能够保持idel状态的对象数
     */
    private int maxIdle = 5;

    /**
     * 当池内没有返回对象时，最大等待时间
     */
    private int maxWaitMillis = 10000;

    /**
     * 当调用borrow Object方法时，是否进行有效性检查
     */
    private boolean testOnBorrow = true;

    /**
     * 是否处于集群
     */
    private boolean cluster = false;

    /**
     * redis服务器的Port
     */
    private int port = 6379;

    /**
     * redis服务器的host
     */
    private String host;

    /**
     * sentinel服务器
     */
    private String sentinelHosts;

    /**
     * Master服务器
     */
    private String sentinelMasterName;

    /**
     * @return  返回是否进行有效性检查
     */
    public boolean isTestOnBorrow() {
        return this.testOnBorrow;
    }

    /**
     * @return 是否是集群
     */
    public boolean isCluster() {
        return this.cluster;
    }
}

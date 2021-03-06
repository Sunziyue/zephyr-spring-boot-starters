package xyz.szy.boot.redis.autoconfigure;

import com.google.common.base.Splitter;
import com.google.common.collect.Sets;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisSentinelPool;
import redis.clients.jedis.util.Pool;
import xyz.szy.boot.redis.properties.RedisProperties;
import xyz.szy.common.redis.utils.JedisTemplate;
import xyz.szy.common.utils.Arguments;

import java.util.Set;

@EnableConfigurationProperties({RedisProperties.class})
public class RedisAutoConfiguration {
    @Autowired
    private RedisProperties properties;

    /**
     * 配置 JedisPoolConfig jedis线程池配置类
     * 并加入到 IOC 管理
     * @return JedisPoolConfig
     */
    @Bean
    public JedisPoolConfig jedisPoolConfig() {
        JedisPoolConfig config = new JedisPoolConfig();
        config.setMaxTotal(this.properties.getMaxTotal());
        config.setMaxIdle(this.properties.getMaxIdle());
        config.setMaxWaitMillis(this.properties.getMaxWaitMillis());
        config.setTestOnBorrow(this.properties.isTestOnBorrow());
        return config;
    }

    /**
     * 配置 jedis线程池
     * 并加入到 IOC 管理
     * @param poolConfig jedis线程池配置类
     * @return redisPool
     */
    @Bean
    public Pool<Jedis> redisPool(JedisPoolConfig poolConfig) {
        if (this.properties.isCluster()) {
            String sentinelProps = this.properties.getSentinelHosts();
            Iterable<String> parts = Splitter.on(',').trimResults().omitEmptyStrings().split(sentinelProps);
            Set<String> sentinelHosts = Sets.newHashSet(parts);
            String masterName = this.properties.getSentinelMasterName();
            return Arguments.isEmpty(this.properties.getAuth()) ? new JedisSentinelPool(masterName, sentinelHosts, poolConfig) : new JedisSentinelPool(masterName, sentinelHosts, poolConfig, 2000, this.properties.getAuth());
        } else {
            return Arguments.isEmpty(this.properties.getAuth()) ? new JedisPool(poolConfig, this.properties.getHost(), this.properties.getPort()) : new JedisPool(poolConfig, this.properties.getHost(), this.properties.getPort(), 2000, this.properties.getAuth());
        }
    }

    /**
     * 配置 Redis线程池模板 类
     * 并加入到 IOC 管理
     * @param pool jedis 线程池
     * @return Redis线程池模板
     */
    @Bean
    public JedisTemplate jedisTemplate(Pool<Jedis> pool) {
        return new JedisTemplate(pool);
    }
}

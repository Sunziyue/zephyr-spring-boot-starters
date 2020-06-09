package xyz.sunziyue.boot.redis.autoconfigure;

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
import xyz.sunziyue.boot.redis.properties.RedisProperties;
import xyz.sunziyue.common.redis.utils.JedisTemplate;
import xyz.sunziyue.common.utils.Arguments;

import java.util.Set;

@EnableConfigurationProperties({RedisProperties.class})
public class RedisAutoConfiguration {
    @Autowired
    private RedisProperties properties;

    public RedisAutoConfiguration() {
    }

    @Bean
    public JedisPoolConfig jedisPoolConfig() {
        JedisPoolConfig config = new JedisPoolConfig();
        config.setMaxTotal(this.properties.getMaxTotal());
        config.setMaxIdle(this.properties.getMaxIdle());
        config.setMaxWaitMillis((long)this.properties.getMaxWaitMillis());
        config.setTestOnBorrow(this.properties.isTestOnBorrow());
        return config;
    }

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

    @Bean
    public JedisTemplate jedisTemplate(Pool<Jedis> pool) {
        return new JedisTemplate(pool);
    }
}

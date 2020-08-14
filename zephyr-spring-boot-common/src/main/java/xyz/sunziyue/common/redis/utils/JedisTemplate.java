package xyz.sunziyue.common.redis.utils;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.exceptions.JedisConnectionException;
import redis.clients.jedis.exceptions.JedisException;
import redis.clients.jedis.util.Pool;

@Data
@Slf4j
public class JedisTemplate {
    private final Pool<Jedis> jediPool;

    @Autowired
    public JedisTemplate(Pool<Jedis> jediPool) {
        this.jediPool = jediPool;
    }

    public <T> T execute(JedisAction<T> jedisAction) throws JedisException {
        return this.execute(jedisAction, 0);
    }

    public <T> T execute(JedisAction<T> jedisAction, int dbIndex) throws JedisException {
        Jedis jedis = null;
        T t;
        try {
            jedis = this.jediPool.getResource();
            jedis.select(dbIndex);
            t = jedisAction.action(jedis);
        } catch (JedisConnectionException jedisConnectionException) {
            log.error("Redis connection lost.", jedisConnectionException);
            throw jedisConnectionException;
        } finally {
            this.closeResource(jedis);
        }

        return t;
    }

    public void execute(JedisActionNoResult jedisAction) throws JedisException {
        this.execute(jedisAction, 0);
    }

    public void execute(JedisActionNoResult jedisAction, int dbIndex) throws JedisException {
        Jedis jedis = null;

        try {
            jedis = this.jediPool.getResource();
            jedis.select(dbIndex);
            jedisAction.action(jedis);
        } catch (JedisConnectionException jedisConnectionException) {
            log.error("Redis connection lost.", jedisConnectionException);
            throw jedisConnectionException;
        } finally {
            this.closeResource(jedis);
        }

    }

    protected void closeResource(Jedis jedis) {
        if (jedis != null) {
            jedis.close();
        }

    }

    public interface JedisActionNoResult {
        void action(Jedis jedis);
    }

    public interface JedisAction<T> {
        T action(Jedis jedis);
    }
}

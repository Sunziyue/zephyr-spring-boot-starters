package xyz.sunziyue.common.redis.utils;

import lombok.extern.slf4j.Slf4j;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.exceptions.JedisConnectionException;
import redis.clients.jedis.exceptions.JedisException;
import redis.clients.jedis.util.Pool;

@Slf4j
public class JedisTemplate {
    private final Pool<Jedis> jedisPool;

    public JedisTemplate(Pool<Jedis> jedisPool) {
        this.jedisPool = jedisPool;
    }

    public <T> T execute(JedisAction<T> jedisAction) throws JedisException {
        return this.execute(jedisAction, 0);
    }

    public <T> T execute(JedisAction<T> jedisAction, int dbIndex) throws JedisException {
        Jedis jedis = null;
        boolean broken = false;

        T t;
        try {
            jedis = this.jedisPool.getResource();
            jedis.select(dbIndex);
            t = jedisAction.action(jedis);
        } catch (JedisConnectionException jedisConnectionException) {
            log.error("Redis connection lost.", jedisConnectionException);
            broken = true;
            throw jedisConnectionException;
        } finally {
            this.closeResource(jedis, broken);
        }

        return t;
    }

    public void execute(JedisActionNoResult jedisAction) throws JedisException {
        this.execute(jedisAction, 0);
    }

    public void execute(JedisActionNoResult jedisAction, int dbIndex) throws JedisException {
        Jedis jedis = null;
        boolean broken = false;

        try {
            jedis = this.jedisPool.getResource();
            jedis.select(dbIndex);
            jedisAction.action(jedis);
        } catch (JedisConnectionException jedisConnectionException) {
            log.error("Redis connection lost.", jedisConnectionException);
            broken = true;
            throw jedisConnectionException;
        } finally {
            this.closeResource(jedis, broken);
        }

    }

    protected void closeResource(Jedis jedis, boolean connectionBroken) {
        if (jedis != null) {
            if (connectionBroken) {
                this.jedisPool.destroy();
            } else {
                this.jedisPool.close();
            }
        }

    }

    public Pool<Jedis> getJedisPool() {
        return this.jedisPool;
    }

    public interface JedisActionNoResult {
        void action(Jedis jedis);
    }

    public interface JedisAction<T> {
        T action(Jedis jedis);
    }
}

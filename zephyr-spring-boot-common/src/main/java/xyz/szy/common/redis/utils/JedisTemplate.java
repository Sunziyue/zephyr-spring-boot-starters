package xyz.szy.common.redis.utils;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.exceptions.JedisConnectionException;
import redis.clients.jedis.exceptions.JedisException;
import redis.clients.jedis.util.Pool;

/**
 * @author Sunziyue
 * jediPool操作，提供回调函数，
 * 使之专注业务，不必关心jedis线程的生命周期
 */

@Data
@Slf4j
public class JedisTemplate {

    /**
     * Jedis线程池
     */
    private final Pool<Jedis> jediPool;

    @Autowired
    public JedisTemplate(Pool<Jedis> jediPool) {
        this.jediPool = jediPool;
    }

    /**
     * 默认在 redis 0 库中 执行操作
     * @param jedisAction Redis 操作的回调方法
     * @return 泛型
     * @throws JedisException Jedis 异常
     */
    public <T> T execute(JedisAction<T> jedisAction) throws JedisException {
        return this.execute(jedisAction, 0);
    }

    /**
     * 在任意Redis库中执行操作
     * @param jedisAction Redis 操作的回调方法
     * @param dbIndex Redis 数据库索引
     * @return 泛型
     * @throws JedisException Jedis 异常
     */
    public <T> T execute(JedisAction<T> jedisAction, int dbIndex) throws JedisException {
        Jedis jedis = null;
        T t;
        try {
            // 在线程池中获取jedis线程
            jedis = this.jediPool.getResource();
            // 选择库
            jedis.select(dbIndex);
            //执行回调方法拿到返回值
            t = jedisAction.action(jedis);
        } catch (JedisConnectionException jedisConnectionException) {
            log.error("Redis connection lost.", jedisConnectionException);
            throw jedisConnectionException;
        } finally {
            // 关闭连接
            this.closeResource(jedis);
        }

        return t;
    }

    /**
     * 默认在 redis 0 库中 执行操作
     * 无返回值
     * @param jedisAction Redis 操作的回调方法
     * @throws JedisException Jedis 异常
     */
    public void execute(JedisActionNoResult jedisAction) throws JedisException {
        this.execute(jedisAction, 0);
    }

    /**
     * 在任意Redis库中执行操作
     * 无返回值
     * @param jedisAction Redis 操作的回调方法
     * @param dbIndex Redis 数据库索引
     * @throws JedisException Jedis 异常
     */
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

    /**
     * 关闭当前连接
     * @param jedis 当前jedis线程
     */
    protected void closeResource(Jedis jedis) {
        if (jedis != null) {
            jedis.close();
        }

    }

    /**
     * 无返回值的Jedis操作回调接口
     */
    public interface JedisActionNoResult {
        void action(Jedis jedis);
    }

    /**
     * 有返回值的Jedis操作回调接口
     * @param <T> 泛型
     */
    public interface JedisAction<T> {
        T action(Jedis jedis);
    }
}

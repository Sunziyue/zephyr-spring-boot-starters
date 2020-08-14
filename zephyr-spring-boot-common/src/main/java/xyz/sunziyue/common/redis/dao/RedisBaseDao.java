package xyz.sunziyue.common.redis.dao;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.Response;
import xyz.sunziyue.common.redis.utils.JedisTemplate;
import xyz.sunziyue.common.redis.utils.KeyUtils;
import xyz.sunziyue.common.redis.utils.StringHashMapper;
import xyz.sunziyue.common.utils.Arguments;

import java.lang.reflect.ParameterizedType;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public abstract class RedisBaseDao<T> {
    public final StringHashMapper<T> stringHashMapper;
    protected final JedisTemplate template;
    protected final Class<T> entityClass;

    @Autowired
    public RedisBaseDao(JedisTemplate template) {
        this.template = template;
        this.entityClass = (Class<T>)((ParameterizedType) this.getClass().getGenericSuperclass()).getActualTypeArguments()[0];
        this.stringHashMapper = new StringHashMapper<>(this.entityClass);
    }

    public List<T> findByIds(Iterable<String> ids) {
        return this.findByKeys(ids, id -> KeyUtils.entityId(this.entityClass, id));
    }

    public List<T> findByKeys(final Iterable<String> keys, final Function<String, String> keyGen) {
        if (Arguments.isEmpty(keys)) {
            return Collections.emptyList();
        } else {
            List<Response<Map<String, String>>> result = this.template.execute((Jedis jedi) -> {
                List<Response<Map<String, String>>> responseList = Lists.newArrayListWithCapacity(Iterables.size(keys));
                Pipeline pipelined = jedi.pipelined();
                for (String key : keys) {
                    responseList.add(pipelined.hgetAll(keyGen.apply(key)));
                }
                pipelined.sync();
                return responseList;
            });
            List<T> entities = Lists.newArrayListWithCapacity(result.size());
            for (Response<Map<String, String>> response : result) {
                entities.add(this.stringHashMapper.fromHash(response.get()));
            }
            return entities;
        }
    }

    public T findByKey(final Long id) {
        Map<String, String> hash = this.template.execute((Jedis jedi) -> jedi.hgetAll(KeyUtils.entityId(this.entityClass, id)));
        return this.stringHashMapper.fromHash(hash);
    }

    public T findByKey(final String key) {
        Map<String, String> hash = this.template.execute((Jedis jedi) -> jedi.hgetAll(KeyUtils.entityId(this.entityClass, key)));
        return this.stringHashMapper.fromHash(hash);
    }

    public Long addByKey(final String Key, T t) {
        return this.template.execute((Jedis jedi) -> jedi.hset(KeyUtils.entityId(this.entityClass, Key), this.stringHashMapper.toHash(t)));
    }

    public Long addByKey(final Long id,  T t) {
        return this.template.execute((Jedis jedi) -> jedi.hset(KeyUtils.entityId(this.entityClass, id), this.stringHashMapper.toHash(t)));
    }

    public Long newId() {
        return this.template.execute((Jedis jedi) -> jedi.incr(KeyUtils.entityCount(this.entityClass)));
    }
}

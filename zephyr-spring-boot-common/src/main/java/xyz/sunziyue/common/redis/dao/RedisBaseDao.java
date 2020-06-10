package xyz.sunziyue.common.redis.dao;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.Response;
import xyz.sunziyue.common.redis.utils.JedisTemplate;
import xyz.sunziyue.common.redis.utils.JedisTemplate.JedisAction;
import xyz.sunziyue.common.redis.utils.KeyUtils;
import xyz.sunziyue.common.redis.utils.StringHashMapper;

import java.lang.reflect.ParameterizedType;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public abstract class RedisBaseDao<T> {
    public final StringHashMapper<T> stringHashMapper;
    protected final JedisTemplate template;
    protected final Class<T> entityClass;

    @Autowired
    public RedisBaseDao(JedisTemplate template) {
        this.template = template;
        this.entityClass = (Class) ((ParameterizedType) this.getClass().getGenericSuperclass()).getActualTypeArguments()[0];
        this.stringHashMapper = new StringHashMapper(this.entityClass);
    }

    public List<T> findByIds(Iterable<String> ids) {
        return this.findByKeys(ids, new Function<String, String>() {
            public String apply(String id) {
                return KeyUtils.entityId(RedisBaseDao.this.entityClass, id);
            }
        });
    }

    public List<T> findByKeys(final Iterable<String> keys, final Function<String, String> keyGen) {
        if (Iterables.isEmpty(keys)) {
            return Collections.emptyList();
        } else {
            List<Response<Map<String, String>>> result = (List) this.template.execute(new JedisAction<List<Response<Map<String, String>>>>() {
                public List<Response<Map<String, String>>> action(Jedis jedis) {
                    List<Response<Map<String, String>>> result = Lists.newArrayListWithCapacity(Iterables.size(keys));
                    Pipeline p = jedis.pipelined();
                    Iterator iterator = keys.iterator();

                    while (iterator.hasNext()) {
                        String key = (String) iterator.next();
                        result.add(p.hgetAll((String) keyGen.apply(key)));
                    }

                    p.sync();
                    return result;
                }
            });
            List<T> entities = Lists.newArrayListWithCapacity(result.size());
            Iterator iterator = result.iterator();

            while (iterator.hasNext()) {
                Response<Map<String, String>> mapResponse = (Response) iterator.next();
                entities.add((T) this.stringHashMapper.fromHash((Map) mapResponse.get()));
            }

            return entities;
        }
    }

    protected T findByKey(final Long id) {
        Map<String, String> hash = (Map) this.template.execute(new JedisAction<Map<String, String>>() {
            public Map<String, String> action(Jedis jedis) {
                return jedis.hgetAll(KeyUtils.entityId(RedisBaseDao.this.entityClass, id));
            }
        });
        return this.stringHashMapper.fromHash(hash);
    }

    protected T findByKey(final String key) {
        Map<String, String> hash = (Map) this.template.execute(new JedisAction<Map<String, String>>() {
            public Map<String, String> action(Jedis jedis) {
                return jedis.hgetAll(KeyUtils.entityId(RedisBaseDao.this.entityClass, key));
            }
        });
        return this.stringHashMapper.fromHash(hash);
    }

    public Long newId() {
        return (Long) this.template.execute(new JedisAction<Long>() {
            public Long action(Jedis jedis) {
                return jedis.incr(KeyUtils.entityCount(RedisBaseDao.this.entityClass));
            }
        });
    }
}
